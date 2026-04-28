
import { useEffect, useState } from 'react'
import { getAllReviews, getApprovedSites } from '../fetch/api'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import ReviewsPopup from '../components/ReviewsPopup'
import PopupErrorBoundary from '../components/PopupErrorBoundary'
import '../styling/ShelterInfo.css'


// Shelter info page component
export default function ShelterInfo() {
	const [approvedSites, setApprovedSites] = useState([])
	const [siteRatings, setSiteRatings] = useState({})
	const [openSiteId, setOpenSiteId] = useState(null)
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState('')
	const [reviewsOpen, setReviewsOpen] = useState(false)
	const [activeSiteInfo, setActiveSiteInfo] = useState(null)

  // Set up body class for Shelter Info page
	useEffect(() => {
		document.body.classList.add('shelterinfo-body')
		return () => document.body.classList.remove('shelterinfo-body')
	}, [])

  // Load approved sites and reviews
	useEffect(() => {
		const loadApprovedSites = async () => {
			setLoading(true)
			setError('')

			try {
				const [sites, reviews] = await Promise.all([
					getApprovedSites(),
					getAllReviews(),
				])

				const reviewList = Array.isArray(reviews) ? reviews : []
				const ratingBuckets = reviewList.reduce((acc, review) => {
					const rawSiteId = review?.siteId ?? review?.site?.siteId
					const siteId = Number(rawSiteId)
					const rating = Number(review?.rating)

					if (!Number.isFinite(siteId) || !Number.isFinite(rating)) {
						return acc
					}

					const current = acc[siteId] || { sum: 0, count: 0 }
					current.sum += rating
					current.count += 1
					acc[siteId] = current
					return acc
				}, {})

				const averagedRatings = Object.entries(ratingBuckets).reduce((acc, [siteId, bucket]) => {
					if (bucket.count > 0) {
						acc[siteId] = bucket.sum / bucket.count
					}
					return acc
				}, {})

				setApprovedSites(Array.isArray(sites) ? sites : [])
				setSiteRatings(averagedRatings)
			} catch {
				setError('Failed to load approved websites.')
				setApprovedSites([])
				setSiteRatings({})
			} finally {
				setLoading(false)
			}
		}

		loadApprovedSites()
	}, [])

  // Toggle open/close for site details
	const toggleSite = (siteId) => {
		setOpenSiteId((currentSiteId) => (currentSiteId === siteId ? null : siteId))
	}

  // Format phone number for display
	const formatPhone = (phone) => {
		const digitsOnly = String(phone || '').replace(/\D/g, '')

		if (digitsOnly.length !== 10) {
			return phone || 'Not provided'
		}

		return `(${digitsOnly.slice(0, 3)}) ${digitsOnly.slice(3, 6)}-${digitsOnly.slice(6)}`
	}

	const formatOverallRating = (siteId, fallbackRating) => {
		const liveRating = siteRatings[siteId]
		const parsedRating = Number(
			Number.isFinite(liveRating) ? liveRating : fallbackRating
		)
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
													       <span>{formatOverallRating(site.siteId, site.rating)}</span>
												       </div>
													       <button
														       className="shelterinfo-reviews-btn"
														       type="button"
														       onClick={() => {
															       setActiveSiteInfo({
																       siteId: site.siteId ?? null,
																       name: site.name || 'Adoption site name coming soon.',
																       url: site.url || 'https://example-adoption-site.org',
																       email: site.email || 'info@example-adoption-site.org',
																       phone: site.phone || '(555) 123-4567',
															       })
															       setReviewsOpen(true)
														       }}
													       >
														       Reviews
													       </button>
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

			   {/* Reviews Popup for shelter site */}
			   <PopupErrorBoundary resetKey={reviewsOpen ? (activeSiteInfo?.siteId || 'open') : 'closed'}>
				   <ReviewsPopup
					   isOpen={reviewsOpen}
					   onClose={() => setReviewsOpen(false)}
					   shelterName={activeSiteInfo?.name}
					   siteInfo={activeSiteInfo || {}}
					   hideInfoTab
				   />
			   </PopupErrorBoundary>
		   </div>
	   )
	}
