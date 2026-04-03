import '../styling/header.css'
import { Logo } from '../components/logo'
import { Link, useNavigate } from 'react-router-dom'
import { useEffect, useRef, useState } from 'react'
import { useAuth } from '../auth/AuthContext.jsx'

export default function HomeHeader() {
  const navigate = useNavigate()
  const menuRef = useRef(null)
  const { username, role, clearAuth } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)

  const isContributor = role === 'ROLE_CONTRIBUTOR' || role === 'ROLE_ADMIN'
  const isAdmin = role === 'ROLE_ADMIN'

  useEffect(() => {
    if (!menuOpen) {
      return undefined
    }

    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOpen(false)
      }
    }

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setMenuOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    document.addEventListener('keydown', handleEscape)

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [menuOpen])

  const handleLogout = () => {
    clearAuth()
    setMenuOpen(false)
    navigate('/login')
  }

  const closeMenu = () => {
    setMenuOpen(false)
  }

  return (
    <header className="site-header">
      <div className="header-left">
        <Logo />
      </div>

      <nav className="header-nav">
        {username && (
          <div className="account-menu" ref={menuRef}>
            <button
              type="button"
              className="header-link account-button"
              onClick={() => setMenuOpen((current) => !current)}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
            >
              ACCOUNT
            </button>

            {menuOpen && (
              <div className="account-dropdown" role="menu">
                <Link to="/profile" className="account-dropdown-link" role="menuitem" onClick={closeMenu}>
                  Account Settings
                </Link>
                <button type="button" className="account-dropdown-link is-placeholder" role="menuitem" disabled>
                  Contributor Application
                </button>
                {isContributor ? (
                  <Link to="/contribute" className="account-dropdown-link" role="menuitem" onClick={closeMenu}>
                    Contribute
                  </Link>
                ) : null}
                {isAdmin ? (
                  <Link to="/request" className="account-dropdown-link" role="menuitem" onClick={closeMenu}>
                    Approval
                  </Link>
                ) : null}
                {isAdmin ? (
                  <button type="button" className="account-dropdown-link is-placeholder" role="menuitem" disabled>
                    User Management
                  </button>
                ) : null}
                <button type="button" className="account-dropdown-link" role="menuitem" onClick={handleLogout}>
                  Log Out
                </button>
              </div>
            )}
          </div>
        )}
        <Link to="/home" className="header-link">HOME</Link>
        <Link to="/about" className="header-link">ABOUT US</Link>
        <Link to="/help" className="header-link">HELP</Link>
        <Link to="/select-animal" className="header-link">FIND A PET</Link>
        {!username && (
          <Link to="/login" className="header-link">LOG IN</Link>
        )}
      </nav>
    </header>
  )
}
