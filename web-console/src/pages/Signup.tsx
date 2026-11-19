import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import logo from '@/assets/images/logo.png'
import heroAlt from '@/assets/images/hero-alt.jpg'
import trustBadges from '@/assets/images/trust-badges.png'

export default function SignupPage() {
  const { signup, user } = useAuth()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function validate(): boolean {
    if (!name.trim() || !email.trim() || !password || !confirmPassword) {
      setError('All fields are required.')
      return false
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Please enter a valid email address.')
      return false
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
      return false
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return false
    }
    setError('')
    return true
  }

  useEffect(() => {
    if (user) {
      navigate('/dashboard', { replace: true })
    }
  }, [user, navigate])

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return
    setBusy(true)
    setError('')
    try {
      await signup({ name: name.trim(), email: email.trim(), password })
      // Navigation handled by useEffect watching user state
    } catch (err) {
      setError((err as Error)?.message || 'Unable to create account. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#F3F4F6] px-4 sm:px-6 lg:px-8">
      <div className="relative w-full max-w-md">
        <div className="absolute inset-0 z-0">
          <img src={heroAlt} alt="" className="w-full h-full object-cover" aria-hidden="true" />
          <div className="absolute inset-0 bg-[#1E3A8A]/70" />
        </div>
        <div className="relative z-10 mx-auto mt-20 mb-10 w-full max-w-sm">
          <div className="flex justify-center mb-6">
            <img src={logo} alt="AnchorGuard" className="h-12 w-auto" />
          </div>
          <div className="bg-white/90 backdrop-blur rounded-2xl p-8 shadow-2xl border border-white/30">
            <h2 className="text-2xl font-bold text-[#1C2530] mb-2 text-center">Create account</h2>
            <p className="text-gray-600 text-sm text-center mb-8">
              Join thousands of parents keeping their children safe online.
            </p>

            {error && (
              <div className="mb-6 p-3 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">
                {error}
              </div>
            )}

            <form onSubmit={onSubmit} className="space-y-4">
              <Input
                label="Full Name"
                id="signup-name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Jane Doe"
                required
                autoComplete="name"
              />
              <Input
                label="Email"
                id="signup-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                autoComplete="email"
              />
              <Input
                label="Password"
                id="signup-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                autoComplete="new-password"
              />
              <Input
                label="Confirm Password"
                id="signup-confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="••••••••"
                required
                autoComplete="new-password"
              />
              <Button type="submit" variant="primary" size="lg" disabled={busy} className="w-full">
                {busy ? 'Creating account…' : 'Create Account'}
              </Button>
            </form>

            <p className="mt-8 text-center text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-[#F97316] font-semibold hover:underline">
                Sign in
              </Link>
            </p>
          </div>

          <div className="mt-10 flex justify-center">
            <img src={trustBadges} alt="Trust badges" className="h-10 w-auto opacity-80" />
          </div>
        </div>
      </div>
    </div>
  )
}
