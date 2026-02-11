import '../styling/header.css'
import { Logo } from '../components/logo'
import { Link } from 'react-router-dom'

export default function HomeHeader() {
  return (
    <header className="site-header">

      <div className="header-left">
        <Logo />
      </div>

      <nav className="header-nav">
        <Link to="/home" className="header-link">HOME</Link>
        <Link to="/about" className="header-link">ABOUT US</Link>
        <Link to="/help" className="header-link">HELP</Link>
        <Link to="/select-animal" className="header-link">FIND A PET</Link>
        <Link to="/login" className="header-link">SIGN IN / LOG OUT</Link>
      </nav>

    </header>
  )
}
