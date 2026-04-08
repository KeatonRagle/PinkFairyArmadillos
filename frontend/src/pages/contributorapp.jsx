import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import { useAuth } from '../auth/AuthContext'
import '../styling/contributorapp.css'

const CONTRIBUTOR_APPLICATIONS_KEY = 'pfa_contributor_applications'

function readContributorApplications() {
	try {
		const rawValue = localStorage.getItem(CONTRIBUTOR_APPLICATIONS_KEY)
		const parsedValue = rawValue ? JSON.parse(rawValue) : {}
		return parsedValue && typeof parsedValue === 'object' ? parsedValue : {}
	} catch {
		return {}
	}
}

function writeContributorApplications(applications) {
	localStorage.setItem(CONTRIBUTOR_APPLICATIONS_KEY, JSON.stringify(applications))
}

export default function ContributorApp() {
	const navigate = useNavigate()
	const { username, role } = useAuth()
	const [reason, setReason] = useState('')
	const [applicationRecord, setApplicationRecord] = useState(null)

	useEffect(() => {
		document.body.classList.add('contributorapp-body')
		return () => document.body.classList.remove('contributorapp-body')
	}, [])

	useEffect(() => {
		if (!username) {
			navigate('/login')
			return
		}

		if (role !== 'ROLE_USER') {
			navigate('/profile')
			return
		}

		const applications = readContributorApplications()
		const existingRecord = applications[username] ?? null
		setApplicationRecord(existingRecord)
		if (existingRecord?.reason) {
			setReason(existingRecord.reason)
		}
	}, [navigate, role, username])

	const statusCopy = useMemo(() => {
		if (!applicationRecord) {
			return {
				label: 'Not Submitted',
				description: 'You have not requested contributor access yet.',
				className: 'is-idle',
			}
		}

		if (applicationRecord.status === 'DENIED') {
			return {
				label: 'Denied',
				description: 'Your last contributor application was denied.',
				className: 'is-denied',
			}
		}

		return {
			label: 'Pending',
			description: 'Your contributor application is pending review.',
			className: 'is-pending',
		}
	}, [applicationRecord])

	const handleSubmit = (event) => {
		event.preventDefault()
		if (!username || applicationRecord) {
			return
		}

		const trimmedReason = reason.trim()
		const nextRecord = {
			status: 'PENDING',
			reason: trimmedReason,
			submittedAt: new Date().toISOString(),
		}

		const applications = readContributorApplications()
		applications[username] = nextRecord
		writeContributorApplications(applications)
		setApplicationRecord(nextRecord)
	}

	return (
		<div className="contributorapp-page">
			<HomeHeader />

			<main className="contributorapp-main">
				<section className="contributorapp-card">
					<div className="contributorapp-heading">
						<h1>Contributor Application</h1>
					</div>

					<div className="contributorapp-grid">
						<section className="contributorapp-panel">
							<h2>What Contributors Do</h2>
							<p>
								Contributors help expand the platform by sending in pet adoption site websites for review. Once approved, those sites can be used to surface more adoptable animals in the app.
							</p>
						</section>
					</div>

					<form className="contributorapp-form" onSubmit={handleSubmit}>
						<div className="contributorapp-form-header">
							<h2>Request Contributor Access</h2>
						</div>

						<label className="contributorapp-field">
							<textarea
								value={reason}
								onChange={(event) => setReason(event.target.value)}
								placeholder="Reason for contributor application"
								rows={7}
								disabled={Boolean(applicationRecord)}
							/>
						</label>

						<div className="contributorapp-actions">
							{!applicationRecord ? (
								<button type="submit" className="contributorapp-button" disabled={!reason.trim()}>
									Submit Application
								</button>
							) : null}
						</div>
					</form>

					<section className="contributorapp-panel contributorapp-status-panel">
						<h2>Application Status</h2>
						<div className={`contributorapp-status ${statusCopy.className}`}>
							<span className="contributorapp-status-label">{statusCopy.label}</span>
							<p>{statusCopy.description}</p>
						</div>
					</section>
				</section>
			</main>
		</div>
	)
}
