/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        pf: {
          primary: 'var(--pf-primary)',
          accent: 'var(--pf-accent)',
          'primary-soft': 'var(--pf-primary-soft)',
          bg: 'var(--pf-bg)',
          'bg-muted': 'var(--pf-bg-muted)',
          'bg-soft': 'var(--pf-bg-soft)',
          surface: 'var(--pf-surface)',
          'surface-elevated': 'var(--pf-surface-elevated)',
          border: 'var(--pf-border)',
          'border-strong': 'var(--pf-border-strong)',
          text: 'var(--pf-text)',
          'text-muted': 'var(--pf-text-muted)',
          'text-subtle': 'var(--pf-text-subtle)',
        },
      },
      boxShadow: {
        pf: 'var(--pf-shadow)',
        'pf-lg': 'var(--pf-shadow-lg)',
      },
      borderRadius: {
        pf: 'var(--pf-radius)',
        'pf-lg': 'var(--pf-radius-lg)',
        'pf-xl': 'var(--pf-radius-xl)',
      },
      fontFamily: {
        pf: ['var(--pf-font)'],
        display: ['var(--pf-font-display)'],
      },
      transitionDuration: {
        pf: '220ms',
      },
      maxWidth: {
        content: 'var(--pf-content-width)',
      },
    },
  },
  plugins: [require('@tailwindcss/typography')],
}
