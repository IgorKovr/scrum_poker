/**
 * PokerCard.test.tsx - Unit Tests for PokerCard Component
 * 
 * This test suite validates the PokerCard component, which renders
 * individual estimation cards that users can click to select.
 * 
 * Test Coverage:
 * 1. Component rendering with different values
 * 2. Selection state visual feedback
 * 3. Click interaction handling
 * 4. CSS classes and styling
 * 5. Accessibility features
 * 6. Special value handling (numbers and symbols)
 * 
 * Testing Approach:
 * - Uses React Testing Library for component testing
 * - Tests both visual appearance and user interactions
 * - Validates accessibility attributes
 * - Ensures proper event handling
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { PokerCard } from '../../components/PokerCard';

describe('PokerCard Component', () => {
  describe('Rendering', () => {
    it('should render card with numeric value', () => {
      render(
        <PokerCard
          value="5"
          isSelected={false}
          onClick={() => {}}
        />
      );

      expect(screen.getByRole('button')).toBeInTheDocument();
      expect(screen.getByText('5')).toBeInTheDocument();
    });

    it('should render card with special symbol value', () => {
      render(
        <PokerCard
          value="?"
          isSelected={false}
          onClick={() => {}}
        />
      );

      expect(screen.getByText('?')).toBeInTheDocument();
    });

    it('should render card with coffee symbol', () => {
      render(
        <PokerCard
          value="☕"
          isSelected={false}
          onClick={() => {}}
        />
      );

      expect(screen.getByText('☕')).toBeInTheDocument();
    });

    it('should render card with decimal value', () => {
      render(
        <PokerCard
          value="0.5"
          isSelected={false}
          onClick={() => {}}
        />
      );

      expect(screen.getByText('0.5')).toBeInTheDocument();
    });

    it('should be a button element', () => {
      render(
        <PokerCard
          value="3"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card.tagName).toBe('BUTTON');
    });
  });

  describe('Selection State', () => {
    it('should apply selected styles when isSelected is true', () => {
      render(
        <PokerCard
          value="8"
          isSelected={true}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card).toHaveClass('bg-blue-600');
      expect(card).toHaveClass('text-white');
      expect(card).toHaveClass('shadow-lg');
      expect(card).toHaveClass('transform');
      expect(card).toHaveClass('-translate-y-1');
      expect(card).toHaveClass('border-blue-700');
    });

    it('should apply default styles when isSelected is false', () => {
      render(
        <PokerCard
          value="8"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card).toHaveClass('bg-white');
      expect(card).toHaveClass('text-gray-800');
      expect(card).toHaveClass('shadow-md');
      expect(card).toHaveClass('border-gray-200');
      expect(card).not.toHaveClass('bg-blue-600');
      expect(card).not.toHaveClass('-translate-y-1');
    });

    it('should have consistent base styles regardless of selection', () => {
      const { rerender } = render(
        <PokerCard
          value="2"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      
      // Check base styles
      expect(card).toHaveClass('relative');
      expect(card).toHaveClass('w-full');
      expect(card).toHaveClass('h-24');
      expect(card).toHaveClass('rounded-lg');
      expect(card).toHaveClass('font-bold');
      expect(card).toHaveClass('text-xl');
      expect(card).toHaveClass('transition-all');
      expect(card).toHaveClass('duration-200');
      expect(card).toHaveClass('border-2');

      // Re-render with selected state
      rerender(
        <PokerCard
          value="2"
          isSelected={true}
          onClick={() => {}}
        />
      );

      // Base styles should remain
      expect(card).toHaveClass('relative');
      expect(card).toHaveClass('w-full');
      expect(card).toHaveClass('h-24');
      expect(card).toHaveClass('rounded-lg');
      expect(card).toHaveClass('font-bold');
      expect(card).toHaveClass('text-xl');
    });
  });

  describe('User Interactions', () => {
    it('should call onClick when card is clicked', async () => {
      const user = userEvent.setup();
      const mockOnClick = vi.fn();

      render(
        <PokerCard
          value="13"
          isSelected={false}
          onClick={mockOnClick}
        />
      );

      const card = screen.getByRole('button');
      await user.click(card);

      expect(mockOnClick).toHaveBeenCalledTimes(1);
    });

    it('should call onClick when card is activated via keyboard', async () => {
      const user = userEvent.setup();
      const mockOnClick = vi.fn();

      render(
        <PokerCard
          value="20"
          isSelected={false}
          onClick={mockOnClick}
        />
      );

      const card = screen.getByRole('button');
      card.focus();
      await user.keyboard('{Enter}');

      expect(mockOnClick).toHaveBeenCalledTimes(1);
    });

    it('should call onClick when space key is pressed', async () => {
      const user = userEvent.setup();
      const mockOnClick = vi.fn();

      render(
        <PokerCard
          value="40"
          isSelected={false}
          onClick={mockOnClick}
        />
      );

      const card = screen.getByRole('button');
      card.focus();
      await user.keyboard(' ');

      expect(mockOnClick).toHaveBeenCalledTimes(1);
    });

    it('should handle multiple clicks correctly', async () => {
      const user = userEvent.setup();
      const mockOnClick = vi.fn();

      render(
        <PokerCard
          value="1"
          isSelected={false}
          onClick={mockOnClick}
        />
      );

      const card = screen.getByRole('button');
      await user.click(card);
      await user.click(card);
      await user.click(card);

      expect(mockOnClick).toHaveBeenCalledTimes(3);
    });

    it('should be focusable', () => {
      render(
        <PokerCard
          value="3"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      card.focus();
      
      expect(document.activeElement).toBe(card);
    });
  });

  describe('Accessibility', () => {
    it('should have proper focus styles', () => {
      render(
        <PokerCard
          value="5"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card).toHaveClass('focus:outline-none');
      expect(card).toHaveClass('focus:ring-2');
      expect(card).toHaveClass('focus:ring-blue-500');
      expect(card).toHaveClass('focus:ring-offset-2');
    });

    it('should be keyboard accessible', () => {
      render(
        <PokerCard
          value="8"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      
      // Should be focusable with tab
      expect(card.tabIndex).toBe(0);
    });

    it('should have appropriate button role', () => {
      render(
        <PokerCard
          value="13"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card).toBeInTheDocument();
    });

    it('should display the card value as button text', () => {
      const cardValue = '21';
      render(
        <PokerCard
          value={cardValue}
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button', { name: cardValue });
      expect(card).toBeInTheDocument();
    });
  });

  describe('Visual States', () => {
    it('should have hover styles in default state', () => {
      render(
        <PokerCard
          value="2"
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card).toHaveClass('hover:bg-gray-50');
      expect(card).toHaveClass('hover:shadow-lg');
    });

    it('should maintain visual consistency across different values', () => {
      const testValues = ['0', '0.5', '1', '2', '3', '5', '8', '13', '20', '40', '?', '☕'];
      
      testValues.forEach(value => {
        const { unmount } = render(
          <PokerCard
            value={value}
            isSelected={false}
            onClick={() => {}}
          />
        );

        const card = screen.getByRole('button');
        
        // All cards should have consistent base styling
        expect(card).toHaveClass('w-full');
        expect(card).toHaveClass('h-24');
        expect(card).toHaveClass('rounded-lg');
        expect(card).toHaveClass('font-bold');
        expect(card).toHaveClass('text-xl');
        
        unmount();
      });
    });

    it('should handle extremely long values gracefully', () => {
      const longValue = 'Very Long Value That Might Overflow';
      
      render(
        <PokerCard
          value={longValue}
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card).toHaveClass('text-xl'); // Should maintain text size
      expect(screen.getByText(longValue)).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty string value', () => {
      render(
        <PokerCard
          value=""
          isSelected={false}
          onClick={() => {}}
        />
      );

      const card = screen.getByRole('button');
      expect(card).toBeInTheDocument();
      // Button should exist even with empty value
    });

    it('should handle null onClick gracefully', () => {
      expect(() => {
        render(
          <PokerCard
            value="5"
            isSelected={false}
            onClick={null as any}
          />
        );
      }).not.toThrow();
    });

    it('should handle undefined onClick gracefully', () => {
      expect(() => {
        render(
          <PokerCard
            value="5"
            isSelected={false}
            onClick={undefined as any}
          />
        );
      }).not.toThrow();
    });

    it('should work with selected state changes', () => {
      const { rerender } = render(
        <PokerCard
          value="8"
          isSelected={false}
          onClick={() => {}}
        />
      );

      let card = screen.getByRole('button');
      expect(card).toHaveClass('bg-white');

      rerender(
        <PokerCard
          value="8"
          isSelected={true}
          onClick={() => {}}
        />
      );

      card = screen.getByRole('button');
      expect(card).toHaveClass('bg-blue-600');

      rerender(
        <PokerCard
          value="8"
          isSelected={false}
          onClick={() => {}}
        />
      );

      card = screen.getByRole('button');
      expect(card).toHaveClass('bg-white');
    });
  });
}); 