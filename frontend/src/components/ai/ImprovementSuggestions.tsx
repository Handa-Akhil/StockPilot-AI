import React from 'react';
import { Card } from '../Card';
import { Button } from '../Button';
import { Sliders, ArrowRight, AlertCircle, AlertTriangle, Info } from 'lucide-react';

export interface ImprovementSuggestion {
  title: string;
  category: string;
  description: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  actionText: string;
}

interface ImprovementSuggestionsProps {
  suggestions: ImprovementSuggestion[];
  onActionClick?: (suggestion: ImprovementSuggestion) => void;
}

export const ImprovementSuggestions: React.FC<ImprovementSuggestionsProps> = ({
  suggestions,
  onActionClick,
}) => {
  const getPriorityBadge = (priority: string) => {
    switch (priority.toUpperCase()) {
      case 'HIGH':
        return { icon: AlertTriangle, color: 'var(--red)', bg: 'var(--red-dim)' };
      case 'MEDIUM':
        return { icon: AlertCircle, color: 'var(--amber)', bg: 'var(--amber-dim)' };
      default:
        return { icon: Info, color: 'var(--indigo-light)', bg: 'var(--indigo-dim)' };
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
        <Sliders size={18} style={{ color: 'var(--indigo-light)' }} />
        <h3 style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'var(--font-display)', margin: 0 }}>
          Portfolio Optimization & Improvement Roadmap
        </h3>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 'var(--space-4)' }}>
        {suggestions.map((item, idx) => {
          const priorityInfo = getPriorityBadge(item.priority);
          const IconComponent = priorityInfo.icon;

          return (
            <Card key={idx} elevation={1} style={{ padding: 'var(--space-4)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', gap: 'var(--space-3)' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-2)' }}>
                  <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    {item.category}
                  </span>

                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    padding: '2px 6px',
                    borderRadius: '4px',
                    background: priorityInfo.bg,
                    color: priorityInfo.color,
                    fontSize: '10px',
                    fontWeight: 800
                  }}>
                    <IconComponent size={10} />
                    <span>{item.priority.toUpperCase()} PRIORITY</span>
                  </div>
                </div>

                <h4 style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text)', margin: '0 0 6px 0' }}>
                  {item.title}
                </h4>

                <p style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.5, margin: 0 }}>
                  {item.description}
                </p>
              </div>

              <div style={{ paddingTop: 'var(--space-2)' }}>
                <Button
                  variant="secondary"
                  onClick={() => onActionClick && onActionClick(item)}
                  style={{ width: '100%', fontSize: '12px', padding: '6px 12px' }}
                >
                  <span>{item.actionText}</span>
                  <ArrowRight size={12} />
                </Button>
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
};
