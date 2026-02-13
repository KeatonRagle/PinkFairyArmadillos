import '../styling/AboutUs.css'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer2'
import { useEffect } from 'react'

export default function About() {

  useEffect(() => {
    document.body.classList.add('about-body')
    return () => document.body.classList.remove('about-body')
  }, [])

  return (
    <div className="about-page">

      <HomeHeader />
    <main className="about-main">
      <div className="section-bar">
        <h1>About Us</h1>
        <p>
          We are a team of six students at Abilene Christian University, 
          collaborating as part of our Software Engineering course. 
          As students, we are excited to apply the skills we've learned 
          in web development, design, and user experience through this project. 
          This project allows us to work together, strengthen our technical abilities, and gain hands-on experience in software development.
        </p>
      </div>

      <div className="the-rest">
        <h2>Our Vision</h2>
        <p>
          Our vision is to create a welcoming and intuitive platform that 
          makes the process of discovering and connecting with future pets 
          simple, enjoyable, and accessible for all users. We aim to design 
          an experience that is visually engaging while remaining easy to navigate, 
          ensuring users can quickly find animals that match their preferences and 
          ifestyle. Through thoughtful design and reliable functionality, we hope to promote 
          responsible pet adoption and help connect animals with caring homes. As students and 
          developers, we also envision this project as an opportunity to grow our collaboration, 
          problem-solving, and technical skills while building something meaningful and impactful.
        </p>
      </div>
      <img src="/images/aboutpic.jpg" className="aboutpic" />
    </main>
      <HomeFooter />

    </div>
  )
}
