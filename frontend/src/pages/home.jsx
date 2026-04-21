import '../styling/home.css'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import HomeHeader from "../components/header"
import HomeFooter from "../components/footer"
import { useNavigate } from 'react-router-dom'
import { getFeaturedPets, getFilteredPets } from '../fetch/api'

function mapPetToAnimal(pet) {
  return {
    id: pet.id,
    name: pet.name,
    breed: pet.breed,
    age: pet.age,
    gender: pet.gender === 'M' ? 'Male' : pet.gender === 'F' ? 'Female' : pet.gender,
    location: pet.location,
    misc: pet.pet_status || pet.pet_type || 'More details coming soon.',
    adoptionSite: pet.adoption_site_name || pet.adoptionSite || 'Adoption site information coming soon.',
    site_id: pet.site_id ?? pet.siteId ?? null,
    adoption_site_url: pet.adoption_site_url || pet.adoptionSiteUrl || null,
    adoption_site_email: pet.adoption_site_email || pet.adoptionSiteEmail || null,
    adoption_site_phone: pet.adoption_site_phone || pet.adoptionSitePhone || null,
    image: pet.img_url || '/images/waveShort.png',
  }
}

export default function Home() {

  const navigate = useNavigate()
  const [featuredPets, setFeaturedPets] = useState([])
  const [featuredError, setFeaturedError] = useState('')
  const [featuredLoading, setFeaturedLoading] = useState(true)

  useEffect(() => {
    document.body.classList.add('home-body')
    return () => document.body.classList.remove('home-body')
  }, [])

  useEffect(() => {
    let isCancelled = false

    const loadFeaturedPets = async () => {
      setFeaturedLoading(true)
      setFeaturedError('')

      try {
        const [featured, pets] = await Promise.all([
          getFeaturedPets(),
          getFilteredPets(),
        ])

        if (isCancelled) return

        const featuredIds = Array.isArray(featured)
          ? featured.map((item) => item.petId)
          : []
        const petMap = new Map(
          (Array.isArray(pets) ? pets : []).map((pet) => [pet.id, mapPetToAnimal(pet)]),
        )

        setFeaturedPets(
          featuredIds
            .map((petId) => petMap.get(petId))
            .filter(Boolean)
            .slice(0, 2),
        )
      } catch {
        if (!isCancelled) {
          setFeaturedPets([])
          setFeaturedError('Unable to load featured pets right now.')
        }
      } finally {
        if (!isCancelled) {
          setFeaturedLoading(false)
        }
      }
    }

    loadFeaturedPets()

    return () => {
      isCancelled = true
    }
  }, [])

  const handleSubmit = (e) => {
    e.preventDefault()
    navigate('/select-animal')
  }

  return (
    <div className="home-page">

      
      <HomeHeader />
      
      <button className="MainButton" onClick={handleSubmit} >Find Your Future Pet</button>
    
      <img src="/images/dogs.jpg" className="bg-dogs" />
      <img src="/images/cats.jpg" className="bg-cats" />
      <img src="/images/bothWaves.svg" className="bg-bothWaves" />

    <section className="featured-section" aria-labelledby="featured-pets-heading">
      <div className="featured-box">
        <h2 id="featured-pets-heading">Featured Pets</h2>
        {featuredLoading ? (
          <div className="featured-grid">
            {[1, 2].map((cardId) => (
              <article key={cardId} className="featured-card featured-card-placeholder" aria-hidden="true">
                <div className="featured-card-image featured-placeholder-image" />
                <div className="featured-card-body">
                  <div className="featured-placeholder-line featured-placeholder-title" />
                  <div className="featured-placeholder-line" />
                  <div className="featured-placeholder-line" />
                </div>
              </article>
            ))}
          </div>
        ) : featuredError ? (
          <p className="featured-empty">{featuredError}</p>
        ) : featuredPets.length === 0 ? (
          <p className="featured-empty">No featured pets are available right now.</p>
        ) : (
          <div className="featured-grid">
            {featuredPets.map((pet) => (
              <Link
                key={pet.id}
                to="/specific-animal"
                state={{ animal: pet }}
                className="featured-card-link"
              >
                <article className="featured-card">
                  <img src={pet.image} alt={pet.name} className="featured-card-image" />
                  <div className="featured-card-body">
                    <h3>{pet.name}</h3>
                    <p>Breed: {pet.breed}</p>
                    <p>Gender: {pet.gender}</p>
                  </div>
                </article>
              </Link>
            ))}
          </div>
        )}
      </div>
    </section>

      <div className="about-section">
        <div className="about-box">
          <h2>About Us</h2>
          <p>
            We are a team of six students at Abilene Christian University, 
            collaborating as part of our Software Engineering course. 
            As students, we are excited to apply the skills we've learned 
            in web development, design, and user experience through this project. 
            This project allows us to work together, strengthen our technical abilities, and gain hands-on experience in software development.
          </p>
          <h2>Our Vision</h2>
          <p>
            Our vision is to create a welcoming and intuitive platform that 
            makes the process of discovering and connecting with future pets 
            simple, enjoyable, and accessible for all users. We aim to design 
            an experience that is visually engaging while remaining easy to navigate, 
            ensuring users can quickly find animals that match their preferences and 
            lifestyle. Through thoughtful design and reliable functionality, we hope to promote 
            responsible pet adoption and help connect animals with caring homes. As students and 
            developers, we also envision this project as an opportunity to grow our collaboration, 
            problem-solving, and technical skills while building something meaningful and impactful.
          </p>
        </div>
      </div>

      <HomeFooter />
    

    </div>
  )
}
