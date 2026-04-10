import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import { useAuth } from '../auth/AuthContext'
import { getBannedUsers, getUnbannedUsers, banUser, unbanUser, getRequestedContributor, promoteToContributor } from '../fetch/api'
import '../styling/usermanage.css'

const FILTER_OPTIONS = [
    { key: 'WHITELISTED', label: 'Whitelisted Users' },
    { key: 'BLACKLISTED', label: 'Blacklisted Users' },
    { key: 'PENDING', label: 'Pending Applications' },
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
    const [bannedUsers, setBannedUsers] = useState([])
    const [unbannedUsers, setUnbannedUsers] = useState([])
    const [pendingUsers, setPendingUsers] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)

    useEffect(() => {
        document.body.classList.add('usermanage-body')
        return () => document.body.classList.remove('usermanage-body')
    }, [])

    useEffect(() => {
        if (!username) { navigate('/login'); return }
        if (role !== 'ROLE_ADMIN') { navigate('/profile'); return }
        fetchUsers()
    }, [username, role])

    async function fetchUsers() {
        setLoading(true)
        setError(null)
        try {
            const [banned, unbanned, pending] = await Promise.all([
                getBannedUsers(),
                getUnbannedUsers(),
                getRequestedContributor(),
            ])
            setBannedUsers(banned)
            setUnbannedUsers(unbanned)
            setPendingUsers(pending)
        } catch (e) {
            setError(e.status)
        } finally {
            setLoading(false)
        }
    }

    async function handleListToggle(user) {
        try {
            if (user.isBanned) {
                await unbanUser(user.id)
            } else {
                await banUser(user.id)
            }
            await fetchUsers()
        } catch (e) {
            alert(`Action failed: ${e.message}`)
        }
    }
    async function handlePromote(user) {
        try {
            await promoteToContributor(user.id)
            await fetchUsers()
        } catch (e) {
            alert(`Action failed: ${e.message}`)
        }
    }
    const visibleEntries =
        activeFilter === 'WHITELISTED' ? unbannedUsers :
        activeFilter === 'BLACKLISTED' ? bannedUsers :
        pendingUsers

    const emptyMessage =
        activeFilter === 'WHITELISTED' ? 'No whitelisted users.' :
        activeFilter === 'BLACKLISTED' ? 'No blacklisted users.' :
        'No pending contributor applications.'

    return (
        <div className="usermanage-page">
            <HomeHeader />
            <main className="usermanage-main">
                <section className="usermanage-card">
                    <div className="usermanage-heading">
                        <h1>User Management</h1>
                    </div>

                    <div className="usermanage-filters" role="tablist">
                        {FILTER_OPTIONS.map((option) => (
                            <button
                                key={option.key}
                                type="button"
                                className={`usermanage-filter ${activeFilter === option.key ? 'is-active' : ''}`}
                                onClick={() => setActiveFilter(option.key)}
                            >
                                {option.label}
                            </button>
                        ))}
                    </div>

                    <section className="usermanage-list-section">
                        {loading ? (
                            <p className="usermanage-empty">Loading...</p>
                        ) : error ? (
                            <p className="usermanage-empty">{error}</p>
                        ) : visibleEntries.length === 0 ? (
                            <p className="usermanage-empty">{emptyMessage}</p>
                        ) : (
                            <div className="usermanage-list">
                                {visibleEntries.map((user) => (
                                    <article key={user.id} className="usermanage-entry">
                                        <div className="usermanage-entry-header">
                                            <div>
                                                <h2>{user.name}</h2>
                                                <p>{user.email}</p>
                                            </div>
                                            <div className="usermanage-badges">
                                                <span className={`usermanage-badge ${user.isBanned ? 'is-blacklisted' : 'is-whitelisted'}`}>
                                                    {user.isBanned ? 'Blacklisted' : 'Whitelisted'}
                                                </span>
                                                <span className="usermanage-badge is-role">{roleLabel(user.role)}</span>
                                                {user.requestedContributor ? (
                                                    <span className="usermanage-badge is-pending">Pending</span>
                                                ) : null}
                                            </div>
                                        </div>

                                        <div className="usermanage-controls">
                                            <button
                                                type="button"
                                                className="usermanage-button usermanage-list-button"
                                                onClick={() => handleListToggle(user)}
                                            >
                                                Move to {user.isBanned ? 'Whitelist' : 'Blacklist'}
                                            </button>

                                            {activeFilter === 'PENDING' ? (
                                                <button
                                                    type="button"
                                                    className="usermanage-button"
                                                    onClick={() => handlePromote(user)}
                                                >
                                                    Approve as Contributor
                                                </button>
                                            ) : null}
                                        </div>
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
