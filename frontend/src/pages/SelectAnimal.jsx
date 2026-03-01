import '../styling/SelectAnimal.css'
import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from "../components/header"
import HomeFooter from "../components/footer"

export default function SelectAnimal() {
  const navigate = useNavigate()

  useEffect(() => {
    document.body.classList.add('selectanimal-body')
    return () => document.body.classList.remove('selectanimal-body')
  }, [])

  return (
    <div className="selectanimal-page">

      <HomeHeader />

      <div className="ButtonRow">
        <button className="ThreeButtons" onClick={() => navigate('/animal-filter')}>
        <img src="/images/dog png.png" alt="Dog" className="button-icon" />
        <span className="button-text">Dogs</span>
        </button>
        <button className="ThreeButtons" onClick={() => navigate('/animal-filter')}>
        <img src="/images/cat png.png" alt="Cat" className="button-icon" />
        <span className="button-text">Cats</span>
        </button>
      </div>

      <img src="/images/dogs.jpg" className="bg-dogs" />
      <img src="/images/waveShort.png" className="bg-wave" />

      <HomeFooter />

    </div>
  )
}
