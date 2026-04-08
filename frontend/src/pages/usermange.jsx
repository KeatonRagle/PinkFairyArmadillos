import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import { useAuth } from '../auth/AuthContext'
import '../styling/usermanage.css'

const USER_RECORDS_KEY = 'pfa_user_records'
const CONTRIBUTOR_APPLICATIONS_KEY = 'pfa_contributor_applications'

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

function readStorageObject(key) {
	try {
		const rawValue = localStorage.getItem(key)
		const parsedValue = rawValue ? JSON.parse(rawValue) : {}
		return parsedValue && typeof parsedValue === 'object' ? parsedValue : {}
	} catch {
		return {}
	}
}

function writeStorageObject(key, value) {
	localStorage.setItem(key, JSON.stringify(value))
}

function createUserRecord(username, role = 'ROLE_USER') {
	return {
		username,
		role,
		listStatus: 'WHITELISTED',
	}
}

function ensureUserRecords(existingRecords, currentUsername, currentRole, applications) {
	const nextRecords = { ...existingRecords }

	if (currentUsername) {
		nextRecords[currentUsername] = {
			...createUserRecord(currentUsername, currentRole || 'ROLE_USER'),
			...(nextRecords[currentUsername] || {}),
			username: currentUsername,
			role: nextRecords[currentUsername]?.role || currentRole || 'ROLE_USER',
		}
	}

	Object.keys(applications).forEach((applicantUsername) => {
		nextRecords[applicantUsername] = {
			...createUserRecord(applicantUsername),
			...(nextRecords[applicantUsername] || {}),
			username: applicantUsername,
		}
	})

	return nextRecords
}

function roleLabel(role) {
	return ROLE_OPTIONS.find((option) => option.value === role)?.label || role
}

