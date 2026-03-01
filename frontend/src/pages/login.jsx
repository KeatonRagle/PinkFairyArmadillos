import { AuthLayout } from '../components/layout'
import { Button } from '../components/button'
import { Checkbox, CheckboxField } from '../components/checkbox'
import { Field, Label } from '../components/fieldset'
import { Heading } from '../components/heading'
import { Input } from '../components/input'
import { Strong, Text, TextLink } from '../components/text'
import { Logo } from '../components/logo'
import { Link, useNavigate } from 'react-router-dom'
import '../styling/login.css'
import { useEffect, useState } from 'react'
import { login } from '../fetch/api'
import { useAuth } from "../auth/AuthContext";

export default function Login() {

  const navigate = useNavigate()
  const { setAuth } = useAuth();

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)


  useEffect(() => {
    document.body.classList.add('login-body')
    return () => document.body.classList.remove('login-body')
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError("")

    setLoading(true)
    try {
      const data = await login({ email, password })
      const token = data.token
      const userObj = data.user
      const displayName = userObj.username ?? userObj.name ?? userObj.email;

      setAuth(token, displayName);

      navigate('/home')
    } catch (err) {
      setError(err?.message || "Login Failed")
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout>

      <Logo className="page-logo" />

      <form onSubmit={handleSubmit} className="login-form">

        <Logo className="card-logo" />

        <Heading>Sign in with your PFA ID</Heading>

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

        {error && (
          <Text className="error-text">
            <Strong>{error}</Strong>
          </Text>
        )}

        <Button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </Button>

        <Text className="signup-text">
          Don't have an account?{' '}
          <Link to="/signup">
            <Strong>Sign up</Strong>
          </Link>
        </Text>

      </form>

    </AuthLayout>
  )
}
