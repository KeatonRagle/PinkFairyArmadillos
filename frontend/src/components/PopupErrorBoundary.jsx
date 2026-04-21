import { Component } from 'react'

export default class PopupErrorBoundary extends Component {
	constructor(props) {
		super(props)
		this.state = { hasError: false }
	}

	static getDerivedStateFromError() {
		return { hasError: true }
	}

	componentDidCatch(error) {
		console.error('Popup render error:', error)
	}

	componentDidUpdate(prevProps) {
		if (this.state.hasError && this.props.resetKey !== prevProps.resetKey) {
			this.setState({ hasError: false })
		}
	}

	render() {
		if (this.state.hasError) {
			return null
		}

		return this.props.children
	}
}
