import React from 'react';
import { Card } from '../Card';
import { Compass, TrendingUp, TrendingDown, Minus, ExternalLink, Globe, Layers, Zap } from 'lucide-react';

export interface NewsHighlight {
  title: string;
  source: string;
  url: string;
  publishedAt: string;
}

interface MarketSentimentProps {
  sentiment: 'BULLISH' | 'NEUTRAL' | 'BEARISH' | string;
  confidence: number;
  summary: string;
  supportingReasons: string[];
  newsHighlights: NewsHighlight[];
  dailyMarketSummary?: string;
  sectorRotation?: string;
  macroeconomicCommentary?: string;
  riskOutlook?: string;
  investmentOpportunities?: string[];
}

export const MarketSentiment: React.FC<MarketSentimentProps> = ({
  sentiment,
  confidence,
  summary,
  supportingReasons,
  newsHighlights,
  dailyMarketSummary,
  sectorRotation,
  macroeconomicCommentary,
  riskOutlook,
  investmentOpportunities,
}) => {
  const getSentimentBadge = (sent: string) => {
    switch (sent.toUpperCase()) {
      case 'BULLISH':
        return { icon: TrendingUp, color: 'var(--green)', bg: 'var(--green-dim)', border: 'rgba(16,185,129,0.3)' };
      case 'BEARISH':
        return { icon: TrendingDown, color: 'var(--red)', bg: 'var(--red-dim)', border: 'rgba(244,63,94,0.3)' };
      default:
        return { icon: Minus, color: 'var(--amber)', bg: 'var(--amber-dim)', border: 'rgba(245,158,11,0.3)' };
    }
  };

  const badgeInfo = getSentimentBadge(sentiment);
  const IconComponent = badgeInfo.icon;

  return (
    <Card elevation={1} style={{ padding: 'var(--space-5)', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
          <Compass size={18} style={{ color: 'var(--indigo-light)' }} />
          <h3 style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'var(--font-display)', margin: 0 }}>
            Macro Market Sentiment Analysis
          </h3>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '4px 12px',
            borderRadius: 'var(--radius-full)',
            background: badgeInfo.bg,
            border: `1px solid ${badgeInfo.border}`,
            color: badgeInfo.color,
            fontSize: '12px',
            fontWeight: 800,
            fontFamily: 'var(--font-mono)'
          }}>
            <IconComponent size={14} />
            <span>{sentiment.toUpperCase()}</span>
          </div>

          <span style={{ fontSize: '12px', color: 'var(--text-3)', fontFamily: 'var(--font-mono)' }}>
            {confidence.toFixed(1)}% Confidence
          </span>
        </div>
      </div>

      <p style={{ fontSize: '13px', color: 'var(--text-2)', lineHeight: 1.6, margin: 0, background: 'var(--surface-2)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
        {dailyMarketSummary || summary}
      </p>

      {/* GEMINI SECTOR & MACRO COMMENTARY */}
      {(sectorRotation || macroeconomicCommentary) && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
          {sectorRotation && (
            <div style={{ background: 'var(--surface)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px', color: 'var(--indigo-light)', fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                <Layers size={12} />
                <span>Sector Rotation Analysis</span>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-2)', margin: 0, lineHeight: 1.4 }}>
                {sectorRotation}
              </p>
            </div>
          )}

          {macroeconomicCommentary && (
            <div style={{ background: 'var(--surface)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px', color: 'var(--green)', fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                <Zap size={12} />
                <span>Macro & Fed Policy Outlook</span>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-2)', margin: 0, lineHeight: 1.4 }}>
                {macroeconomicCommentary}
              </p>
            </div>
          )}
        </div>
      )}

      {/* SUPPORTING DRIVERS */}
      <div>
        <h4 style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 'var(--space-2)' }}>
          Key Market Drivers
        </h4>
        <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
          {(investmentOpportunities || supportingReasons).map((reason, idx) => (
            <li key={idx} style={{ fontSize: '12px', color: 'var(--text-2)', display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
              <span style={{ color: 'var(--indigo-light)', fontWeight: 800 }}>•</span>
              <span>{reason}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* NEWS SUMMARY HIGHLIGHTS */}
      {newsHighlights && newsHighlights.length > 0 && (
        <div style={{ paddingTop: 'var(--space-3)', borderTop: '1px solid var(--border)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: 'var(--space-3)' }}>
            <Globe size={14} style={{ color: 'var(--indigo-light)' }} />
            <h4 style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em', margin: 0 }}>
              AI Macro Intelligence Headlines
            </h4>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: 'var(--space-3)' }}>
            {newsHighlights.map((news, idx) => (
              <a
                key={idx}
                href={news.url}
                target="_blank"
                rel="noopener noreferrer"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  padding: 'var(--space-3)',
                  background: 'var(--surface)',
                  border: '1px solid var(--border)',
                  borderRadius: 'var(--radius-sm)',
                  textDecoration: 'none',
                  transition: 'all var(--duration-hover)'
                }}
                onMouseOver={(e) => (e.currentTarget.style.borderColor = 'var(--border-bright)')}
                onMouseOut={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
              >
                <strong style={{ fontSize: '12px', color: 'var(--text)', lineHeight: 1.4, marginBottom: '6px' }}>
                  {news.title}
                </strong>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '10px', color: 'var(--text-3)' }}>
                  <span>{news.source}</span>
                  <ExternalLink size={10} style={{ color: 'var(--indigo-light)' }} />
                </div>
              </a>
            ))}
          </div>
        </div>
      )}
    </Card>
  );
};
