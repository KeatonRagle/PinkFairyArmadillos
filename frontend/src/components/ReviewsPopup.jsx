import { createPortal } from 'react-dom'
import { useEffect, useState } from 'react'
import '../styling/ReviewsPopup.css'

// --- Placeholder data (replace with API calls once backend is ready) ---
const PLACEHOLDER_REVIEWS = [
	{
		reviewId: 1,
		username: 'Jane D.',
		rating: 5,
		rwComment: 'Amazing shelter — the staff were so helpful and the animals were well cared for.',
		rwDate: '2026-03-10',
	},
	{
		reviewId: 2,
		username: 'Marcus T.',
		rating: 4,
		rwComment: 'Great experience overall. The adoption process was smooth and fast.',
		rwDate: '2026-02-22',
	},
	{
		reviewId: 3,
		username: 'Priya S.',
		rating: 3,
		rwComment: 'Decent place. Could improve communication but the animals looked happy.',
		rwDate: '2026-01-05',
	},
]

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

export default function ReviewsPopup({ isOpen, onClose, shelterName }) {
	const [view, setView] = useState('list') // 'list' | 'add'
	const [rating, setRating] = useState(0)
	const [comment, setComment] = useState('')
	const [isSubmitting, setIsSubmitting] = useState(false)

	// Reset add-review form whenever popup opens/closes
	useEffect(() => {
		if (isOpen) {
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

	const handleSubmit = (e) => {
		e.preventDefault()
		if (!rating || !comment.trim()) return
		setIsSubmitting(true)

		// TODO: replace with real API call
		// submitReview({ siteID: animal.siteId, userID: currentUser.id, rating, comment })
		//   .then(() => setView('list'))
		//   .finally(() => setIsSubmitting(false))

		// Placeholder: just go back to list
		setTimeout(() => {
			setIsSubmitting(false)
			setView('list')
		}, 600)
	}

	return createPortal(
		<div className="reviews-overlay" onClick={onClose}>
			<div className="reviews-modal" onClick={(e) => e.stopPropagation()}>
				<div className="reviews-modal-header">
					<h3>Reviews</h3>
					<button className="reviews-close-btn" onClick={onClose} aria-label="Close">✕</button>
				</div>

				{view === 'list' && (
					<>
						<div className="reviews-list">
							{PLACEHOLDER_REVIEWS.length === 0 ? (
								<p className="reviews-empty">No reviews yet. Be the first!</p>
							) : (
								PLACEHOLDER_REVIEWS.map((r) => (
									<div key={r.reviewId} className="reviews-item">
										<div className="reviews-item-header">
											<span className="reviews-item-username">{r.username}</span>
											<StarDisplay value={r.rating} />
										</div>
										<p className="reviews-item-comment">{r.rwComment}</p>
										<span className="reviews-item-date">{new Date(r.rwDate).toLocaleDateString('en-US', { month: '2-digit', day: '2-digit', year: 'numeric' })}</span>
									</div>
								))
							)}
						</div>

						<div className="reviews-modal-footer">
							<button className="reviews-add-btn" onClick={() => setView('add')}>
								+ Add a Review
							</button>
						</div>
					</>
				)}

				{view === 'add' && (
					<form className="reviews-add-form" onSubmit={handleSubmit}>
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
			</div>
		</div>,
		document.body
	)
}
