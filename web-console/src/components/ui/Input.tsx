import * as React from 'react'

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  helperText?: string
}

export function Input({ label, error, helperText, className, id, ...props }: InputProps) {
  const inputId = id || `input-${Math.random().toString(36).slice(2)}`

  return (
    <div className="field">
      {label && <label htmlFor={inputId}>{label}</label>}
      <input
        id={inputId}
        className={[
          'w-full rounded-lg border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#F97316]',
          error ? 'border-red-500 focus:ring-red-500' : 'border-[#B9C4CE] focus:border-[#1E3A8A]',
          className,
        ].filter(Boolean).join(' ')}
        {...props}
      />
      {error && <p className="field-error">{error}</p>}
      {!error && helperText && <p className="text-xs text-gray-500">{helperText}</p>}
    </div>
  )
}
