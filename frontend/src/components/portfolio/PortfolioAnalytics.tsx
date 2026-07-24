'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { Skeleton } from '@/components/Skeleton';
import { apiFetch } from '@/utils/api';
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  PieChart as PieIcon,
  Activity,
  ShieldAlert,
  Award,
  BarChart2,
  RefreshCw,
  Info
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  Legend
} from 'recharts';

interface PortfolioAnalyticsProps {
  portfolioId: number | null;
  onRefreshTrigger?: () => void;
}

const COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899'];

export const PortfolioAnalytics: React.FC<PortfolioAnalyticsProps> = ({
  portfolioId,
  onRefreshTrigger
}) => {
  const [loading, setLoading] = useState(true);
  const [performance, setPerformance] = useState<any>(null);
  const [allocation, setAllocation] = useState<any>(null);
  const [risk, setRisk] = useState<any>(null);
  const [history, setHistory] = useState<any>(null);
  const [topPerformers, setTopPerformers] = useState<any>(null);
  const [timeframe, setTimeframe] = useState('1M');

  const fetchAnalytics = useCallback(async () => {
    setLoading(true);
    try {
      const pidQuery = portfolioId ? `?portfolioId=${portfolioId}` : '';
      const pidQueryTf = portfolioId ? `?portfolioId=${portfolioId}&timeframe=${timeframe}` : `?timeframe=${timeframe}`;

      const [perfData, allocData, riskData, histData, topData] = await Promise.all([
        apiFetch(`/api/v1/portfolio/performance${pidQuery}`).catch(() => null),
        apiFetch(`/api/v1/portfolio/allocation${pidQuery}`).catch(() => null),
        apiFetch(`/api/v1/portfolio/risk${pidQuery}`).catch(() => null),
        apiFetch(`/api/v1/portfolio/history${pidQueryTf}`).catch(() => null),
        apiFetch(`/api/v1/portfolio/top-performers${pidQuery}`).catch(() => null)
      ]);

      if (perfData) setPerformance(perfData);
      if (allocData) setAllocation(allocData);
      if (riskData) setRisk(riskData);
      if (histData) setHistory(histData);
      if (topData) setTopPerformers(topData);
    } catch (err) {
      console.error('Failed to load portfolio analytics', err);
    } finally {
      setLoading(false);
    }
  }, [portfolioId, timeframe]);

  useEffect(() => {
    fetchAnalytics();
  }, [fetchAnalytics]);

  const handleRefresh = () => {
    fetchAnalytics();
    if (onRefreshTrigger) onRefreshTrigger();
  };

  if (loading && !performance) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 'var(--space-4)' }}>
          <Skeleton height="100px" />
          <Skeleton height="100px" />
          <Skeleton height="100px" />
          <Skeleton height="100px" />
        </div>
        <Skeleton height="300px" />
      </div>
    );
  }

  const assetPieData = allocation && allocation.assetAllocationValue
    ? Object.entries(allocation.assetAllocationValue).map(([name, value]) => ({
        name,
        value: Number(value)
      }))
    : [];

  const sectorPieData = allocation && allocation.sectorAllocationValue
    ? Object.entries(allocation.sectorAllocationValue).map(([name, value]) => ({
        name,
        value: Number(value)
      }))
    : [];

  const historyPoints = history && history.points ? history.points : [];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      {/* 1. TOP EXECUTIVE KPI CARDS */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 'var(--space-4)' }}>
        {/* Total Portfolio Value */}
        <Card elevation={1} style={{ padding: 'var(--space-4)', position: 'relative', overflow: 'hidden' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-3)', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>
            <span>Current Value</span>
            <DollarSign size={16} style={{ color: 'var(--indigo-light)' }} />
          </div>
          <div style={{ fontSize: '26px', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--text)', marginBottom: '4px' }}>
            ${performance ? Number(performance.currentValue).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', fontWeight: 600 }}>
            <span style={{ color: performance && performance.totalProfitLoss >= 0 ? 'var(--green)' : 'var(--red)', fontFamily: 'var(--font-mono)' }}>
              {performance && performance.totalProfitLoss >= 0 ? '+' : ''}
              ${performance ? Number(performance.totalProfitLoss).toFixed(2) : '0.00'} ({performance ? Number(performance.totalProfitLossPercent).toFixed(2) : '0.00'}%)
            </span>
            <span style={{ color: 'var(--text-3)', fontSize: '11px' }}>all-time</span>
          </div>
        </Card>

        {/* Total Investment (Cost Basis) */}
        <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-3)', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>
            <span>Total Investment</span>
            <PieIcon size={16} style={{ color: 'var(--indigo-light)' }} />
          </div>
          <div style={{ fontSize: '26px', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--text)', marginBottom: '4px' }}>
            ${performance ? Number(performance.totalInvestment).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-3)' }}>
            Invested capital across active holdings
          </div>
        </Card>

        {/* Today's Change */}
        <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-3)', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>
            <span>Today's Return</span>
            {performance && performance.todaysGainLoss >= 0 ? (
              <TrendingUp size={16} style={{ color: 'var(--green)' }} />
            ) : (
              <TrendingDown size={16} style={{ color: 'var(--red)' }} />
            )}
          </div>
          <div style={{ fontSize: '26px', fontWeight: 800, fontFamily: 'var(--font-mono)', color: performance && performance.todaysGainLoss >= 0 ? 'var(--green)' : 'var(--red)', marginBottom: '4px' }}>
            {performance && performance.todaysGainLoss >= 0 ? '+' : ''}
            ${performance ? Number(performance.todaysGainLoss).toFixed(2) : '0.00'}
          </div>
          <div style={{ fontSize: '12px', fontWeight: 600, color: performance && performance.todaysGainLoss >= 0 ? 'var(--green)' : 'var(--red)', fontFamily: 'var(--font-mono)' }}>
            {performance && performance.todaysReturnPercent >= 0 ? '+' : ''}
            {performance ? Number(performance.todaysReturnPercent).toFixed(2) : '0.00'}% today
          </div>
        </Card>

        {/* Diversity & Risk Score */}
        <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-3)', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>
            <span>Health & Diversity</span>
            <ShieldAlert size={16} style={{ color: 'var(--amber)' }} />
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginBottom: '4px' }}>
            <span style={{ fontSize: '26px', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)' }}>
              {risk ? risk.diversityScore : 50}/100
            </span>
            <span style={{
              fontSize: '10px', fontWeight: 700, padding: '2px 8px', borderRadius: '12px', textTransform: 'uppercase',
              background: risk && risk.riskLevel === 'LOW' ? 'var(--green-dim)' : (risk && risk.riskLevel === 'MODERATE' ? 'var(--indigo-dim)' : 'var(--red-dim)'),
              color: risk && risk.riskLevel === 'LOW' ? 'var(--green)' : (risk && risk.riskLevel === 'MODERATE' ? 'var(--indigo-light)' : 'var(--red)')
            }}>
              {risk ? risk.riskLevel : 'MODERATE'} RISK
            </span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-3)' }}>
            Beta: {risk ? Number(risk.portfolioBeta).toFixed(2) : '1.00'} • Max Holding: {risk ? Number(risk.topHoldingConcentrationPercent).toFixed(1) : '0.0'}%
          </div>
        </Card>
      </div>

      {/* 2. INTERACTIVE PORTFOLIO PERFORMANCE TIMELINE CHART */}
      <Card elevation={1} style={{ padding: 'var(--space-5)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-4)', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: 700, margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={18} style={{ color: 'var(--indigo-light)' }} />
              <span>Portfolio Performance History</span>
            </h3>
            <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>Total Portfolio Value vs Investment Cost Basis</span>
          </div>

          <div style={{ display: 'flex', gap: '4px', background: 'var(--surface-2)', padding: '4px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
            {['1W', '1M', '3M', '6M', '1Y', 'ALL'].map((tf) => (
              <button
                key={tf}
                onClick={() => setTimeframe(tf)}
                style={{
                  border: 'none', padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '11px', fontWeight: 600,
                  background: timeframe === tf ? 'var(--indigo-dim)' : 'transparent',
                  color: timeframe === tf ? 'var(--indigo-light)' : 'var(--text-2)'
                }}
              >
                {tf}
              </button>
            ))}
          </div>
        </div>

        <div style={{ width: '100%', height: 280 }}>
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={historyPoints} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="colorVal" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                </linearGradient>
                <linearGradient id="colorCost" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.2}/>
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <XAxis dataKey="date" stroke="var(--text-3)" fontSize={11} tickLine={false} />
              <YAxis stroke="var(--text-3)" fontSize={11} tickLine={false} tickFormatter={(v) => `$${(v / 1e3).toFixed(1)}k`} />
              <Tooltip
                contentStyle={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)', borderRadius: '8px', color: 'var(--text)' }}
                formatter={(val: any) => [`$${Number(val).toFixed(2)}`, '']}
              />
              <Area type="monotone" dataKey="portfolioValue" name="Portfolio Value" stroke="#6366f1" strokeWidth={2} fillOpacity={1} fill="url(#colorVal)" />
              <Area type="monotone" dataKey="investmentValue" name="Cost Basis" stroke="#10b981" strokeWidth={1.5} strokeDasharray="3 3" fillOpacity={1} fill="url(#colorCost)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </Card>

      {/* 3. ASSET ALLOCATION & SECTOR ALLOCATION PIE CHARTS */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 'var(--space-4)' }}>
        {/* Asset Class Allocation */}
        <Card elevation={1} style={{ padding: 'var(--space-5)' }}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, marginBottom: 'var(--space-3)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <PieIcon size={16} style={{ color: 'var(--indigo-light)' }} />
            <span>Asset Class Breakdown</span>
          </h3>
          {assetPieData.length > 0 ? (
            <div style={{ width: '100%', height: 220 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={assetPieData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={4} dataKey="value">
                    {assetPieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(val: any) => [`$${Number(val).toFixed(2)}`, 'Value']} />
                  <Legend wrapperStyle={{ fontSize: '11px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div style={{ color: 'var(--text-3)', fontSize: '12px', textAlign: 'center', padding: 'var(--space-5)' }}>No asset allocation data available.</div>
          )}
        </Card>

        {/* Sector Allocation */}
        <Card elevation={1} style={{ padding: 'var(--space-5)' }}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, marginBottom: 'var(--space-3)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <BarChart2 size={16} style={{ color: 'var(--green)' }} />
            <span>Sector Diversification</span>
          </h3>
          {sectorPieData.length > 0 ? (
            <div style={{ width: '100%', height: 220 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={sectorPieData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={4} dataKey="value">
                    {sectorPieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[(index + 2) % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(val: any) => [`$${Number(val).toFixed(2)}`, 'Value']} />
                  <Legend wrapperStyle={{ fontSize: '11px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div style={{ color: 'var(--text-3)', fontSize: '12px', textAlign: 'center', padding: 'var(--space-5)' }}>No sector allocation data available.</div>
          )}
        </Card>
      </div>

      {/* 4. TOP PERFORMERS & WORST PERFORMERS CARDS */}
      {topPerformers && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 'var(--space-4)' }}>
          {/* Top Performers */}
          <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--green)', fontWeight: 700, fontSize: '13px', marginBottom: 'var(--space-3)' }}>
              <Award size={16} />
              <span>TOP PERFORMING ASSETS</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              {topPerformers.topPerformers && topPerformers.topPerformers.length > 0 ? (
                topPerformers.topPerformers.map((h: any) => (
                  <div key={h.symbol} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 12px', background: 'var(--surface)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                    <div>
                      <strong style={{ fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)' }}>{h.symbol}</strong>
                      <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-3)' }}>${Number(h.currentPrice).toFixed(2)} / unit</span>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <span style={{ color: 'var(--green)', fontWeight: 700, fontFamily: 'var(--font-mono)', fontSize: '13px' }}>
                        +{Number(h.unrealizedPlPercent).toFixed(2)}%
                      </span>
                      <span style={{ display: 'block', fontSize: '11px', color: 'var(--green)' }}>+${Number(h.unrealizedPl).toFixed(2)}</span>
                    </div>
                  </div>
                ))
              ) : (
                <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>No asset holdings found.</span>
              )}
            </div>
          </Card>

          {/* Worst Performers */}
          <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--red)', fontWeight: 700, fontSize: '13px', marginBottom: 'var(--space-3)' }}>
              <TrendingDown size={16} />
              <span>UNDERPERFORMING ASSETS</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              {topPerformers.worstPerformers && topPerformers.worstPerformers.length > 0 ? (
                topPerformers.worstPerformers.map((h: any) => (
                  <div key={h.symbol} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 12px', background: 'var(--surface)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                    <div>
                      <strong style={{ fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)' }}>{h.symbol}</strong>
                      <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-3)' }}>${Number(h.currentPrice).toFixed(2)} / unit</span>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <span style={{ color: h.unrealizedPl >= 0 ? 'var(--green)' : 'var(--red)', fontWeight: 700, fontFamily: 'var(--font-mono)', fontSize: '13px' }}>
                        {Number(h.unrealizedPlPercent) >= 0 ? '+' : ''}{Number(h.unrealizedPlPercent).toFixed(2)}%
                      </span>
                      <span style={{ display: 'block', fontSize: '11px', color: h.unrealizedPl >= 0 ? 'var(--green)' : 'var(--red)' }}>
                        {Number(h.unrealizedPl) >= 0 ? '+' : ''}${Number(h.unrealizedPl).toFixed(2)}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>No asset holdings found.</span>
              )}
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};
