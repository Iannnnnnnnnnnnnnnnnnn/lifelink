import { Empty, Skeleton } from 'antd';
import type { Philosopher, PhilosophyResponseItem } from '../../api/philosophy';
import { PhilosopherCard } from './PhilosopherCard';

interface PhilosophyResultGridProps {
  items: PhilosophyResponseItem[];
  philosophers: Philosopher[];
  loading: boolean;
  onCopy: (item: PhilosophyResponseItem) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function PhilosophyResultGrid({ items, philosophers, loading, onCopy, t }: PhilosophyResultGridProps) {
  const philosopherMap = new Map(philosophers.map((philosopher) => [philosopher.code, philosopher]));

  if (loading) {
    return (
      <div className="philosophy-result-grid">
        {[1, 2, 3].map((item) => (
          <div className="philosopher-card philosopher-card-loading" key={item}>
            <Skeleton active paragraph={{ rows: 6 }} />
          </div>
        ))}
      </div>
    );
  }

  if (!items.length) {
    return (
      <div className="philosophy-empty-result">
        <Empty description={t('philosophy.emptyResult')} />
      </div>
    );
  }

  return (
    <div className="philosophy-result-grid">
      {items.map((item) => (
        <PhilosopherCard
          key={item.philosopherCode}
          item={item}
          philosopher={philosopherMap.get(item.philosopherCode)}
          onCopy={onCopy}
          t={t}
        />
      ))}
    </div>
  );
}
