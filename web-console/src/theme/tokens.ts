export const anchorTokens = {
  brand: {
    name: 'AnchorGuard',
    tagline: 'Guided access, anchored in trust'
  },
  colors: {
    primary: '#2E6F8E',
    primaryDark: '#1A4A5F',
    secondary: '#4CAF7A',
    danger: '#C0504D',
    warning: '#D9A441',
    surface: '#FFFFFF',
    surfaceAlt: '#F7F9FB',
    surfaceVariant: '#E8EEF3',
    onPrimary: '#FFFFFF',
    onSurface: '#1C2530',
    onSurfaceVariant: '#5B6B7C',
    outline: '#B9C4CE',
    success: '#3E8E5A',
    error: '#C0504D'
  },
  typography: {
    fontFamily: 'Inter, system-ui, -apple-system, "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
    monoFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
    headingWeight: 600,
    bodyWeight: 400,
    bodyMediumWeight: 500
  },
  radius: {
    small: '4px',
    medium: '8px',
    large: '12px'
  },
  elevation: {
    level1: '0 1px 2px rgba(28,37,48,0.08)',
    level2: '0 4px 12px rgba(28,37,48,0.10)',
    level3: '0 8px 24px rgba(28,37,48,0.12)'
  }
} as const

export type AnchorTokens = typeof anchorTokens
