import { Link } from 'react-router-dom'

export function Text({ children, className = '', ...props }) {
  return (
    <p
      className={className}
      {...props}
    >
      {children}
    </p>
  )
}

export function TextLink({ children, href, className = '', ...props }) {
  return (
    <Link
      to={href}
      className={`text-indigo-600 hover:text-indigo-500 ${className}`}
      {...props}
    >
      {children}
    </Link>
  )
}


export function Strong({ children, className = '' }) {
  return (
    <strong className={`font-semibold ${className}`}>
      {children}
    </strong>
  )
}