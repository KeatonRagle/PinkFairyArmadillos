export function Heading({ children, className = '', ...props }) {
  return (
    <h2
      className={`text-2xl font-bold leading-9 tracking-tight text-gray-900 dark:text-white ${className}`}
      {...props}
    >
      {children}
    </h2>
  )
}