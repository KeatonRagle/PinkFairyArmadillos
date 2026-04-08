import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import { getFilteredPets } from '../fetch/api'
import '../styling/AnimalFilter.css'

const placeholderCards = [1, 2, 3, 4, 5, 6]
const genderOptions = [
	{ value: 'M', label: 'Male' },
	{ value: 'F', label: 'Female' },
]
const sizeOptions = ['Small', 'Medium', 'Large']
const ageOptions = [
	{ key: 'puppy', label: 'Puppy:  < 1 year', endAge: 0 },
	{ key: 'young', label: 'Young: 1-3 years', startAge: 1, endAge: 3 },
	{ key: 'adult', label: 'Adult: 3-8 years', startAge: 3, endAge: 8 },
	{ key: 'senior', label: 'Senior: > 8 years', startAge: 8 },
]

function normalize(value) {
	if (value === null || value === undefined) return ''
	return String(value).trim().toLowerCase()
}

function filterPetsClientSide(pets, filters) {
	const { petType, gender, startAge, endAge, breed, size } = filters
	const normPetType = normalize(petType)
	const normGender = normalize(gender)
	const normBreed = normalize(breed)
	const normSize = normalize(size)

	return pets.filter((pet) => {
		if (normPetType && normalize(pet.pet_type) !== normPetType) return false
		if (normGender && normalize(pet.gender) !== normGender) return false
		if (normBreed && !normalize(pet.breed).includes(normBreed)) return false
		if (normSize && normalize(pet.size) !== normSize) return false

		const age = Number(pet.age)
		if (Number.isFinite(startAge) && (!Number.isFinite(age) || age < startAge)) return false
		if (Number.isFinite(endAge) && (!Number.isFinite(age) || age > endAge)) return false

		return true
	})
}

function mapPetToAnimal(pet) {
	return {
		id: pet.id,
		name: pet.name,
		breed: pet.breed,
		age: pet.age,
		gender: pet.gender === 'M' ? 'Male' : pet.gender === 'F' ? 'Female' : pet.gender,
		location: pet.location,
		misc: pet.pet_status || pet.pet_type || 'More details coming soon.',
		adoptionSite: 'Adoption site information coming soon.',
		image: pet.img_url || '/images/waveShort.png',
	}
}

