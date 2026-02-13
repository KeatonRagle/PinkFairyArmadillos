import { AuthLayout } from '../components/layout'
import { Button } from '../components/button'
import { Field, Label } from '../components/fieldset'
import { Heading } from '../components/heading'
import { Input } from '../components/input'
import { Strong, Text, TextLink } from '../components/text'
import { Logo } from '../components/logo'
import { Link, useNavigate } from 'react-router-dom'
import '../styling/signup.css'
import { useEffect } from 'react'



export default function Signup() {
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
    <div className="signup-page">
    <AuthLayout>

      <Logo className="page-logo" />

      <form onSubmit={handleSubmit} className="login-form">

        <Logo className="card-logo" />

        <Heading>Create your account</Heading>

        <Field>
          <Input type="email" name="email" placeholder="PFA ID" />
        </Field>

        <Field>
          <Input type="password" name="password" placeholder="Password" />
        </Field>

        <Field>
          <Input type="password" name="confirmPassword" placeholder="Confirm Password" />
        </Field>

        <Button type="submit">
          Sign Up
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
