import React, { useState, useEffect, useCallback } from 'react';
import { Bell } from 'lucide-react';
import { apiFetch } from '@/utils/api';
import { NotificationDrawer } from './NotificationDrawer';
import { ToastNotification, ToastItem } from './ToastNotification';

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: string;
  readStatus: boolean;
  timestamp: string;
  aiExplanation?: string;
}

export const NotificationBell: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [activeToast, setActiveToast] = useState<ToastItem | null>(null);

  const fetchNotifications = useCallback(async () => {
    try {
      const list = await apiFetch('/api/v1/notifications');
      setNotifications(list || []);
      const countRes = await apiFetch('/api/v1/notifications/unread-count');
      setUnreadCount(countRes.unreadCount || 0);
    } catch (err) {
      console.error('Failed to fetch notifications', err);
    }
  }, []);

  // Poll notifications every 30 seconds
  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [fetchNotifications]);

  const handleMarkAsRead = async (id: number) => {
    try {
      await apiFetch(`/api/v1/notifications/${id}/read`, { method: 'PATCH' });
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await apiFetch('/api/v1/notifications/read-all', { method: 'PATCH' });
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteNotification = async (id: number) => {
    try {
      await apiFetch(`/api/v1/notifications/${id}`, { method: 'DELETE' });
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div style={{ position: 'relative' }}>
      <button
        onClick={() => setIsDrawerOpen(!isDrawerOpen)}
        aria-label={`Notifications ${unreadCount > 0 ? `(${unreadCount} unread)` : ''}`}
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
          cursor: 'pointer',
          position: 'relative',
          transition: 'all var(--duration-hover)'
        }}
      >
        <Bell size={16} />
        {unreadCount > 0 && (
          <span style={{
            position: 'absolute',
            top: '-2px',
            right: '-2px',
            backgroundColor: 'var(--red)',
            color: '#fff',
            fontSize: '10px',
            fontWeight: 800,
            borderRadius: 'var(--radius-full)',
            minWidth: '16px',
            height: '16px',
            padding: '0 4px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 8px rgba(244, 63, 94, 0.6)'
          }}>
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {/* NOTIFICATION DRAWER POPOVER */}
      <NotificationDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        notifications={notifications}
        unreadCount={unreadCount}
        onMarkAsRead={handleMarkAsRead}
        onMarkAllAsRead={handleMarkAllAsRead}
        onDeleteNotification={handleDeleteNotification}
      />

      {/* TOAST POPUP BANNER */}
      {activeToast && (
        <ToastNotification
          toast={activeToast}
          onClose={() => setActiveToast(null)}
        />
      )}
    </div>
  );
};
