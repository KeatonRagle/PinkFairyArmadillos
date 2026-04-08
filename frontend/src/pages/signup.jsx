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
import { useAuth } from "../auth/AuthContext";


export default function Signup() {
  const navigate = useNavigate()
  const { setAuth } = useAuth();

    const [username, setUsername] = useState("")
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
      const data = await registerUser({ name: username, email, password })
      const token = data.token
      const userObj = data.user
      const displayName = userObj.name ?? username ?? userObj.email
      const role = userObj.role;

      setAuth(token, displayName, role);

      navigate('/home')
    } catch (err) {
        if (err.status === 409) {
            setError("An account with that email already exists.")
        } else {
            setError("Something went wrong. Please try again.")
        }
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
            type="text"
            name="username"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </Field>

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
          <Link to="/login">
            <Strong>Sign in</Strong>
          </Link>
        </Text>

      </form>

    </AuthLayout>
    </div>
  )
}
