import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger';
  loading?: boolean;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className = '', variant = 'primary', loading = false, disabled, children, ...props }, ref) => {
    const variantClass = `btn-${variant}`;
    const isLoadingOrDisabled = loading || disabled;

    return (
      <button
        ref={ref}
        disabled={isLoadingOrDisabled}
        className={`btn-base ${variantClass} ${className}`}
        {...props}
      >
        {loading ? (
          <>
            <span style={{
              width: '14px', 
              height: '14px', 
              border: '2px solid rgba(255,255,255,0.2)', 
              borderTopColor: 'currentColor', 
              borderRadius: '50%', 
              animation: 'spin 0.8s linear infinite',
              display: 'inline-block'
            }} />
            <span>Loading...</span>
            <style jsx global>{`
              @keyframes spin {
                to { transform: rotate(360deg); }
              }
            `}</style>
          </>
        ) : children}
      </button>
    );
  }
);

Button.displayName = 'Button';