export default function AnimalFilter() {
	const filterPanelRef = useRef(null)
	const location = useLocation()
	const selectedPetType = location.state?.petType ?? null

	const [openFilter, setOpenFilter] = useState(null)
	const [breedSearchText, setBreedSearchText] = useState('')
	const [debouncedBreedSearchText, setDebouncedBreedSearchText] = useState('')
	const [shelterSearchText, setShelterSearchText] = useState('')
	const [selectedGender, setSelectedGender] = useState(null)
	const [selectedSize, setSelectedSize] = useState(null)
	const [selectedAgeRange, setSelectedAgeRange] = useState(null)

	const [isLoading, setIsLoading] = useState(true)
	const [hasLoaded, setHasLoaded] = useState(false)
	const [errorMessage, setErrorMessage] = useState('')
	const [animals, setAnimals] = useState([])

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

	const toggleGender = (genderValue) => {
		setSelectedGender((currentValue) =>
			currentValue === genderValue ? null : genderValue,
		)
		setOpenFilter(null)
	}

	const toggleSize = (sizeValue) => {
		setSelectedSize((currentValue) =>
			currentValue === sizeValue ? null : sizeValue,
		)
		setOpenFilter(null)
	}

	const toggleAgeRange = (ageOption) => {
		setSelectedAgeRange((currentValue) =>
			currentValue?.key === ageOption.key ? null : ageOption,
		)
		setOpenFilter(null)
	}

	useEffect(() => {
		document.body.classList.add('animalfilter-body')
		return () => document.body.classList.remove('animalfilter-body')
	}, [])

	useEffect(() => {
		const handleDocumentMouseDown = (event) => {
			if (!filterPanelRef.current?.contains(event.target)) {
				setOpenFilter(null)
			}
		}

		document.addEventListener('mousedown', handleDocumentMouseDown)
		return () => document.removeEventListener('mousedown', handleDocumentMouseDown)
	}, [])

	useEffect(() => {
		const timeoutId = window.setTimeout(() => {
			setDebouncedBreedSearchText(breedSearchText.trim())
		}, 300)

		return () => window.clearTimeout(timeoutId)
	}, [breedSearchText])

	useEffect(() => {
		let isCancelled = false

		const loadFilteredPets = async () => {
			setIsLoading(true)
			setErrorMessage('')

			try {
				// Fetch all pets (no server-side filters) and filter client-side
                const allPets = await getFilteredPets({
                    petType: selectedPetType,
                    gender: selectedGender,
                    breed: debouncedBreedSearchText,
                    startAge: selectedAgeRange?.startAge,
                    endAge: selectedAgeRange?.endAge,
                    size: selectedSize,
                })
				const petsArray = Array.isArray(allPets) ? allPets : []

				if (!isCancelled) {
					setAnimals(petsArray.map(mapPetToAnimal))
				}
			} catch {
				if (!isCancelled) {
					setAnimals([])
					setErrorMessage('Unable to load pets right now.')
				}
			} finally {
				if (!isCancelled) {
					setIsLoading(false)
					setHasLoaded(true)
				}
			}
		}

		loadFilteredPets()

		return () => {
			isCancelled = true
		}
	}, [
		debouncedBreedSearchText,
		selectedAgeRange,
		selectedGender,
		selectedPetType,
		selectedSize,
	])

	const genderLabel = selectedGender === 'M' ? 'Male' : selectedGender === 'F' ? 'Female' : null
	const ageLabel = selectedAgeRange?.label ?? null

	return (
		<div className="animalfilter-page">
			<HomeHeader />

			<main className="animalfilter-main">
				<section className="animalfilter-panel" ref={filterPanelRef}>
					<h1>Filters</h1>

					<div className={`gender-filter-group ${openFilter === 'gender' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown gender-toggle"
							onClick={() => toggleFilter('gender')}
						>
							{genderLabel ? `Gender: ${genderLabel}` : 'Gender'}
						</button>
						{openFilter === 'gender' && (
							<div className="gender-options">
								{genderOptions.map((option) => (
									<button
										key={option.value}
										type="button"
										className={`gender-option-button ${selectedGender === option.value ? 'is-selected' : ''}`}
										onClick={() => toggleGender(option.value)}
									>
										{option.label}
									</button>
								))}
							</div>
						)}
					</div>

					<div className={`breed-filter-group ${openFilter === 'breed' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown breed-toggle"
							onClick={() => toggleFilter('breed')}
						>
							{breedSearchText ? `Breed: ${breedSearchText}` : 'Breed'}
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
							{selectedSize ? `Size: ${selectedSize}` : 'Size'}
						</button>
						{openFilter === 'size' && (
							<div className="size-options">
								{sizeOptions.map((sizeOption) => (
									<button
										key={sizeOption}
										type="button"
										className={`size-option-button ${selectedSize === sizeOption ? 'is-selected' : ''}`}
										onClick={() => toggleSize(sizeOption)}
									>
										{sizeOption}
									</button>
								))}
							</div>
						)}
					</div>

                    {/* <div className={`age-filter-group ${openFilter === 'age' ? 'open' : ''}`}>
						<button
							type="button"
							className="filter-dropdown age-toggle"
							onClick={() => toggleFilter('age')}
						>
							{ageLabel ? `Age: ${ageLabel}` : 'Age'}
						</button>
						{openFilter === 'age' && (
							<div className="age-options">
								{ageOptions.map((option) => (
									<button
										key={option.key}
										type="button"
										className={`age-option-button ${selectedAgeRange?.key === option.key ? 'is-selected' : ''}`}
										onClick={() => toggleAgeRange(option)}
									>
										{option.label}
									</button>
								))}
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
					</div> */}
				</section>

				<section className="animal-results" aria-label="Animal results">
					{isLoading ? (
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
					) : errorMessage ? (
						<p className="animal-results-empty">{errorMessage}</p>
					) : hasLoaded && animals.length === 0 ? (
						<p className="animal-results-empty">No animals match the selected filters.</p>
					) : (
						animals.map((animal) => (
							<Link
								key={animal.id}
								to="/specific-animal"
								state={{ animal }}
								className="animal-card-link"
							>
								<article className="animal-card">
									<div className="animal-card-image-wrap">
										<img src={animal.image} alt={animal.name} className="animal-card-image" />
									</div>
									<div className="animal-card-info">
										<h2>{animal.name}</h2>
										<p>Breed: {animal.breed}</p>
										<p>Gender: {animal.gender}</p>
									</div>
								</article>
							</Link>
						))
					)}
				</section>
			</main>

			<HomeFooter />
		</div>
	)
}
