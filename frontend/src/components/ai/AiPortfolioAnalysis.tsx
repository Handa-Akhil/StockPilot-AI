import React from 'react';
import { Card } from '../Card';
import { CheckCircle, AlertTriangle, TrendingUp, ShieldX, PieChart } from 'lucide-react';

interface AiPortfolioAnalysisProps {
  strengths: string[];
  weaknesses: string[];
  opportunities: string[];
  risks: string[];
  diversificationAnalysis: string;
}

export const AiPortfolioAnalysis: React.FC<AiPortfolioAnalysisProps> = ({
  strengths,
  weaknesses,
  opportunities,
  risks,
  diversificationAnalysis,
}) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <h3 style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'var(--font-display)', margin: 0 }}>
        AI Portfolio SWOT Analysis
      </h3>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 'var(--space-4)' }}>
        {/* STRENGTHS */}
        <Card elevation={1} style={{ padding: 'var(--space-4)', borderTop: '3px solid var(--green)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
            <CheckCircle size={16} style={{ color: 'var(--green)' }} />
            <h4 style={{ fontSize: '14px', fontWeight: 700, color: 'var(--green)', margin: 0 }}>Strengths</h4>
          </div>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            {strengths.map((item, index) => (
              <li key={index} style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.5, display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                <span style={{ color: 'var(--green)', fontWeight: 800 }}>•</span>
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </Card>

        {/* WEAKNESSES */}
        <Card elevation={1} style={{ padding: 'var(--space-4)', borderTop: '3px solid var(--amber)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
            <AlertTriangle size={16} style={{ color: 'var(--amber)' }} />
            <h4 style={{ fontSize: '14px', fontWeight: 700, color: 'var(--amber)', margin: 0 }}>Weaknesses</h4>
          </div>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            {weaknesses.map((item, index) => (
              <li key={index} style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.5, display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                <span style={{ color: 'var(--amber)', fontWeight: 800 }}>•</span>
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </Card>

        {/* OPPORTUNITIES */}
        <Card elevation={1} style={{ padding: 'var(--space-4)', borderTop: '3px solid var(--indigo-light)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
            <TrendingUp size={16} style={{ color: 'var(--indigo-light)' }} />
            <h4 style={{ fontSize: '14px', fontWeight: 700, color: 'var(--indigo-light)', margin: 0 }}>Opportunities</h4>
          </div>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            {opportunities.map((item, index) => (
              <li key={index} style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.5, display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                <span style={{ color: 'var(--indigo-light)', fontWeight: 800 }}>•</span>
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </Card>

        {/* RISKS */}
        <Card elevation={1} style={{ padding: 'var(--space-4)', borderTop: '3px solid var(--red)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
            <ShieldX size={16} style={{ color: 'var(--red)' }} />
            <h4 style={{ fontSize: '14px', fontWeight: 700, color: 'var(--red)', margin: 0 }}>Risks</h4>
          </div>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            {risks.map((item, index) => (
              <li key={index} style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.5, display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                <span style={{ color: 'var(--red)', fontWeight: 800 }}>•</span>
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </Card>
      </div>

      {/* DIVERSIFICATION COMMENTARY */}
      <Card elevation={1} style={{ padding: 'var(--space-4)', background: 'var(--surface-2)', display: 'flex', alignItems: 'flex-start', gap: 'var(--space-3)' }}>
        <PieChart size={18} style={{ color: 'var(--indigo-light)', flexShrink: 0, marginTop: '2px' }} />
        <div>
          <h4 style={{ fontSize: '13px', fontWeight: 700, margin: '0 0 4px 0' }}>Diversification Assessment</h4>
          <p style={{ fontSize: '12px', color: 'var(--text-2)', margin: 0, lineHeight: 1.5 }}>
            {diversificationAnalysis}
          </p>
        </div>
      </Card>
    </div>
  );
};
