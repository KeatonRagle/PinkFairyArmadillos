import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import { useAuth } from '../auth/AuthContext'
import '../styling/profile.css'

export default function Profile() {
	const navigate = useNavigate()
	const { username, role } = useAuth()

	useEffect(() => {
		document.body.classList.add('profile-body')
		return () => document.body.classList.remove('profile-body')
	}, [])

	useEffect(() => {
		if (!username) {
			navigate('/login')
		}
	}, [navigate, username])

	const roleLabel = role === 'ROLE_ADMIN'
		? 'Admin'
		: role === 'ROLE_CONTRIBUTOR'
			? 'Contributor'
			: 'User'

	return (
		<div className="profile-page">
			<HomeHeader />

			<main className="profile-main">
				<section className="profile-card">
					<h1>Account Settings</h1>

					<div className="profile-grid">
						<div className="profile-field">
							<span className="profile-label">Email</span>
							<span className="profile-value">{username || 'Not signed in'}</span>
						</div>
						<div className="profile-field">
							<span className="profile-label">Role</span>
							<span className="profile-value">{roleLabel}</span>
						</div>
					</div>

					<div className="profile-actions">
						<Link to="/home" className="profile-action-link">Back to Home</Link>
					</div>
				</section>
			</main>


		</div>
	)
}