export default function UserManage() {
	const navigate = useNavigate()
	const { token, username, role, setAuth } = useAuth()
	const [activeFilter, setActiveFilter] = useState('WHITELISTED')
	const [userRecords, setUserRecords] = useState({})
	const [applications, setApplications] = useState({})

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
			return
		}

		const storedApplications = readStorageObject(CONTRIBUTOR_APPLICATIONS_KEY)
		const storedUsers = readStorageObject(USER_RECORDS_KEY)
		const normalizedUsers = ensureUserRecords(storedUsers, username, role, storedApplications)

		writeStorageObject(USER_RECORDS_KEY, normalizedUsers)
		setApplications(storedApplications)
		setUserRecords(normalizedUsers)
	}, [navigate, role, username])

	const userEntries = useMemo(
		() => Object.values(userRecords).sort((left, right) => left.username.localeCompare(right.username)),
		[userRecords],
	)

	const applicationEntries = useMemo(
		() => Object.entries(applications)
			.map(([applicantUsername, application]) => ({
				username: applicantUsername,
				...application,
				role: userRecords[applicantUsername]?.role || 'ROLE_USER',
				listStatus: userRecords[applicantUsername]?.listStatus || 'WHITELISTED',
			}))
			.sort((left, right) => left.username.localeCompare(right.username)),
		[applications, userRecords],
	)

	const counts = useMemo(
		() => ({
			WHITELISTED: userEntries.filter((entry) => entry.listStatus === 'WHITELISTED').length,
			BLACKLISTED: userEntries.filter((entry) => entry.listStatus === 'BLACKLISTED').length,
			PENDING: applicationEntries.filter((entry) => entry.status === 'PENDING').length,
			DENIED: applicationEntries.filter((entry) => entry.status === 'DENIED').length,
		}),
		[applicationEntries, userEntries],
	)

	const visibleEntries = useMemo(() => {
		if (activeFilter === 'WHITELISTED' || activeFilter === 'BLACKLISTED') {
			return userEntries
				.filter((entry) => entry.listStatus === activeFilter)
				.map((entry) => ({ type: 'user', ...entry }))
		}

		return applicationEntries
			.filter((entry) => entry.status === activeFilter)
			.map((entry) => ({ type: 'application', ...entry }))
	}, [activeFilter, applicationEntries, userEntries])

	const persistUsers = (nextUsers) => {
		writeStorageObject(USER_RECORDS_KEY, nextUsers)
		setUserRecords(nextUsers)
	}

	const persistApplications = (nextApplications) => {
		writeStorageObject(CONTRIBUTOR_APPLICATIONS_KEY, nextApplications)
		setApplications(nextApplications)
	}

	const handleListToggle = (targetUsername) => {
		const currentRecord = userRecords[targetUsername]
		if (!currentRecord) {
			return
		}

		const nextUsers = {
			...userRecords,
			[targetUsername]: {
				...currentRecord,
				listStatus: currentRecord.listStatus === 'BLACKLISTED' ? 'WHITELISTED' : 'BLACKLISTED',
			},
		}

		persistUsers(nextUsers)
	}

	const updateRole = (targetUsername, nextRole) => {
		const existingRecord = userRecords[targetUsername] || createUserRecord(targetUsername)
		const nextUsers = {
			...userRecords,
			[targetUsername]: {
				...existingRecord,
				username: targetUsername,
				role: nextRole,
			},
		}

		persistUsers(nextUsers)

		if (targetUsername === username) {
			setAuth(token, username, nextRole)
		}
	}

	const handleApplicationDecision = (targetUsername, nextStatus) => {
		const currentApplication = applications[targetUsername]
		if (!currentApplication) {
			return
		}

		const nextApplications = {
			...applications,
			[targetUsername]: {
				...currentApplication,
				status: nextStatus,
			},
		}
		persistApplications(nextApplications)

		if (nextStatus === 'APPROVED') {
			updateRole(targetUsername, 'ROLE_CONTRIBUTOR')
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
							</button>
						))}
					</div>

					<section className="usermanage-list-section">
						{visibleEntries.length === 0 ? (
							<p className="usermanage-empty">{emptyMessage}</p>
						) : (
							<div className="usermanage-list">
								{visibleEntries.map((entry) => (
									<article key={`${entry.type}-${entry.username}`} className="usermanage-entry">
										<div className="usermanage-entry-header">
											<div>
												<h2>{entry.username}</h2>
												{entry.type === 'application' ? <p>Contributor application</p> : null}
											</div>
											<div className="usermanage-badges">
												<span className={`usermanage-badge ${entry.listStatus === 'BLACKLISTED' ? 'is-blacklisted' : 'is-whitelisted'}`}>
													{entry.listStatus === 'BLACKLISTED' ? 'Blacklisted' : 'Whitelisted'}
												</span>
												<span className="usermanage-badge is-role">{roleLabel(entry.role)}</span>
												{entry.type === 'application' ? (
													<span className={`usermanage-badge ${entry.status === 'DENIED' ? 'is-denied' : entry.status === 'APPROVED' ? 'is-approved' : 'is-pending'}`}>
														{entry.status}
													</span>
												) : null}
											</div>
										</div>

										<div className="usermanage-controls">
											<div className="usermanage-role-stack">
												<label className="usermanage-control">
												<span>Role</span>
												<select value={entry.role} onChange={(event) => updateRole(entry.username, event.target.value)}>
													{ROLE_OPTIONS.map((option) => (
														<option key={option.value} value={option.value}>{option.label}</option>
													))}
												</select>
												</label>

												<button type="button" className="usermanage-button usermanage-list-button" onClick={() => handleListToggle(entry.username)}>
													Move to {entry.listStatus === 'BLACKLISTED' ? 'Whitelist' : 'Blacklist'}
												</button>
											</div>

											{entry.type === 'application' ? (
												<div className="usermanage-application-actions">
													<button type="button" className="usermanage-button" onClick={() => handleApplicationDecision(entry.username, 'APPROVED')}>
														Approve Application
													</button>
													<button type="button" className="usermanage-button is-secondary" onClick={() => handleApplicationDecision(entry.username, 'DENIED')}>
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
