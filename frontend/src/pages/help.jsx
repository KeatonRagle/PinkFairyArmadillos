import { useEffect } from "react"
import HomeHeader from "../components/header"
import HomeFooter from "../components/footer2"
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
          <p>this is placeholder text</p>
        </section>

        <section className="help-content">
          <h2>Frequently Asked Questions</h2>
          <p>
            placeholder for asked questions.
          </p>
        </section>

      </main>

      <HomeFooter />

    </div>
  )
}
