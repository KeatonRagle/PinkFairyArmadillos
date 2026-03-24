import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import '../styling/contribute.css'


const CONTRIBUTIONS_KEY = 'pfa_contributions'

export default function Contribute() {

	const [formData, setFormData] = useState({
		link: '',
		name: '',
		email: '',
		phone: '',
	})
	const [loading, setLoading] = useState(false)
	const [error, setError] = useState('')
	const [success, setSuccess] = useState('')

	useEffect(() => {
		document.body.classList.add('contribute-body')
		return () => document.body.classList.remove('contribute-body')
	}, [])


	const handleChange = (event) => {
		const { name, value } = event.target
		setFormData((current) => ({ ...current, [name]: value }))
	}

	const handleSubmit = async (event) => {
		event.preventDefault()
		setError('')
		setSuccess('')

		setLoading(true)
		try {

			const contributions = JSON.parse(localStorage.getItem(CONTRIBUTIONS_KEY) || '[]')
			const newContribution = {
				id: Date.now(),
				status: 'PENDING',
				submittedAt: new Date().toISOString(),
				...formData,
			}

			localStorage.setItem(CONTRIBUTIONS_KEY, JSON.stringify([newContribution, ...contributions]))
			setSuccess('Contribution submitted for admin review.')
			setFormData({ link: '', name: '', email: '', phone: '' })
		} catch (submitError) {
			setError(submitError?.message || 'Failed to submit contribution.')
		} finally {
			setLoading(false)
		}
	}

	return (
		<div className="contribute-page">
			<HomeHeader />

			<main className="contribute-main">
				<form className="contribute-form" onSubmit={handleSubmit}>
					<h1>Contribute</h1>
					<input name="link" type="url" placeholder="Link" value={formData.link} onChange={handleChange} required />
					<input name="name" type="text" placeholder="Name" value={formData.name} onChange={handleChange} required />
					<input name="email" type="email" placeholder="Email" value={formData.email} onChange={handleChange} required />
					<input name="phone" type="text" placeholder="Phone Number" value={formData.phone} onChange={handleChange} required />
					<button type="submit" disabled={loading}>{loading ? 'Submitting...' : 'Submit'}</button>
					{error && <p className="contribute-error">{error}</p>}
					{success && <p className="contribute-success">{success}</p>}
				</form>
			</main>

		</div>
	)
}
