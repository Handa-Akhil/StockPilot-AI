import React from 'react';
import { Card } from '../Card';
import { Sparkles, ShieldAlert, PieChart, Activity, CheckCircle2, FileText, Compass } from 'lucide-react';

interface AiOverviewCardProps {
  healthScore: number;
  overallRiskLevel: string;
  diversificationScore: number;
  confidenceScore: number;
  portfolioSummary?: string;
  investmentCommentary?: string;
  longTermOutlook?: string;
  shortTermOutlook?: string;
  riskAnalysis?: string;
}

export const AiOverviewCard: React.FC<AiOverviewCardProps> = ({
  healthScore,
  overallRiskLevel,
  diversificationScore,
  confidenceScore,
  portfolioSummary,
  investmentCommentary,
  longTermOutlook,
  shortTermOutlook,
  riskAnalysis,
}) => {
  const getRiskColor = (risk: string) => {
    switch (risk.toUpperCase()) {
      case 'LOW':
        return 'var(--green)';
      case 'MODERATE':
        return 'var(--indigo-light)';
      case 'HIGH':
        return 'var(--amber)';
      case 'AGGRESSIVE':
        return 'var(--red)';
      default:
        return 'var(--indigo-light)';
    }
  };

  return (
    <Card elevation={2} style={{ padding: 'var(--space-5)', background: 'linear-gradient(135deg, rgba(99,102,241,0.06) 0%, rgba(12,12,16,0.95) 100%)', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
          <div style={{
            background: 'var(--indigo)',
            borderRadius: 'var(--radius-sm)',
            padding: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--shadow-glow)'
          }}>
            <Sparkles size={18} style={{ color: '#fff' }} />
          </div>
          <div>
            <h2 style={{ fontSize: '18px', fontWeight: 800, fontFamily: 'var(--font-display)', margin: 0 }}>
              AI Portfolio Executive Summary
            </h2>
            <p style={{ fontSize: '12px', color: 'var(--text-3)', margin: 0 }}>
              Real-time multi-dimensional portfolio intelligence & Gemini narratives
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', background: 'var(--indigo-dim)', border: '1px solid var(--border-focus)', padding: '4px 12px', borderRadius: 'var(--radius-full)' }}>
          <CheckCircle2 size={12} style={{ color: 'var(--indigo-light)' }} />
          <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--indigo-light)', fontFamily: 'var(--font-mono)' }}>
            AI Confidence: {confidenceScore.toFixed(1)}%
          </span>
        </div>
      </div>

      {/* METRIC DIALS GRID */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 'var(--space-4)' }}>
        {/* 1. HEALTH SCORE */}
        <div style={{ background: 'var(--surface-2)', padding: 'var(--space-4)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-2)' }}>
            <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Portfolio Health
            </span>
            <Activity size={14} style={{ color: 'var(--green)' }} />
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px' }}>
            <span style={{ fontSize: '32px', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--text)' }}>
              {healthScore}
            </span>
            <span style={{ fontSize: '14px', color: 'var(--text-3)' }}>/100</span>
          </div>
          <div style={{ width: '100%', height: '4px', background: 'var(--surface-3)', borderRadius: '2px', marginTop: '8px', overflow: 'hidden' }}>
            <div style={{ width: `${healthScore}%`, height: '100%', background: healthScore > 75 ? 'var(--green)' : healthScore > 50 ? 'var(--amber)' : 'var(--red)', transition: 'width 0.5s ease' }} />
          </div>
        </div>

        {/* 2. OVERALL RISK LEVEL */}
        <div style={{ background: 'var(--surface-2)', padding: 'var(--space-4)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-2)' }}>
            <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Risk Assessment
            </span>
            <ShieldAlert size={14} style={{ color: getRiskColor(overallRiskLevel) }} />
          </div>
          <div style={{ fontSize: '24px', fontWeight: 800, fontFamily: 'var(--font-display)', color: getRiskColor(overallRiskLevel), marginTop: '4px' }}>
            {overallRiskLevel}
          </div>
          <span style={{ fontSize: '11px', color: 'var(--text-3)', display: 'block', marginTop: '4px' }}>
            Calculated via asset volatility metrics
          </span>
        </div>

        {/* 3. DIVERSIFICATION SCORE */}
        <div style={{ background: 'var(--surface-2)', padding: 'var(--space-4)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-2)' }}>
            <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Diversification
            </span>
            <PieChart size={14} style={{ color: 'var(--indigo-light)' }} />
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px' }}>
            <span style={{ fontSize: '32px', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--text)' }}>
              {diversificationScore}
            </span>
            <span style={{ fontSize: '14px', color: 'var(--text-3)' }}>/100</span>
          </div>
          <div style={{ width: '100%', height: '4px', background: 'var(--surface-3)', borderRadius: '2px', marginTop: '8px', overflow: 'hidden' }}>
            <div style={{ width: `${diversificationScore}%`, height: '100%', background: 'var(--indigo-light)', transition: 'width 0.5s ease' }} />
          </div>
        </div>
      </div>

      {/* GEMINI NARRATIVE INSIGHTS (When present) */}
      {(portfolioSummary || investmentCommentary) && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)', paddingTop: 'var(--space-3)', borderTop: '1px solid var(--border)' }}>
          {portfolioSummary && (
            <div style={{ background: 'var(--surface-2)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px', color: 'var(--indigo-light)', fontSize: '12px', fontWeight: 700 }}>
                <FileText size={14} />
                <span>Executive Portfolio Summary</span>
              </div>
              <p style={{ fontSize: '13px', color: 'var(--text-2)', lineHeight: 1.5, margin: 0 }}>
                {portfolioSummary}
              </p>
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: 'var(--space-3)' }}>
            {shortTermOutlook && (
              <div style={{ background: 'var(--surface)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                <strong style={{ fontSize: '11px', color: 'var(--amber)', textTransform: 'uppercase', letterSpacing: '0.05em', display: 'block', marginBottom: '2px' }}>
                  Near-Term (3-6 Month) Outlook
                </strong>
                <p style={{ fontSize: '12px', color: 'var(--text-2)', margin: 0, lineHeight: 1.4 }}>
                  {shortTermOutlook}
                </p>
              </div>
            )}

            {longTermOutlook && (
              <div style={{ background: 'var(--surface)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                <strong style={{ fontSize: '11px', color: 'var(--green)', textTransform: 'uppercase', letterSpacing: '0.05em', display: 'block', marginBottom: '2px' }}>
                  Multi-Year Long-Term Outlook
                </strong>
                <p style={{ fontSize: '12px', color: 'var(--text-2)', margin: 0, lineHeight: 1.4 }}>
                  {longTermOutlook}
                </p>
              </div>
            )}
          </div>

          {investmentCommentary && (
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px', fontSize: '12px', color: 'var(--text-3)', fontStyle: 'italic', background: 'rgba(99,102,241,0.04)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px dashed var(--border-bright)' }}>
              <Compass size={14} style={{ color: 'var(--indigo-light)', flexShrink: 0, marginTop: '2px' }} />
              <span>Advisor Note: {investmentCommentary}</span>
            </div>
          )}
        </div>
      )}
    </Card>
  );
};
