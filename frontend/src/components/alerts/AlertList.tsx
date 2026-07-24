import React from 'react';
import { Card } from '../Card';
import { Button } from '../Button';
import { PriceAlert } from './AlertModal';
import { Bell, Plus, Trash2, Edit2, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

interface AlertListProps {
  alerts: PriceAlert[];
  loading: boolean;
  onCreateNew: () => void;
  onEdit: (alert: PriceAlert) => void;
  onToggle: (id: number) => void;
  onDelete: (id: number) => void;
  onRefresh: () => void;
}

export const AlertList: React.FC<AlertListProps> = ({
  alerts,
  loading,
  onCreateNew,
  onEdit,
  onToggle,
  onDelete,
  onRefresh,
}) => {
  const formatConditionText = (alert: PriceAlert) => {
    switch (alert.condition) {
      case 'PRICE_ABOVE':
        return `Price Above $${alert.targetPrice.toFixed(2)}`;
      case 'PRICE_BELOW':
        return `Price Below $${alert.targetPrice.toFixed(2)}`;
      case 'PERCENTAGE_CHANGE':
        return `Movement >= ±${alert.targetPrice}%`;
      case 'DAILY_GAIN_LOSS':
        return `Daily Shift >= $${alert.targetPrice}`;
      case 'VOLUME_SPIKE':
        return `Volume >= ${alert.targetPrice.toLocaleString()} shares`;
      default:
        return `${alert.condition} $${alert.targetPrice}`;
    }
  };

  return (
    <Card elevation={1} style={{ padding: 'var(--space-5)', display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
          <Bell size={18} style={{ color: 'var(--indigo-light)' }} />
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'var(--font-display)', margin: 0 }}>
              Real-Time AI Price Alerts
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--text-3)', margin: 0 }}>
              Background monitoring with 60-second automated scans & Gemini AI explanations
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
          <Button variant="secondary" onClick={onRefresh} style={{ padding: '6px 12px', fontSize: '12px' }}>
            <RefreshCw size={12} />
          </Button>
          <Button onClick={onCreateNew} style={{ padding: '6px 12px', fontSize: '12px' }}>
            <Plus size={14} />
            <span>Create Alert</span>
          </Button>
        </div>
      </div>

      {loading ? (
        <p style={{ fontSize: '13px', color: 'var(--text-3)', textAlign: 'center', padding: 'var(--space-4) 0' }}>
          Loading price alerts...
        </p>
      ) : alerts.length > 0 ? (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border)', color: 'var(--text-3)', textTransform: 'uppercase', fontSize: '10px', letterSpacing: '0.05em' }}>
                <th style={{ padding: '10px 8px' }}>Symbol</th>
                <th style={{ padding: '10px 8px' }}>Condition</th>
                <th style={{ padding: '10px 8px', textAlign: 'center' }}>Status</th>
                <th style={{ padding: '10px 8px', textAlign: 'center' }}>Triggered</th>
                <th style={{ padding: '10px 8px', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {alerts.map((alert) => (
                <tr key={alert.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '12px 8px' }}>
                    <strong style={{ color: 'var(--indigo-light)', fontFamily: 'var(--font-mono)', fontSize: '14px' }}>
                      {alert.symbol}
                    </strong>
                  </td>

                  <td style={{ padding: '12px 8px', fontFamily: 'var(--font-mono)', color: 'var(--text-2)' }}>
                    {formatConditionText(alert)}
                  </td>

                  <td style={{ padding: '12px 8px', textAlign: 'center' }}>
                    <button
                      onClick={() => alert.id && onToggle(alert.id)}
                      style={{
                        border: 'none',
                        background: alert.enabled ? 'var(--green-dim)' : 'var(--surface-3)',
                        color: alert.enabled ? 'var(--green)' : 'var(--text-3)',
                        padding: '2px 8px',
                        borderRadius: 'var(--radius-full)',
                        fontSize: '11px',
                        fontWeight: 700,
                        cursor: 'pointer',
                        transition: 'all 0.15s ease'
                      }}
                    >
                      {alert.enabled ? 'ACTIVE' : 'DISABLED'}
                    </button>
                  </td>

                  <td style={{ padding: '12px 8px', textAlign: 'center' }}>
                    {alert.triggered ? (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', color: 'var(--amber)', fontSize: '11px', fontWeight: 700 }}>
                        <AlertCircle size={12} />
                        <span>TRIGGERED</span>
                      </span>
                    ) : (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', color: 'var(--text-3)', fontSize: '11px' }}>
                        <CheckCircle2 size={12} />
                        <span>MONITORING</span>
                      </span>
                    )}
                  </td>

                  <td style={{ padding: '12px 8px', textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '6px' }}>
                      <button
                        onClick={() => onEdit(alert)}
                        title="Edit Alert"
                        style={{ background: 'transparent', border: 'none', color: 'var(--text-2)', cursor: 'pointer', padding: '4px' }}
                      >
                        <Edit2 size={14} />
                      </button>
                      <button
                        onClick={() => alert.id && onDelete(alert.id)}
                        title="Delete Alert"
                        style={{ background: 'transparent', border: 'none', color: 'var(--red)', cursor: 'pointer', padding: '4px' }}
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: 'var(--space-5) 0', color: 'var(--text-3)' }}>
          <p style={{ fontSize: '13px', margin: '0 0 12px 0' }}>No active price alerts configured.</p>
          <Button variant="secondary" onClick={onCreateNew} style={{ fontSize: '12px', padding: '6px 12px' }}>
            <Plus size={12} />
            <span>Create Your First Alert</span>
          </Button>
        </div>
      )}
    </Card>
  );
};
