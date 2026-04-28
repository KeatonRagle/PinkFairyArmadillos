// Field wrapper component for form fields
export function Field({ children, className = '' }) {
  return (
    <div className={`field ${className}`}>
      {children}
    </div>
  )
}

// Label component for form fields
export function Label({ children, className = '', ...props }) {
  return (
    <label
      className={className}
      {...props}
    >
      {children}
    </label>
  )
}