import { useEffect } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/ShelterInfo.css'

export default function ShelterInfo() {
	useEffect(() => {
		document.body.classList.add('shelterinfo-body')
		return () => document.body.classList.remove('shelterinfo-body')
	}, [])

	return (
		<div className="shelterinfo-page">
			<HomeHeader />
			<main className="shelterinfo-main" />
			<HomeFooter />
		</div>
	)
}
