import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import { useAuth } from '../auth/AuthContext.jsx'
import { submitPost, getAllPosts, getCommentsByPost, submitComment } from '../fetch/api'
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

	const { username, id } = useAuth()

	const [postContent, setPostContent] = useState('')
	const [posts, setPosts] = useState([])
	const [openPosts, setOpenPosts] = useState([])

	const [comments, setComments] = useState([])
	const [commentsContent, setCommentsContent] = useState([])

	const [loading, setLoading] = useState(false)
	const [error, setError] = useState('')
	const [success, setSuccess] = useState('')

	useEffect(() => {
		const loadPosts = async () => {
			try {
				// Fetch all pets (no server-side filters) and filter client-side
				setPosts(await getAllPosts())
				setComments(posts.map(() => []))
				setOpenPosts(posts.map(() => false))
				console.log("openPosts:")
				console.log(openPosts)
				console.log("comments:")
				console.log(comments)
			} catch (err) {
				console.log(err.message)
				setPosts([])
				setOpenPosts([])
				setComments([])
				setError('Unable to load posts right now.')
			} 
		}

		loadPosts()
	}, [])

	useEffect(() => {
		console.log("openPosts:")
		console.log(openPosts)
		const newComments = openPosts.map((isOpen, index, arr) => {
			if (isOpen) {
				const loadComments = async() => {
					return await getCommentsByPost(posts[index].postID)
				}

				return loadComments()
			} else {
				return []
			}
		})

		setComments(newComments)
	}, [openPosts])

	const handleChangePost = (event) => {
		const { value } = event.target
		setPostContent(value)
	}

	const handleSubmitPost = async (event) => {
		event.preventDefault()
		setError('')
		setSuccess('')
		setLoading(true)
		try {
			await submitPost({
				userID: id,
				comment: postContent
			})
			setSuccess('Post created successfully')
			setPostContent('')
		} catch (err) {
			if (err.status === 409) {
				setError('Post has already been created')
			} else {
				setError('Failed to create post.\n' + err.message)
			}
		} finally {
			setLoading(false)
		}
	}

	const handleChangeComment = (event) => {
		const { value } = event.target
		const { index } = event.currentTarget.dataset.id
		const newCommentsContent = [...commentsContent]
		newCommentsContent[index] = value
		setCommentsContent(newCommentsContent)
	}

	const handleSubmitComment = async (event) => {
		const { index } = event.currentTarget.dataset.id

		event.preventDefault()
		setError('')
		setSuccess('')
		setLoading(true)
		try {
			console.log(id)
			console.log(posts[index].postID)
			console.log(commentsContent[index])
			await submitComment({
				userID: id,
				postID: posts[index].postID,
				comment: commentsContent[index]
			})
			setSuccess('Comment created successfully')
			setPostContent('')
		} catch (err) {
			if (err.status === 409) {
				setError('Comment has already been created')
			} else {
				setError('Failed to create comment.\n' + err.message)
			}
		} finally {
			setLoading(false)
		}
	}

	console.log(posts)
	return (
		<div className="discussionboard-page">
			<HomeHeader />
			<main className="discussionboard-main">
				<section className="discussionposts-panel">
					<h1>Discussion Posts</h1>
					{
						posts.map((post, index, arr) => (

							<div>
								<p>{post.content}</p>
								<div className={`post-comments-group ${openPosts[index] === true ? 'open' : ''}`}>
									<button
										type="button"
										className="post-comments-dropdown post-comments-toggle"
										onClick={() => setOpenPosts(prevToggles =>{
											const newToggles = [...prevToggles]
											newToggles[index] = !newToggles[index]
											return newToggles
										})}
									/>
									{openPosts[index] === true && (
										<div className="post-comments-panel">
											{
												comments[index].map((comment, commentIndex, commentArr) => (
													<div>
														<p>{comment.comment}</p>
													</div>
												))
											}
											<form data-id={index} className="discussionboard-form" onSubmit={handleSubmitComment}>
												<h3>Make a Comment</h3>
												<textarea name="content" rows="8" onChange={handleChangeComment} required>{commentsContent[index]}</textarea>
												<button type="submit" disabled={loading}>{loading ? 'Submitting...' : 'Submit'}</button>
												{error && <p className="contribute-error">{error}</p>}
												{success && <p className="contribute-success">{success}</p>}
											</form>
										</div>
									)}
								</div>
							</div>
							
						))
					}
				</section>
				<form className="discussionboard-form" onSubmit={handleSubmitPost}>
					<h3>Make a Post</h3>
					<textarea name="content" rows="8" onChange={handleChangePost} required>{postContent}</textarea>
					<button type="submit" disabled={loading}>{loading ? 'Submitting...' : 'Submit'}</button>
					{error && <p className="contribute-error">{error}</p>}
					{success && <p className="contribute-success">{success}</p>}
				</form>
				{/* <aside className="toppost-sidebar" aria-label="Top posts">
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
				</aside> */}
			</main>
			<HomeFooter />
		</div>
	)
}
