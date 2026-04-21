import { useEffect, useState } from 'react'
import HomeHeader from '../components/header'
import HomeFooter from '../components/footer'
import { submitSite } from '../fetch/api'
import '../styling/contribute.css'


export default function Contribute() {

	const [formData, setFormData] = useState({
		url: '',
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


	const formatPhoneNumber = (value) => {
		// Remove all non-digit characters
		const digitsOnly = value.replace(/\D/g, '')
		
		// Limit to 10 digits
		const limitedDigits = digitsOnly.slice(0, 10)
		
		// Format as (XXX) XXX-XXXX
		if (limitedDigits.length === 0) return ''
		if (limitedDigits.length <= 3) return `(${limitedDigits}`
		if (limitedDigits.length <= 6) return `(${limitedDigits.slice(0, 3)}) ${limitedDigits.slice(3)}`
		return `(${limitedDigits.slice(0, 3)}) ${limitedDigits.slice(3, 6)}-${limitedDigits.slice(6)}`
	}

	const handleChange = (event) => {
		const { name, value } = event.target
		if (name === 'phone') {
			const formattedPhone = formatPhoneNumber(value)
			setFormData((current) => ({ ...current, [name]: formattedPhone }))
		} else {
			setFormData((current) => ({ ...current, [name]: value }))
		}
	}

	const handleSubmit = async (event) => {
		event.preventDefault()
		setError('')
		setSuccess('')
		setLoading(true)
		try {
			// Strip formatting from phone before submitting
			const submissionData = {
				...formData,
				phone: formData.phone.replace(/\D/g, '')
			}
            await submitSite(submissionData)
			setSuccess('Contribution submitted for admin review.')
			setFormData({ url: '', name: '', email: '', phone: '' })
		} catch (err) {
            if (err.status === 409) {
                setError('Site has already been submitted')
            } else {
                setError('Failed to submit contribution.')
            }
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
					<input name="url" type="url" placeholder="Link" value={formData.url} onChange={handleChange} required />
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
