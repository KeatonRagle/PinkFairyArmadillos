import '../styling/footer.css'
import { Link } from 'react-router-dom'

export default function HomeFooter() {
  return (
    <footer className="site-footer">
      <div className="footer-content">
        <p className="footer-text">ARE YOU A NEW ADOPTER? CHECK OUT OUR NEW ADOPTERS GUIDE.</p>
        <div className="footer-button-row">
          <Link to="/adopters-info" className="footer-button">ADOPTER INFO</Link>
          <Link to="/shelter-info" className="footer-button">SHELTER INFO</Link>
        </div>
      </div>
    </footer>
  )
}