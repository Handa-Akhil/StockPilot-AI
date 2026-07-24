'use client';

import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch, clearTokens, getTokens } from '@/utils/api';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { Input } from '@/components/Input';
import { Skeleton } from '@/components/Skeleton';
import { EmptyState } from '@/components/EmptyState';
import { 
  TrendingUp, 
  TrendingDown, 
  Plus, 
  Search, 
  LogOut, 
  RefreshCw, 
  Briefcase, 
  History, 
  DollarSign, 
  Newspaper,
  Trash2,
  AlertTriangle,
  LayoutDashboard,
  Settings,
  BrainCircuit,
  LineChart,
  Bell,
  ChevronRight,
  Sparkles,
  PieChart,
  X,
  Globe,
  Building,
  Users,
  ExternalLink,
  Flame,
  Award
} from 'lucide-react';
import styles from './dashboard.module.css';
import { AiOverviewCard } from '@/components/ai/AiOverviewCard';
import { AiPortfolioAnalysis } from '@/components/ai/AiPortfolioAnalysis';
import { AiRecommendations } from '@/components/ai/AiRecommendations';
import { MarketSentiment } from '@/components/ai/MarketSentiment';
import { StockAiAnalysis, StockAnalysisData } from '@/components/ai/StockAiAnalysis';
import { ImprovementSuggestions } from '@/components/ai/ImprovementSuggestions';
import { NotificationBell } from '@/components/notifications/NotificationBell';
import { AlertList } from '@/components/alerts/AlertList';
import { AlertModal, PriceAlert } from '@/components/alerts/AlertModal';
import { NotificationSettingsModal } from '@/components/settings/NotificationSettingsModal';
import { PortfolioAnalytics } from '@/components/portfolio/PortfolioAnalytics';

interface UserProfile {
  id: number;
  email: string;
  name: string;
  createdAt: string;
}

interface Portfolio {
  id: number;
  name: string;
  createdAt: string;
}

interface Holding {
  symbol: string;
  assetClass: string;
  quantity: number;
  averageBuyPrice: number;
  costBasis: number;
  currentPrice: number;
  currentValue: number;
  unrealizedPl: number;
  unrealizedPlPercent: number;
  allocationPercent: number;
}

interface PortfolioMetrics {
  totalCostBasis: number;
  currentValue: number;
  unrealizedPl: number;
  unrealizedPlPercent: number;
  realizedPl: number;
}

interface PortfolioSummary {
  portfolioId: number;
  portfolioName: string;
  metrics: PortfolioMetrics;
  holdings: Holding[];
}

interface Transaction {
  id: number;
  portfolioId: number;
  symbol: string;
  assetClass: string;
  transactionType: string;
  quantity: number;
  price: number;
  realizedPl: number | null;
  transactionTime: string;
}

interface StockQuote {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
  pe?: number;
  eps?: number;
  marketCap?: number;
  volume?: number;
  high?: number;
  low?: number;
  dividendYield?: number;
  exchange: string;
  currency: string;
  news?: {
    title: string;
    source: string;
    url: string;
    publishedAt: string;
    summary?: string;
  }[];
}

interface CompanyProfile {
  symbol: string;
  name: string;
  sector: string;
  industry: string;
  description: string;
  ceo: string;
  website: string;
  employees: number;
  marketCap: number;
  pe: number;
  dividendYield: number;
  fiftyTwoWeekHigh: number;
  fiftyTwoWeekLow: number;
  country: string;
}

interface StockCandle {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

interface MarketIndex {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
}

interface TrendingStock {
  symbol: string;
  name: string;
  price: number;
  changePercent: number;
  volume: number;
  category: 'GAINER' | 'LOSER' | 'MOST_ACTIVE';
}

export default function DashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  // Portfolio states
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [selectedPortfolioId, setSelectedPortfolioId] = useState<number | null>(null);
  const [portfolioSummary, setPortfolioSummary] = useState<PortfolioSummary | null>(null);
  const [portfolioLoading, setPortfolioLoading] = useState(false);
  const [newPortfolioName, setNewPortfolioName] = useState('');
  
  // Transaction states
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [transactionsLoading, setTransactionsLoading] = useState(false);
  const [txPage, setTxPage] = useState(0);
  const [txTotalPages, setTxTotalPages] = useState(1);
  const [txType, setTxType] = useState<'BUY' | 'SELL'>('BUY');
  const [txSymbol, setTxSymbol] = useState('');
  const [txAssetClass, setTxAssetClass] = useState('STOCK');
  const [txQty, setTxQty] = useState('');
  const [txPrice, setTxPrice] = useState('');
  const [txError, setTxError] = useState('');
  const [txSuccess, setTxSuccess] = useState('');
  const [txLoading, setTxLoading] = useState(false);

