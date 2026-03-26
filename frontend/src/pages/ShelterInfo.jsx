import { useEffect, useRef, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/ShelterInfo.css'

export default function ShelterInfo() {
	const [openFilter, setOpenFilter] = useState(null)
	const [selectedRadius, setSelectedRadius] = useState('')
	const [selectedType, setSelectedType] = useState('')
	const topbarRef = useRef(null)
	const placeholderShelters = [1, 2, 3, 4, 5, 6]

	const radiusOptions = [
		'Within 20 miles',
		'Within 40 miles',
		'Within 80 miles',
		'Within 100 miles',
	]

	const shelterTypeOptions = [
		'Municipal Shelter',
		'Private Shelter',
		'Rescue Organization',
		'Foster-Based Rescue',
		'Breed-Specific Rescue',
		'Sanctuary',
		'Adoption Center',
	]

	const toggleFilter = (filterName) => {
		setOpenFilter((currentFilter) =>
			currentFilter === filterName ? null : filterName,
		)
	}

	useEffect(() => {
		document.body.classList.add('shelterinfo-body')
		return () => document.body.classList.remove('shelterinfo-body')
	}, [])

	useEffect(() => {
		const handleDocumentMouseDown = (event) => {
			if (!topbarRef.current?.contains(event.target)) {
				setOpenFilter(null)
			}
		}

		document.addEventListener('mousedown', handleDocumentMouseDown)
		return () => document.removeEventListener('mousedown', handleDocumentMouseDown)
	}, [])

	return (
		<div className="shelterinfo-page">
			<HomeHeader />
			<main className="shelterinfo-main" >
				<section className="shelterinfo-topbar" ref={topbarRef}>
					<h1>Filter shelter by:</h1>
					<div className="shelterinfo-filter-row">
						<input
							type="text"
							className="shelterinfo-search-input shelterinfo-location-input"
							placeholder="Your City"
							aria-label="Location"
						/>

						<div className={`shelterinfo-dropdown-group radius-dropdown-group ${openFilter === 'radius' ? 'open' : ''}`}>
							<button
								type="button"
								className={`shelterinfo-dropdown-toggle ${selectedRadius ? '' : 'shelterinfo-dropdown-placeholder'}`}
								onClick={() => toggleFilter('radius')}
							>
								{selectedRadius || 'Search Radius'}
							</button>
							{openFilter === 'radius' && (
								<div className="shelterinfo-dropdown-options">
									{radiusOptions.map((radiusOption) => (
										<button
											key={radiusOption}
											type="button"
											className="shelterinfo-dropdown-option-button"
											onClick={() => {
												setSelectedRadius(radiusOption)
												setOpenFilter(null)
											}}
										>
											{radiusOption}
										</button>
									))}
								</div>
							)}
						</div>

						<div className={`shelterinfo-dropdown-group type-dropdown-group ${openFilter === 'type' ? 'open' : ''}`}>
							<button
								type="button"
								className={`shelterinfo-dropdown-toggle ${selectedType ? '' : 'shelterinfo-dropdown-placeholder'}`}
								onClick={() => toggleFilter('type')}
							>
								{selectedType || 'Shelter Type'}
							</button>
							{openFilter === 'type' && (
								<div className="shelterinfo-dropdown-options">
									{shelterTypeOptions.map((typeOption) => (
										<button
											key={typeOption}
											type="button"
											className="shelterinfo-dropdown-option-button"
											onClick={() => {
												setSelectedType(typeOption)
												setOpenFilter(null)
											}}
										>
											{typeOption}
										</button>
									))}
								</div>
							)}
						</div>
					</div>
				</section>

				<section className="shelterinfo-placeholder-grid" aria-label="Shelter placeholders">
					{placeholderShelters.map((placeholderId) => (
						<article key={placeholderId} className="shelterinfo-placeholder-card" aria-hidden="true" />
					))}
				</section>
			</main>
		</div>
	)
}
