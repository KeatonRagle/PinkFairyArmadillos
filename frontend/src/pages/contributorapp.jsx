import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import { useAuth } from '../auth/AuthContext'
import { requestContributor, getCurrentUser } from '../fetch/api'
import '../styling/contributorapp.css'

export default function ContributorApp() {
	const navigate = useNavigate()
	const { username, role } = useAuth()
	const [submitted, setSubmitted] = useState(false)
    const [denied, setDenied] = useState(false)
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState(null)

	useEffect(() => {
		document.body.classList.add('contributorapp-body')
		return () => document.body.classList.remove('contributorapp-body')
	}, [])

	useEffect(() => {
		if (!username) { navigate('/login'); return }
		if (role !== 'ROLE_USER') { navigate('/profile'); return }

		getCurrentUser()
			.then((user) => {
				if (user.requestedContributor == 'P') setSubmitted(true)
				if (user.requestedContributor == 'D') setDenied(true)
			})
			.catch(() => setError('Failed to load user data.'))
			.finally(() => setLoading(false))
	}, [navigate, role, username])

	const handleSubmit = async (event) => {
		event.preventDefault()
		setLoading(true)
		setError(null)
		try {
			await requestContributor()
			setSubmitted(true)
		} catch (e) {
			if (e.status === 409) {
				setError('You have already submitted a contributor application.')
			} else {
				setError('Something went wrong. Please try again.')
			}
		} finally {
			setLoading(false)
		}
	}

	const statusCopy = useMemo(() => {
		if (submitted) {
			return {
				label: 'Pending',
				description: 'Your contributor application is pending review.',
				className: 'is-pending',
			}
		} else if (denied) {
            return {
                label: 'Denied',
                description: 'Your contributor application has been denied.',
                className: 'is-denied',
            }
        }
		return {
			label: 'Not Submitted',
			description: 'You have not requested contributor access yet.',
			className: 'is-idle',
		}
	}, [submitted, denied])

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

					{loading ? null : (
						<form className="contributorapp-form" onSubmit={handleSubmit}>
							{error ? <p className="contributorapp-error">{error}</p> : null}
							<div className="contributorapp-actions">
								{!submitted && !denied ? (
									<button type="submit" className="contributorapp-button" disabled={loading}>
										Submit Application
									</button>
								) : null}
							</div>
						</form>
					)}

					<section className="contributorapp-panel contributorapp-status-panel">
						<h2>Application Status</h2>
						{loading ? (
							<p>Loading...</p>
						) : (
							<div className={`contributorapp-status ${statusCopy.className}`}>
								<span className="contributorapp-status-label">{statusCopy.label}</span>
								<p>{statusCopy.description}</p>
							</div>
						)}
					</section>
				</section>
			</main>
		</div>
	)
}