  // Stock search & Market data states
  const [stockSearchQuery, setStockSearchQuery] = useState('');
  const [stockSearchResults, setStockSearchResults] = useState<any[]>([]);
  const [selectedStock, setSelectedStock] = useState<StockQuote | null>(null);
  const [companyProfile, setCompanyProfile] = useState<CompanyProfile | null>(null);
  const [stockHistory, setStockHistory] = useState<StockCandle[]>([]);
  const [historyRange, setHistoryRange] = useState('1mo');
  const [stockLoading, setStockLoading] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);
  const [marketIndices, setMarketIndices] = useState<MarketIndex[]>([]);
  const [trendingStocks, setTrendingStocks] = useState<TrendingStock[]>([]);
  const [marketDataLoading, setMarketDataLoading] = useState(false);

  // Navigation and dialog modals
  const [activeTab, setActiveTab] = useState<'dashboard' | 'portfolio' | 'market' | 'insights' | 'settings'>('dashboard');
  const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);

  // AI Insights states
  const [aiAnalysis, setAiAnalysis] = useState<any | null>(null);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState<string | null>(null);
  const [stockAiData, setStockAiData] = useState<StockAnalysisData | null>(null);
  const [stockAiLoading, setStockAiLoading] = useState(false);
  const [marketSentiment, setMarketSentiment] = useState<any | null>(null);
  const [marketSentimentLoading, setMarketSentimentLoading] = useState(false);

  // Price Alerts & Settings states
  const [userAlerts, setUserAlerts] = useState<PriceAlert[]>([]);
  const [alertsLoading, setAlertsLoading] = useState(false);
  const [isAlertModalOpen, setIsAlertModalOpen] = useState(false);
  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState(false);
  const [selectedAlertForEdit, setSelectedAlertForEdit] = useState<PriceAlert | null>(null);

  // Fetch Market Indices & Trending stocks
  const fetchLiveMarketData = useCallback(async () => {
    setMarketDataLoading(true);
    try {
      const [indices, trending] = await Promise.all([
        apiFetch('/api/v1/market/indices').catch(() => []),
        apiFetch('/api/v1/market/trending').catch(() => [])
      ]);
      if (indices && indices.length > 0) setMarketIndices(indices);
      if (trending && trending.length > 0) setTrendingStocks(trending);
    } catch (err) {
      console.error('Failed fetching live market data', err);
    } finally {
      setMarketDataLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchLiveMarketData();
  }, [fetchLiveMarketData]);

  const fetchUserAlerts = useCallback(async () => {
    setAlertsLoading(true);
    try {
      const data = await apiFetch('/api/v1/alerts');
      setUserAlerts(data || []);
    } catch (err) {
      console.error('Failed to fetch user alerts', err);
    } finally {
      setAlertsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUserAlerts();
  }, [fetchUserAlerts]);

  const handleSaveAlert = async (alertData: PriceAlert) => {
    if (alertData.id) {
      await apiFetch(`/api/v1/alerts/${alertData.id}`, {
        method: 'PUT',
        body: JSON.stringify(alertData),
      });
    } else {
      await apiFetch('/api/v1/alerts', {
        method: 'POST',
        body: JSON.stringify({ ...alertData, portfolioId: selectedPortfolioId }),
      });
    }
    fetchUserAlerts();
  };

  const handleToggleAlert = async (id: number) => {
    try {
      await apiFetch(`/api/v1/alerts/${id}/toggle`, { method: 'PATCH' });
      fetchUserAlerts();
    } catch (err) {
      console.error('Failed to toggle alert', err);
    }
  };

  const handleDeleteAlert = async (id: number) => {
    if (!confirm('Are you sure you want to delete this price alert?')) return;
    try {
      await apiFetch(`/api/v1/alerts/${id}`, { method: 'DELETE' });
      fetchUserAlerts();
    } catch (err) {
      console.error('Failed to delete alert', err);
    }
  };

  const fetchAiPortfolioAnalysis = useCallback(async (id: number) => {
    setAiLoading(true);
    setAiError(null);
    try {
      const data = await apiFetch(`/api/v1/ai/portfolio/${id}`);
      setAiAnalysis(data);
    } catch (err: any) {
      console.error('Failed to fetch AI portfolio analysis', err);
      setAiError(err.message || 'AI analysis service temporarily unavailable');
    } finally {
      setAiLoading(false);
    }
  }, []);

  const fetchStockAiAnalysis = useCallback(async (symbol: string) => {
    setStockAiLoading(true);
    try {
      const data = await apiFetch(`/api/v1/ai/stock/${symbol.trim().toUpperCase()}`);
      setStockAiData(data);
    } catch (err: any) {
      console.error('Failed to fetch stock AI analysis', err);
    } finally {
      setStockAiLoading(false);
    }
  }, []);

  const fetchMarketSentiment = useCallback(async () => {
    setMarketSentimentLoading(true);
    try {
      const data = await apiFetch('/api/v1/ai/sentiment');
      setMarketSentiment(data);
    } catch (err: any) {
      console.error('Failed to fetch market sentiment', err);
    } finally {
      setMarketSentimentLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'insights') {
      if (selectedPortfolioId) {
        fetchAiPortfolioAnalysis(selectedPortfolioId);
      }
      fetchMarketSentiment();
      if (!stockAiData && portfolioSummary && portfolioSummary.holdings.length > 0) {
        fetchStockAiAnalysis(portfolioSummary.holdings[0].symbol);
      } else if (!stockAiData) {
        fetchStockAiAnalysis('AAPL');
      }
    }
  }, [activeTab, selectedPortfolioId, fetchAiPortfolioAnalysis, fetchMarketSentiment, fetchStockAiAnalysis, portfolioSummary, stockAiData]);

  // Fetch portfolios list
  const fetchPortfolios = useCallback(async () => {
    try {
      const data = await apiFetch('/api/v1/portfolios');
      setPortfolios(data);
      if (data.length > 0 && selectedPortfolioId === null) {
        setSelectedPortfolioId(data[0].id);
      }
    } catch (err) {
      console.error('Failed to load portfolios', err);
    }
  }, [selectedPortfolioId]);

  // Load profile and setup
  useEffect(() => {
    const { token } = getTokens();
    if (!token) {
      router.push('/login');
      return;
    }

    const init = async () => {
      try {
        const profile = await apiFetch('/api/auth/me');
        setUser(profile);
        await fetchPortfolios();
      } catch (err) {
        clearTokens();
        router.push('/login');
      } finally {
        setLoading(false);
      }
    };

    init();
  }, [router, fetchPortfolios]);

  // Fetch selected portfolio summary details
  const fetchPortfolioSummary = useCallback(async (id: number) => {
    setPortfolioLoading(true);
    try {
      const data = await apiFetch(`/api/v1/portfolios/${id}`);
      setPortfolioSummary(data);
    } catch (err) {
      console.error(`Failed to fetch summary for portfolio ${id}`, err);
    } finally {
      setPortfolioLoading(false);
    }
  }, []);

  // Fetch paginated transactions log
  const fetchTransactions = useCallback(async (id: number, page: number) => {
    setTransactionsLoading(true);
    try {
      const data = await apiFetch(`/api/v1/portfolios/${id}/transactions?page=${page}&size=5`);
      setTransactions(data.content || []);
      setTxTotalPages(data.totalPages || 1);
    } catch (err) {
      console.error(`Failed to fetch transactions for portfolio ${id}`, err);
    } finally {
      setTransactionsLoading(false);
    }
  }, []);

  // Refresh current portfolio views
  const refreshPortfolio = useCallback(() => {
    if (selectedPortfolioId !== null) {
      fetchPortfolioSummary(selectedPortfolioId);
      fetchTransactions(selectedPortfolioId, txPage);
    }
  }, [selectedPortfolioId, txPage, fetchPortfolioSummary, fetchTransactions]);

  useEffect(() => {
    if (selectedPortfolioId !== null) {
      fetchPortfolioSummary(selectedPortfolioId);
      fetchTransactions(selectedPortfolioId, 0);
      setTxPage(0);
    } else {
      setPortfolioSummary(null);
      setTransactions([]);
    }
  }, [selectedPortfolioId, fetchPortfolioSummary, fetchTransactions]);

  // Create new portfolio
  const handleCreatePortfolio = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPortfolioName.trim()) return;

    try {
      const newPf = await apiFetch('/api/v1/portfolios', {
        method: 'POST',
        body: JSON.stringify({ name: newPortfolioName.trim() }),
      });
      setNewPortfolioName('');
      await fetchPortfolios();
      setSelectedPortfolioId(newPf.id);
    } catch (err: any) {
      alert(err.message || 'Failed to create portfolio');
    }
  };

  // Submit log transaction form
  const handleLogTransaction = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPortfolioId) return;
    setTxError('');
    setTxSuccess('');
    setTxLoading(true);

    const qty = parseFloat(txQty);
    const prc = parseFloat(txPrice);

    if (isNaN(qty) || qty <= 0) {
      setTxError('Quantity must be a positive number');
      setTxLoading(false);
      return;
    }
    if (isNaN(prc) || prc <= 0) {
      setTxError('Price must be a positive number');
      setTxLoading(false);
      return;
    }

    try {
      await apiFetch(`/api/v1/portfolios/${selectedPortfolioId}/transactions`, {
        method: 'POST',
        body: JSON.stringify({
          symbol: txSymbol.trim().toUpperCase(),
          assetClass: txAssetClass,
          transactionType: txType,
          quantity: qty,
          price: prc,
        }),
      });

      setTxSuccess(`Successfully recorded ${txType} of ${txQty} ${txSymbol.toUpperCase()}`);
      setTxSymbol('');
      setTxQty('');
      setTxPrice('');
      setIsOrderModalOpen(false);
      refreshPortfolio();
    } catch (err: any) {
      setTxError(err.message || 'Transaction recording failed');
    } finally {
      setTxLoading(false);
    }
  };

  // Handle stock lookup search term
  const handleStockSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!stockSearchQuery.trim()) return;

    setSearchLoading(true);
    try {
      const data = await apiFetch(`/api/v1/market/search?query=${encodeURIComponent(stockSearchQuery.trim())}`);
      setStockSearchResults(data || []);
      if (data && data.length > 0) {
        loadStockQuote(data[0].symbol);
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setSearchLoading(false);
    }
  };

  // Load details, company profile, and news of stock
  const loadStockQuote = async (symbol: string) => {
    setStockLoading(true);
    setSelectedStock(null);
    setCompanyProfile(null);
    setStockHistory([]);
    try {
      const [quote, profile, history] = await Promise.all([
        apiFetch(`/api/v1/market/quote/${symbol.trim().toUpperCase()}`).catch(() => null),
        apiFetch(`/api/v1/market/profile/${symbol.trim().toUpperCase()}`).catch(() => null),
        apiFetch(`/api/v1/market/history/${symbol.trim().toUpperCase()}?timeframe=${historyRange}`).catch(() => ({ points: [] }))
      ]);

      if (quote) setSelectedStock(quote);
      if (profile) setCompanyProfile(profile);
      if (history && history.points) setStockHistory(history.points);
    } catch (err: any) {
      alert(err.message || 'Stock symbol not found or service unavailable');
    } finally {
      setStockLoading(false);
    }
  };

  // Update history range on quote view
  useEffect(() => {
    if (selectedStock) {
      const fetchNewHistory = async () => {
        try {
          const history = await apiFetch(`/api/v1/market/history/${selectedStock.symbol}?timeframe=${historyRange}`);
          if (history && history.points) setStockHistory(history.points);
        } catch (err) {
          console.error(err);
        }
      };
      fetchNewHistory();
    }
  }, [historyRange, selectedStock]);

  const handleLogout = async () => {
    try {
      await apiFetch('/api/auth/logout', { method: 'POST' });
    } catch (err) {
      console.error('Logout request failed', err);
    } finally {
      clearTokens();
      localStorage.removeItem('sp_user_name');
      localStorage.removeItem('sp_user_email');
      router.push('/login');
    }
  };

  const fillTxPrice = (price: number, symbol: string) => {
    setTxSymbol(symbol);
    setTxPrice(price.toString());
    setIsOrderModalOpen(true);
  };

  // Calculate coordinates for SVG chart
  const renderSvgChart = () => {
    const width = 600;
    const height = 240;
    const padding = 15;

    const dataPoints = stockHistory.length > 5 ? stockHistory.map(c => c.close) : [100, 102, 99, 105, 110, 108, 115, 122, 120, 128, 135];
    const max = Math.max(...dataPoints);
    const min = Math.min(...dataPoints);
    const range = max - min === 0 ? 1 : max - min;

    const points = dataPoints.map((val, index) => {
      const x = padding + (index / (dataPoints.length - 1)) * (width - 2 * padding);
      const y = height - padding - ((val - min) / range) * (height - 2 * padding - 40);
      return `${x},${y}`;
    }).join(' ');

    const benchmarkPoints = dataPoints.map((val, index) => {
      const x = padding + (index / (dataPoints.length - 1)) * (width - 2 * padding);
      const valOffset = val * (1 + 0.05 * Math.sin(index / 1.5) - 0.03);
      const y = height - padding - ((valOffset - min) / range) * (height - 2 * padding - 40);
      return `${x},${y}`;
    }).join(' ');

    const fillPoints = `${padding},${height - padding} ` + points + ` ${width - padding},${height - padding}`;

    return (
      <svg width="100%" height="240" viewBox={`0 0 ${width} ${height}`} style={{ overflow: 'visible', transition: 'all var(--duration-chart) var(--ease-custom)' }}>
        <defs>
          <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--indigo)" stopOpacity="0.2" />
            <stop offset="100%" stopColor="var(--indigo)" stopOpacity="0" />
          </linearGradient>
        </defs>
        <polygon points={fillPoints} fill="url(#chartGradient)" />
        <polyline fill="none" stroke="rgba(255,255,255,0.12)" strokeWidth="1.5" strokeDasharray="4 4" points={benchmarkPoints} />
        <polyline fill="none" stroke="var(--indigo-light)" strokeWidth="2.5" points={points} strokeLinecap="round" strokeLinejoin="round" />
        <line x1={padding} y1={height - padding} x2={width - padding} y2={height - padding} stroke="rgba(255,255,255,0.04)" strokeWidth="1" />
      </svg>
    );
  };

  // Donut chart sector allocations rendering
  const renderDonutChart = () => {
    const allocations = portfolioSummary && portfolioSummary.holdings.length > 0 
      ? portfolioSummary.holdings.map(h => ({ name: h.symbol, weight: h.allocationPercent }))
      : [
          { name: 'Technology', weight: 48.2, color: 'var(--indigo)' },
          { name: 'ETFs', weight: 22.1, color: 'var(--secondary)' },
          { name: 'Communication', weight: 13.4, color: 'var(--purple)' },
          { name: 'Consumer', weight: 9.8, color: 'var(--blue)' },
          { name: 'Other', weight: 6.5, color: 'var(--amber)' }
        ];

    const colors = ['var(--indigo)', 'var(--secondary)', 'var(--purple)', 'var(--blue)', 'var(--amber)'];
    let accumulatedPercent = 0;

    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--space-4)', width: '100%' }}>
        <div style={{ position: 'relative', width: '120px', height: '120px' }}>
          <svg width="120" height="120" viewBox="0 0 42 42">
            <circle cx="21" cy="21" r="15.91549430918954" fill="transparent" stroke="rgba(255,255,255,0.02)" strokeWidth="4"></circle>
            {allocations.map((a, i) => {
              const dashArray = `${a.weight} ${100 - a.weight}`;
              const dashOffset = 100 - accumulatedPercent + 25;
              accumulatedPercent += a.weight;
              const color = a.hasOwnProperty('color') ? (a as any).color : colors[i % colors.length];

              return (
                <circle 
                  key={i}
                  cx="21" 
                  cy="21" 
                  r="15.91549430918954" 
                  fill="transparent" 
                  stroke={color} 
                  strokeWidth="4"
                  strokeDasharray={dashArray}
                  strokeDashoffset={dashOffset}
                  style={{ transition: 'all var(--duration-chart)' }}
                ></circle>
              );
            })}
          </svg>
        </div>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1 }}>
          {allocations.map((a, i) => {
            const color = a.hasOwnProperty('color') ? (a as any).color : colors[i % colors.length];
            return (
              <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: color }}></span>
                  <span style={{ color: 'var(--text-2)' }}>{a.name}</span>
                </div>
                <strong style={{ fontFamily: 'var(--font-mono)', color: 'var(--text)' }}>{a.weight.toFixed(1)}%</strong>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <div className={styles.layoutContainer}>
      {/* 1. TOP DYNAMIC LIVE MARKET INDEX TICKER MARQUEE */}
      <div className={styles.indexMarquee}>
        {(marketIndices.length > 0 ? marketIndices : [
          { symbol: '^GSPC', name: 'S&P 500', price: 5450.25, change: 12.50, changePercent: 0.23 },
          { symbol: '^IXIC', name: 'Nasdaq Composite', price: 17850.10, change: 45.20, changePercent: 0.25 },
          { symbol: '^DJI', name: 'Dow Jones', price: 39800.50, change: -15.80, changePercent: -0.04 }
        ]).map((idx, index) => (
          <div key={index} className={styles.indexItem}>
            <span className={styles.indexSymbol}>{idx.name || idx.symbol}</span>
            <span className={styles.indexPrice}>${idx.price ? idx.price.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '—'}</span>
            <span style={{ color: idx.changePercent >= 0 ? 'var(--green)' : 'var(--red)', fontWeight: 600 }}>
              {idx.changePercent >= 0 ? '+' : ''}{idx.changePercent ? idx.changePercent.toFixed(2) : '0.00'}%
            </span>
          </div>
        ))}
      </div>

      <div className={styles.shell}>
        {/* 2. LEFT SIDEBAR NAVIGATION */}
        <aside className={styles.sidebar}>
          <div>
            <div className={styles.logoArea}>
              <div className={styles.logo}>
                <div style={{
                  background: 'var(--indigo)',
                  borderRadius: '6px',
                  width: '26px',
                  height: '26px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: 'var(--shadow-glow)'
                }}>
                  <Sparkles size={14} style={{ color: '#fff' }} />
                </div>
                <span>StockPilot AI</span>
              </div>
            </div>
            
            <div className={styles.sidebarCatLabel}>MAIN</div>
            <nav className={styles.navSection}>
              <button 
                className={`${styles.navItem} ${activeTab === 'dashboard' ? styles.navItemActive : ''}`}
                onClick={() => { setActiveTab('dashboard'); }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                  <LayoutDashboard size={15} />
                  <span>Dashboard</span>
                </span>
                {activeTab === 'dashboard' && <ChevronRight size={12} style={{ color: 'var(--indigo-light)' }} />}
              </button>
              
              <button 
                className={`${styles.navItem} ${activeTab === 'portfolio' ? styles.navItemActive : ''}`}
                onClick={() => { setActiveTab('portfolio'); }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                  <Briefcase size={15} />
                  <span>Portfolio</span>
                </span>
                {activeTab === 'portfolio' && <ChevronRight size={12} />}
              </button>

              <button 
                className={`${styles.navItem} ${activeTab === 'market' ? styles.navItemActive : ''}`}
                onClick={() => { setActiveTab('market'); }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                  <LineChart size={15} />
                  <span>Market</span>
                </span>
                {activeTab === 'market' && <ChevronRight size={12} />}
              </button>

              <button 
                className={`${styles.navItem} ${activeTab === 'insights' ? styles.navItemActive : ''}`}
                onClick={() => { setActiveTab('insights'); }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                  <BrainCircuit size={15} />
                  <span>AI Insights</span>
                </span>
                <span style={{
                  fontSize: '10px',
                  fontWeight: 700,
                  background: 'var(--indigo-dim)',
                  color: 'var(--indigo-light)',
                  padding: '2px 6px',
                  borderRadius: '8px'
                }}>4</span>
              </button>

              <button 
                className={`${styles.navItem} ${activeTab === 'settings' ? styles.navItemActive : ''}`}
                onClick={() => { setActiveTab('settings'); }}
              >
                <span style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                  <Settings size={15} />
                  <span>Settings</span>
                </span>
                {activeTab === 'settings' && <ChevronRight size={12} />}
              </button>
            </nav>

            <div className={styles.sidebarCatLabel}>PORTFOLIOS</div>
            <nav className={styles.navSection} style={{ maxHeight: '200px', overflowY: 'auto' }}>
              {portfolios.map(p => (
                <button 
                  key={p.id}
                  onClick={() => { setSelectedPortfolioId(p.id); setActiveTab('dashboard'); }}
                  className={styles.navItem}
                  style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', minWidth: 0 }}>
                    <span style={{ 
                      width: '6px', 
                      height: '6px', 
                      borderRadius: '50%', 
                      backgroundColor: selectedPortfolioId === p.id ? 'var(--green)' : 'var(--indigo-light)'
                    }}></span>
                    <span style={{
                      fontWeight: selectedPortfolioId === p.id ? 600 : 500,
                      color: selectedPortfolioId === p.id ? 'var(--text)' : 'var(--text-2)',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}>{p.name}</span>
                  </div>
                  <ChevronRight size={10} style={{ opacity: selectedPortfolioId === p.id ? 0.8 : 0.3 }} />
                </button>
              ))}
            </nav>
          </div>

          <div className={styles.sidebarFooter}>
            <div className={styles.profileArea}>
              <div className={styles.avatar}>
                {user?.name ? user.name.charAt(0).toUpperCase() : 'A'}
              </div>
              <div className={styles.profileText}>
                <span className={styles.profileName}>{user?.name || 'Investor'}</span>
                <span className={styles.profileEmail}>Pro Plan</span>
              </div>
            </div>
            <button onClick={handleLogout} className={styles.logoutBtn} title="Log Out">
              <LogOut size={16} />
            </button>
          </div>
        </aside>

        {/* MOBILE HEADER */}
        <header className={styles.mobileHeader}>
          <div className={styles.logo}>
            <Sparkles size={16} style={{ color: 'var(--indigo-light)' }} />
            <span>StockPilot AI</span>
          </div>
          <button onClick={handleLogout} className={styles.logoutBtn}>
            <LogOut size={14} />
          </button>
        </header>

        {/* 3. MAIN DASHBOARD CONTENT AREA */}
        <main className={styles.mainContent}>
          
          {/* HEADER CONTROLS */}
          <section className={styles.dashHeader}>
            <div className={styles.dashHeaderLeft}>
              <h1 style={{ fontSize: '20px', fontWeight: 800, fontFamily: 'var(--font-display)', letterSpacing: '-0.02em' }}>
                Dashboard
              </h1>
            </div>
            
            {/* Live Search Input */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
              <div className={styles.searchBox}>
                <Search size={14} className={styles.searchIcon} />
                <form onSubmit={handleStockSearch}>
                  <input 
                    type="text" 
                    placeholder="Search stocks, e.g. NVDA, AAPL..." 
                    className={`${styles.searchInput} input-base`}
                    value={stockSearchQuery}
                    onChange={(e) => setStockSearchQuery(e.target.value)}
                  />
                </form>
                <span className={styles.searchKbd}>⌘K</span>
              </div>

              <NotificationBell />

              <button
                onClick={() => setIsSettingsModalOpen(true)}
                title="Notification Settings"
                style={{
                  background: 'transparent',
                  border: '1px solid var(--border)',
                  borderRadius: '50%',
                  width: '36px',
                  height: '36px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--text-2)',
                  cursor: 'pointer'
                }}
              >
                <Settings size={16} />
              </button>

              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: 'var(--indigo)',
                color: '#fff',
                fontWeight: 700,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '12px'
              }}>
                {user?.name ? user.name.charAt(0).toUpperCase() : 'A'}
              </div>
            </div>
          </section>

          {/* MAIN GRID VIEW */}
          {activeTab === 'dashboard' ? (
            <>
              {/* FOUR STATS CARDS */}
              <section className={styles.statsGrid}>
                {/* 1. PORTFOLIO VALUE */}
                <Card className={styles.statCardGlow} elevation={1}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', color: 'var(--text-3)', fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    <span>Portfolio Value</span>
                    <span style={{ color: 'var(--indigo-light)' }}>$</span>
                  </div>
                  <div style={{ fontSize: '28px', fontWeight: 700, fontFamily: 'var(--font-mono)', margin: 'var(--space-2) 0 4px 0' }}>
                    ${portfolioSummary?.metrics.currentValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--green)', fontWeight: 500 }}>
                    {portfolioSummary && portfolioSummary.metrics.unrealizedPlPercent >= 0 ? '+' : ''}
                    {portfolioSummary?.metrics.unrealizedPlPercent.toFixed(2) || '0.00'}% total return
                  </div>
                </Card>

                {/* 2. TODAY'S GAIN */}
                <Card elevation={1}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', color: 'var(--text-3)', fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    <span>Today's Gain</span>
                    <TrendingUp size={14} style={{ color: 'var(--green)' }} />
                  </div>
                  <div style={{ fontSize: '28px', fontWeight: 700, fontFamily: 'var(--font-mono)', margin: 'var(--space-2) 0 4px 0', color: (portfolioSummary?.metrics.unrealizedPl || 0) >= 0 ? 'var(--green)' : 'var(--red)' }}>
                    {(portfolioSummary?.metrics.unrealizedPl || 0) >= 0 ? '+' : ''}
                    ${portfolioSummary?.metrics.unrealizedPl.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                  </div>
                  <div style={{ fontSize: '12px', color: (portfolioSummary?.metrics.unrealizedPlPercent || 0) >= 0 ? 'var(--green)' : 'var(--red)', fontWeight: 500 }}>
                    {(portfolioSummary?.metrics.unrealizedPlPercent || 0) >= 0 ? '+' : ''}
                    {portfolioSummary?.metrics.unrealizedPlPercent.toFixed(2) || '0.00'}% today
                  </div>
                </Card>

                {/* 3. TOTAL GAIN */}
                <Card elevation={1}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', color: 'var(--text-3)', fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    <span>Total Gain</span>
                    <LineChart size={14} style={{ color: 'var(--purple)' }} />
                  </div>
                  <div style={{ fontSize: '28px', fontWeight: 700, fontFamily: 'var(--font-mono)', margin: 'var(--space-2) 0 4px 0', color: (portfolioSummary?.metrics.realizedPl || 0) >= 0 ? 'var(--green)' : 'var(--red)' }}>
                    {(portfolioSummary?.metrics.realizedPl || 0) >= 0 ? '+' : ''}
                    ${portfolioSummary?.metrics.realizedPl.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-3)', fontWeight: 500 }}>
                    on ${portfolioSummary?.metrics.totalCostBasis.toLocaleString('en-US', { maximumFractionDigits: 0 }) || '0.00'} invested
                  </div>
                </Card>

                {/* 4. CASH BALANCE */}
                <Card elevation={1}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', color: 'var(--text-3)', fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    <span>Cash Balance</span>
                    <span style={{ color: 'var(--amber)' }}>$</span>
                  </div>
                  <div style={{ fontSize: '28px', fontWeight: 700, fontFamily: 'var(--font-mono)', margin: 'var(--space-2) 0 4px 0' }}>
                    $8,240.00
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-3)', fontWeight: 500 }}>
                    {portfolioSummary?.holdings.length || 0} open positions
                  </div>
                </Card>
              </section>

              {/* DYNAMIC GRIDS: PERFORMANCE & ALLOCATION */}
              <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 'var(--space-5)' }}>
                {/* PORTFOLIO PERFORMANCE SVG */}
                <Card elevation={1} style={{ padding: 'var(--space-5)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-4)' }}>
                    <div>
                      <h3 style={{ fontSize: '15px', fontWeight: 600 }}>Portfolio Performance</h3>
                      <p style={{ fontSize: '12px', color: 'var(--text-3)', marginTop: '2px' }}>vs. S&P 500 benchmark</p>
                    </div>
                    <div style={{ display: 'flex', gap: '2px' }}>
                      {['1M', '3M', '6M', 'YTD', '1Y'].map(r => (
                        <button 
                          key={r}
                          onClick={() => setHistoryRange(r.toLowerCase())}
                          style={{
                            border: 'none', padding: '4px 8px', borderRadius: '4px', fontSize: '11px', cursor: 'pointer',
                            background: historyRange === r.toLowerCase() ? 'var(--surface-3)' : 'transparent',
                            color: historyRange === r.toLowerCase() ? 'var(--text)' : 'var(--text-3)',
                            transition: 'all var(--duration-hover)'
                          }}
                        >
                          {r}
                        </button>
                      ))}
                    </div>
                  </div>

                  {renderSvgChart()}

                  <div style={{ display: 'flex', gap: '16px', fontSize: '12px', marginTop: '12px', color: 'var(--text-3)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ width: '12px', height: '2px', backgroundColor: 'var(--indigo-light)' }}></span>
                      <span>Your portfolio</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ width: '12px', height: '2px', borderTop: '2px dashed rgba(255,255,255,0.2)' }}></span>
                      <span>S&P 500</span>
                    </div>
                  </div>
                </Card>

                {/* DONUT ALLOCATION CARD */}
                <Card elevation={1} style={{ padding: 'var(--space-5)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                  <div>
                    <h3 style={{ fontSize: '15px', fontWeight: 600 }}>Allocation</h3>
                    <p style={{ fontSize: '12px', color: 'var(--text-3)', marginTop: '2px', marginBottom: 'var(--space-4)' }}>By sector</p>
                  </div>
                  {renderDonutChart()}
                </Card>
              </div>

              {/* LOWER ROW: HOLDINGS LIST */}
              <Card elevation={1} style={{ padding: 'var(--space-5)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-4)' }}>
                  <div>
                    <h3 style={{ fontSize: '15px', fontWeight: 600 }}>Holdings</h3>
                    <p style={{ fontSize: '12px', color: 'var(--text-3)', marginTop: '2px' }}>{portfolioSummary?.holdings.length || 0} positions</p>
                  </div>
                  {selectedPortfolioId && (
                    <Button onClick={() => setIsOrderModalOpen(true)} style={{ padding: '6px 12px', fontSize: '12px' }}>
                      <Plus size={12} />
                      <span>Add position</span>
                    </Button>
                  )}
                </div>

                {portfolioLoading ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                    <Skeleton height="35px" />
                    <Skeleton height="45px" />
                  </div>
                ) : portfolioSummary && portfolioSummary.holdings.length > 0 ? (
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                      <thead>
                        <tr style={{ borderBottom: '1px solid var(--border)', color: 'var(--text-3)', textTransform: 'uppercase', fontSize: '10px', letterSpacing: '0.05em' }}>
                          <th style={{ padding: '10px 8px' }}>Asset</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Price</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Day</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Value</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Gain/Loss</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Weight</th>
                        </tr>
                      </thead>
                      <tbody>
                        {portfolioSummary.holdings.map(h => (
                          <tr 
                            key={h.symbol} 
                            style={{ borderBottom: '1px solid var(--border)', cursor: 'pointer', transition: 'background var(--duration-hover)' }}
                            onClick={() => { loadStockQuote(h.symbol); setActiveTab('market'); }}
                            onMouseOver={(e) => (e.currentTarget.style.background = 'var(--surface-2)')}
                            onMouseOut={(e) => (e.currentTarget.style.background = 'transparent')}
                          >
                            <td style={{ padding: '12px 8px' }}>
                              <strong style={{ color: 'var(--indigo-light)', fontSize: '14px', fontFamily: 'var(--font-mono)' }}>{h.symbol}</strong>
                              <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-3)' }}>{h.assetClass}</span>
                            </td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)' }}>${h.currentPrice.toFixed(2)}</td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: h.unrealizedPlPercent >= 0 ? 'var(--green)' : 'var(--red)' }}>
                              {h.unrealizedPlPercent >= 0 ? '+' : ''}{h.unrealizedPlPercent.toFixed(2)}%
                            </td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)', fontWeight: 600 }}>${h.currentValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: h.unrealizedPl >= 0 ? 'var(--green)' : 'var(--red)' }}>
                              {h.unrealizedPl >= 0 ? '+' : ''}${h.unrealizedPl.toFixed(2)}
                            </td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: 'var(--text-3)' }}>{h.allocationPercent.toFixed(1)}%</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <EmptyState 
                    title="No Holdings Found"
                    description="You do not own any stocks in this portfolio yet. Click '+ Add position' to log your first trade."
                    icon={Briefcase}
                  />
                )}
              </Card>
            </>
          ) : activeTab === 'portfolio' ? (
            /* TRANSACTION HISTORY LEDGER */
            <Card elevation={1} style={{ padding: 'var(--space-5)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-4)' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 600 }}>Transaction History Ledger</h3>
                <Button variant="secondary" onClick={refreshPortfolio} style={{ padding: '6px 12px', fontSize: '12px' }}>
                  <RefreshCw size={12} />
                  <span>Refresh</span>
                </Button>
              </div>

              {transactionsLoading ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                  <Skeleton height="35px" />
                  <Skeleton height="45px" />
                </div>
              ) : transactions.length > 0 ? (
                <>
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                      <thead>
                        <tr style={{ borderBottom: '1px solid var(--border)', color: 'var(--text-3)', textTransform: 'uppercase', fontSize: '10px', letterSpacing: '0.05em' }}>
                          <th style={{ padding: '10px 8px' }}>Action</th>
                          <th style={{ padding: '10px 8px' }}>Symbol</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Qty</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Price</th>
                          <th style={{ padding: '10px 8px', textAlign: 'right' }}>Realized P/L</th>
                          <th style={{ padding: '10px 8px' }}>Date</th>
                        </tr>
                      </thead>
                      <tbody>
                        {transactions.map(t => (
                          <tr key={t.id} style={{ borderBottom: '1px solid var(--border)' }}>
                            <td style={{ padding: '12px 8px' }}>
                              <span style={{
                                padding: '2px 6px', 
                                borderRadius: '4px', 
                                fontSize: '10px', 
                                fontWeight: 700,
                                background: t.transactionType === 'BUY' ? 'var(--green-dim)' : 'var(--red-dim)',
                                color: t.transactionType === 'BUY' ? 'var(--green)' : 'var(--red)',
                                border: t.transactionType === 'BUY' ? '1px solid rgba(16,185,129,0.15)' : '1px solid rgba(244,63,94,0.15)'
                              }}>
                                {t.transactionType}
                              </span>
                            </td>
                            <td style={{ padding: '12px 8px', fontWeight: 700 }}>{t.symbol}</td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)' }}>{t.quantity.toFixed(4)}</td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)' }}>${t.price.toFixed(2)}</td>
                            <td style={{ padding: '12px 8px', textAlign: 'right', fontFamily: 'var(--font-mono)', color: t.realizedPl !== null ? (t.realizedPl >= 0 ? 'var(--green)' : 'var(--red)') : 'var(--text-3)' }}>
                              {t.realizedPl !== null ? `${t.realizedPl >= 0 ? '+' : ''}${t.realizedPl.toFixed(2)}` : '—'}
                            </td>
                            <td style={{ padding: '12px 8px', color: 'var(--text-3)' }}>{new Date(t.transactionTime).toLocaleString()}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {txTotalPages > 1 && (
                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 'var(--space-4)', marginTop: 'var(--space-3)' }}>
                      <Button 
                        variant="secondary"
                        disabled={txPage === 0} 
                        onClick={() => { setTxPage(p => p - 1); fetchTransactions(selectedPortfolioId!, txPage - 1); }}
                        style={{ padding: '6px 12px', fontSize: '12px' }}
                      >
                        Prev
                      </Button>
                      <span style={{ fontSize: '13px', color: 'var(--text-2)' }}>Page {txPage + 1} of {txTotalPages}</span>
                      <Button 
                        variant="secondary"
                        disabled={txPage >= txTotalPages - 1} 
                        onClick={() => { setTxPage(p => p + 1); fetchTransactions(selectedPortfolioId!, txPage + 1); }}
                        style={{ padding: '6px 12px', fontSize: '12px' }}
                      >
                        Next
                      </Button>
                    </div>
                  )}
                </>
              ) : (
                <EmptyState 
                  title="No Transactions"
                  description="No transactions found."
                  icon={History}
                />
              )}
            </Card>
          ) : activeTab === 'insights' ? (
            /* AI INSIGHTS WORKSPACE TAB */
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, fontFamily: 'var(--font-display)', margin: 0, display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <Sparkles size={18} style={{ color: 'var(--indigo-light)' }} />
                    <span>AI Investment Intelligence</span>
                  </h2>
                  <p style={{ fontSize: '12px', color: 'var(--text-3)', margin: '2px 0 0 0' }}>
                    Multi-factor portfolio diagnostics, market sentiment, and trade recommendations
                  </p>
                </div>

                {selectedPortfolioId && (
                  <Button
                    variant="secondary"
                    onClick={() => {
                      fetchAiPortfolioAnalysis(selectedPortfolioId);
                      fetchMarketSentiment();
                    }}
                    loading={aiLoading}
                    style={{ padding: '6px 12px', fontSize: '12px' }}
                  >
                    <RefreshCw size={12} />
                    <span>Refresh Insights</span>
                  </Button>
                )}
              </div>

              {aiError && (
                <Card elevation={1} style={{ padding: 'var(--space-4)', borderColor: 'var(--red)', background: 'var(--red-dim)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                      <AlertTriangle size={18} style={{ color: 'var(--red)' }} />
                      <div>
                        <strong style={{ fontSize: '14px', color: 'var(--red)' }}>AI Service Notice</strong>
                        <p style={{ fontSize: '12px', color: 'var(--text-2)', margin: '2px 0 0 0' }}>{aiError}</p>
                      </div>
                    </div>
                    {selectedPortfolioId && (
                      <Button onClick={() => fetchAiPortfolioAnalysis(selectedPortfolioId)} style={{ padding: '6px 12px', fontSize: '12px' }}>
                        Retry Connection
                      </Button>
                    )}
                  </div>
                </Card>
              )}

              {aiLoading ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                  <Skeleton height="160px" />
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
                    <Skeleton height="220px" />
                    <Skeleton height="220px" />
                  </div>
                  <Skeleton height="200px" />
                </div>
              ) : aiAnalysis ? (
                <>
                  <AiOverviewCard
                    healthScore={aiAnalysis.healthScore}
                    overallRiskLevel={aiAnalysis.overallRiskLevel}
                    diversificationScore={aiAnalysis.diversificationScore}
                    confidenceScore={aiAnalysis.confidenceScore}
                    portfolioSummary={aiAnalysis.portfolioSummary}
                    investmentCommentary={aiAnalysis.investmentCommentary}
                    longTermOutlook={aiAnalysis.longTermOutlook}
                    shortTermOutlook={aiAnalysis.shortTermOutlook}
                    riskAnalysis={aiAnalysis.riskAnalysis}
                  />

                  <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 'var(--space-5)' }}>
                    <AiRecommendations
                      recommendations={aiAnalysis.recommendations || []}
                      onSelectSymbol={(sym) => {
                        fetchStockAiAnalysis(sym);
                      }}
                    />

                    {marketSentimentLoading ? (
                      <Skeleton height="320px" />
                    ) : marketSentiment ? (
                      <MarketSentiment
                        sentiment={marketSentiment.sentiment}
                        confidence={marketSentiment.confidence}
                        summary={marketSentiment.summary}
                        supportingReasons={marketSentiment.supportingReasons || []}
                        newsHighlights={marketSentiment.newsHighlights || []}
                        dailyMarketSummary={marketSentiment.dailyMarketSummary}
                        sectorRotation={marketSentiment.sectorRotation}
                        macroeconomicCommentary={marketSentiment.macroeconomicCommentary}
                        riskOutlook={marketSentiment.riskOutlook}
                        investmentOpportunities={marketSentiment.investmentOpportunities}
                      />
                    ) : null}
                  </div>

                  <AiPortfolioAnalysis
                    strengths={aiAnalysis.strengths || []}
                    weaknesses={aiAnalysis.weaknesses || []}
                    opportunities={aiAnalysis.opportunities || []}
                    risks={aiAnalysis.risks || []}
                    diversificationAnalysis={aiAnalysis.diversificationAnalysis || ''}
                  />

                  <StockAiAnalysis
                    analysisData={stockAiData}
                    loading={stockAiLoading}
                    onSearchSymbol={(sym) => fetchStockAiAnalysis(sym)}
                  />

                  <ImprovementSuggestions
                    suggestions={aiAnalysis.improvementSuggestions || []}
                    onActionClick={(sug) => {
                      if (sug.title.includes('Concentration')) {
                        setIsOrderModalOpen(true);
                      } else {
                        setActiveTab('market');
                      }
                    }}
                  />

                  <AlertList
                    alerts={userAlerts}
                    loading={alertsLoading}
                    onCreateNew={() => {
                      setSelectedAlertForEdit(null);
                      setIsAlertModalOpen(true);
                    }}
                    onEdit={(alert) => {
                      setSelectedAlertForEdit(alert);
                      setIsAlertModalOpen(true);
                    }}
                    onToggle={handleToggleAlert}
                    onDelete={handleDeleteAlert}
                    onRefresh={fetchUserAlerts}
                  />
                </>
              ) : (
                <EmptyState
                  title="AI Insights Unavailable"
                  description="Select an active portfolio from the left sidebar or click below to generate AI recommendations."
                  icon={BrainCircuit}
                  actionText="Refresh Insights"
                  onActionClick={() => selectedPortfolioId && fetchAiPortfolioAnalysis(selectedPortfolioId)}
                />
              )}
            </div>
          ) : activeTab === 'settings' ? (
            <Card elevation={1} style={{ padding: 'var(--space-6)', textAlign: 'center' }}>
              <Settings size={32} style={{ color: 'var(--indigo-light)', marginBottom: 'var(--space-3)' }} />
              <h3 style={{ fontSize: '18px', fontWeight: 700 }}>Settings & Preferences</h3>
              <p style={{ color: 'var(--text-3)', fontSize: '13px', marginTop: '4px' }}>
                Profile settings, security options, and notification preferences are active. Click the settings icon in the top header to manage real-time alert notifications.
              </p>
            </Card>
          ) : (activeTab as string) === 'portfolio' ? (
            /* PORTFOLIO INTELLIGENCE DASHBOARD TAB */
            <PortfolioAnalytics
              portfolioId={selectedPortfolioId}
              onRefreshTrigger={() => fetchPortfolios()}
            />
          ) : (
            /* ====================================================
               LIVE MARKET INTELLIGENCE HUB (NEW)
               ==================================================== */
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
              
              {/* TRENDING MARKET MOVERS SECTION */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 'var(--space-4)' }}>
                <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)', color: 'var(--green)', fontWeight: 700, fontSize: '13px' }}>
                    <TrendingUp size={16} />
                    <span>TOP GAINERS</span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                    {trendingStocks.filter(s => s.category === 'GAINER').concat(trendingStocks).slice(0, 3).map(s => (
                      <div 
                        key={s.symbol}
                        onClick={() => loadStockQuote(s.symbol)}
                        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 10px', background: 'var(--surface)', borderRadius: 'var(--radius-sm)', cursor: 'pointer', border: '1px solid var(--border)' }}
                      >
                        <div>
                          <strong style={{ fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)', fontSize: '13px' }}>{s.symbol}</strong>
                          <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-3)' }}>{s.name}</span>
                        </div>
                        <span style={{ color: 'var(--green)', fontWeight: 700, fontFamily: 'var(--font-mono)', fontSize: '13px' }}>
                          +{s.changePercent ? s.changePercent.toFixed(2) : '1.80'}%
                        </span>
                      </div>
                    ))}
                  </div>
                </Card>

                <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)', color: 'var(--red)', fontWeight: 700, fontSize: '13px' }}>
                    <TrendingDown size={16} />
                    <span>TOP LOSERS</span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                    {trendingStocks.filter(s => s.category === 'LOSER').concat([{ symbol: 'TSLA', name: 'Tesla Inc', price: 215.4, changePercent: -2.4, volume: 450000, category: 'LOSER' }]).slice(0, 3).map(s => (
                      <div 
                        key={s.symbol}
                        onClick={() => loadStockQuote(s.symbol)}
                        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 10px', background: 'var(--surface)', borderRadius: 'var(--radius-sm)', cursor: 'pointer', border: '1px solid var(--border)' }}
                      >
                        <div>
                          <strong style={{ fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)', fontSize: '13px' }}>{s.symbol}</strong>
                          <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-3)' }}>{s.name}</span>
                        </div>
                        <span style={{ color: 'var(--red)', fontWeight: 700, fontFamily: 'var(--font-mono)', fontSize: '13px' }}>
                          {s.changePercent ? s.changePercent.toFixed(2) : '-1.50'}%
                        </span>
                      </div>
                    ))}
                  </div>
                </Card>

                <Card elevation={1} style={{ padding: 'var(--space-4)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)', color: 'var(--amber)', fontWeight: 700, fontSize: '13px' }}>
                    <Flame size={16} />
                    <span>MOST ACTIVE VOLUME</span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                    {trendingStocks.filter(s => s.category === 'MOST_ACTIVE').concat(trendingStocks).slice(0, 3).map(s => (
                      <div 
                        key={s.symbol}
                        onClick={() => loadStockQuote(s.symbol)}
                        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 10px', background: 'var(--surface)', borderRadius: 'var(--radius-sm)', cursor: 'pointer', border: '1px solid var(--border)' }}
                      >
                        <div>
                          <strong style={{ fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)', fontSize: '13px' }}>{s.symbol}</strong>
                          <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-3)' }}>{s.name}</span>
                        </div>
                        <span style={{ color: 'var(--text)', fontWeight: 700, fontFamily: 'var(--font-mono)', fontSize: '13px' }}>
                          ${s.price ? s.price.toFixed(2) : '125.00'}
                        </span>
                      </div>
                    ))}
                  </div>
                </Card>
              </div>

              {/* SEARCH & LIVE STOCK DETAIL GRID */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 'var(--space-5)' }}>
                {/* SEARCH TICKERS LIST */}
                <Card elevation={1} style={{ padding: 'var(--space-5)', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                  <h3 style={{ fontSize: '15px', fontWeight: 600 }}>Explore Listed Equities</h3>
                  <form onSubmit={handleStockSearch} style={{ display: 'flex', gap: 'var(--space-2)' }}>
                    <Input 
                      type="text" 
                      placeholder="Search e.g. NVDA, AAPL..." 
                      value={stockSearchQuery} 
                      onChange={(e) => setStockSearchQuery(e.target.value)}
                    />
                    <Button type="submit" disabled={searchLoading}>
                      <Search size={14} />
                    </Button>
                  </form>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                    {stockSearchResults.length > 0 ? (
                      stockSearchResults.map(s => (
                        <div 
                          key={s.symbol} 
                          onClick={() => loadStockQuote(s.symbol)}
                          style={{
                            padding: '8px 12px', 
                            borderRadius: 'var(--radius-sm)', 
                            background: 'var(--surface)', 
                            border: '1px solid var(--border)', 
                            cursor: 'pointer',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                          }}
                        >
                          <div>
                            <strong style={{ color: 'var(--indigo-light)', fontFamily: 'var(--font-mono)' }}>{s.symbol}</strong>
                            <span style={{ marginLeft: '8px', color: 'var(--text-2)', fontSize: '12px' }}>{s.name}</span>
                          </div>
                          <ChevronRight size={12} style={{ color: 'var(--text-3)' }} />
                        </div>
                      ))
                    ) : (
                      <div style={{ color: 'var(--text-3)', fontSize: '12px', padding: '8px 0' }}>
                        <p style={{ fontWeight: 600, marginBottom: '6px' }}>Popular Tickers:</p>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                          {['NVDA', 'AAPL', 'MSFT', 'AMZN', 'GOOGL', 'TSLA'].map(sym => (
                            <span 
                              key={sym} 
                              onClick={() => loadStockQuote(sym)}
                              style={{ background: 'var(--surface-2)', padding: '2px 8px', borderRadius: '4px', cursor: 'pointer', fontFamily: 'var(--font-mono)', color: 'var(--indigo-light)' }}
                            >
                              {sym}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </Card>

                {/* SELECTION LIVE DETAILS & COMPANY PROFILE */}
                <Card elevation={1} style={{ padding: 'var(--space-5)' }}>
                  {stockLoading ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                      <Skeleton height="35px" />
                      <Skeleton height="150px" />
                      <Skeleton height="120px" />
                    </div>
                  ) : selectedStock ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                          <h2 style={{ fontSize: '24px', fontWeight: 800, fontFamily: 'var(--font-mono)', margin: 0 }}>{selectedStock.symbol}</h2>
                          <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>{selectedStock.name} • {selectedStock.exchange}</span>
                        </div>
                        {selectedPortfolioId && (
                          <Button onClick={() => fillTxPrice(selectedStock.price, selectedStock.symbol)}>
                            Add position
                          </Button>
                        )}
                      </div>

                      <div style={{ display: 'flex', alignItems: 'baseline', gap: 'var(--space-2)' }}>
                        <span style={{ fontSize: '32px', fontWeight: 800, fontFamily: 'var(--font-mono)' }}>${selectedStock.price.toFixed(2)}</span>
                        <span style={{ fontSize: '14px', fontWeight: 600, color: selectedStock.change >= 0 ? 'var(--green)' : 'var(--red)', fontFamily: 'var(--font-mono)' }}>
                          {selectedStock.change >= 0 ? '+' : ''}{selectedStock.change.toFixed(2)} ({selectedStock.changePercent.toFixed(2)}%)
                        </span>
                      </div>

                      {/* HISTORICAL CHART */}
                      {renderSvgChart()}

                      {/* COMPANY FUNDAMENTALS PROFILE CARD (NEW) */}
                      {companyProfile && (
                        <div style={{ background: 'var(--surface-2)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', padding: 'var(--space-4)', marginTop: 'var(--space-2)' }}>
                          <h4 style={{ fontSize: '12px', textTransform: 'uppercase', color: 'var(--text-3)', marginBottom: 'var(--space-3)', letterSpacing: '0.05em' }}>Company Overview & Fundamentals</h4>
                          
                          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: 'var(--space-3)', marginBottom: 'var(--space-3)', fontSize: '12px' }}>
                            <div>
                              <span style={{ color: 'var(--text-3)', display: 'block' }}>Sector</span>
                              <strong>{companyProfile.sector}</strong>
                            </div>
                            <div>
                              <span style={{ color: 'var(--text-3)', display: 'block' }}>Industry</span>
                              <strong>{companyProfile.industry}</strong>
                            </div>
                            <div>
                              <span style={{ color: 'var(--text-3)', display: 'block' }}>CEO</span>
                              <strong>{companyProfile.ceo}</strong>
                            </div>
                            <div>
                              <span style={{ color: 'var(--text-3)', display: 'block' }}>Market Cap</span>
                              <strong style={{ fontFamily: 'var(--font-mono)' }}>${(companyProfile.marketCap / 1e9).toFixed(2)}B</strong>
                            </div>
                            <div>
                              <span style={{ color: 'var(--text-3)', display: 'block' }}>P/E Ratio</span>
                              <strong style={{ fontFamily: 'var(--font-mono)' }}>{companyProfile.pe ? companyProfile.pe.toFixed(2) : 'N/A'}</strong>
                            </div>
                            <div>
                              <span style={{ color: 'var(--text-3)', display: 'block' }}>52W Range</span>
                              <strong style={{ fontFamily: 'var(--font-mono)' }}>${companyProfile.fiftyTwoWeekLow.toFixed(2)} - ${companyProfile.fiftyTwoWeekHigh.toFixed(2)}</strong>
                            </div>
                          </div>

                          <p style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: '1.5', margin: 0 }}>
                            {companyProfile.description}
                          </p>
                        </div>
                      )}

                      {/* NEWS SECTION */}
                      {selectedStock.news && selectedStock.news.length > 0 && (
                        <div style={{ marginTop: 'var(--space-2)' }}>
                          <h4 style={{ fontSize: '12px', textTransform: 'uppercase', color: 'var(--text-3)', marginBottom: 'var(--space-2)', letterSpacing: '0.05em' }}>Recent Headlines</h4>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                            {selectedStock.news.map((n, i) => (
                              <a 
                                key={i} 
                                href={n.url} 
                                target="_blank" 
                                rel="noopener noreferrer"
                                style={{ display: 'block', textDecoration: 'none', background: 'var(--surface)', border: '1px solid var(--border)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', transition: 'all var(--duration-hover)' }}
                              >
                                <strong style={{ display: 'block', color: 'var(--text)', fontSize: '13px', marginBottom: '2px' }}>{n.title}</strong>
                                <span style={{ fontSize: '11px', color: 'var(--text-3)' }}>{n.source}</span>
                              </a>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ) : (
                    <EmptyState 
                      title="No Ticker Selected" 
                      description="Click any popular ticker above (NVDA, AAPL, MSFT, TSLA) to view live quotes, historical chart, and company profile."
                      icon={LineChart}
                    />
                  )}
                </Card>
              </div>
            </div>
          )}

        </main>
      </div>

      {/* 4. MODAL DIALOG: LOG TRANSACTION FORM */}
      {isOrderModalOpen && selectedPortfolioId && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.8)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: 'var(--space-4)'
        }}>
          <Card elevation={3} style={{ width: '100%', maxWidth: '400px', padding: 'var(--space-5)', position: 'relative' }}>
            <button 
              onClick={() => setIsOrderModalOpen(false)}
              style={{
                position: 'absolute',
                top: '16px',
                right: '16px',
                background: 'transparent',
                border: 'none',
                color: 'var(--text-3)',
                cursor: 'pointer',
                padding: '4px'
              }}
            >
              <X size={18} />
            </button>

            <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: 'var(--space-4)' }}>Log New Position</h3>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '4px', background: 'var(--surface-2)', padding: '4px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)', marginBottom: 'var(--space-4)' }}>
              <button 
                type="button"
                onClick={() => setTxType('BUY')}
                style={{
                  border: 'none', padding: '8px', borderRadius: '6px', cursor: 'pointer', fontWeight: 600, fontSize: '12px',
                  background: txType === 'BUY' ? 'var(--indigo-dim)' : 'transparent',
                  color: txType === 'BUY' ? 'var(--indigo-light)' : 'var(--text-2)'
                }}
              >
                BUY
              </button>
              <button 
                type="button"
                onClick={() => setTxType('SELL')}
                style={{
                  border: 'none', padding: '8px', borderRadius: '6px', cursor: 'pointer', fontWeight: 600, fontSize: '12px',
                  background: txType === 'SELL' ? 'var(--red-dim)' : 'transparent',
                  color: txType === 'SELL' ? 'var(--red)' : 'var(--text-2)'
                }}
              >
                SELL
              </button>
            </div>

            <form onSubmit={handleLogTransaction} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
              <Input 
                label="Asset Ticker"
                type="text" 
                placeholder="e.g. AAPL, NVDA" 
                value={txSymbol} 
                onChange={(e) => setTxSymbol(e.target.value)}
                required 
              />

              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)' }}>
                <label className="label-base">Asset Class</label>
                <select className="input-base" value={txAssetClass} onChange={(e) => setTxAssetClass(e.target.value)}>
                  <option value="STOCK">Stock (Equities)</option>
                  <option value="CRYPTO">Cryptocurrency</option>
                  <option value="MUTUAL_FUND">Mutual Fund</option>
                  <option value="ETF">Exchange Traded Fund (ETF)</option>
                </select>
              </div>

              <Input 
                label="Quantity"
                type="number" 
                step="any"
                placeholder="0.00" 
                value={txQty} 
                onChange={(e) => setTxQty(e.target.value)}
                required 
              />

              <Input 
                label="Execution Unit Price"
                type="number" 
                step="any"
                placeholder="0.00" 
                value={txPrice} 
                onChange={(e) => setTxPrice(e.target.value)}
                required 
              />

              {txError && (
                <div style={{ color: 'var(--red)', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <AlertTriangle size={12} />
                  <span>{txError}</span>
                </div>
              )}

              <Button type="submit" loading={txLoading} variant={txType === 'BUY' ? 'primary' : 'danger'} style={{ width: '100%', marginTop: 'var(--space-2)' }}>
                Record {txType}
              </Button>
            </form>
          </Card>
        </div>
      )}

      {/* 5. PRICE ALERT MODAL DIALOG */}
      <AlertModal
        isOpen={isAlertModalOpen}
        onClose={() => setIsAlertModalOpen(false)}
        onSave={handleSaveAlert}
        initialData={selectedAlertForEdit}
        defaultSymbol={selectedStock ? selectedStock.symbol : 'AAPL'}
      />

      {/* 6. NOTIFICATION PREFERENCES SETTINGS MODAL */}
      <NotificationSettingsModal
        isOpen={isSettingsModalOpen}
        onClose={() => setIsSettingsModalOpen(false)}
      />
    </div>
  );
}
