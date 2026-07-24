import React from 'react';
import { Card } from '../Card';
import { Button } from '../Button';
import { Target, Zap, Shield, ArrowUpRight } from 'lucide-react';

export interface Recommendation {
  symbol: string;
  action: 'BUY' | 'HOLD' | 'SELL';
  confidence: number;
  explanation: string;
  expectedImpact: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
}

interface AiRecommendationsProps {
  recommendations: Recommendation[];
  onSelectSymbol?: (symbol: string) => void;
}

export const AiRecommendations: React.FC<AiRecommendationsProps> = ({
  recommendations,
  onSelectSymbol,
}) => {
  const getActionBadgeStyle = (action: string) => {
    switch (action.toUpperCase()) {
      case 'BUY':
        return { bg: 'var(--green-dim)', color: 'var(--green)', border: 'rgba(16,185,129,0.3)' };
      case 'SELL':
        return { bg: 'var(--red-dim)', color: 'var(--red)', border: 'rgba(244,63,94,0.3)' };
      default:
        return { bg: 'var(--amber-dim)', color: 'var(--amber)', border: 'rgba(245,158,11,0.3)' };
    }
  };

  const getRiskBadgeStyle = (risk: string) => {
    switch (risk.toUpperCase()) {
      case 'LOW':
        return { color: 'var(--green)' };
      case 'HIGH':
        return { color: 'var(--red)' };
      default:
        return { color: 'var(--amber)' };
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'var(--font-display)', margin: 0 }}>
          AI Trade & Position Recommendations
        </h3>
        <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>
          {recommendations.length} Actionable Insight{recommendations.length !== 1 ? 's' : ''}
        </span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 'var(--space-4)' }}>
        {recommendations.map((rec, idx) => {
          const actionStyle = getActionBadgeStyle(rec.action);
          const riskStyle = getRiskBadgeStyle(rec.riskLevel);

          return (
            <Card key={idx} elevation={1} hoverable style={{ padding: 'var(--space-4)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', gap: 'var(--space-3)' }}>
              <div>
                {/* Header info */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-3)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <strong style={{ fontSize: '16px', fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)' }}>
                      {rec.symbol}
                    </strong>
                    <span style={{
                      padding: '2px 8px',
                      borderRadius: '4px',
                      fontSize: '11px',
                      fontWeight: 800,
                      backgroundColor: actionStyle.bg,
                      color: actionStyle.color,
                      border: `1px solid ${actionStyle.border}`,
                      fontFamily: 'var(--font-mono)'
                    }}>
                      {rec.action}
                    </span>
                  </div>

                  <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-3)', fontFamily: 'var(--font-mono)' }}>
                    {rec.confidence.toFixed(1)}% Confidence
                  </div>
                </div>

                {/* Explanation text */}
                <p style={{ fontSize: '13px', color: 'var(--text-2)', lineHeight: 1.5, margin: 0 }}>
                  {rec.explanation}
                </p>
              </div>

              {/* Impact & Risk metrics */}
              <div style={{ paddingTop: 'var(--space-3)', borderTop: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--indigo-light)', fontWeight: 600 }}>
                  <Zap size={12} />
                  <span>{rec.expectedImpact}</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--text-3)' }}>
                  <Shield size={12} style={{ color: riskStyle.color }} />
                  <span>Risk: <strong style={{ color: riskStyle.color }}>{rec.riskLevel}</strong></span>
                </div>

                {onSelectSymbol && (
                  <Button
                    variant="secondary"
                    onClick={() => onSelectSymbol(rec.symbol)}
                    style={{ padding: '4px 8px', fontSize: '11px' }}
                  >
                    <span>Analyze</span>
                    <ArrowUpRight size={12} />
                  </Button>
                )}
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
};
