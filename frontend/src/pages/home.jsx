import '../styling/home.css'
import { useEffect } from 'react'
import HomeHeader from "../components/header"
import HomeFooter from "../components/footer"
import { useNavigate } from 'react-router-dom'

export default function Home() {

  const navigate = useNavigate()

  useEffect(() => {
    document.body.classList.add('home-body')
    return () => document.body.classList.remove('home-body')
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
