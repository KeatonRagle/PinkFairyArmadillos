import '../styling/SelectAnimal.css'
import { useEffect } from 'react'
import HomeHeader from "../components/header"
import HomeFooter from "../components/footer"

export default function SelectAnimal() {

  useEffect(() => {
    document.body.classList.add('selectanimal-body')
    return () => document.body.classList.remove('selectanimal-body')
  }, [])

  return (
    <div className="selectanimal-page">

      <HomeHeader />
      <HomeFooter />

      <div className="ButtonRow">
        <button className="ThreeButtons">
        <img src="/images/dog png.png" alt="Dog" className="button-icon" />
        <span className="button-text">Dogs</span>
        </button>
        <button className="ThreeButtons">
        <img src="/images/cat png.png" alt="Cat" className="button-icon" />
        <span className="button-text">Cats</span>
        </button>
        <button className="ThreeButtons">
        <img src="/images/bunny.png" alt="Other" className="button-icon" />
        <span className="button-text">Other Animals</span>
        </button>
      </div>

      <img src="/images/dogs.jpg" className="bg-dogs" />
      <img src="/images/waveShort.svg" className="bg-wave" />


    </div>
  )
}
