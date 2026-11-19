import { NavLink as RRNavLink, type NavLinkProps, type NavLinkRenderProps } from 'react-router-dom'
import * as React from 'react'

export interface NavLinkComponentProps extends NavLinkProps {}

export function NavLink({ className, ...props }: NavLinkComponentProps) {
  return (
    <RRNavLink
      className={({ isActive }: NavLinkRenderProps) => {
        const base =
          'px-3 py-2 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-[#1E3A8A]'
        const active = isActive ? 'bg-[#1E3A8A] text-white' : 'text-gray-600 hover:bg-[#F3F4F6] hover:text-[#1E3A8A]'
        const extra = typeof className === 'function' ? className({ isActive, isPending: false, isTransitioning: false }) : className ?? ''
        return [base, active, extra].filter(Boolean).join(' ')
      }}
      {...props}
    />
  )
}
