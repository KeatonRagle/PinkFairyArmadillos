import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/AnimalFilter.css'

export default function AnimalFilter() {
	const [openFilter, setOpenFilter] = useState(null)
	const [breedSearchText, setBreedSearchText] = useState('')
	const [shelterSearchText, setShelterSearchText] = useState('')
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
			</main>

			<HomeFooter />
		</div>
	)
}