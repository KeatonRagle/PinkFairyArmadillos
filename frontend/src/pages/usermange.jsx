import { useEffect, useMemo, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import { useAuth } from '../auth/AuthContext'
import {
	getBannedUsers,
	getUnbannedUsers,
	getRequestedContributor,
	getDeniedContributor,
	banUser,
	unbanUser,
	promoteToContributor,
	denyContributor,
} from '../fetch/api'
import '../styling/usermanage.css'

const FILTER_OPTIONS = [
	{ key: 'WHITELISTED', label: 'Whitelisted Users' },
	{ key: 'BLACKLISTED', label: 'Blacklisted Users' },
	{ key: 'PENDING', label: 'Pending Applications' },
	{ key: 'DENIED', label: 'Denied Applications' },
]

const ROLE_OPTIONS = [
	{ value: 'ROLE_USER', label: 'User' },
	{ value: 'ROLE_CONTRIBUTOR', label: 'Contributor' },
	{ value: 'ROLE_ADMIN', label: 'Admin' },
]

function roleLabel(role) {
	return ROLE_OPTIONS.find((o) => o.value === role)?.label || role
}

export default function UserManage() {
	const navigate = useNavigate()
	const { username, role } = useAuth()
	const [activeFilter, setActiveFilter] = useState('WHITELISTED')
	const [whitelistedUsers, setWhitelistedUsers] = useState([])
	const [blacklistedUsers, setBlacklistedUsers] = useState([])
	const [pendingApplications, setPendingApplications] = useState([])
	const [deniedApplications, setDeniedApplications] = useState([])
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState(null)

	useEffect(() => {
		document.body.classList.add('usermanage-body')
		return () => document.body.classList.remove('usermanage-body')
	}, [])

	useEffect(() => {
		if (!username) {
			navigate('/login')
			return
		}
		if (role !== 'ROLE_ADMIN') {
			navigate('/profile')
		}
	}, [navigate, role, username])

	const loadData = useCallback(async () => {
		if (role !== 'ROLE_ADMIN') return
		setLoading(true)
		setError(null)
		try {
			const [unbanned, banned, pending, denied] = await Promise.all([
				getUnbannedUsers(),
				getBannedUsers(),
				getRequestedContributor(),
				getDeniedContributor(),
			])
			setWhitelistedUsers(unbanned || [])
			setBlacklistedUsers(banned || [])
			setPendingApplications(pending || [])
			setDeniedApplications(denied || [])
		} catch {
			setError('Failed to load user data. Please try again.')
		} finally {
			setLoading(false)
		}
	}, [role])

	useEffect(() => {
		loadData()
	}, [loadData])

	const counts = useMemo(
		() => ({
			WHITELISTED: whitelistedUsers.length,
			BLACKLISTED: blacklistedUsers.length,
			PENDING: pendingApplications.length,
			DENIED: deniedApplications.length,
		}),
		[whitelistedUsers, blacklistedUsers, pendingApplications, deniedApplications],
	)

	const visibleEntries = useMemo(() => {
		if (activeFilter === 'WHITELISTED')
			return whitelistedUsers.map((u) => ({ ...u, type: 'user', listStatus: 'WHITELISTED' }))
		if (activeFilter === 'BLACKLISTED')
			return blacklistedUsers.map((u) => ({ ...u, type: 'user', listStatus: 'BLACKLISTED' }))
		if (activeFilter === 'PENDING')
			return pendingApplications.map((u) => ({ ...u, type: 'application', status: 'PENDING', listStatus: 'WHITELISTED' }))
		return deniedApplications.map((u) => ({ ...u, type: 'application', status: 'DENIED', listStatus: 'WHITELISTED' }))
	}, [activeFilter, whitelistedUsers, blacklistedUsers, pendingApplications, deniedApplications])

	const handleListToggle = async (entry) => {
		try {
			if (entry.listStatus === 'BLACKLISTED') {
				await unbanUser(entry.id)
			} else {
				await banUser(entry.id)
			}
			await loadData()
		} catch {
			setError('Failed to update user status.')
		}
	}

	// NOTE: Only ROLE_CONTRIBUTOR promotion is supported by the current API.
	// Promoting to ROLE_ADMIN or demoting to ROLE_USER requires additional backend endpoints.
	const handleRoleChange = async (entry, nextRole) => {
		if (nextRole === entry.role) return
		if (nextRole === 'ROLE_CONTRIBUTOR') {
			try {
				await promoteToContributor(entry.id)
				await loadData()
			} catch {
				setError('Failed to update role.')
			}
		} else {
			setError('Promoting to Admin or demoting to User is not yet supported by the API.')
		}
	}

	const handleApplicationDecision = async (entry, decision) => {
		try {
			if (decision === 'APPROVED') {
				await promoteToContributor(entry.id)
			} else {
				await denyContributor(entry.id)
			}
			await loadData()
		} catch {
			setError(`Failed to ${decision === 'APPROVED' ? 'approve' : 'deny'} application.`)
		}
	}

	const emptyMessage =
		activeFilter === 'WHITELISTED'
			? 'No whitelisted users yet.'
			: activeFilter === 'BLACKLISTED'
				? 'No blacklisted users yet.'
				: activeFilter === 'PENDING'
					? 'No pending contributor applications.'
					: 'No denied contributor applications.'

	return (
		<div className="usermanage-page">
			<HomeHeader />

			<main className="usermanage-main">
				<section className="usermanage-card">
					<div className="usermanage-heading">
						<h1>User Management</h1>
					</div>

					<div className="usermanage-filters" role="tablist" aria-label="User management filters">
						{FILTER_OPTIONS.map((option) => (
							<button
								key={option.key}
								type="button"
								className={`usermanage-filter ${activeFilter === option.key ? 'is-active' : ''}`}
								onClick={() => setActiveFilter(option.key)}
							>
								<span>{option.label}</span>
								{counts[option.key] > 0 && (
									<span className="usermanage-filter-count">{counts[option.key]}</span>
								)}
							</button>
						))}
					</div>

					{error && (
						<p className="usermanage-error" role="alert">
							{error}
						</p>
					)}

					<section className="usermanage-list-section">
						{loading ? (
							<p className="usermanage-empty">Loading...</p>
						) : visibleEntries.length === 0 ? (
							<p className="usermanage-empty">{emptyMessage}</p>
						) : (
							<div className="usermanage-list">
								{visibleEntries.map((entry) => (
									<article key={`${entry.type}-${entry.id}`} className="usermanage-entry">
										<div className="usermanage-entry-header">
											<div>
												<h2>{entry.username}</h2>
												{entry.type === 'application' ? <p>Contributor application</p> : null}
											</div>
											<div className="usermanage-badges">
												<span
													className={`usermanage-badge ${entry.listStatus === 'BLACKLISTED' ? 'is-blacklisted' : 'is-whitelisted'}`}
												>
													{entry.listStatus === 'BLACKLISTED' ? 'Blacklisted' : 'Whitelisted'}
												</span>
												<span className="usermanage-badge is-role">{roleLabel(entry.role)}</span>
												{entry.type === 'application' ? (
													<span
														className={`usermanage-badge ${
															entry.status === 'DENIED'
																? 'is-denied'
																: entry.status === 'APPROVED'
																	? 'is-approved'
																	: 'is-pending'
														}`}
													>
														{entry.status}
													</span>
												) : null}
											</div>
										</div>

										<div className="usermanage-controls">
											<div className="usermanage-role-stack">
												<label className="usermanage-control">
													<span>Role</span>
													<select
														value={entry.role}
														onChange={(e) => handleRoleChange(entry, e.target.value)}
													>
														{ROLE_OPTIONS.map((option) => (
															<option key={option.value} value={option.value}>
																{option.label}
															</option>
														))}
													</select>
												</label>

												<button
													type="button"
													className="usermanage-button usermanage-list-button"
													onClick={() => handleListToggle(entry)}
												>
													Move to {entry.listStatus === 'BLACKLISTED' ? 'Whitelist' : 'Blacklist'}
												</button>
											</div>

											{entry.type === 'application' ? (
												<div className="usermanage-application-actions">
													<button
														type="button"
														className="usermanage-button"
														onClick={() => handleApplicationDecision(entry, 'APPROVED')}
													>
														Approve Application
													</button>
													<button
														type="button"
														className="usermanage-button is-secondary"
														onClick={() => handleApplicationDecision(entry, 'DENIED')}
													>
														Deny Application
													</button>
												</div>
											) : null}
										</div>

										{entry.type === 'application' && entry.reason ? (
											<p className="usermanage-reason">{entry.reason}</p>
										) : null}
									</article>
								))}
							</div>
						)}
					</section>
				</section>
			</main>
		</div>
	)
}
