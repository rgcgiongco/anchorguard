/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          DEFAULT: '#1E3A8A',
          dark: '#15306B',
        },
        orange: {
          DEFAULT: '#F97316',
          dark: '#C2410C',
        },
        white: '#FFFFFF',
        gray: {
          light: '#F3F4F6',
        },
      },
    },
  },
  plugins: [],
}
