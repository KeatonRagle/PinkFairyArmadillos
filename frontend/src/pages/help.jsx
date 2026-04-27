import { useEffect } from "react"
import HomeHeader from "../components/header"
import HomeFooter from "../components/footer"
import "../styling/help.css"

// Help and support page component
export default function Help() {

    // Set up body class for Help page
  useEffect(() => {
    document.body.classList.add("help-body")
    return () => document.body.classList.remove("help-body")
  }, [])

    // Render Help page content
  return (
    <div className="help-page">

      <HomeHeader />

      <main className="help-main">

        <section className="help-hero">
          <h1>Help & Support</h1>
          <h2>Frequently Asked Questions</h2>
        </section>

        <section className="help-content">
        </section>

      </main>

      <HomeFooter />

    </div>
  )
}
