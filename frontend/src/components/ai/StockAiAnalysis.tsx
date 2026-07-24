import React from 'react';
import { Card } from '../Card';
import { Button } from '../Button';
import { Search, BrainCircuit, Activity, BarChart2, ShieldAlert, CheckCircle2, ThumbsUp, ThumbsDown } from 'lucide-react';

export interface StockAnalysisData {
  symbol: string;
  name: string;
  aiSummary: string;
  technicalOutlook: string;
  fundamentalOutlook: string;
  riskFactors: string[];
  keyInsights: string[];
  suggestedAction: string;
  confidence: number;
  businessSummary?: string;
  bullishFactors?: string[];
  bearishFactors?: string[];
  technicalInterpretation?: string;
  fundamentalInterpretation?: string;
  confidenceExplanation?: string;
}

interface StockAiAnalysisProps {
  analysisData: StockAnalysisData | null;
  loading: boolean;
  onSearchSymbol: (symbol: string) => void;
}

export const StockAiAnalysis: React.FC<StockAiAnalysisProps> = ({
  analysisData,
  loading,
  onSearchSymbol,
}) => {
  const [inputSymbol, setInputSymbol] = React.useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (inputSymbol.trim()) {
      onSearchSymbol(inputSymbol.trim().toUpperCase());
    }
  };

  return (
    <Card elevation={1} style={{ padding: 'var(--space-5)', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      {/* Header Search input */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
          <BrainCircuit size={18} style={{ color: 'var(--indigo-light)' }} />
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'var(--font-display)', margin: 0 }}>
              Single-Ticker AI Deep Dive
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--text-3)', margin: 0 }}>
              Technical, fundamental, and Gemini multi-factor diagnostics
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 'var(--space-2)' }}>
          <input
            type="text"
            placeholder="Search e.g. NVDA, TSLA..."
            value={inputSymbol}
            onChange={(e) => setInputSymbol(e.target.value)}
            className="input-base"
            style={{ width: '180px', height: '36px', fontSize: '13px' }}
          />
          <Button type="submit" loading={loading} style={{ padding: '0 12px', height: '36px' }}>
            <Search size={14} />
          </Button>
        </form>
      </div>

      {loading ? (
        <div style={{ padding: 'var(--space-6) 0', textAlign: 'center', color: 'var(--text-3)', fontSize: '13px' }}>
          Generating deep-dive AI report for {inputSymbol || 'symbol'}...
        </div>
      ) : analysisData ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          {/* Header Banner */}
          <div style={{
            background: 'var(--surface-2)',
            padding: 'var(--space-4)',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 'var(--space-3)'
          }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 'var(--space-2)' }}>
                <h2 style={{ fontSize: '22px', fontWeight: 800, fontFamily: 'var(--font-mono)', margin: 0, color: 'var(--indigo-light)' }}>
                  {analysisData.symbol}
                </h2>
                <span style={{ fontSize: '14px', color: 'var(--text-2)' }}>{analysisData.name}</span>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-2)', marginTop: '4px', margin: 0, lineHeight: 1.5 }}>
                {analysisData.businessSummary || analysisData.aiSummary}
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
              <span style={{ fontSize: '10px', textTransform: 'uppercase', color: 'var(--text-3)', letterSpacing: '0.05em' }}>
                Suggested Action
              </span>
              <span style={{
                padding: '4px 12px',
                borderRadius: 'var(--radius-sm)',
                fontSize: '12px',
                fontWeight: 800,
                background: 'var(--indigo-dim)',
                color: 'var(--indigo-light)',
                border: '1px solid var(--border-focus)',
                fontFamily: 'var(--font-mono)'
              }}>
                {analysisData.suggestedAction}
              </span>
            </div>
          </div>

          {/* Side-by-side Technical & Fundamental Interpretations */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 'var(--space-4)' }}>
            {/* TECHNICAL OUTLOOK */}
            <div style={{ background: 'var(--surface)', padding: 'var(--space-4)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-2)' }}>
                <Activity size={16} style={{ color: 'var(--green)' }} />
                <h4 style={{ fontSize: '13px', fontWeight: 700, margin: 0 }}>Technical Indicators & Momentum</h4>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.5, margin: 0 }}>
                {analysisData.technicalInterpretation || analysisData.technicalOutlook}
              </p>
            </div>

            {/* FUNDAMENTAL OUTLOOK */}
            <div style={{ background: 'var(--surface)', padding: 'var(--space-4)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-2)' }}>
                <BarChart2 size={16} style={{ color: 'var(--indigo-light)' }} />
                <h4 style={{ fontSize: '13px', fontWeight: 700, margin: 0 }}>Fundamental Metrics & Valuation</h4>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.5, margin: 0 }}>
                {analysisData.fundamentalInterpretation || analysisData.fundamentalOutlook}
              </p>
            </div>
          </div>

          {/* Bullish & Bearish Factors (When Gemini is active) */}
          {(analysisData.bullishFactors || analysisData.bearishFactors) && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 'var(--space-4)' }}>
              {analysisData.bullishFactors && (
                <div style={{ background: 'var(--surface)', padding: 'var(--space-4)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: 'var(--space-2)', color: 'var(--green)' }}>
                    <ThumbsUp size={14} />
                    <strong style={{ fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Bullish Catalysts</strong>
                  </div>
                  <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                    {analysisData.bullishFactors.map((factor, idx) => (
                      <li key={idx} style={{ fontSize: '12px', color: 'var(--text-2)', display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                        <span style={{ color: 'var(--green)' }}>+</span>
                        <span>{factor}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {analysisData.bearishFactors && (
                <div style={{ background: 'var(--surface)', padding: 'var(--space-4)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: 'var(--space-2)', color: 'var(--red)' }}>
                    <ThumbsDown size={14} />
                    <strong style={{ fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Bearish Risk Headwinds</strong>
                  </div>
                  <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                    {analysisData.bearishFactors.map((factor, idx) => (
                      <li key={idx} style={{ fontSize: '12px', color: 'var(--text-2)', display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                        <span style={{ color: 'var(--red)' }}>-</span>
                        <span>{factor}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}

          {/* Key Catalysts & Risk Factors */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 'var(--space-4)' }}>
            <div>
              <h4 style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 'var(--space-2)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <CheckCircle2 size={12} style={{ color: 'var(--green)' }} />
                <span>Key Insights</span>
              </h4>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                {analysisData.keyInsights.map((insight, idx) => (
                  <li key={idx} style={{ fontSize: '12px', color: 'var(--text-2)', display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                    <span style={{ color: 'var(--green)' }}>✓</span>
                    <span>{insight}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div>
              <h4 style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 'var(--space-2)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <ShieldAlert size={12} style={{ color: 'var(--amber)' }} />
                <span>Risk Factors</span>
              </h4>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                {analysisData.riskFactors.map((risk, idx) => (
                  <li key={idx} style={{ fontSize: '12px', color: 'var(--text-2)', display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
                    <span style={{ color: 'var(--amber)' }}>!</span>
                    <span>{risk}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      ) : (
        <p style={{ color: 'var(--text-3)', fontSize: '12px', textAlign: 'center', padding: 'var(--space-4) 0' }}>
          Enter a ticker symbol above to generate AI diagnostics.
        </p>
      )}
    </Card>
  );
};
