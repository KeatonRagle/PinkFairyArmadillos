
import { useEffect, useLayoutEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import ReviewsPopup from '../components/ReviewsPopup'
import { getAllReviews } from '../fetch/api'
import PopupErrorBoundary from '../components/PopupErrorBoundary'
import '../styling/specificanimal.css'

const previewAnimal = {
	name: 'dog',
	image: '/images/dogs.jpg',
	adoptionSite: 'All Kind Animal Initiative',
	location: 'Abilene, TX',
	gender: 'Female',
	age: 2,
	breed: 'Terrier Mix',
}

// Specific animal details page component
export default function SpecificAnimal() {
	const location = useLocation()
	const animal = location.state?.animal ?? previewAnimal
	const [siteRating, setSiteRating] = useState(null)
	const filters = location.state?.filters ?? null
	const backToFiltersState = filters ? { filters, petType: filters.petType } : undefined
	const animalAge = () => {
		if (!animal) return null

		const ageComponents = {
			years: Math.floor(animal.age / 52),
			months: Math.floor((animal.age % 52) / 4),
			weeks: animal.age % 52 % 4,
		}

		ageComponents.text = `${ageComponents.years} years, ${ageComponents.months} months, ${ageComponents.weeks} weeks`

		return ageComponents
	}
	const isPreview = !location.state?.animal
	const [reviewsOpen, setReviewsOpen] = useState(false)

  // Set up body class for Specific Animal page
	useEffect(() => {
		document.body.classList.add('specificanimal-body')
		return () => document.body.classList.remove('specificanimal-body')
	}, [])

	// Load and compute shelter rating
  // Load and compute shelter rating
	useLayoutEffect(() => {
		let isMounted = true
		const loadRating = async () => {
			try {
				const reviews = await getAllReviews()
				const siteId = animal.site_id ?? animal.siteId ?? null
				if (!siteId) return
				const filtered = Array.isArray(reviews)
					? reviews.filter(r => String(r.siteId ?? r.site?.siteId) === String(siteId))
					: []
				if (filtered.length === 0) {
					if (isMounted) setSiteRating(null)
					return
				}
				const avg = filtered.reduce((sum, r) => sum + Number(r.rating || 0), 0) / filtered.length
				if (isMounted) setSiteRating(avg)
			} catch {
				if (isMounted) setSiteRating(null)
			}
		}
		loadRating()
		return () => { isMounted = false }
	}, [animal.site_id, animal.siteId])

  // Render Specific Animal page content
	return (
		<div className="specificanimal-page">
			<HomeHeader
				backLink={{
					to: '/animal-filter',
					label: 'BACK TO FILTERS',
					ariaLabel: 'Back to filters',
					state: backToFiltersState,
					renderAsArrow: true,
				}}
			/>

			<main className="specificanimal-main">
				<section className="specificanimal-media-column">
					<div className="specificanimal-main-image-wrap">
						<img src={animal.image} alt={animal.name} className="specificanimal-main-image" />
					</div>

					<div className="specificanimal-gallery-row" aria-label="Additional photos">
						{animal.secondaryImages.map((item, index) => (
							<div key={animal.name + index} className="specificanimal-main-image-wrap">
								<img src={item} alt={animal.name} className="specificanimal-main-image" />
							</div>
						))}
					</div>
				</section>

				<section className="specificanimal-details-column">
					<div className="specificanimal-adoption-card">
						<h1>Find out how you can adopt</h1>
						<p className="specificanimal-site-name">{animal.adoptionSite}</p>

						{isPreview ? (
							<p className="specificanimal-preview-copy">
								Preview blah blah blah blah.
							</p>
						) : null}
						<button
							className="specificanimal-reviews-btn"
							onClick={() => setReviewsOpen(true)}
						>
							Reviews and Info
						</button>
					</div>

					<div className="specificanimal-about-card">
						<h2>About, {animal.name}</h2>
						<div className="specificanimal-detail-list">
						  <div className="specificanimal-detail-item">
						    <span className="specificanimal-detail-label">Location</span>
						    <span>{animal.location || 'Location coming soon.'}</span>
						  </div>
						  <div className="specificanimal-detail-item">
						    <span className="specificanimal-detail-label">Gender</span>
						    <span>{animal.gender || 'Unknown'}</span>
						  </div>
						  <div className="specificanimal-detail-item">
						    <span className="specificanimal-detail-label">Age</span>
						    <span>{animalAge().text ?? 'Unknown'}</span>
						  </div>
						  <div className="specificanimal-detail-item">
						    <span className="specificanimal-detail-label">Breed</span>
						    <span>{animal.breed || 'Unknown'}</span>
						  </div>
						  {animal.size && (
						    <div className="specificanimal-detail-item">
						      <span className="specificanimal-detail-label">Size</span>
						      <span>{animal.size}</span>
						    </div>
						  )}
						</div>
					</div>
				</section>
			</main>

			<HomeFooter />

			<PopupErrorBoundary resetKey={reviewsOpen ? 'open' : 'closed'}>
				<ReviewsPopup
					isOpen={reviewsOpen}
					onClose={() => setReviewsOpen(false)}
					shelterName={animal.adoptionSite}
					siteInfo={{
						siteId: animal.site_id ?? animal.siteId ?? null,
						name: animal.adoption_site_name || animal.adoptionSite || 'Adoption site name coming soon.',
						url: animal.adoption_site_url || animal.adoptionSiteUrl || 'https://example-adoption-site.org',
						email: animal.adoption_site_email || animal.adoptionSiteEmail || 'info@example-adoption-site.org',
						phone: animal.adoption_site_phone || animal.adoptionSitePhone || '(555) 123-4567',
					}}
				/>
			</PopupErrorBoundary>
		</div>
	)
}
