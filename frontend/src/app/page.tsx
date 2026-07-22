'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { getTokens } from '@/utils/api';

export default function RootPage() {
  const router = useRouter();

  useEffect(() => {
    const { token } = getTokens();
    if (token) {
      router.replace('/dashboard');
    } else {
      router.replace('/login');
    }
  }, [router]);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', flexDirection: 'column', gap: '15px', backgroundColor: '#080b11', color: '#ffffff' }}>
      <div style={{ width: '40px', height: '40px', border: '3px solid rgba(255,255,255,0.1)', borderTopColor: '#7c3aed', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
      <p style={{ color: '#9ca3af', fontSize: '14px', fontFamily: 'sans-serif' }}>Redirecting to StockPilot AI...</p>
      <style jsx>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
