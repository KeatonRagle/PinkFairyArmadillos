import { createPortal } from 'react-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext.jsx'
import { getAllReviews, submitReview } from '../fetch/api'
import '../styling/ReviewsPopup.css'

function StarRating({ value, onChange }) {
	const [hovered, setHovered] = useState(0)
	return (
		<div className="reviews-star-input" aria-label="Rating">
			{[1, 2, 3, 4, 5].map((star) => (
				<button
					key={star}
					type="button"
					className={`reviews-star-btn ${star <= (hovered || value) ? 'filled' : ''}`}
					onMouseEnter={() => setHovered(star)}
					onMouseLeave={() => setHovered(0)}
					onClick={() => onChange(star)}
					aria-label={`${star} star${star !== 1 ? 's' : ''}`}
				>
					★
				</button>
			))}
		</div>
	)
}

function StarDisplay({ value }) {
	return (
		<span className="reviews-star-display" aria-label={`${value} out of 5 stars`}>
			{[1, 2, 3, 4, 5].map((star) => (
				<span key={star} className={star <= value ? 'filled' : ''}>★</span>
			))}
		</span>
	)
}

export default function ReviewsPopup({ isOpen, onClose, shelterName, siteInfo = {} }) {
	const { id: currentUserId } = useAuth()
	const [activeTab, setActiveTab] = useState('reviews') // 'reviews' | 'info'
	const [view, setView] = useState('list') // 'list' | 'add'
	const [rating, setRating] = useState(0)
	const [comment, setComment] = useState('')
	const [isSubmitting, setIsSubmitting] = useState(false)
	const [reviews, setReviews] = useState([])
	const [loadingReviews, setLoadingReviews] = useState(false)
	const [reviewsError, setReviewsError] = useState('')

	// Reset add-review form whenever popup opens/closes
	useEffect(() => {
		if (isOpen) {
			setActiveTab('reviews')
			setView('list')
			setRating(0)
			setComment('')
		}
	}, [isOpen])

	// Lock body scroll while open
	useEffect(() => {
		if (isOpen) {
			document.body.style.overflow = 'hidden'
		} else {
			document.body.style.overflow = 'unset'
		}
		return () => { document.body.style.overflow = 'unset' }
	}, [isOpen])

	if (!isOpen) return null

	const infoContent = {
		siteId: siteInfo.siteId || null,
		name: siteInfo.name || shelterName || 'Adoption site name coming soon.',
		url: siteInfo.url || 'https://example-adoption-site.org',
		email: siteInfo.email || 'info@example-adoption-site.org',
		phone: siteInfo.phone || '(555) 123-4567',
	}

	const normalizeReview = (review, index) => {
		if (!review || typeof review !== 'object') {
			return {
				reviewId: `invalid-${index}`,
				username: 'Anonymous',
				rating: 0,
				rwComment: '',
				rwDate: null,
				siteId: null,
				siteName: '',
			}
		}

		const parsedRating = Number(review.rating)
		const siteId = review.site?.siteId ?? review.siteId ?? null

		return {
			reviewId: review.reviewId ?? review.id ?? `${siteId || 'site'}-${index}`,
			username: review.user?.name || review.username || 'Anonymous',
			rating: Number.isFinite(parsedRating) ? parsedRating : 0,
			rwComment: String(review.rwComment || review.comment || ''),
			rwDate: review.rwDate || review.date || null,
			siteId,
			siteName: review.site?.name || '',
		}
	}

	const loadReviews = async () => {
		setLoadingReviews(true)
		setReviewsError('')

		try {
			const response = await getAllReviews()
			const reviewList = Array.isArray(response)
				? response.map((review, index) => normalizeReview(review, index))
				: []

			let filteredReviews = reviewList
			if (infoContent.siteId) {
				filteredReviews = reviewList.filter((review) => String(review.siteId) === String(infoContent.siteId))
			} else {
				const normalizedName = String(infoContent.name || '').trim().toLowerCase()
				if (normalizedName) {
					filteredReviews = reviewList.filter((review) => String(review.siteName || '').trim().toLowerCase() === normalizedName)
				}
			}

			setReviews(filteredReviews)
		} catch {
			setReviewsError('Unable to load reviews right now.')
			setReviews([])
		} finally {
			setLoadingReviews(false)
		}
	}

	useEffect(() => {
		if (isOpen && activeTab === 'reviews' && view === 'list') {
			loadReviews()
		}
	}, [isOpen, activeTab, view])

	const handleSubmit = async (e) => {
		e.preventDefault()
		if (!rating || !comment.trim()) return
		if (!currentUserId) {
			setReviewsError('Please log in to submit a review.')
			return
		}
		if (!infoContent.siteId) {
			setReviewsError('This pet is missing a site reference, so a review cannot be submitted yet.')
			return
		}

		setIsSubmitting(true)
		setReviewsError('')

		try {
			await submitReview({
				userID: Number(currentUserId),
				siteId: Number(infoContent.siteId),
				rating: Number(rating),
				comment: comment.trim(),
			})

			await loadReviews()
			setComment('')
			setRating(0)
			setActiveTab('reviews')
			setView('list')
		} catch {
			setReviewsError('Failed to submit review.')
		} finally {
			setIsSubmitting(false)
		}
	}

	const formatReviewDate = (rawDate) => {
		if (!rawDate) return 'No date'

		const parsedDate = new Date(rawDate)
		if (Number.isNaN(parsedDate.getTime())) return 'No date'

		return parsedDate.toLocaleDateString('en-US', {
			month: '2-digit',
			day: '2-digit',
			year: 'numeric',
		})
	}

	return createPortal(
		<div className="reviews-overlay" onClick={onClose}>
			<div className="reviews-modal" onClick={(e) => e.stopPropagation()}>
				<div className="reviews-modal-header">
					<h3>Reviews and Info</h3>
					<button className="reviews-close-btn" onClick={onClose} aria-label="Close">✕</button>
				</div>

				<div className="reviews-tab-row" role="tablist" aria-label="Reviews and information tabs">
					<button
						type="button"
						className={`reviews-tab-btn ${activeTab === 'reviews' ? 'active' : ''}`}
						onClick={() => {
							setActiveTab('reviews')
							setView('list')
						}}
						role="tab"
						aria-selected={activeTab === 'reviews'}
					>
						Reviews
					</button>
					<button
						type="button"
						className={`reviews-tab-btn ${activeTab === 'info' ? 'active' : ''}`}
						onClick={() => setActiveTab('info')}
						role="tab"
						aria-selected={activeTab === 'info'}
					>
						Info
					</button>
				</div>

				{activeTab === 'reviews' && view === 'list' && (
					<>
						<div className="reviews-list">
							{loadingReviews ? (
								<p className="reviews-empty">Loading reviews...</p>
							) : reviewsError ? (
								<p className="reviews-empty">{reviewsError}</p>
							) : reviews.length === 0 ? (
								<p className="reviews-empty">No reviews yet. Be the first!</p>
							) : (
								reviews.map((r, index) => (
									<div key={r.reviewId} className="reviews-item">
										<div className="reviews-item-header">
											<span className="reviews-item-username">{String(r?.username || `User ${index + 1}`)}</span>
											<StarDisplay value={Number(r?.rating) || 0} />
										</div>
										<p className="reviews-item-comment">{String(r?.rwComment || '')}</p>
										<span className="reviews-item-date">{formatReviewDate(r?.rwDate)}</span>
									</div>
								))
							)}
						</div>

						<div className="reviews-modal-footer">
							<button
								className="reviews-add-btn"
								onClick={() => setView('add')}
								disabled={!currentUserId || !infoContent.siteId}
							>
								+ Add a Review
							</button>
						</div>
					</>
				)}

				{activeTab === 'reviews' && view === 'add' && (
					<form className="reviews-add-form" onSubmit={handleSubmit}>
						{reviewsError ? <p className="reviews-empty">{reviewsError}</p> : null}
						<label className="reviews-form-label">Your Rating</label>
						<StarRating value={rating} onChange={setRating} />

						<label className="reviews-form-label" htmlFor="review-comment">Your Review</label>
						<textarea
							id="review-comment"
							className="reviews-textarea"
							rows={5}
							placeholder="Share your experience with this shelter..."
							value={comment}
							onChange={(e) => setComment(e.target.value)}
							required
						/>

						<div className="reviews-form-actions">
							<button
								type="button"
								className="reviews-cancel-btn"
								onClick={() => setView('list')}
							>
								Back
							</button>
							<button
								type="submit"
								className="reviews-submit-btn"
								disabled={isSubmitting || !rating || !comment.trim()}
							>
								{isSubmitting ? 'Submitting...' : 'Submit'}
							</button>
						</div>
					</form>
				)}

				{activeTab === 'info' && (
					<div className="reviews-info-panel">
						<div className="reviews-info-row">
							<span className="reviews-info-label">Name</span>
							<span>{infoContent.name}</span>
						</div>
						<div className="reviews-info-row">
							<span className="reviews-info-label">Link</span>
							<a href={infoContent.url} target="_blank" rel="noreferrer" className="reviews-info-link">
								{infoContent.url}
							</a>
						</div>
						<div className="reviews-info-row">
							<span className="reviews-info-label">Email</span>
							<span>{infoContent.email}</span>
						</div>
						<div className="reviews-info-row">
							<span className="reviews-info-label">Phone</span>
							<span>{infoContent.phone}</span>
						</div>
					</div>
				)}
			</div>
		</div>,
		document.body
	)
}
