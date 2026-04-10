import logo from '../assets/logo.png'
import { Link } from 'react-router-dom'

export function Logo({ className = '' }) {
  return (
    <Link to="/home" className={`logo ${className}`} aria-label="Go to home page">
      <img src={logo} alt="Logo" />
    </Link>
  )
}
