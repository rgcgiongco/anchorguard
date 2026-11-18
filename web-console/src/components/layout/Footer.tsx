export function Footer() {
  return (
    <footer className="footer">
      <span>AnchorGuard Console</span>
      <span> · v0.1.0</span>
      <span> · <a href="#" onClick={(e) => e.preventDefault()}>Privacy</a></span>
      <span> · <a href="#" onClick={(e) => e.preventDefault()}>Terms</a></span>
    </footer>
  )
}
