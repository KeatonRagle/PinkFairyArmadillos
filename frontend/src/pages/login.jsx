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
import { useEffect } from 'react'

export default function Login() {

  const navigate = useNavigate()

  useEffect(() => {
    document.body.classList.add('login-body')
    return () => document.body.classList.remove('login-body')
  }, [])

  const handleSubmit = (e) => {
    e.preventDefault()
    navigate('/home')
  }
  return (
    <AuthLayout>

      <Logo className="page-logo" />

      <form onSubmit={handleSubmit} className="login-form">

        <Logo className="card-logo" />

        <Heading>Sign in with your PFA ID</Heading>

        <Field>
          <Input type="email" name="email" placeholder="PFA ID" />
        </Field>

        <Field>
          <Input type="password" name="password" placeholder="Password"/>
        </Field>

        <Button type="submit">
          Login
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
