import React, { useState, useEffect } from 'react';
import { Card } from '../Card';
import { Button } from '../Button';
import { Input } from '../Input';
import { X, Bell, AlertTriangle } from 'lucide-react';

export type AlertCondition = 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE' | 'DAILY_GAIN_LOSS' | 'VOLUME_SPIKE';

export interface PriceAlert {
  id?: number;
  symbol: string;
  condition: AlertCondition;
  targetPrice: number;
  portfolioId?: number;
  enabled?: boolean;
  triggered?: boolean;
}

interface AlertModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (alertData: PriceAlert) => Promise<void>;
  initialData?: PriceAlert | null;
  defaultSymbol?: string;
}

export const AlertModal: React.FC<AlertModalProps> = ({
  isOpen,
  onClose,
  onSave,
  initialData,
  defaultSymbol = '',
}) => {
  const [symbol, setSymbol] = useState('');
  const [condition, setCondition] = useState<AlertCondition>('PRICE_ABOVE');
  const [targetPrice, setTargetPrice] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (initialData) {
      setSymbol(initialData.symbol);
      setCondition(initialData.condition);
      setTargetPrice(initialData.targetPrice.toString());
    } else {
      setSymbol(defaultSymbol);
      setCondition('PRICE_ABOVE');
      setTargetPrice('');
    }
    setError('');
  }, [initialData, defaultSymbol, isOpen]);

  // Trap Escape key for accessibility
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const targetVal = parseFloat(targetPrice);
    if (!symbol.trim()) {
      setError('Symbol is required');
      return;
    }
    if (isNaN(targetVal) || targetVal <= 0) {
      setError('Target value must be a positive number');
      return;
    }

    setLoading(true);
    try {
      await onSave({
        id: initialData?.id,
        symbol: symbol.trim().toUpperCase(),
        condition,
        targetPrice: targetVal,
      });
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to save alert');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="alert-modal-title"
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
        padding: 'var(--space-4)',
        animation: 'fadeIn 0.2s var(--ease-custom)'
      }}
    >
      <Card elevation={3} style={{ width: '100%', maxWidth: '420px', padding: 'var(--space-5)', position: 'relative' }}>
        <button
          onClick={onClose}
          aria-label="Close Modal"
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

        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-4)' }}>
          <Bell size={18} style={{ color: 'var(--indigo-light)' }} />
          <h3 id="alert-modal-title" style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>
            {initialData ? 'Edit Price Alert' : 'Create Real-Time Price Alert'}
          </h3>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          <Input
            label="Ticker Symbol"
            type="text"
            placeholder="e.g. AAPL, TSLA"
            value={symbol}
            onChange={(e) => setSymbol(e.target.value)}
            required
            disabled={loading}
          />

          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-1)' }}>
            <label className="label-base">Alert Condition</label>
            <select
              className="input-base"
              value={condition}
              onChange={(e) => setCondition(e.target.value as AlertCondition)}
              disabled={loading}
            >
              <option value="PRICE_ABOVE">Price Above ($)</option>
              <option value="PRICE_BELOW">Price Below ($)</option>
              <option value="PERCENTAGE_CHANGE">Percentage Change (±%)</option>
              <option value="DAILY_GAIN_LOSS">Daily Gain/Loss ($)</option>
              <option value="VOLUME_SPIKE">Volume Spike (Shares)</option>
            </select>
          </div>

          <Input
            label={condition === 'PERCENTAGE_CHANGE' ? 'Target Change (%)' : 'Target Threshold Value'}
            type="number"
            step="any"
            placeholder="0.00"
            value={targetPrice}
            onChange={(e) => setTargetPrice(e.target.value)}
            required
            disabled={loading}
          />

          {error && (
            <div style={{ color: 'var(--red)', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <AlertTriangle size={12} />
              <span>{error}</span>
            </div>
          )}

          <Button type="submit" loading={loading} style={{ width: '100%', marginTop: 'var(--space-2)' }}>
            {initialData ? 'Update Alert' : 'Create Alert'}
          </Button>
        </form>
      </Card>
    </div>
  );
};
