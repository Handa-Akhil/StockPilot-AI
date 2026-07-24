import React, { useEffect } from 'react';
import { Card } from '../Card';
import { Bell, Sparkles, X } from 'lucide-react';

export interface ToastItem {
  id: string | number;
  title: string;
  message: string;
  aiExplanation?: string;
}

interface ToastNotificationProps {
  toast: ToastItem;
  onClose: () => void;
}

export const ToastNotification: React.FC<ToastNotificationProps> = ({ toast, onClose }) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, 6000);
    return () => clearTimeout(timer);
  }, [onClose]);

  return (
    <div
      style={{
        position: 'fixed',
        top: '20px',
        right: '20px',
        zIndex: 10000,
        maxWidth: '360px',
        width: '100%',
        animation: 'fadeInScale 0.25s var(--ease-custom)'
      }}
    >
      <Card
        elevation={3}
        style={{
          padding: 'var(--space-4)',
          background: 'var(--surface-3)',
          border: '1px solid var(--border-focus)',
          boxShadow: 'var(--shadow-lg)'
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '4px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Bell size={16} style={{ color: 'var(--green)' }} />
            <strong style={{ fontSize: '13px', color: 'var(--text)' }}>{toast.title}</strong>
          </div>
          <button
            onClick={onClose}
            style={{ background: 'transparent', border: 'none', color: 'var(--text-3)', cursor: 'pointer', padding: '2px' }}
          >
            <X size={14} />
          </button>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--text-2)', lineHeight: 1.4, margin: '0 0 6px 0' }}>
          {toast.message}
        </p>

        {toast.aiExplanation && (
          <div style={{
            background: 'var(--indigo-dim)',
            padding: '6px 8px',
            borderRadius: '4px',
            fontSize: '11px',
            color: 'var(--indigo-light)',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '4px'
          }}>
            <Sparkles size={12} style={{ flexShrink: 0, marginTop: '2px' }} />
            <span>{toast.aiExplanation}</span>
          </div>
        )}
      </Card>
    </div>
  );
};
