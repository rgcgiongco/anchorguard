import { Link } from 'react-router-dom'
import { useState } from 'react'
import { Navbar } from '@/components/layout/Navbar'
import { Footer } from '@/components/layout/Footer'
import { Button } from '@/components/ui/Button'
import heroAlt from '@/assets/images/hero-alt.jpg'
import appBlocker from '@/assets/images/app-blocker.jpg'
import locationSafety from '@/assets/images/location-safety.jpg'
import dashboardHero from '@/assets/images/dashboard-hero.jpg'
import trustBadges from '@/assets/images/trust-badges.png'

const APP_BLOCKER_BULLETS = [
  'Block distracting or unsafe apps with a single tap',
  'Set custom schedules for school hours and bedtime',
  'Whitelist educational apps that always stay accessible',
  'Receive alerts when a blocked app is attempted',
]

const LOCATION_BULLETS = [
  'Real-time location tracking on a map',
  'Geofence alerts when your child enters or leaves safe zones',
  'Location history for the past 30 days',
  'Low-battery location warnings',
]

const SCREEN_TIME_BULLETS = [
  'Daily and weekly screen time summaries',
  'Per-app usage breakdown with charts',
  'Set daily time limits per app or category',
  'Timer-based lock when limits are reached',
]

const FAQS = [
  {
    q: 'Can I block apps remotely?',
    a: 'Yes. From the parent console you can block or allow any app on your child’s device in real time.',
  },
  {
    q: 'Does location tracking drain the battery?',
    a: 'We use optimized location updates that minimize battery impact while keeping accuracy high.',
  },
  {
    q: 'Can my child bypass the controls?',
    a: 'AnchorGuard uses device-level enforcement with tamper protection and requires your PIN to change settings.',
  },
  {
    q: 'Is there a free plan?',
    a: 'Yes. The free plan covers one device with core blocking and location features.',
  },
]

function FeatureBlock({ image, imageSide, title, description, bullets }: {
  image: string
  imageSide: 'left' | 'right'
  title: string
  description: string
  bullets: string[]
}) {
  return (
    <section className="px-4 sm:px-6 lg:px-8 py-20 bg-white border-y border-[#F3F4F6]">
      <div className="mx-auto max-w-7xl">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {imageSide === 'left' ? (
            <>
              <div className="order-2">
                <h2 className="text-3xl font-bold tracking-tight mb-4">{title}</h2>
                <p className="text-gray-600 leading-relaxed mb-6">{description}</p>
                <ul className="space-y-3 text-gray-700 max-w-lg">
                  {bullets.map((b, i) => (
                    <li key={i} className="flex items-start gap-3">
                      <span className="flex shrink-0 mt-1">
                        <svg className="w-5 h-5 text-[#F97316]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                      </span>
                      {b}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="order-1 lg:order-2">
                <img src={image} alt={title} className="w-full h-auto rounded-xl shadow-lg border border-gray-200" />
              </div>
            </>
          ) : (
            <>
              <div className="order-1 lg:order-1">
                <img src={image} alt={title} className="w-full h-auto rounded-xl shadow-lg border border-gray-200" />
              </div>
              <div className="order-2 lg:order-2">
                <h2 className="text-3xl font-bold tracking-tight mb-4">{title}</h2>
                <p className="text-gray-600 leading-relaxed mb-6">{description}</p>
                <ul className="space-y-3 text-gray-700 max-w-lg">
                  {bullets.map((b, i) => (
                    <li key={i} className="flex items-start gap-3">
                      <span className="flex shrink-0 mt-1">
                        <svg className="w-5 h-5 text-[#F97316]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                        </svg>
                      </span>
                      {b}
                    </li>
                  ))}
                </ul>
              </div>
            </>
          )}
        </div>
      </div>
    </section>
  )
}

function FAQItem({ q, a }: { q: string; a: string }) {
  const [open, setOpen] = useState(false)
  return (
    <div className="border border-gray-200 rounded-lg overflow-hidden">
      <button
        onClick={() => setOpen((prev) => !prev)}
        className="w-full flex items-center justify-between px-4 py-3 text-left bg-[#F3F4F6] hover:bg-gray-200 focus:outline-none"
        aria-expanded={open}
      >
        <span className="font-medium">{q}</span>
        <svg className={`w-5 h-5 transition-transform ${open ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      {open && (
        <div className="px-4 pb-4">
          <p className="text-gray-600">{a}</p>
        </div>
      )}
    </div>
  )
}

export default function FeaturesPage() {
  return (
    <div className="min-h-screen bg-[#F3F4F6] text-[#1C2530]">
      <Navbar />

      <section className="relative overflow-hidden">
        <div className="absolute inset-0 z-0">
          <img src={heroAlt} alt="" className="w-full h-full object-cover opacity-40" aria-hidden="true" />
        </div>
        <div className="relative z-10 px-4 sm:px-6 lg:px-8 py-20 lg:py-28 text-center">
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight mb-6">Features</h1>
          <p className="text-lg text-gray-600 mb-10 max-w-2xl mx-auto leading-relaxed">
            Everything you need to keep your family safe online — from app blocking to location safety and screen-time insights.
          </p>
        </div>
      </section>

      <FeatureBlock
        image={appBlocker}
        imageSide="left"
        title="App Blocker"
        description="Block distracting or unsafe apps on your child’s device with a single tap. Set schedules, time limits, and always-allow educational apps."
        bullets={APP_BLOCKER_BULLETS}
      />

      <FeatureBlock
        image={locationSafety}
        imageSide="right"
        title="Location & Geofencing"
        description="Know where your child is with real-time location tracking and get alerts when they leave designated safe zones."
        bullets={LOCATION_BULLETS}
      />

      <FeatureBlock
        image={dashboardHero}
        imageSide="left"
        title="Screen Time & Activity"
        description="See app usage and screen time at a glance. Set daily limits per app or category, and get weekly reports delivered to your inbox."
        bullets={SCREEN_TIME_BULLETS}
      />

      <section className="px-4 sm:px-6 lg:px-8 py-20 bg-[#F3F4F6]">
        <div className="mx-auto max-w-7xl text-center">
          <h2 className="text-3xl font-bold tracking-tight mb-4">Trusted by parents everywhere</h2>
          <p className="text-gray-600 mb-10 max-w-xl mx-auto">Award-winning safety features recognized by leading organizations.</p>
          <img src={trustBadges} alt="Trust badges" className="w-full max-w-md h-auto object-contain mx-auto" />
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-16 bg-white border-t border-[#F3F4F6]">
        <div className="mx-auto max-w-2xl">
          <h2 className="text-2xl font-bold tracking-tight mb-8 text-center">Frequently asked questions</h2>
          <div className="space-y-3">
            {FAQS.map((faq, i) => (
              <FAQItem key={i} q={faq.q} a={faq.a} />
            ))}
          </div>
        </div>
      </section>

      <section className="relative overflow-hidden px-4 sm:px-6 lg:px-8 py-24">
        <div className="relative z-10 mx-auto max-w-2xl text-center bg-[#1E3A8A] p-8 sm:p-12 rounded-2xl shadow-lg text-white">
          <h2 className="text-2xl sm:text-3xl font-bold mb-4">Start protecting your family today</h2>
          <p className="text-orange-100 mb-8">Join thousands of parents keeping their children safe online.</p>
          <Link to="/signup">
            <Button variant="ghost" size="lg" className="w-full sm:w-auto text-[#1E3A8A] hover:text-white border-white/30">
              Create Free Account
            </Button>
          </Link>
        </div>
      </section>

      <Footer />
    </div>
  )
}
