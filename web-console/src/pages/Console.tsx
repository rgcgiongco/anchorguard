import { Link } from 'react-router-dom'
import { Navbar } from '@/components/layout/Navbar'
import { Footer } from '@/components/layout/Footer'
import { Button } from '@/components/ui/Button'
import dashboardHero from '@/assets/images/dashboard-hero.jpg'
import dashboardUI from '@/assets/images/dashboard-ui.jpg'

const DEVICES = [
  { name: "Emma's iPhone", model: 'iPhone 14', status: 'Online', lastSeen: '2 min ago', blocked: 3 },
  { name: "Emma's iPad", model: 'iPad Air', status: 'Offline', lastSeen: '4 hours ago', blocked: 1 },
  { name: "Family Kindle", model: 'Kindle Kids', status: 'Online', lastSeen: '15 min ago', blocked: 5 },
]

const FEATURE_HIGHLIGHTS = [
  {
    title: 'Device Cards',
    description: 'See every linked device at a glance — name, model, online status, and last seen.',
  },
  {
    title: 'App Blocking Toggles',
    description: 'Enable or disable app blocking per device with a single toggle. Whitelist educational apps.',
  },
  {
    title: 'Screen Time Charts',
    description: 'Visual daily and weekly usage charts. Set limits per app or category with timer-based locks.',
  },
  {
    title: 'Activity Log',
    description: 'Chronological log of app launches, blocked attempts, and location check-ins.',
  },
]

export default function ConsolePage() {
  return (
    <div className="min-h-screen bg-[#F3F4F6] text-[#1C2530]">
      <Navbar />

      <section className="relative overflow-hidden">
        <div className="absolute inset-0 z-0">
          <img
            src={dashboardHero}
            alt=""
            className="w-full h-full object-cover opacity-40"
            aria-hidden="true"
          />
        </div>
        <div className="relative z-10 px-4 sm:px-6 lg:px-8 py-20 lg:py-28 text-center">
          <span className="inline-block px-4 py-1.5 rounded-full bg-orange-100 text-orange-700 text-sm font-semibold mb-6">
            Parent Console Preview
          </span>
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight mb-6">
            AnchorGuard Parent Console
          </h1>
          <p className="text-lg text-gray-600 mb-10 max-w-2xl mx-auto leading-relaxed">
            Everything you need to manage your family's digital safety — from one central dashboard.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <Link to="/login">
              <Button variant="primary" size="lg" className="w-full sm:w-auto">
                Log In
              </Button>
            </Link>
            <Link to="/signup">
              <Button variant="ghost" size="lg" className="w-full sm:w-auto">
                Create Account
              </Button>
            </Link>
          </div>
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-20 bg-white border-y border-[#F3F4F6]">
        <div className="mx-auto max-w-7xl">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold tracking-tight mb-4">See the console in action</h2>
            <p className="text-gray-600 max-w-xl mx-auto">
              A preview of the parent dashboard after you log in — real devices, real controls, real peace of mind.
            </p>
          </div>
          <div className="relative">
            <img
              src={dashboardUI}
              alt="Dashboard UI mockup"
              className="w-full h-auto rounded-2xl shadow-xl border border-gray-200"
            />
            <div className="absolute -inset-1 bg-gradient-to-r from-[#F97316] to-[#1E3A8A] rounded-2xl blur opacity-20 pointer-events-none" />
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 -mt-16 px-4">
              {DEVICES.map((device) => (
                <div key={device.name} className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <div className="flex justify-between items-start mb-2">
                    <span className="font-semibold text-[#1C2530]">{device.name}</span>
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                      device.status === 'Online' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
                    }`}>{device.status}</span>
                  </div>
                  <div className="text-sm text-gray-600 mb-3">
                    {device.model} • Last seen {device.lastSeen}
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-medium text-[#1E3A8A]">{device.blocked} blocked</span>
                    <span className="text-xs text-gray-400">apps</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="px-4 sm:px-6 lg:px-8 py-20 bg-[#F3F4F6]">
        <div className="mx-auto max-w-7xl">
          <h2 className="text-3xl font-bold tracking-tight mb-12 text-center">Feature highlights</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {FEATURE_HIGHLIGHTS.map((feature, i) => (
              <Card key={i}>
                <div className="w-10 h-10 rounded-lg bg-[#1E3A8A]/10 flex items-center justify-center mb-4 text-[#1E3A8A] font-bold text-lg">
                  {i === 0 ? '📱' : i === 1 ? '🚫' : i === 2 ? '📊' : '📋'}
                </div>
                <h3 className="text-lg font-bold mb-2">{feature.title}</h3>
                <p className="text-gray-600 text-sm leading-relaxed">{feature.description}</p>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="relative overflow-hidden px-4 sm:px-6 lg:px-8 py-24">
        <div className="relative z-10 mx-auto max-w-2xl text-center bg-[#1E3A8A] p-8 sm:p-12 rounded-2xl shadow-lg text-white">
          <h2 className="text-2xl sm:text-3xl font-bold mb-4">Ready to take control?</h2>
          <p className="text-orange-100 mb-8">Join thousands of parents keeping their children safe online.</p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <Link to="/login">
              <Button variant="ghost" size="lg" className="w-full sm:w-auto text-white hover:text-orange-100 border-white/30">
                Log In
              </Button>
            </Link>
            <Link to="/signup">
              <Button variant="primary" size="lg" className="w-full sm:w-auto">
                Create Account
              </Button>
            </Link>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  )
}

function Card({ children }: { children: React.ReactNode }) {
  return (
    <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
      {children}
    </div>
  )
}
