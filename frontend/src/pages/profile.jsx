import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import { useAuth } from '../auth/AuthContext'
import { getCurrentUser, updateCurrentUsername, updateCurrentEmail, updateCurrentPassword, getCurrentUserPrefs, addUserPref, deleteUserPref } from '../fetch/api'
import UserPrefsPopup from '../components/UserPrefsPopup';
import '../styling/profile.css'

// Profile page component
export default function Profile() {
	const navigate = useNavigate()
	const { token, username, role, setAuth } = useAuth()
	const [usernameDraft, setUsernameDraft] = useState('')
	const [emailDraft, setEmailDraft] = useState('')
	const [currentPasswordDraft, setCurrentPasswordDraft] = useState('')
	const [newPasswordDraft, setNewPasswordDraft] = useState('')
	const [confirmPasswordDraft, setConfirmPasswordDraft] = useState('')
	const [statusMessage, setStatusMessage] = useState('')
	const [currentEmail, setCurrentEmail] = useState('')
	const [isSavingUsername, setIsSavingUsername] = useState(false)
    const [isSavingEmail, setIsSavingEmail] = useState(false)
    const [isSavingPassword, setIsSavingPassword] = useState(false)

	const [isLoading, setIsLoading] = useState(true)
	const [hasLoaded, setHasLoaded] = useState(false)
	const [errorMessage, setErrorMessage] = useState('')
	const [userPreferences, setUserPreferences] = useState([])

	const [isPopupOpen, setIsPopupOpen] = useState(false);

	const traitMappings = new Map([
		['BREED', 'Breed'],
		['SIZE', 'Size'],
		['GENDER', 'Gender'],
		['AGE_MIN', 'Minimum Age'],
		['AGE_MAX', 'Maximum Age']
	])

  // Set up body class for Profile page
	useEffect(() => {
		document.body.classList.add('profile-body')
		return () => document.body.classList.remove('profile-body')
	}, [])

  // Redirect to login if not authenticated
	useEffect(() => {
		if (!username) {
			navigate('/login')
		}
	}, [navigate, username])

  // Load user preferences
	useEffect(() => {
		let isCancelled = false

		const loadUserPrefs = async () => {
			setIsLoading(true)
			setErrorMessage('')

			try {
				const allPrefs = await getCurrentUserPrefs()
				const prefsArray = Array.isArray(allPrefs) ? allPrefs : []

				if (!isCancelled) {
					setUserPreferences(prefsArray)
				}
			} catch {
				if (!isCancelled) {
					setUserPreferences([])
					setErrorMessage('Unable to load user preferences right now.')
				}
			} finally {
				if (!isCancelled) {
					setIsLoading(false)
					setHasLoaded(true)
				}
			}
		}

		loadUserPrefs()

		return () => {
			isCancelled = true
		}
	}, [])

	const handleAddPref = async (trait, value) => {
		setErrorMessage('');
		setIsLoading(true);

		try {
			const newUserPref = await addUserPref({
				pref: trait,
				value: value
			});

			setUserPreferences([...userPreferences, newUserPref]);
		} catch (err) {
			setErrorMessage('Failed to add user pref: ' + err.message);
		} finally {
			setIsLoading(false);
		}
	}

	const handleDeletePref = async (prefID) => {
		setErrorMessage('')
		setIsLoading(true)

		try {
			await deleteUserPref(prefID)
			 setUserPreferences(userPreferences.filter(pref => pref.id !== prefID));
		} catch (err) {
			setErrorMessage('Failed to delete user preference: ' + err.message);
		} finally {
			setIsLoading(false);
		}
	}

	useEffect(() => {
		if (!token) {
			return
		}

		let isMounted = true

		const loadCurrentUser = async () => {
			try {
				const user = await getCurrentUser()
				if (!isMounted) {
					return
				}

				setCurrentEmail(user.email ?? '')
				setUsernameDraft(user.name ?? '')
			} catch {
				if (isMounted) {
					setCurrentEmail('')
				}
			}
		}

		loadCurrentUser()

		return () => {
			isMounted = false
		}
	}, [token])

	const roleLabel = role === 'ROLE_ADMIN'
		? 'Admin'
		: role === 'ROLE_CONTRIBUTOR'
			? 'Contributor'
			: 'User'


    const handleEmailSubmit = async (event) => {
        event.preventDefault()

        const trimmedEmail = emailDraft.trim()
        if (!trimmedEmail) {
            setStatusMessage('Email cannot be empty.')
            return
        }

        setIsSavingEmail(true)
        setStatusMessage('')

        try {
            const updatedUser = await updateCurrentEmail(trimmedEmail)
            setCurrentEmail(updatedUser.email ?? trimmedEmail)
            setEmailDraft('')
            setStatusMessage('Email updated.')
        } catch (e) {
            if (e.status === 409) {
                setStatusMessage("An account with that email already exists.")
            } else {
                setStatusMessage('Unable to update email right now.')
            }
        } finally {
            setIsSavingEmail(false)
        }
    }

    const handlePasswordSubmit = async (event) => {
        event.preventDefault()

        if (!currentPasswordDraft) {
            setStatusMessage('Please enter your current password.')
            return
        }
        if (!newPasswordDraft) {
            setStatusMessage('Please enter a new password.')
            return
        }
        if (newPasswordDraft !== confirmPasswordDraft) {
            setStatusMessage('New passwords do not match.')
            return
        }

    setIsSavingPassword(true)
    setStatusMessage('')

    try {
        const data = await updateCurrentPassword(newPasswordDraft)
		const token = data.token
       	const userObj = data.user
      	const displayName = userObj.name ?? userObj.email;
     	const role = userObj.role;
      	const id = userObj.id;

      	setAuth(token, displayName, role, id);
        setCurrentPasswordDraft('')
        setNewPasswordDraft('')
        setConfirmPasswordDraft('')
        setStatusMessage('Password updated.')
    } catch {
        setStatusMessage('Unable to update password right now.')
    } finally {
        setIsSavingPassword(false)
    }
}

	const handleUsernameSubmit = async (event) => {
		event.preventDefault()

		const trimmedUsername = usernameDraft.trim()
		if (!trimmedUsername) {
			setStatusMessage('Username cannot be empty.')
			return
		}

		setIsSavingUsername(true)
		setStatusMessage('')

		try {
			const updatedUser = await updateCurrentUsername(trimmedUsername)
			setAuth(token, updatedUser.name ?? trimmedUsername, updatedUser.role)
			setUsernameDraft(updatedUser.name ?? trimmedUsername)
			setCurrentEmail(updatedUser.email ?? currentEmail)
			setStatusMessage('Username updated.')
		} catch {
			setStatusMessage('Unable to update username right now.')
		} finally {
			setIsSavingUsername(false)
		}
	}

  // Render Profile page content
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
							<span className="profile-value">{currentEmail || 'Not signed in'}</span>
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
							onSubmit={handleUsernameSubmit}
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
							<button type="submit" className="profile-button" disabled={isSavingUsername}>
								{isSavingUsername ? 'Saving...' : 'Save Username'}
							</button>
						</form>

                        <form
                            className="profile-settings-panel"
                            onSubmit={handleEmailSubmit}
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
                            <button type="submit" className="profile-button" disabled={isSavingEmail}>
                                {isSavingEmail ? 'Saving...' : 'Change Email'}
                            </button>
                        </form>

                        <form
                            className="profile-settings-panel profile-settings-panel-wide"
                            onSubmit={handlePasswordSubmit}
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
                            <button type="submit" className="profile-button profile-password-button" disabled={isSavingPassword}>
                                {isSavingPassword ? 'Saving...' : 'Change Password'}
                            </button>
                        </form>

						<section className="profile-settings-panel profile-settings-panel-wide">
							<h2>Pet Preferences</h2>
							<p className="profile-status" style={{ marginBottom: '20px', textAlign: 'left' }}>
								Manage the traits you are looking for in a pet.
							</p>

							<div className="profile-grid">
								{userPreferences.length > 0 ? (
									userPreferences.map((pref) => (
										<div key={pref.id} className="profile-input-group">
											<span className="profile-label">
												{traitMappings.get(pref.trait)}
											</span>
											<div className="pref-input-container"> 
												<input
													type="text"
													value={pref.value}
													readOnly
													className="profile-input-readonly"
												/>
												<button
													type="button"
													className="pref-delete-x"
													onClick={() => handleDeletePref(pref.id)}
												>
													×
												</button>
											</div>
										</div>
									))
								) : (
									<p className="no-prefs">No preferences added yet.</p>
								)}
							</div>

							<button 
								type="button" 
								className="profile-button add-pref-bottom-btn" 
								onClick={() => setIsPopupOpen(true)}
							>
								+ Add New Preference
							</button>
						</section>
					</div>
					
					

					<UserPrefsPopup 
						isOpen={isPopupOpen}
						title="Add Pet Preference"
						onClose={() => setIsPopupOpen(false)}
						onSubmit={handleAddPref}
					/>

					<div className="profile-actions">
						<Link to="/home" className="profile-action-link">Back to Home</Link>
					</div>
				</section>
			</main>
		</div>
	)
}
