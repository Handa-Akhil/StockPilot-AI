import React from 'react';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  elevation?: 0 | 1 | 2 | 3;
  hoverable?: boolean;
}

export const Card = React.forwardRef<HTMLDivElement, CardProps>(
  ({ className = '', elevation = 1, hoverable = false, children, ...props }, ref) => {
    const elevationClass = `elevation-${elevation}`;
    const hoverClass = hoverable ? 'card-hoverable' : '';
    
    return (
      <div
        ref={ref}
        className={`card-base ${elevationClass} ${hoverClass} ${className}`}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = 'Card';
