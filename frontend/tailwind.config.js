/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  darkMode: "media", // Responds to system/browser dark mode preference
  theme: {
    extend: {
      // Custom dark mode colors for consistent theming
      colors: {
        "dark-bg": "#1a202c",
        "dark-surface": "#2d3748",
        "dark-border": "#4a5568",
        "dark-text": "#e2e8f0",
        "dark-text-secondary": "#a0aec0",
      },
    },
  },
  plugins: [],
};
