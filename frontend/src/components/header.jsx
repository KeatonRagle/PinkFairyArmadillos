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
  const isRegularUser = Boolean(username) && role === 'ROLE_USER'

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
        <div className="header-nav-links">
          <Link to="/home" className="header-link">HOME</Link>
          <Link to="/about" className="header-link">ABOUT US</Link>
          <Link to="/help" className="header-link">HELP</Link>
          <Link to="/select-animal" className="header-link">FIND A PET</Link>
          {!username && (
            <Link to="/login" className="header-link">LOG IN</Link>
          )}
        </div>

        {username && (
          <div className="header-account-section">
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
                  {isRegularUser ? (
                    <Link to="/contributor-application" className="account-dropdown-link" role="menuitem" onClick={closeMenu}>
                      Contributor Application
                    </Link>
                  ) : null}
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
                    <Link to="/user-management" className="account-dropdown-link" role="menuitem" onClick={closeMenu}>
                      User Management
                    </Link>
                  ) : null}
                  <button type="button" className="account-dropdown-link" role="menuitem" onClick={handleLogout}>
                    Log Out
                  </button>
                </div>
              )}
            </div>

            <span className="header-username" aria-label="Signed in username">
              {username}
            </span>
          </div>
        )}
      </nav>
    </header>
  )
}
