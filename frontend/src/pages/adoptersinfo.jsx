import { useEffect } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/adoptersinfo.css'

export default function AdoptersInfo() {
	useEffect(() => {
		document.body.classList.add('adoptersinfo-body')
		return () => document.body.classList.remove('adoptersinfo-body')
	}, [])

	return (
		<div className="adoptersinfo-page">
			<HomeHeader />

			<main className="adoptersinfo-main">
				<section className="adoptersinfo-hero">
					<h1>Adopting Tips</h1>
					<p>
						Every animal is different, and it takes time for them to adjust to life in a new home.
						One of the most helpful guides for new adopters is the 3-3-3 rule.
					</p>
				</section>

				<section className="adoptersinfo-card">
					<h2>The 3-3-3 Rule</h2>
					<div className="rule-grid">
						<article>
							<h3>First 3 Days</h3>
							<p>
								The first days may feel overwhelming while your pet adjusts to new sights, smells,
								sounds, and people.
							</p>
						</article>
						<article>
							<h3>After 3 Weeks</h3>
							<p>
								Most pets begin understanding their routine and often start feeling more comfortable
								coming out of their shell.
							</p>
						</article>
						<article>
							<h3>After 3 Months</h3>
							<p>
								This is often when they feel safe and secure, and are ready to build a deeper,
								trusting bond with you.
							</p>
						</article>
					</div>
				</section>

				<section className="adoptersinfo-card">
					<h2>Common New Pet Behaviors — and How to Help</h2>

					<h3>Stress</h3>
					<p>
						New homes can be intense at first. Keep introductions slow and calm, and set up a safe
						space (like a guest room) with food, water, toys, litter boxes, a kennel, and other
						essentials.
					</p>
					<p>
						Keep life quiet and consistent for at least the first week, and delay large gatherings.
						We strongly recommend a vet check before introductions to resident pets or full access
						to your home.
					</p>

					<h3>Hiding</h3>
					<p>
						Shy pets need patience. Use slow movements, sit quietly nearby, and let them come to
						you at their pace. In busy homes, remind everyone to use gentle handling and quiet
						voices.
					</p>

					<h3>Destructive Behaviors (Chewing or Scratching)</h3>
					<p>
						As your pet spends more time alone, they may need extra mental stimulation. Try stuffed
						treat toys, puzzle feeders, snuffle mats, crinkle toys, and regular play sessions.
					</p>

					<h3>House Soiling Accidents</h3>
					<p>
						Structure helps reduce stress and confusion. Keep feeding, walks, potty breaks, and play
						on a predictable daily schedule so your pet can learn what to expect.
					</p>
				</section>

				<section className="adoptersinfo-card">
					<h2>Introducing a New Pet to Resident Pets</h2>
					<ul>
						<li>
							Take things slow. If possible, give your new pet a few weeks to decompress before
							introductions.
						</li>
						<li>
							Feed pets at the same time on opposite sides of a closed door so they learn each
							other’s scent in a low-stress way.
						</li>
						<li>
							For dog-and-cat introductions, keep the dog leashed and under control during early
							meetings.
						</li>
					</ul>
				</section>

				<section className="adoptersinfo-card">
					<h2>Helpful First-Week Tips</h2>
					<ul>
						<li>Pet-proof your home before giving full access.</li>
						<li>Use positive reinforcement for desired behavior.</li>
						<li>Keep ID tags and microchip details updated.</li>
						<li>Schedule regular rest periods and avoid overstimulation.</li>
						<li>Be patient with progress and celebrate small wins.</li>
					</ul>
				</section>
			</main>

			<HomeFooter />
		</div>
	)
}
