// Checkbox input component
export function Checkbox({ className = '', ...props }) {
  return (
    <input
      type="checkbox"
      className={className}
      {...props}
    />
  )
}

// Wrapper for checkbox and label
export function CheckboxField({ children, className = '' }) {
  return (
    <div className={`checkbox-field ${className}`}>
      {children}
    </div>
  )
}