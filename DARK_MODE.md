# 🌙 Dark Mode Support

The Scrum Poker application includes comprehensive dark mode support that automatically responds to your system or browser preferences.

## ✨ Features

- **Automatic Detection**: Uses CSS `@media (prefers-color-scheme: dark)` to detect system preferences
- **No UI Toggle Needed**: Seamlessly switches between themes based on OS settings
- **Comprehensive Coverage**: All components support both light and dark modes
- **Smooth Transitions**: 200ms transitions for color changes
- **Accessibility Maintained**: Proper contrast ratios in both themes

## 🎨 Design Philosophy

### Light Mode (Default)

- Clean, bright interface with white backgrounds
- Gray text hierarchy for clear readability
- Blue accent colors for interactive elements
- Traditional card-like appearance

### Dark Mode

- Reduced eye strain with dark backgrounds (`#1a202c`, `#2d3748`)
- Light text (`#e2e8f0`, `#a0aec0`) for readability
- Adjusted blue tones optimized for dark backgrounds
- Consistent visual hierarchy and spacing

## 🔧 Technical Implementation

### Tailwind CSS Configuration

```javascript
// tailwind.config.js
module.exports = {
  darkMode: "media", // Responds to system preference
  theme: {
    extend: {
      colors: {
        "dark-bg": "#1a202c", // Main background
        "dark-surface": "#2d3748", // Card/container backgrounds
        "dark-border": "#4a5568", // Borders and dividers
        "dark-text": "#e2e8f0", // Primary text
        "dark-text-secondary": "#a0aec0", // Secondary text
      },
    },
  },
};
```

### CSS Classes Pattern

Every component uses the `dark:` variant pattern:

```css
/* Light mode (default) | Dark mode variant */
bg-white dark:bg-dark-surface
text-gray-900 dark:text-dark-text
border-gray-200 dark:border-dark-border
```

## 📱 How to Test Dark Mode

### System-Level (Recommended)

1. **macOS**: System Preferences → General → Appearance → Dark
2. **Windows**: Settings → Personalization → Colors → Dark
3. **Linux (GNOME)**: Settings → Appearance → Dark

### Browser-Level

1. **Chrome DevTools**:

   - Open DevTools (F12)
   - Command Palette (Ctrl+Shift+P)
   - Type "Rendering"
   - Find "Emulate CSS media feature prefers-color-scheme"
   - Select "dark"

2. **Firefox DevTools**:
   - Open DevTools (F12)
   - Go to Inspector
   - Click the settings gear
   - Check "Simulate prefers-color-scheme: dark"

## 🎯 Components with Dark Mode Support

### Core Components

- **App.tsx**: Main application background
- **NameEntry.tsx**: Welcome form and inputs
- **PokerRoom.tsx**: Main game interface
- **PokerCard.tsx**: Interactive estimation cards
- **UserTable.tsx**: User list and session controls

### UI Elements

- ✅ Background colors and surfaces
- ✅ Text colors (primary, secondary, muted)
- ✅ Button states (normal, hover, focus)
- ✅ Form inputs and validation
- ✅ Borders and dividers
- ✅ Interactive feedback
- ✅ Loading states and icons

## 🧪 Testing Coverage

The application includes automated tests for dark mode:

```bash
cd frontend
npm test -- darkmode.test.tsx
```

Tests verify:

- CSS classes are properly applied
- Components render with dark variants
- Media query strategy is implemented

## 💡 Best Practices

### For Users

- Enable dark mode at the system level for the best experience
- The app will automatically match your preference
- No manual configuration needed

### For Developers

- All new components should include `dark:` variants
- Use the custom color palette for consistency
- Test components in both light and dark modes
- Maintain proper contrast ratios

## 🔮 Future Enhancements

Potential improvements for future versions:

- Manual theme toggle override
- Additional theme variants (high contrast, etc.)
- Theme persistence in local storage
- Automatic time-based switching
- Custom accent color options

## 📖 Resources

- [CSS `prefers-color-scheme` MDN](https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-color-scheme)
- [Tailwind CSS Dark Mode](https://tailwindcss.com/docs/dark-mode)
- [Web Accessibility and Dark Mode](https://developer.mozilla.org/en-US/docs/Web/Accessibility/Understanding_WCAG/Perceivable/Color_contrast)

---

**Note**: Dark mode is automatically enabled when your system is set to dark mode. No additional configuration is required!
