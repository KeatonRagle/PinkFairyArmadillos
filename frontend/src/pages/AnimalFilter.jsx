import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/AnimalFilter.css'

export default function AnimalFilter() {
	const [openFilter, setOpenFilter] = useState(null)
	const [breedSearchText, setBreedSearchText] = useState('')
	const [shelterSearchText, setShelterSearchText] = useState('')
	const [isLoading] = useState(false)
	const [currentPage, setCurrentPage] = useState(1)
	const animalsPerPage = 6
	// ai added these comments, this is for the api stuff
	// Data contract for API results:
	// Each animal should include: id, name, breed, age, gender, image
	// If backend field names differ, map them to these names before rendering cards.
	const [animals] = useState([])
	const placeholderCards = [1, 2, 3, 4, 5, 6]
	const [compatibilitySelections, setCompatibilitySelections] = useState({
		dogs: false,
		cats: false,
		otherAnimals: false,
		adults: false,
		children: false,
	})

	const compatibilityOptions = [
		{ key: 'dogs', label: 'Dogs' },
		{ key: 'cats', label: 'Cats' },
		{ key: 'otherAnimals', label: 'Other Animals' },
		{ key: 'adults', label: 'Adults' },
		{ key: 'children', label: 'Children' },
	]

	const toggleCompatibilitySelection = (key) => {
		setCompatibilitySelections((previous) => ({
			...previous,
			[key]: !previous[key],
		}))
	}

	const toggleFilter = (filterName) => {
		setOpenFilter((currentFilter) =>
			currentFilter === filterName ? null : filterName,
		)
	}

	useEffect(() => {
		document.body.classList.add('animalfilter-body')
		return () => document.body.classList.remove('animalfilter-body')
	}, [])

	const totalPages = Math.max(1, Math.ceil(animals.length / animalsPerPage))
	const startIndex = (currentPage - 1) * animalsPerPage
	const paginatedAnimals = animals.slice(startIndex, startIndex + animalsPerPage)

	useEffect(() => {
		if (currentPage > totalPages) {
			setCurrentPage(totalPages)
		}
	}, [currentPage, totalPages])

	// ai added these comments, this is for the api stuff
	// Future API hookup:
	// 1) set loading true before request
	// 2) map backend response into the animals shape above
	// 3) set loading false after response/error

	return (
		<div className="animalfilter-page">
			<HomeHeader />

			<main className="animalfilter-main">
				<section className="animalfilter-panel">
					<h1>Filters</h1>

					<div className={`gender-filter-group ${openFilter === 'gender' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown gender-toggle"
							onClick={() => toggleFilter('gender')}
						>
							Gender
						</button>
						{openFilter === 'gender' && (
							<div className="gender-options">
								<button type="button" className="gender-option-button">Male</button>
								<button type="button" className="gender-option-button">Female</button>
							</div>
						)}
					</div>

					<div className={`breed-filter-group ${openFilter === 'breed' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown breed-toggle"
							onClick={() => toggleFilter('breed')}
						>
							Breed
						</button>
						{openFilter === 'breed' && (
							<div className="breed-options">
								<input
									type="text"
									className="breed-search-input"
									placeholder="Search breed"
									value={breedSearchText}
									onChange={(event) => setBreedSearchText(event.target.value)}
								/>
							</div>
						)}
					</div>

					<div className={`size-filter-group ${openFilter === 'size' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown size-toggle"
							onClick={() => toggleFilter('size')}
						>
							Size
						</button>
						{openFilter === 'size' && (
							<div className="size-options">
								<button type="button" className="size-option-button">Small</button>
								<button type="button" className="size-option-button">Medium</button>
								<button type="button" className="size-option-button">Large</button>
							</div>
						)}
					</div>

					<div className={`age-filter-group ${openFilter === 'age' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown age-toggle"
							onClick={() => toggleFilter('age')}
						>
							Age
						</button>
						{openFilter === 'age' && (
							<div className="age-options">
								<button type="button" className="age-option-button">Puppy:  &lt; 1 year</button>
								<button type="button" className="age-option-button">Young: 1-3 years</button>
								<button type="button" className="age-option-button">Adult: 3-8 years</button>
								<button type="button" className="age-option-button">Senior: &gt; 8 years</button>
							</div>
						)}
					</div>

					<div className={`compatibility-filter-group ${openFilter === 'compatibility' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown compatibility-toggle"
							onClick={() => toggleFilter('compatibility')}
						>
							Compatibility
						</button>
						{openFilter === 'compatibility' && (
							<div className="compatibility-options">
								{compatibilityOptions.map((option) => (
									<label key={option.key} className="compatibility-option-item">
										<input
											type="checkbox"
											checked={compatibilitySelections[option.key]}
											onChange={() => toggleCompatibilitySelection(option.key)}
										/>
										<span>{option.label}</span>
									</label>
								))}
							</div>
						)}
					</div>

					<div className={`shelter-filter-group ${openFilter === 'shelter' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown shelter-toggle"
							onClick={() => toggleFilter('shelter')}
						>
							Shelter
						</button>
						{openFilter === 'shelter' && (
							<div className="shelter-options">
								<input
									type="text"
									className="shelter-search-input"
									placeholder="Search shelter"
									value={shelterSearchText}
									onChange={(event) => setShelterSearchText(event.target.value)}
								/>
							</div>
						)}
					</div>

					<div className={`price-filter-group ${openFilter === 'price' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown price-toggle"
							onClick={() => toggleFilter('price')}
						>
							Price
						</button>
						{openFilter === 'price' && (
							<div className="price-options">
								<button type="button" className="price-option-button">Low: &lt; $100</button>
								<button type="button" className="price-option-button">Medium: $100 - $200</button>
								<button type="button" className="price-option-button">High: &gt; $200</button>
							</div>
						)}
					</div>
				</section>

				<section className="animal-results" aria-label="Animal results">
					{isLoading || animals.length === 0 ? (
						placeholderCards.map((cardId) => (
							<article key={cardId} className="animal-card animal-card-placeholder" aria-hidden="true">
								<div className="animal-card-image-wrap animal-placeholder-image" />
								<div className="animal-card-info animal-placeholder-info">
									<div className="animal-placeholder-line animal-placeholder-title" />
									<div className="animal-placeholder-line" />
									<div className="animal-placeholder-line" />
									<div className="animal-placeholder-line" />
								</div>
							</article>
						))
					) : (
						paginatedAnimals.map((animal) => (
							<article key={animal.id} className="animal-card">
								<div className="animal-card-image-wrap">
									<img src={animal.image} alt={animal.name} className="animal-card-image" />
								</div>
								<div className="animal-card-info">
									<h2>{animal.name}</h2>
									<p>Breed: {animal.breed}</p>
									<p>Age: {animal.age}</p>
									<p>Gender: {animal.gender}</p>
								</div>
							</article>
						))
					)}

					{!isLoading && animals.length > animalsPerPage && currentPage < totalPages && (
						<button
							type="button"
							className="animal-results-next"
							onClick={() => setCurrentPage((page) => page + 1)}
							aria-label="Next results page"
						>
							→
						</button>
					)}
				</section>
			</main>

			<HomeFooter />
		</div>
	)
}