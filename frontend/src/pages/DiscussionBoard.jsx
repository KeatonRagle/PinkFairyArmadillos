import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/DiscussionBoard.css'

export default function DiscussionBoard() {
	const [selectedTimeframe, setSelectedTimeframe] = useState('week')

	const timeframeOptions = [
		{ key: 'week', label: 'Week' },
		{ key: 'month', label: 'Month' },
		{ key: 'year', label: 'Year' },
		{ key: 'all-time', label: 'All-time' },
	]

	const topCommentPlaceholdersByTimeframe = {
		week: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
		month: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
		year: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
		'all-time': [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
	}

	const visibleTopComments = topCommentPlaceholdersByTimeframe[selectedTimeframe]

	useEffect(() => {
		document.body.classList.add('discussionboard-body')
		return () => document.body.classList.remove('discussionboard-body')
	}, [])

	return (
		<div className="discussionboard-page">
			<HomeHeader />
			<main className="discussionboard-main">
				<aside className="toppost-sidebar" aria-label="Top posts">
					<h2>Top Post</h2>
					<div className="toppost-timeframes" role="group" aria-label="Top post timeframe">
						{timeframeOptions.map((timeframeOption) => (
							<button
								key={timeframeOption.key}
								type="button"
								className={`toppost-timeframe-button ${selectedTimeframe === timeframeOption.key ? 'active' : ''}`}
								onClick={() => setSelectedTimeframe(timeframeOption.key)}
								aria-pressed={selectedTimeframe === timeframeOption.key}
							>
								{timeframeOption.label}
							</button>
						))}
					</div>

					<ol className="toppost-list">
						{visibleTopComments.map((placeholderNumber, index) => (
							<li key={placeholderNumber} className="toppost-list-item">
								<span className="toppost-rank">{index + 1}.</span>
								<div className="toppost-avatar" aria-hidden="true" />
								<div className="toppost-comment-placeholder" aria-hidden="true" />
							</li>
						))}
					</ol>
				</aside>
			</main>
			<HomeFooter />
		</div>
	)
}
