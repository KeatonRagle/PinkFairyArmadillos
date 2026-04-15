import { useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/specificanimal.css'

const placeholderGallery = [1, 2, 3]
const previewAnimal = {
	name: 'dog',
	image: '/images/dogs.jpg',
	adoptionSite: 'All Kind Animal Initiative',
	location: 'Abilene, TX',
	gender: 'Female',
	age: 2,
	breed: 'Terrier Mix',
	misc: 'Friendly with families, enjoys walks, and is ready for a home visit.',
}

export default function SpecificAnimal() {
	const location = useLocation()
	const animal = location.state?.animal ?? previewAnimal
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

	useEffect(() => {
		document.body.classList.add('specificanimal-body')
		return () => document.body.classList.remove('specificanimal-body')
	}, [])

	return (
		<div className="specificanimal-page">
			<HomeHeader />

			<main className="specificanimal-main">
				<section className="specificanimal-media-column">
					<div className="specificanimal-main-image-wrap">
						<img src={animal.image} alt={animal.name} className="specificanimal-main-image" />
					</div>

					<div className="specificanimal-gallery-row" aria-label="Additional photos">
						{placeholderGallery.map((item) => (
							<div key={item} className="specificanimal-gallery-placeholder">
								<span>Photo</span>
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
							<div className="specificanimal-detail-item">
								<span className="specificanimal-detail-label">Misc</span>
								<span>{animal.misc}</span>
							</div>
						</div>
					</div>
				</section>
			</main>

			<HomeFooter />
		</div>
	)
}
