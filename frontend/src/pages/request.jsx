import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import '../styling/request.css'

// Shared key used by the Contribute page and this page to read/write
// contribution records in localStorage.
const CONTRIBUTIONS_KEY = 'pfa_contributions'
const STATUS_SECTIONS = [
	{
		key: 'PENDING',
		label: 'Pending',
		emptyMessage: 'No pending contributions.',
	},
	{
		key: 'APPROVED',
		label: 'Approved',
		emptyMessage: 'No approved contributions.',
	},
	{
		key: 'DENIED',
		label: 'Denied',
		emptyMessage: 'No denied contributions.',
	},
]

export default function Request() {
	// contributions: full list of submissions to review
	// loading: controls initial and reload state while data is being read
	// error: stores a user-facing error message for load/update failures
	const [contributions, setContributions] = useState([])
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState('')

	// Apply/remove a page-specific body class so request-page styling
	// is scoped to this route while the component is mounted.
	useEffect(() => {
		document.body.classList.add('request-body')
		return () => document.body.classList.remove('request-body')
	}, [])

	// Loads contribution entries from localStorage and normalizes the
	// value to an array so rendering logic has a consistent shape.
	const loadContributions = async () => {
		setError('')
		setLoading(true)
		try {
			// If the key is missing, default to an empty list.
			const data = JSON.parse(localStorage.getItem(CONTRIBUTIONS_KEY) || '[]')
			setContributions(Array.isArray(data) ? data : [])
		} catch (fetchError) {
			setError(fetchError?.message || 'Failed to load contributions.')
		} finally {
			setLoading(false)
		}
	}

	useEffect(() => {
		loadContributions()
	}, [])

	// Updates one contribution status (APPROVED or DENIED), then writes
	// the updated list back to localStorage and syncs UI state.
	const handleStatusChange = async (id, status) => {
		try {
			const updatedList = contributions.map((item) =>
				item.id === id ? { ...item, status } : item
			)
			localStorage.setItem(CONTRIBUTIONS_KEY, JSON.stringify(updatedList))
			setContributions(updatedList)
		} catch (updateError) {
			setError(updateError?.message || 'Failed to update status.')
		}
	}

	// Deletes a contribution by ID, updates localStorage, and syncs UI state.
	const handleDelete = async (id) => {
		try {
			const updatedList = contributions.filter((item) => item.id !== id)
			localStorage.setItem(CONTRIBUTIONS_KEY, JSON.stringify(updatedList))
			setContributions(updatedList)
		} catch (deleteError) {
			setError(deleteError?.message || 'Failed to delete contribution.')
		}
	}

	const contributionsByStatus = STATUS_SECTIONS.reduce((sections, section) => {
		sections[section.key] = contributions.filter((item) => item.status === section.key)
		return sections
	}, {})

	return (
		<div className="request-page">
			<HomeHeader />

			<main className="request-main">
				<div className="request-heading">
					<h1>Contribution Approval</h1>
				</div>

				{/* State-driven status messages for loading/errors/empty list */}
				{loading && <p>Loading contributions...</p>}
				{error && <p className="request-error">{error}</p>}

			{!loading && (
					<div className="request-sections">
						{STATUS_SECTIONS.map((section) => (
							<section key={section.key} className="request-section">
								<div className="request-section-header">
									<h2>{section.label}</h2>
									<span>{contributionsByStatus[section.key].length}</span>
								</div>

								{contributionsByStatus[section.key].length === 0 ? (
									<p className="request-empty">{section.emptyMessage}</p>
								) : (
									<div className="request-list">
										{contributionsByStatus[section.key].map((item) => (
											<article key={item.id} className="request-card">
												<p><strong>Status:</strong> {item.status}</p>
												<p><strong>Link:</strong> <a href={item.link} target="_blank" rel="noreferrer">{item.link}</a></p>
												<p><strong>Name:</strong> {item.name}</p>
												<p><strong>Email:</strong> {item.email}</p>
												<p><strong>Phone:</strong> {item.phone}</p>
												<p><strong>Date Submitted:</strong> {new Date(item.submittedAt).toLocaleDateString()}</p>

												<div className="request-actions">
													{item.status !== 'PENDING' && (
														<button type="button" onClick={() => handleStatusChange(item.id, 'PENDING')}>Mark Pending</button>
													)}
													{item.status !== 'APPROVED' && (
														<button type="button" onClick={() => handleStatusChange(item.id, 'APPROVED')}>Approve</button>
													)}
													{item.status !== 'DENIED' && (
														<button type="button" onClick={() => handleStatusChange(item.id, 'DENIED')}>Deny</button>
													)}
													<button type="button" onClick={() => handleDelete(item.id)} className="request-delete-btn">Remove</button>
												</div>
											</article>
										))}
									</div>
								)}
							</section>
						))}
					</div>
				)}
			</main>

		</div>
	)
}
