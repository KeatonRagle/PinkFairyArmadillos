import '../styling/footer.css'
import { Link } from 'react-router-dom'

export default function HomeFooter() {
  return (
    <footer className="site-footer">
      <div className="footer-content">
        <p className="footer-text">ARE YOU A NEW ADOPTER? CHECK OUT OUR NEW ADOPTERS GUIDE.</p>
        <button className="footer-button">ADOPTER INFO</button>
      </div>
    </footer>
  )
}