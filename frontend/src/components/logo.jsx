import logo from '../assets/logo.png'

export function Logo({ className = '' }) {
  return (
    <div className={`logo ${className}`}>
      <img src={logo} alt="Logo" />
    </div>
  )
}
