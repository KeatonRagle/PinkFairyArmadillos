import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/request.css'

// changes
const CONTRIBUTIONS_KEY = 'pfa_contributions'

export default function Request() {
	// changes
	const [contributions, setContributions] = useState([])
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState('')

	useEffect(() => {
		document.body.classList.add('request-body')
		return () => document.body.classList.remove('request-body')
	}, [])

	// changes
	const loadContributions = async () => {
		setError('')
		setLoading(true)
		try {
			// changes
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

	// changes
	const handleStatusChange = async (id, status) => {
		try {
			// changes
			const updatedList = contributions.map((item) =>
				item.id === id ? { ...item, status } : item
			)
			localStorage.setItem(CONTRIBUTIONS_KEY, JSON.stringify(updatedList))
			setContributions(updatedList)
		} catch (updateError) {
			setError(updateError?.message || 'Failed to update status.')
		}
	}

	return (
		<div className="request-page">
			<HomeHeader />

			<main className="request-main">
				<h1>Contribution Approval</h1>

				{loading && <p>Loading contributions...</p>}
				{error && <p className="request-error">{error}</p>}

				{!loading && contributions.length === 0 && <p>No contributions available.</p>}

				<div className="request-list">
					{contributions.map((item) => (
						<article key={item.id} className="request-card">
							<p><strong>Status:</strong> {item.status}</p>
							<p><strong>Link:</strong> <a href={item.link} target="_blank" rel="noreferrer">{item.link}</a></p>
							<p><strong>Name:</strong> {item.name}</p>
							<p><strong>Email:</strong> {item.email}</p>
							<p><strong>Phone:</strong> {item.phone}</p>
							<p><strong>Address:</strong> {item.address}</p>
							<p><strong>Submitted:</strong> {item.submittedAt}</p>

							<div className="request-actions">
								<button type="button" onClick={() => handleStatusChange(item.id, 'APPROVED')}>Approve</button>
								<button type="button" onClick={() => handleStatusChange(item.id, 'DENIED')}>Deny</button>
							</div>
						</article>
					))}
				</div>
			</main>

			<HomeFooter />
		</div>
	)
}
