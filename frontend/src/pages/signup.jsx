import { AuthLayout } from '../components/layout'
import { Button } from '../components/button'
import { Field, Label } from '../components/fieldset'
import { Heading } from '../components/heading'
import { Input } from '../components/input'
import { Strong, Text, TextLink } from '../components/text'
import { Logo } from '../components/logo'
import { Link, useNavigate } from 'react-router-dom'
import '../styling/signup.css'
import { useEffect, useState } from 'react'
import { registerUser } from '../fetch/api'



export default function Signup() {
  const navigate = useNavigate()

    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [confirmPassword, setConfirmPassword] = useState("")
    const [error, setError] = useState("")
    const [loading, setLoading] = useState(false)

  useEffect(() => {
    document.body.classList.add('login-body')
    return () => document.body.classList.remove('login-body')
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError("")

    if (password !== confirmPassword) {
      setError("Passwords do not match.")
      return
    }

    setLoading(true)
    try {
      const data = await registerUser({ name: "User", email, password })
      localStorage.setItem("token", data.token)

      navigate('/home')
    } catch (err) {
      setError(err?.message || "Failed to create account.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="signup-page">
    <AuthLayout>

      <Logo className="page-logo" />

      <form onSubmit={handleSubmit} className="login-form">

        <Logo className="card-logo" />

        <Heading>Create your account</Heading>

        <Field>
          <Input
            type="email"
            name="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </Field>

        <Field>
          <Input 
            type="password"
            name="password" 
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </Field>

        <Field>
          <Input 
            type="password" 
            name="confirmPassword" 
            placeholder="Confirm Password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
        </Field>

        {error && (
          <Text className="error-text">
            <Strong>{error}</Strong>
          </Text>
        )}

        <Button type="submit" disabled={loading}>
          {loading ? "Creating..." : "Sign Up"}
        </Button>

        <Text className="signup-text">
          Already have an account?{' '}
          <Link to="/">
            <Strong>Sign in</Strong>
          </Link>
        </Text>

      </form>

    </AuthLayout>
    </div>
  )
}
