import { useEffect, useState } from 'react'
import { getApprovedSites } from '../fetch/api'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/ShelterInfo.css'

export default function ShelterInfo() {
	const [approvedSites, setApprovedSites] = useState([])
	const [openSiteId, setOpenSiteId] = useState(null)
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState('')

	useEffect(() => {
		document.body.classList.add('shelterinfo-body')
		return () => document.body.classList.remove('shelterinfo-body')
	}, [])

	useEffect(() => {
		const loadApprovedSites = async () => {
			setLoading(true)
			setError('')

			try {
				const sites = await getApprovedSites()
				setApprovedSites(Array.isArray(sites) ? sites : [])
			} catch {
				setError('Failed to load approved websites.')
				setApprovedSites([])
			} finally {
				setLoading(false)
			}
		}

		loadApprovedSites()
	}, [])

	const toggleSite = (siteId) => {
		setOpenSiteId((currentSiteId) => (currentSiteId === siteId ? null : siteId))
	}

	const formatPhone = (phone) => {
		const digitsOnly = String(phone || '').replace(/\D/g, '')

		if (digitsOnly.length !== 10) {
			return phone || 'Not provided'
		}

		return `(${digitsOnly.slice(0, 3)}) ${digitsOnly.slice(3, 6)}-${digitsOnly.slice(6)}`
	}

	const formatOverallRating = (rating) => {
		const parsedRating = Number(rating)
		return Number.isFinite(parsedRating) ? `${parsedRating.toFixed(1)} / 5` : 'Not rated yet'
	}

	const visibleSites = Array.isArray(approvedSites) ? approvedSites : []

	return (
		<div className="shelterinfo-page">
			<HomeHeader />
			<main className="shelterinfo-main">
				<section className="shelterinfo-hero">
					<p className="shelterinfo-eyebrow">Adoption Sites</p>
				</section>

				<section className="shelterinfo-list-section" aria-label="Approved shelters and rescue websites">
					{loading ? <p className="shelterinfo-status">Loading approved websites...</p> : null}
					{error ? <p className="shelterinfo-status shelterinfo-status-error">{error}</p> : null}

					{!loading && !error && visibleSites.length === 0 ? (
						<p className="shelterinfo-status">No approved websites are available yet.</p>
					) : null}

					{!loading && !error ? (
						<div className="shelterinfo-site-list">
							{visibleSites.map((site, index) => {
								const isOpen = openSiteId === site.siteId

								return (
									<article key={site.siteId ?? site.url ?? index} className={`shelterinfo-site-card ${isOpen ? 'open' : ''}`}>
										<button
											type="button"
											className="shelterinfo-site-toggle"
											onClick={() => toggleSite(site.siteId)}
											aria-expanded={isOpen}
										>
											<div className="shelterinfo-site-summary">
												<p className="shelterinfo-site-name">{site.name || 'Unnamed website'}</p>
												<p className="shelterinfo-site-url">{site.url || 'No website link provided'}</p>
											</div>
											<span className="shelterinfo-site-icon" aria-hidden="true">{isOpen ? '−' : '+'}</span>
										</button>

										{isOpen ? (
											<div className="shelterinfo-site-details">
												<div className="shelterinfo-detail-row">
													<span className="shelterinfo-detail-label">Website</span>
													<a href={site.url} target="_blank" rel="noreferrer" className="shelterinfo-detail-link">
														{site.url}
													</a>
												</div>
												<div className="shelterinfo-detail-row">
													<span className="shelterinfo-detail-label">Email</span>
													<span>{site.email || 'Not provided'}</span>
												</div>
												<div className="shelterinfo-detail-row">
													<span className="shelterinfo-detail-label">Phone</span>
													<span>{formatPhone(site.phone)}</span>
												</div>
												<div className="shelterinfo-detail-row">
													<span className="shelterinfo-detail-label">Rating</span>
													<span>{formatOverallRating(site.rating)}</span>
												</div>
											</div>
										) : null}
									</article>
								)
							})}
						</div>
					) : null}
				</section>
			</main>
			<HomeFooter />
		</div>
	)
}
