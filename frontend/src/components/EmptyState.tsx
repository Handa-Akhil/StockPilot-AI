import React from 'react';
import { LucideIcon } from 'lucide-react';
import { Button } from './Button';

interface EmptyStateProps {
  title: string;
  description: string;
  icon?: LucideIcon;
  actionText?: string;
  onActionClick?: () => void;
  secondaryText?: string;
  onSecondaryClick?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  description,
  icon: Icon,
  actionText,
  onActionClick,
  secondaryText,
  onSecondaryClick,
}) => {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      textAlign: 'center',
      padding: 'var(--space-6) var(--space-4)',
      background: 'rgba(255, 255, 255, 0.01)',
      border: '1px dashed var(--border-subtle)',
      borderRadius: '8px',
      width: '100%',
    }}>
      {Icon && (
        <div style={{
          width: '48px',
          height: '48px',
          borderRadius: '50%',
          background: 'rgba(255, 255, 255, 0.02)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 'var(--space-4)',
          color: 'var(--text-muted)'
        }}>
          <Icon size={24} />
        </div>
      )}
      <h4 style={{
        fontSize: '16px',
        fontWeight: 600,
        color: 'var(--text-main)',
        marginBottom: 'var(--space-2)'
      }}>
        {title}
      </h4>
      <p style={{
        fontSize: '14px',
        color: 'var(--text-muted)',
        maxWidth: '320px',
        marginBottom: 'var(--space-5)',
        lineHeight: 1.5
      }}>
        {description}
      </p>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', width: '100%', maxWidth: '200px' }}>
        {actionText && onActionClick && (
          <Button onClick={onActionClick}>
            {actionText}
          </Button>
        )}
        {secondaryText && onSecondaryClick && (
          <button
            onClick={onSecondaryClick}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'var(--text-muted)',
              fontSize: '13px',
              cursor: 'pointer',
              textDecoration: 'underline',
              padding: 'var(--space-1) 0',
              transition: 'color var(--duration-hover) var(--ease-custom)'
            }}
            onMouseOver={(e) => (e.currentTarget.style.color = 'var(--text-main)')}
            onMouseOut={(e) => (e.currentTarget.style.color = 'var(--text-muted)')}
          >
            {secondaryText}
          </button>
        )}
      </div>
    </div>
  );
};
