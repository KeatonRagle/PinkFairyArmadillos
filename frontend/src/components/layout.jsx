export function AuthLayout({ children, ...props }) {
  return (
    <main
      className="auth-layout"
      {...props}
    >
      {children}
    </main>
  )
}