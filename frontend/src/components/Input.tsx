import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className = '', label, error, id, ...props }, ref) => {
    const errorBorderClass = error ? 'border-danger' : '';

    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)', width: '100%' }}>
        {label && (
          <label htmlFor={id} className="label-base">
            {label}
          </label>
        )}
        <input
          id={id}
          ref={ref}
          className={`input-base ${errorBorderClass} ${className}`}
          {...props}
        />
        {error && (
          <span style={{ fontSize: '12px', color: 'var(--danger)', marginTop: '2px' }}>
            {error}
          </span>
        )}
        <style jsx>{`
          .border-danger {
            border-color: var(--danger) !important;
          }
          .border-danger:focus {
            box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.2) !important;
          }
        `}</style>
      </div>
    );
  }
);

Input.displayName = 'Input';
