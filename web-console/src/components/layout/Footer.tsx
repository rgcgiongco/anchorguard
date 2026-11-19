import { Link } from 'react-router-dom'
import trustBadges from '@/assets/images/trust-badges.png'

const FOOTER_LINKS = [
  { label: 'Features', href: '/features' },
  { label: 'Console', href: '/console' },
  { label: 'Pricing', href: '/pricing' },
  { label: 'Privacy', href: '#' },
  { label: 'Terms', href: '#' },
  { label: 'Help', href: '#' },
]

export function Footer() {
  return (
    <footer className="border-t border-[#F3F4F6] bg-[#1E3A8A] text-white py-12">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-8 mb-12">
          <div className="col-span-2 md:col-span-3">
            <div className="flex items-center gap-2 mb-6">
              <img src={trustBadges} alt="Trust badges" className="h-8 w-auto" />
            </div>
            <p className="text-gray-300 text-sm leading-relaxed max-w-xs">
              AnchorGuard helps parents keep their children safe online with guided access,
              app blocking, location safety, and activity insights — all anchored in trust.
            </p>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-white tracking-uppercase mb-4">
              Product
            </h3>
            <ul className="space-y-3 text-sm text-gray-300">
              {FOOTER_LINKS.slice(0, 3).map((link) => (
                <li key={link.label}>
                  <Link
                    to={link.href}
                    className="hover:text-orange-400 transition-colors"
                    onClick={(e) => {
                      if (link.href.startsWith('#')) e.preventDefault()
                    }}
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-white tracking-uppercase mb-4">
              Company
            </h3>
            <ul className="space-y-3 text-sm text-gray-300">
              {FOOTER_LINKS.slice(3).map((link) => (
                <li key={link.label}>
                  <Link
                    to={link.href}
                    className="hover:text-orange-400 transition-colors"
                    onClick={(e) => {
                      if (link.href.startsWith('#')) e.preventDefault()
                    }}
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-700 pt-8 flex flex-col md:flex-row justify-between items-center gap-4 text-xs text-gray-400">
          <p>© {new Date().getFullYear()} AnchorGuard. All rights reserved.</p>
          <div className="flex items-center gap-4">
            <a href="#" className="hover:text-white transition-colors">
              Privacy
            </a>
            <a href="#" className="hover:text-white transition-colors">
              Terms
            </a>
          </div>
        </div>
      </div>
    </footer>
  )
}
