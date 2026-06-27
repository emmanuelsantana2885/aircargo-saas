/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        mono: ['Lucida Console', 'Monaco', 'Courier New', 'monospace'],
      },
      fontSize: {
        xs: '16px',
      },
    },
  },
  plugins: [],
}
