import React, { useState, useEffect } from 'react';
import { Card } from '../Card';
import { Button } from '../Button';
import { apiFetch } from '@/utils/api';
import { Settings, Bell, Mail, Sparkles, X, Check, AlertCircle } from 'lucide-react';

interface NotificationSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const NotificationSettingsModal: React.FC<NotificationSettingsModalProps> = ({ isOpen, onClose }) => {
  const [emailNotifications, setEmailNotifications] = useState(true);
  const [realtimeNotifications, setRealtimeNotifications] = useState(true);
  const [aiNotifications, setAiNotifications] = useState(true);
  const [loading, setLoading] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setLoading(true);
      apiFetch('/api/v1/user/settings')
        .then((res) => {
          if (res) {
            setEmailNotifications(res.emailNotificationsEnabled ?? true);
            setRealtimeNotifications(res.realtimeNotificationsEnabled ?? true);
            setAiNotifications(res.aiNotificationsEnabled ?? true);
          }
        })
        .catch((err) => console.error(err))
        .finally(() => setLoading(false));
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSave = async () => {
    setLoading(true);
    try {
      await apiFetch('/api/v1/user/settings', {
        method: 'PATCH',
        body: JSON.stringify({
          emailNotificationsEnabled: emailNotifications,
          realtimeNotificationsEnabled: realtimeNotifications,
          aiNotificationsEnabled: aiNotifications,
        }),
      });
      setSavedSuccess(true);
      setTimeout(() => {
        setSavedSuccess(false);
        onClose();
      }, 1200);
    } catch (err) {
      console.error('Failed to save settings', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      style={{
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
      }}
    >
      <Card elevation={3} style={{ width: '100%', maxWidth: '440px', padding: 'var(--space-5)', position: 'relative' }}>
        <button
          onClick={onClose}
          aria-label="Close Settings Modal"
          style={{ position: 'absolute', top: '16px', right: '16px', background: 'transparent', border: 'none', color: 'var(--text-3)', cursor: 'pointer' }}
        >
          <X size={18} />
        </button>

        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-4)' }}>
          <Settings size={18} style={{ color: 'var(--indigo-light)' }} />
          <h3 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>Notification Preferences</h3>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)', marginBottom: 'var(--space-5)' }}>
          {/* EMAIL NOTIFICATIONS */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--surface-2)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
              <Mail size={18} style={{ color: 'var(--indigo-light)' }} />
              <div>
                <strong style={{ fontSize: '13px', display: 'block' }}>Email Notifications</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-3)' }}>Receive price alert emails via Resend</span>
              </div>
            </div>
            <input
              type="checkbox"
              checked={emailNotifications}
              onChange={(e) => setEmailNotifications(e.target.checked)}
              style={{ width: '18px', height: '18px', cursor: 'pointer', accentColor: 'var(--indigo)' }}
            />
          </div>

          {/* REALTIME NOTIFICATIONS */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--surface-2)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
              <Bell size={18} style={{ color: 'var(--green)' }} />
              <div>
                <strong style={{ fontSize: '13px', display: 'block' }}>Real-Time STOMP Alerts</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-3)' }}>Instant popups & WebSocket notifications</span>
              </div>
            </div>
            <input
              type="checkbox"
              checked={realtimeNotifications}
              onChange={(e) => setRealtimeNotifications(e.target.checked)}
              style={{ width: '18px', height: '18px', cursor: 'pointer', accentColor: 'var(--indigo)' }}
            />
          </div>

          {/* AI NOTIFICATIONS */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--surface-2)', padding: 'var(--space-3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
              <Sparkles size={18} style={{ color: 'var(--amber)' }} />
              <div>
                <strong style={{ fontSize: '13px', display: 'block' }}>AI Gemini Insights</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-3)' }}>Include AI narrative explanations</span>
              </div>
            </div>
            <input
              type="checkbox"
              checked={aiNotifications}
              onChange={(e) => setAiNotifications(e.target.checked)}
              style={{ width: '18px', height: '18px', cursor: 'pointer', accentColor: 'var(--indigo)' }}
            />
          </div>
        </div>

        {savedSuccess ? (
          <div style={{ color: 'var(--green)', fontSize: '13px', textAlign: 'center', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', padding: '8px 0' }}>
            <Check size={16} />
            <span>Settings saved successfully!</span>
          </div>
        ) : (
          <Button onClick={handleSave} loading={loading} style={{ width: '100%' }}>
            Save Preferences
          </Button>
        )}
      </Card>
    </div>
  );
};
