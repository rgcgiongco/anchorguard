import { Link } from 'react-router-dom'
import { useState } from 'react'
import { Navbar } from '@/components/layout/Navbar'
import { Footer } from '@/components/layout/Footer'
import { Button } from '@/components/ui/Button'
import trustBadges from '@/assets/images/trust-badges.png'

const FEATURES = {
  free: [
    '1 linked device',
    'Basic app blocking',
    'Manual screen time limits',
    'Email support',
  ],
  monthly: [
    'Up to 5 linked devices',
    'All app blocking features',
    'Advanced screen time controls',
    'Location & geofencing alerts',
    'Activity reports & weekly summaries',
    'Priority email support',
  ],
  annual: [
    'Up to 5 linked devices',
    'All app blocking features',
    'Advanced screen time controls',
    'Location & geofencing alerts',
    'Activity reports & weekly summaries',
    'Priority email support',
    '2 months free vs monthly',
  ],
}

const FAQS = [
  {
    q: 'Can I change or cancel my plan anytime?',
    a: 'Yes. You can upgrade, downgrade, or cancel your subscription at any time from your account settings. Changes take effect at the start of your next billing cycle.',
  },
  {
    q: 'How many devices can I manage?',
    a: 'The Free plan covers 1 device. Both paid plans cover up to 5 devices per household.',
  },
  {
    q: 'What payment methods are supported?',
    a: 'We support GCash and Maya for fast, secure payments in the Philippines, plus major credit/debit cards internationally.',
  },
  {
    q: 'Is there a free trial?',
    a: 'Yes. The Free plan lets you try AnchorGuard on 1 device with no credit card required. Upgrade anytime to unlock all features.',
  },
]

export default function PricingPage() {
  return (
    <div className="min-h-screen bg-[#F3F4F6] text-[#1C2530]">
      <Navbar />

      <section className="relative overflow-hidden py-20 lg:py-28 px-4 sm:px-6 lg:px-8">
        <div className="absolute inset-0 z-0">
          <img
            src={trustBadges}
            alt=""
            className="w-full h-full object-cover opacity-30"
            aria-hidden="true"
          />
        </div>
        <div className="relative z-10 mx-auto max-w-3xl text-center">
          <span className="inline-block px-4 py-1.5 rounded-full bg-orange-100 text-orange-700 text-sm font-semibold mb-6">
            Simple, transparent pricing
          </span>
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight mb-6">
            Choose your plan
          </h1>
          <p className="text-lg text-gray-600 mb-10 max-w-xl mx-auto leading-relaxed">
            Protect your family with guided access, app blocking, location safety, and activity insights.
            Cancel anytime. No hidden fees.
          </p>
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-20 lg:py-24">
        <div className="mx-auto max-w-5xl">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <PlanCard
              name="Free"
              price="₱0"
              period="/mo"
              description="Limited features, 1 device"
              features={FEATURES.free}
              ctaLabel="Get Started Free"
              ctaVariant="primary"
              ctaTo="/signup"
              highlighted={false}
            />
            <PlanCard
              name="Monthly"
              price="₱249"
              period="/mo"
              description="Up to 5 devices, all features"
              features={FEATURES.monthly}
              ctaLabel="Choose Plan"
              ctaVariant="primary"
              ctaTo="/signup"
              highlighted={true}
              badge="Most Popular"
            />
            <PlanCard
              name="Annual"
              price="₱2,490"
              period="/yr"
              description="Up to 5 devices, 2 months free"
              features={FEATURES.annual}
              ctaLabel="Choose Plan"
              ctaVariant="ghost"
              ctaTo="/signup"
              highlighted={false}
            />
          </div>
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-20 bg-[#F3F4F6] border-y border-[#F3F4F6]">
        <div className="mx-auto max-w-3xl">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold tracking-tight mb-4">Frequently asked questions</h2>
            <p className="text-gray-600">Everything you need to know about billing and plans.</p>
          </div>
          <div className="space-y-4">
            {FAQS.map((faq, i) => (
              <FAQItem key={i} q={faq.q} a={faq.a} />
            ))}
          </div>
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-24">
        <div className="relative z-10 mx-auto max-w-2xl text-center bg-[#1E3A8A] p-8 sm:p-12 rounded-2xl shadow-lg text-white overflow-hidden">
          <div className="absolute inset-0 bg-orange-500/10" />
          <h2 className="text-2xl sm:text-3xl font-bold mb-4 relative z-10">Ready to protect your family?</h2>
          <p className="text-orange-100 mb-8 relative z-10">
            Join thousands of parents keeping their children safe online. Pay with GCash, Maya, or card.
          </p>
          <Link to="/signup" className="relative z-10">
            <Button variant="ghost" size="lg" className="w-full sm:w-auto text-[#1E3A8A] hover:text-white border-white/30">
              Get Started Free
            </Button>
          </Link>
        </div>
      </section>

      <Footer />
    </div>
  )
}

function PlanCard({
  name,
  price,
  period,
  description,
  features,
  ctaLabel,
  ctaVariant,
  ctaTo,
  highlighted = false,
  badge,
}: {
  name: string
  price: string
  period: string
  description: string
  features: string[]
  ctaLabel: string
  ctaVariant: 'primary' | 'secondary' | 'ghost'
  ctaTo: string
  highlighted?: boolean
  badge?: string
}) {
  return (
    <div className={[
      'flex flex-col rounded-2xl border p-8 shadow-sm transition-all',
      highlighted
        ? 'border-[#1E3A8A] bg-white shadow-xl scale-105 z-10'
        : 'border-gray-200 bg-white hover:shadow-md',
    ].join(' ')}>
      {badge && (
        <div className="mb-4 inline-flex items-center gap-1 px-3 py-1 rounded-full bg-orange-100 text-orange-700 text-xs font-bold uppercase tracking-wider">
          <span className="w-1.5 h-1.5 rounded-full bg-orange-500" /> {badge}
        </div>
      )}
      <h3 className="text-xl font-bold text-[#1C2530] mb-2">{name}</h3>
      <div className="flex items-baseline gap-1 mb-3">
        <span className="text-4xl font-bold text-[#1E3A8A]">{price}</span>
        <span className="text-sm text-gray-500">{period}</span>
      </div>
      <p className="text-sm text-gray-600 mb-6">{description}</p>
      <ul className="space-y-3 mb-8 flex-1">
        {features.map((f, i) => (
          <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
            <svg className="w-4 h-4 shrink-0 mt-0.5 text-[#1E3A8A]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            {f}
          </li>
        ))}
      </ul>
      <Link to={ctaTo}>
        <Button variant={ctaVariant} size="lg" className="w-full">
          {ctaLabel}
        </Button>
      </Link>
    </div>
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
