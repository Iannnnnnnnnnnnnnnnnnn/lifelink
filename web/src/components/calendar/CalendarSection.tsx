import { Typography } from 'antd';
import type { ReactNode } from 'react';

interface CalendarSectionProps {
  title: string;
  count: number;
  children: ReactNode;
}

export function CalendarSection({ title, count, children }: CalendarSectionProps) {
  return (
    <section className="calendar-section">
      <div className="calendar-section-header">
        <Typography.Title level={5}>{title}</Typography.Title>
        <span className="calendar-section-count">{count}</span>
      </div>
      <div className="calendar-section-body">{children}</div>
    </section>
  );
}
