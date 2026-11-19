import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { NavLink } from '@/components/ui/NavLink'
import logo from '@/assets/images/logo.png'

const NAV_LINKS = [
  { href: '/', label: 'Home' },
  { href: '/features', label: 'Features' },
  { href: '/console', label: 'Console' },
  { href: '/pricing', label: 'Pricing' },
  { href: '/dashboard/locations', label: 'Location' },
]

export function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const location = useLocation()

  return (
    <nav className="sticky top-0 z-40 border-b border-[#F3F4F6] bg-white/95 backdrop-blur">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-3">
            <img src={logo} alt="AnchorGuard logo" className="h-8 w-auto" />
            <span className="text-xl font-bold text-[#1E3A8A] tracking-tight">AnchorGuard</span>
          </div>

          <div className="hidden md:flex items-center gap-6">
            {NAV_LINKS.map((link) => (
              <NavLink
                key={link.href}
                to={link.href}
                end={link.href === '/'}
                className={({ isActive }: { isActive: boolean }) =>
                  isActive
                    ? 'text-[#F97316] font-semibold'
                    : 'text-gray-700 hover:text-[#F97316]'
                }
              >
                {link.label}
              </NavLink>
            ))}
            <Link to="/pricing">
              <Button variant="ghost" size="sm">
                Get Started
              </Button>
            </Link>
          </div>

          <div className="flex items-center gap-2 md:hidden">
            <button
              type="button"
              onClick={() => setIsMenuOpen((prev) => !prev)}
              className="p-2 rounded-md text-gray-600 hover:bg-[#F3F4F6] focus:outline-none"
              aria-label="Main menu"
              aria-expanded={isMenuOpen}
            >
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                {isMenuOpen ? (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                ) : (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                )}
              </svg>
            </button>
          </div>
        </div>
      </div>

      {isMenuOpen && (
        <div className="md:hidden border-t border-[#F3F4F6] bg-white">
          <div className="px-4 py-4 space-y-2">
            {NAV_LINKS.map((link) => (
              <NavLink
                key={link.href}
                to={link.href}
                end={link.href === '/'}
                onClick={() => setIsMenuOpen(false)}
                className={({ isActive }: { isActive: boolean }) =>
                  isActive
                    ? 'block px-3 py-2 rounded-md bg-orange-50 text-[#F97316] font-medium'
                    : 'block px-3 py-2 rounded-md text-gray-700 hover:bg-[#F3F4F6] hover:text-[#F97316]'
                }
              >
                {link.label}
              </NavLink>
            ))}
            <Link to="/pricing" onClick={() => setIsMenuOpen(false)}>
              <Button variant="ghost" size="sm" className="w-full">
                Get Started
              </Button>
            </Link>
          </div>
        </div>
      )}
    </nav>
  )
}
