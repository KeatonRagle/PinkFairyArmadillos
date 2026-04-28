// Input component for text fields
export function Input({ className = '', ...props }) {
  return (
    <input
      className={className}
      {...props}
    />
  )
}