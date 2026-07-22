'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch, clearTokens, getTokens } from '@/utils/api';
import styles from './dashboard.module.css';

interface UserProfile {
  id: number;
  email: string;
  name: string;
  createdAt: string;
}

export default function DashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const { token } = getTokens();
    if (!token) {
      router.push('/login');
      return;
    }

    const fetchProfile = async () => {
      try {
        const data = await apiFetch('/api/auth/me');
        setUser(data);
      } catch (err) {
        // Token might be invalid/expired and refresh failed
        clearTokens();
        router.push('/login');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [router]);

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

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', flexDirection: 'column', gap: '15px' }}>
        <div style={{ width: '40px', height: '40px', border: '3px solid rgba(255,255,255,0.1)', borderTopColor: '#7c3aed', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
        <p style={{ color: '#9ca3af', fontSize: '14px' }}>Loading Dashboard...</p>
        <style jsx>{`
          @keyframes spin {
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  return (
    <div className={styles.dashboard}>
      <header className={styles.header}>
        <div className={styles.logo}>StockPilot AI</div>
        <div className={styles.userArea}>
          <div className={styles.welcomeText}>
            Welcome, <span className={styles.username}>{user?.name || 'Investor'}</span>
          </div>
          <button className={styles.logoutBtn} onClick={handleLogout}>
            Log Out
          </button>
        </div>
      </header>

      <main className={styles.content}>
        <section className={styles.greetingSection}>
          <h1 className={styles.title}>Market Overview</h1>
          <p className={styles.subtitle}>Welcome back to your financial control center.</p>
        </section>

        {/* Market Indices Section */}
        <section className={styles.indexGrid}>
          <div className={`${styles.indexCard} glass-card`}>
            <div className={styles.indexName}>S&P 500</div>
            <div className={styles.indexValue}>5,544.75</div>
            <div className={`${styles.indexChange} ${styles.positive}`}>+42.15 (+0.77%)</div>
          </div>
          <div className={`${styles.indexCard} glass-card`}>
            <div className={styles.indexName}>NASDAQ</div>
            <div className={styles.indexValue}>18,010.50</div>
            <div className={`${styles.indexChange} ${styles.positive}`}>+195.20 (+1.10%)</div>
          </div>
          <div className={`${styles.indexCard} glass-card`}>
            <div className={styles.indexName}>NIFTY 50</div>
            <div className={styles.indexValue}>24,478.90</div>
            <div className={`${styles.indexChange} ${styles.negative}`}>-35.40 (-0.14%)</div>
          </div>
          <div className={`${styles.indexCard} glass-card`}>
            <div className={styles.indexName}>SENSEX</div>
            <div className={styles.indexValue}>80,429.04</div>
            <div className={`${styles.indexChange} ${styles.negative}`}>-102.61 (-0.13%)</div>
          </div>
        </section>

        {/* Dashboard Grid */}
        <section className={styles.grid}>
          <div className={`${styles.card} glass-card`}>
            <div className={styles.cardTitle}>User Session Context</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '10px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                <span style={{ color: '#9ca3af' }}>User ID:</span>
                <span>{user?.id}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                <span style={{ color: '#9ca3af' }}>Registered Email:</span>
                <span>{user?.email}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                <span style={{ color: '#9ca3af' }}>Profile Name:</span>
                <span>{user?.name}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                <span style={{ color: '#9ca3af' }}>Account Created:</span>
                <span>{user ? new Date(user.createdAt).toLocaleDateString() : ''}</span>
              </div>
            </div>
          </div>

          <div className={`${styles.card} glass-card`}>
            <div className={styles.cardTitle}>Module Integration Checklist</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '10px', fontSize: '14px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span className={styles.pulseDot} />
                <span>Authentication & Authorization (Active)</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#9ca3af' }}>
                <span style={{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: '#374151', display: 'inline-block', marginRight: '8px' }} />
                <span>Market & Stock Analytics (Scheduled)</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#9ca3af' }}>
                <span style={{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: '#374151', display: 'inline-block', marginRight: '8px' }} />
                <span>AI Explanations & Assistant (Scheduled)</span>
              </div>
            </div>
          </div>
        </section>

        {/* E2E Flow status */}
        <section className={styles.moduleStatus}>
          <h3>Sprint 1 Authentication Module is fully integrated!</h3>
          <p style={{ color: '#9ca3af', marginTop: '8px', fontSize: '14px' }}>
            Tokens are safely stored, refreshed, and authorized on active request pipelines.
          </p>
        </section>
      </main>
    </div>
  );
}
