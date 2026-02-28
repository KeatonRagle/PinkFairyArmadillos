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
        <button className="ThreeButtonsHome newest-arrivals-button" onClick={handleNewestArrivalsClick}>
          <span className="newest-arrivals-title">Newest Arrivals</span>
          <ul className="newest-arrivals-list">
            <li>Meet the newest animals to join us!</li>
            <li>recent arrivals you can learn about, visit, or adopt.</li>
          </ul>
        </button>
        <button className="ThreeButtonsHome discussion-board-button" onClick={handleDiscussionBoardClick}>
          <span className="discussion-board-title">Discussion Board</span>
          <ul className="discussion-board-list">
            <li>Share your thoughts, ask questions, and chat with others about animals, care tips, and experiences.</li>
          </ul>
        </button>
        <button className="ThreeButtonsHome shelter-info-button" onClick={handleShelterInfoClick}>
          <span className="shelter-info-title">Shelter Info</span>
          <ul className="shelter-info-list">
            <li>Learn about different shelters including hours, locations, policies etc.</li>
          </ul>
        </button>
      </div>
    
      <img src="/images/dogs.jpg" className="bg-dogs" />
      <img src="/images/cats.jpg" className="bg-cats" />
      <img src="/images/bothWaves.svg" className="bg-bothWaves" /> 

      <HomeFooter />
    

    </div>
  )
}
