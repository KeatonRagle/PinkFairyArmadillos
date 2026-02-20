import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/AnimalFilter.css'

export default function AnimalFilter() {
	const [showGenderOptions, setShowGenderOptions] = useState(false)
	const [showSizeOptions, setShowSizeOptions] = useState(false)

	useEffect(() => {
		document.body.classList.add('animalfilter-body')
		return () => document.body.classList.remove('animalfilter-body')
	}, [])

	return (
		<div className="animalfilter-page">
			<HomeHeader />

			<main className="animalfilter-main">
				<section className="animalfilter-panel">
					<h1>Filters</h1>

					<div className={`gender-filter-group ${showGenderOptions ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown gender-toggle"
							onClick={() => setShowGenderOptions((open) => !open)}
						>
							Gender
						</button>
						{showGenderOptions && (
							<div className="gender-options">
								<button type="button" className="gender-option-button">Male</button>
								<button type="button" className="gender-option-button">Female</button>
							</div>
						)}
					</div>

					<select className="filter-dropdown" defaultValue="Breed">
						<option disabled>Breed</option>
					</select>

					<div className={`size-filter-group ${showSizeOptions ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown size-toggle"
							onClick={() => setShowSizeOptions((open) => !open)}
						>
							Size
						</button>
						{showSizeOptions && (
							<div className="size-options">
								<button type="button" className="size-option-button">Small</button>
								<button type="button" className="size-option-button">Medium</button>
								<button type="button" className="size-option-button">Large</button>
							</div>
						)}
					</div>

					<select className="filter-dropdown" defaultValue="Age">
						<option disabled>Age</option>
					</select>

					<select className="filter-dropdown" defaultValue="Compatibility">
						<option disabled>Compatibility</option>
					</select>

					<select className="filter-dropdown" defaultValue="Shelter">
						<option disabled>Shelter</option>
					</select>

					<select className="filter-dropdown" defaultValue="Price">
						<option disabled>Price</option>
					</select>
				</section>
			</main>

			<HomeFooter />
		</div>
	)
}