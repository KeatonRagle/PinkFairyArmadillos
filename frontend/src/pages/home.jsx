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

  const handleNewestArrivalsClick = () => {
    navigate('/animal-filter')
  }

  const handleDiscussionBoardClick = () => {
    navigate('/discussion-board')
  }

  const handleShelterInfoClick = () => {
    navigate('/shelter-info')
  }

  return (
    <div className="home-page">

      
      <HomeHeader />
      
      <button className="MainButton" onClick={handleSubmit} >Find Your Future Pet</button>
      <div className = "ButtonRowHome">
        <button className="ThreeButtonsHome" onClick={handleNewestArrivalsClick}>Newest Arrivals</button>
        <button className="ThreeButtonsHome" onClick={handleDiscussionBoardClick}>Discussion Board</button>
        <button className="ThreeButtonsHome" onClick={handleShelterInfoClick}>Shelter Info</button>
      </div>
    
      <img src="/images/dogs.jpg" className="bg-dogs" />
      <img src="/images/cats.jpg" className="bg-cats" />
      <img src="/images/bothWaves.svg" className="bg-bothWaves" /> 

      <HomeFooter />
    

    </div>
  )
}
