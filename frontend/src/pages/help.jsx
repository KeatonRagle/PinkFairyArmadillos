import { useEffect } from "react"
import HomeHeader from "../components/header"
import HomeFooter from "../components/footer"
import "../styling/help.css"

export default function Help() {

  useEffect(() => {
    document.body.classList.add("help-body")
    return () => document.body.classList.remove("help-body")
  }, [])

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
