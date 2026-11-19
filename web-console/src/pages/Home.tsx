import { Link } from 'react-router-dom'
import { Navbar } from '@/components/layout/Navbar'
import { Footer } from '@/components/layout/Footer'
import { Button } from '@/components/ui/Button'
import heroHome from '@/assets/images/hero-home.jpg'
import heroAlt from '@/assets/images/hero-alt.jpg'
import appBlocker from '@/assets/images/app-blocker.jpg'
import locationSafety from '@/assets/images/location-safety.jpg'
import dashboardHero from '@/assets/images/dashboard-hero.jpg'
import trustBadges from '@/assets/images/trust-badges.png'

const STATS = [
  { value: '10K+', label: 'Families protected' },
  { value: '24/7', label: 'Real-time monitoring' },
  { value: '500+', label: 'Apps blockable' },
  { value: '99.9%', label: 'Uptime guarantee' },
]

const FEATURES = [
  {
    image: appBlocker,
    title: 'App Blocker',
    description: 'Block distracting or unsafe apps on your child’s device with a single tap. Set schedules and time limits.',
  },
  {
    image: locationSafety,
    title: 'Location Safety',
    description: 'Know where your child is with real-time location tracking and geofence alerts when they leave safe zones.',
  },
  {
    image: dashboardHero,
    title: 'Activity Dashboard',
    description: 'See app usage, screen time, and web history at a glance. Weekly reports delivered to your inbox.',
  },
]

export default function HomePage() {
  return (
    <div className="min-h-screen bg-[#F3F4F6] text-[#1C2530]">
      <Navbar />

      <section className="relative overflow-hidden">
        <div className="absolute inset-0 z-0">
          <img
            src={heroAlt}
            alt=""
            className="w-full h-full object-cover opacity-40"
            aria-hidden="true"
          />
        </div>
        <div className="relative z-10 px-4 sm:px-6 lg:px-8 py-20 lg:py-32">
          <div className="mx-auto max-w-5xl text-center">
            <span className="inline-block px-4 py-1.5 rounded-full bg-orange-100 text-orange-700 text-sm font-semibold mb-6">
              Trusted by 10,000+ parents
            </span>
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight mb-6">
              Protect Your Child’s Digital World
            </h1>
            <p className="text-lg sm:text-xl text-gray-600 mb-10 max-w-2xl mx-auto leading-relaxed">
              Guided access, app blocking, location safety, and activity insights —
              everything you need to keep your family safe online, anchored in trust.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
              <Link to="/signup">
                <Button variant="primary" size="lg" className="w-full sm:w-auto">
                  Get Started Free
                </Button>
              </Link>
              <Link to="/features">
                <Button variant="ghost" size="lg" className="w-full sm:w-auto">
                  How It Works
                </Button>
              </Link>
            </div>

            <div className="mt-16 grid grid-cols-2 sm:grid-cols-4 gap-8 border-t border-gray-300/50 pt-8">
              {STATS.map((stat, i) => (
                <div key={i} className="text-center">
                  <div className="text-2xl font-bold text-[#1E3A8A]">{stat.value}</div>
                  <div className="text-sm text-gray-500">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-20 lg:py-24 bg-white">
        <div className="mx-auto max-w-7xl">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold tracking-tight mb-4">Everything you need, in one place</h2>
            <p className="text-gray-600 max-w-2xl mx-auto">
              Powerful parental controls designed to be simple to set up and easy to use.
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {FEATURES.map((feature) => (
              <Card key={feature.title}>
                <img
                  src={feature.image}
                  alt={feature.title}
                  className="w-full h-48 object-cover rounded-lg mb-6"
                />
                <h3 className="text-xl font-bold mb-3">{feature.title}</h3>
                <p className="text-gray-600 leading-relaxed">{feature.description}</p>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-20 lg:py-24 bg-[#F3F4F6]">
        <div className="mx-auto max-w-7xl">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold tracking-tight mb-4">Trusted by parents everywhere</h2>
            <p className="text-gray-600">Award-winning safety features recognized by leading organizations.</p>
          </div>
          <div className="flex flex-col md:flex-row gap-8 justify-center items-center">
            <img
              src={trustBadges}
              alt="Trust badges"
              className="w-full max-w-md h-auto object-contain"
            />
          </div>
        </div>
      </section>

      <section className="relative px-4 sm:px-6 lg:px-8 py-24 overflow-hidden">
        <div className="absolute inset-0 z-0">
          <img
            src={heroAlt}
            alt=""
            className="w-full h-full object-cover opacity-30"
            aria-hidden="true"
          />
        </div>
        <div className="relative z-10 mx-auto max-w-2xl text-center bg-white p-8 sm:p-12 rounded-2xl shadow-lg border border-gray-200">
          <h2 className="text-2xl sm:text-3xl font-bold mb-4">Ready to protect your family?</h2>
          <p className="text-gray-600 mb-8">Join thousands of parents keeping their children safe online.</p>
          <Link to="/signup">
            <Button variant="primary" size="lg" className="w-full sm:w-auto">
              Create Free Account
            </Button>
          </Link>
        </div>
      </section>

      <Footer />
    </div>
  )
}

function Card({ children }: { children: React.ReactNode }) {
  return (
    <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
      {children}
    </div>
  )
}
