'use client';

import React, { useState, useEffect, Suspense } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { apiFetch, setTokens } from '@/utils/api';
import styles from './login.module.css';

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [sessionExpired, setSessionExpired] = useState(false);

  useEffect(() => {
    if (searchParams && searchParams.get('expired') === 'true') {
      setSessionExpired(true);
    }
  }, [searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSessionExpired(false);

    try {
      const data = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      
      // Save tokens
      setTokens(data.token, data.refreshToken);
      
      // Save user details temporarily for UI greeting
      localStorage.setItem('sp_user_name', data.name || '');
      localStorage.setItem('sp_user_email', data.email || '');

      router.push('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`${styles.card} glass-card`}>
      <div className={styles.titleArea}>
        <div className={styles.logo}>StockPilot AI</div>
        <p className={styles.subtitle}>Understand Markets. Don't Just Watch Them.</p>
      </div>

      {error && <div className={styles.error}>{error}</div>}
      {sessionExpired && <div className={styles.notice}>Your session has expired. Please log in again.</div>}

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.group}>
          <label className={styles.label} htmlFor="email">Email Address</label>
          <input
            className={styles.input}
            type="email"
            id="email"
            placeholder="name@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
        </div>

        <div className={styles.group}>
          <label className={styles.label} htmlFor="password">Password</label>
          <input
            className={styles.input}
            type="password"
            id="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
          />
        </div>

        <button className={`${styles.button} glow-btn`} type="submit" disabled={loading}>
          {loading ? 'Signing In...' : 'Sign In'}
        </button>
      </form>

      <div className={styles.footer}>
        Don't have an account?{' '}
        <Link href="/register" className={styles.link}>
          Sign Up
        </Link>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <div className={styles.container}>
      <Suspense fallback={
        <div style={{ color: '#9ca3af', fontSize: '14px', textAlign: 'center' }}>
          Loading Login Form...
        </div>
      }>
        <LoginForm />
      </Suspense>
    </div>
  );
}
