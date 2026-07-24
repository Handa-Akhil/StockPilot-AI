import React, { useEffect } from 'react';
import { Card } from '../Card';
import { NotificationItem } from './NotificationBell';
import { Bell, X, CheckCheck, Sparkles, Trash2, ExternalLink } from 'lucide-react';

interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  notifications: NotificationItem[];
  unreadCount: number;
  onMarkAsRead: (id: number) => void;
  onMarkAllAsRead: () => void;
  onDeleteNotification: (id: number) => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({
  isOpen,
  onClose,
  notifications,
  unreadCount,
  onMarkAsRead,
  onMarkAllAsRead,
  onDeleteNotification,
}) => {
  // Trap escape key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <Card
      elevation={3}
      style={{
        position: 'absolute',
        top: '46px',
        right: 0,
        width: '380px',
        maxHeight: '520px',
        zIndex: 500,
        padding: 'var(--space-4)',
        display: 'flex',
        flexDirection: 'column',
        gap: 'var(--space-3)',
        animation: 'fadeIn 0.2s var(--ease-custom)',
        border: '1px solid var(--border-bright)'
      }}
    >
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 'var(--space-2)', borderBottom: '1px solid var(--border)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
          <Bell size={16} style={{ color: 'var(--indigo-light)' }} />
          <strong style={{ fontSize: '14px' }}>Notifications</strong>
          {unreadCount > 0 && (
            <span style={{ fontSize: '11px', background: 'var(--indigo-dim)', color: 'var(--indigo-light)', padding: '1px 6px', borderRadius: '8px', fontWeight: 700 }}>
              {unreadCount} new
            </span>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          {unreadCount > 0 && (
            <button
              onClick={onMarkAllAsRead}
              title="Mark all as read"
              style={{ background: 'transparent', border: 'none', color: 'var(--text-3)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '2px', fontSize: '11px' }}
            >
              <CheckCheck size={14} />
            </button>
          )}
          <button
            onClick={onClose}
            aria-label="Close Notification Drawer"
            style={{ background: 'transparent', border: 'none', color: 'var(--text-3)', cursor: 'pointer', padding: '2px' }}
          >
            <X size={16} />
          </button>
        </div>
      </div>

      {/* List items */}
      <div style={{ overflowY: 'auto', maxHeight: '420px', display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
        {notifications.length > 0 ? (
          notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.readStatus && onMarkAsRead(n.id)}
              style={{
                padding: 'var(--space-3)',
                borderRadius: 'var(--radius-sm)',
                background: n.readStatus ? 'var(--surface)' : 'var(--surface-2)',
                border: n.readStatus ? '1px solid var(--border)' : '1px solid var(--border-focus)',
                cursor: 'pointer',
                transition: 'all 0.15s ease',
                position: 'relative'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '4px' }}>
                <strong style={{ fontSize: '13px', color: n.readStatus ? 'var(--text-2)' : 'var(--text)', fontWeight: 700 }}>
                  {n.title}
                </strong>
                <button
                  onClick={(e) => { e.stopPropagation(); onDeleteNotification(n.id); }}
                  title="Delete Notification"
                  style={{ background: 'transparent', border: 'none', color: 'var(--text-3)', cursor: 'pointer', padding: '2px' }}
                >
                  <Trash2 size={12} />
                </button>
              </div>

              <p style={{ fontSize: '12px', color: 'var(--text-3)', lineHeight: 1.4, margin: '0 0 6px 0' }}>
                {n.message}
              </p>

              {/* GEMINI AI EXPLANATION BOX */}
              {n.aiExplanation && (
                <div style={{
                  background: 'rgba(99, 102, 241, 0.08)',
                  border: '1px solid rgba(99, 102, 241, 0.2)',
                  borderRadius: 'var(--radius-sm)',
                  padding: '6px 8px',
                  fontSize: '11px',
                  color: 'var(--text-2)',
                  lineHeight: 1.4,
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '6px'
                }}>
                  <Sparkles size={12} style={{ color: 'var(--indigo-light)', flexShrink: 0, marginTop: '2px' }} />
                  <div>
                    <strong style={{ color: 'var(--indigo-light)', display: 'block', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                      Gemini AI Explanation
                    </strong>
                    <span>{n.aiExplanation}</span>
                  </div>
                </div>
              )}

              <span style={{ fontSize: '10px', color: 'var(--text-4)', display: 'block', marginTop: '6px', textAlign: 'right' }}>
                {new Date(n.timestamp).toLocaleString()}
              </span>
            </div>
          ))
        ) : (
          <p style={{ color: 'var(--text-3)', fontSize: '12px', textAlign: 'center', padding: 'var(--space-5) 0' }}>
            No notifications yet.
          </p>
        )}
      </div>
    </Card>
  );
};
