import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import Popup from '../components/popup.jsx'
import { useAuth } from '../auth/AuthContext.jsx'
import { submitPost, getAllPosts, deletePost, getCommentsByPost, submitComment, deleteComment } from '../fetch/api'
import '../styling/DiscussionBoard.css'

// Discussion board page component
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

  // Set up body class for Discussion Board page
	useEffect(() => {
		document.body.classList.add('discussionboard-body')
		return () => document.body.classList.remove('discussionboard-body')
	}, [])

	const { username, id } = useAuth()
	const [activePopupIndex, setActivePopupIndex] = useState(-1)

	const [posts, setPosts] = useState([])

	const [loading, setLoading] = useState(false)
	const [error, setError] = useState('')

	useEffect(() => {
		const loadPosts = async () => {
			try {
				const initialPosts = await getAllPosts()
				const formattedPosts = await Promise.all(initialPosts.map(async (post) => {
					const fetchedComments = await getCommentsByPost(post.postID);
					return {
						...post,
						isOpen: false,
						comments: fetchedComments,
					}
				}))
				setPosts(formattedPosts)
			} catch (err) {
				console.log(err.message)
				setPosts([])
				setError('Unable to load posts right now.')
			} 
		}

		loadPosts()
	}, [])

  // Toggle comments for a post
	const toggleComments = async (index) => {
		const updatedPosts = [...posts];
		const post = updatedPosts[index];

		post.isOpen = !post.isOpen;

		if (post.isOpen) {
			try {
				const fetchedComments = await getCommentsByPost(post.postID);
				post.comments = fetchedComments;
			} catch (err) {
				console.error("Failed to load comments", err);
			}
		}

		setPosts(updatedPosts);
	};

	const handleSubmitPost = async (index, post) => {
		setError('')
		setLoading(true)
		try {
			console.log(id)
			console.log(post)
			const newPost = await submitPost({
				userID: id,
				post: post
			})

			setPosts(prevPosts => [
            {
                ...newPost,
                isOpen: false,
                comments: [],
            },
            ...prevPosts
        ]);
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

	const handleSubmitComment = async (index, comment) => {
		setError('');
		setLoading(true);

		try {
			console.log(id)
			console.log(posts[index].postID)
			console.log(comment)
			const newComment = await submitComment({
				userID: id,
				postID: posts[index].postID,
				comment: comment
			});

			setPosts(prevPosts => {
				const updatedPosts = [...prevPosts];
				const targetPost = { ...updatedPosts[index] };
				targetPost.comments = [...targetPost.comments, newComment];
				updatedPosts[index] = targetPost;
				
				return updatedPosts;
			});
		} catch (err) {
			setError('Failed to add comment: ' + err.message);
		} finally {
			setLoading(false);
		}
	}

	const handleDeletePost = async (postID) => {
		setError('')
		setLoading(true)

		try {
			await deletePost(postID)
			setPosts(prevPosts => prevPosts.filter(post => post.postID !== postID))
		} catch (err) {
			setError('Failed to delete post: ' + err.message);
		} finally {
			setLoading(false);
		}
	}

	const handleDeleteComment = async (postID, commentID) => {
		setError('')
		setLoading(true)

		try {
			await deleteComment(commentID)
			setPosts(prevPosts => prevPosts.map(post => {
				if (post.postID === postID) {
					return {
						...post,
						comments: post.comments.filter(comment => comment.commentID !== commentID)
					}
				}

				return post
			}))
		} catch (err) {
			setError('Failed to delete post: ' + err.message);
		} finally {
			setLoading(false);
		}
	}

	return (
		<div className="discussionboard-page">
			<HomeHeader />
			<main className="discussionboard-main">
				<section className="discussionposts-panel">
					<h1>Discussions</h1>
					<button 
						className="create-post-trigger" 
						onClick={() => setActivePopupIndex(0)}
						disabled={!id}
					>
						{id ? "Start a New Discussion" : "Log in to Start a New Discussion!"} 
					</button>
					{posts.map((post, index) => (
						<article key={post.postID || index} className="post-card">
							<div className="post-header-grid">
								<div className="user-info-side">
									<div className="user-avatar-tiny">{post.username?.charAt(0).toUpperCase()}</div>
									<span className="username">{post.username}</span>
								</div>

								{id === post.userID && (
									<button className="delete-icon-btn" onClick={() => handleDeletePost(post.postID)}>
										<i className="fa-regular fa-trash-can"></i>
									</button>
								)}
							</div>
							<p className="post-content">{post.content}</p>
							
							<button 
								type="button" 
								className="post-comments-toggle"
								onClick={() => toggleComments(index)}
							>
								{post.isOpen ? 'Close Comments' : `View Comments (${post.comments.length})`}
								<span>{post.isOpen ? '▲' : '▼'}</span>
							</button>

							{post.isOpen && (
								<div className="comments-container">
									{post.comments.map((comment, cIndex) => (
										<div key={comment.commentID || cIndex} className="comment-item">
											<div className="comment-header-grid">
												<div className="user-info-side">
													<div className="user-avatar-tiny" style={{width: '24px', height: '24px', fontSize: '0.8rem'}}>
														{comment.username?.charAt(0).toUpperCase()}
													</div>
													<span className="username">{comment.username}</span>
												</div>
												
												{id === comment.userID && (
													<button className="delete-icon-btn" onClick={() => handleDeleteComment(post.postID, comment.commentID)}>
														<i className="fa-regular fa-trash-can"></i>
													</button>
												)}
											</div>
											<p className="comment-content">{comment.comment}</p>
										</div>
									))}
									<button 
										className="add-comment-btn" 
										onClick={() => setActivePopupIndex(index + 1)}
										disabled={!id}
									>
										{id ? "+ Leave a Comment" : "Log in to Leave a Comment!"}
									</button>
									
									<Popup 
										index={index}
										isOpen={activePopupIndex === index + 1}
										isLoading={loading}
										errorMessage={error}
										placeholder="Write your reply..."
										title="Reply to Post"
										onClose={() => setActivePopupIndex(-1)}
										onSubmit={handleSubmitComment}
									/>
								</div>
							)}
						</article>
					))}
				</section>
				{/*
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
				*/}
			</main>
			
			<HomeFooter />
			
			<Popup 
				index={0}
				isOpen={activePopupIndex === 0}
				isLoading={loading}
				errorMessage={error}
				placeholder="What's on your mind?"
				title="New Discussion"
				onClose={() => setActivePopupIndex(-1)}
				onSubmit={handleSubmitPost}
			/>
		</div>
	)
}
