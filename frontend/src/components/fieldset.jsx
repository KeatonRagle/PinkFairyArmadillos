export function Field({ children, className = '' }) {
  return (
    <div className={`field ${className}`}>
      {children}
    </div>
  )
}

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