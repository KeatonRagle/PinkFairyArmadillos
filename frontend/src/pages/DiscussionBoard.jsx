import { useEffect } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/DiscussionBoard.css'

export default function DiscussionBoard() {
	useEffect(() => {
		document.body.classList.add('discussionboard-body')
		return () => document.body.classList.remove('discussionboard-body')
	}, [])

	return (
		<div className="discussionboard-page">
			<HomeHeader />
			<main className="discussionboard-main" />
			<HomeFooter />
		</div>
	)
}
