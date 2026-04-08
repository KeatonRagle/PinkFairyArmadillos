import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import { useAuth } from '../auth/AuthContext'
import '../styling/profile.css'

export default function Profile() {
	const navigate = useNavigate()
	const { username, role } = useAuth()
	const [usernameDraft, setUsernameDraft] = useState('')
	const [emailDraft, setEmailDraft] = useState('')
	const [currentPasswordDraft, setCurrentPasswordDraft] = useState('')
	const [newPasswordDraft, setNewPasswordDraft] = useState('')
	const [confirmPasswordDraft, setConfirmPasswordDraft] = useState('')
	const [statusMessage, setStatusMessage] = useState('')

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

	const handlePlaceholderSubmit = (event, message) => {
		event.preventDefault()
		setStatusMessage(message)
	}

	return (
		<div className="profile-page">
			<HomeHeader />

			<main className="profile-main">
				<section className="profile-card">
					<h1>Account Settings</h1>
					

					<div className="profile-grid">
						<div className="profile-field">
							<span className="profile-label">Current Username</span>
							<span className="profile-value">{username || 'Not set yet'}</span>
						</div>
						<div className="profile-field">
							<span className="profile-label">Current Email</span>
							<span className="profile-value">{username || 'Not signed in'}</span>
						</div>
						<div className="profile-field">
							<span className="profile-label">Role</span>
							<span className="profile-value">{roleLabel}</span>
						</div>
					</div>

					{statusMessage ? <p className="profile-status">{statusMessage}</p> : null}

					<div className="profile-settings-sections">
						<form
							className="profile-settings-panel"
							onSubmit={(event) => handlePlaceholderSubmit(event, '')}
						>
							<h2>Create Username</h2>
							<label className="profile-input-group">
								<span className="profile-label">Username</span>
								<input
									type="text"
									value={usernameDraft}
									onChange={(event) => setUsernameDraft(event.target.value)}
									placeholder="Choose a username"
								/>
							</label>
							<button type="submit" className="profile-button">Save Username</button>
						</form>

						<form
							className="profile-settings-panel"
							onSubmit={(event) => handlePlaceholderSubmit(event, '')}
						>
							<h2>Change Email</h2>
							<label className="profile-input-group">
								<span className="profile-label">New Email</span>
								<input
									type="email"
									value={emailDraft}
									onChange={(event) => setEmailDraft(event.target.value)}
									placeholder="Enter a new email"
								/>
							</label>
							<button type="submit" className="profile-button">Change Email</button>
						</form>

						<form
							className="profile-settings-panel profile-settings-panel-wide"
							onSubmit={(event) => handlePlaceholderSubmit(event, '')}
						>
							<h2>Change Password</h2>
							<div className="profile-password-grid">
								<label className="profile-input-group">
									<span className="profile-label">Current Password</span>
									<input
										type="password"
										value={currentPasswordDraft}
										onChange={(event) => setCurrentPasswordDraft(event.target.value)}
										placeholder="Enter current password"
									/>
								</label>
								<label className="profile-input-group">
									<span className="profile-label">New Password</span>
									<input
										type="password"
										value={newPasswordDraft}
										onChange={(event) => setNewPasswordDraft(event.target.value)}
										placeholder="Enter new password"
									/>
								</label>
								<label className="profile-input-group">
									<span className="profile-label">Confirm New Password</span>
									<input
										type="password"
										value={confirmPasswordDraft}
										onChange={(event) => setConfirmPasswordDraft(event.target.value)}
										placeholder="Confirm new password"
									/>
								</label>
							</div>
							<button type="submit" className="profile-button profile-password-button">Change Password</button>
						</form>
					</div>

					<div className="profile-actions">
						<Link to="/home" className="profile-action-link">Back to Home</Link>
					</div>
				</section>
			</main>
		</div>
	)
}
