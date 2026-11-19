import * as React from 'react'

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost'
  size?: 'sm' | 'default' | 'lg'
}

export function Button({ className, variant = 'primary', size = 'default', ...props }: ButtonProps) {
  const base =
    'inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50'

  const variants = {
    primary: 'bg-orange-600 text-white hover:bg-orange-700 focus-visible:ring-orange-600',
    secondary:
      'border border-[#1E3A8A] text-[#1E3A8A] hover:bg-[#1E3A8A] hover:text-white focus-visible:ring-[#1E3A8A]',
    ghost: 'text-[#1E3A8A] hover:bg-[#F3F4F6] focus-visible:ring-[#1E3A8A]',
  }

  const sizes = {
    sm: 'h-9 rounded-md px-3',
    default: 'h-10 px-4 py-2',
    lg: 'h-11 rounded-md px-8',
  }

  return (
    <button className={[base, variants[variant], sizes[size], className].filter(Boolean).join(' ')} {...props} />
  )
}
