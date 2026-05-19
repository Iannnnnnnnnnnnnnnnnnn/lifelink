import { CalendarOutlined, DownOutlined, LeftOutlined, PlusOutlined, ReloadOutlined, RightOutlined } from '@ant-design/icons';
import { Button, Dropdown, Segmented, Select, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import type { TFunction } from 'i18next';
import type { Dayjs } from 'dayjs';
import type { RelationshipSummary } from '../../api/relationship';
import { formatMonthTitle } from './calendarUtils';

interface CalendarHeaderProps {
  relationships: RelationshipSummary[];
  relationshipId?: number;
  month: Dayjs;
  loading?: boolean;
  isMobile?: boolean;
  language?: string;
  t: TFunction;
  onRelationshipChange: (id: number) => void;
  onToday: () => void;
  onPreviousMonth: () => void;
  onNextMonth: () => void;
  onRefresh: () => void;
  onCreateEvent: () => void;
}

export function CalendarHeader({
  relationships,
  relationshipId,
  month,
  loading,
  isMobile,
  language,
  t,
  onRelationshipChange,
  onToday,
  onPreviousMonth,
  onNextMonth,
  onRefresh,
  onCreateEvent,
}: CalendarHeaderProps) {
  const moreItems: MenuProps['items'] = [
    { key: 'today', label: t('calendar.today'), onClick: onToday },
    { key: 'refresh', label: t('common.refresh'), onClick: onRefresh },
    { key: 'add', label: t('calendar.addEvent'), onClick: onCreateEvent },
  ];

  return (
    <div className="calendar-header">
      <div className="calendar-title-block">
        <Typography.Title level={1}>{t('calendar.title')}</Typography.Title>
        <Typography.Text>{t('calendar.subtitle')}</Typography.Text>
      </div>
      <div className="calendar-header-controls">
        <Select
          className="calendar-relationship-select"
          value={relationshipId}
          onChange={onRelationshipChange}
          options={relationships.map((item) => ({ value: item.id, label: item.name }))}
        />
        <div className="calendar-month-nav">
          {!isMobile && <Button onClick={onToday}>{t('calendar.today')}</Button>}
          <Button aria-label={t('calendar.previousMonth')} icon={<LeftOutlined />} onClick={onPreviousMonth} />
          <div className="calendar-current-month">{formatMonthTitle(month, language)}</div>
          <Button aria-label={t('calendar.nextMonth')} icon={<RightOutlined />} onClick={onNextMonth} />
        </div>
        <Segmented
          className="calendar-view-switch"
          value="month"
          options={[
            { label: t('calendar.viewMonth'), value: 'month' },
            { label: t('calendar.viewWeek'), value: 'week', disabled: true },
            { label: t('calendar.viewDay'), value: 'day', disabled: true },
          ]}
        />
        {isMobile ? (
          <Dropdown menu={{ items: moreItems }} trigger={['click']}>
            <Button icon={<DownOutlined />}>{t('calendar.moreActions')}</Button>
          </Dropdown>
        ) : (
          <Space>
            <Button icon={<ReloadOutlined />} loading={loading} onClick={onRefresh}>{t('common.refresh')}</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={onCreateEvent}>{t('calendar.addEvent')}</Button>
          </Space>
        )}
      </div>
      {isMobile && (
        <Button className="calendar-floating-add" type="primary" shape="circle" icon={<CalendarOutlined />} onClick={onCreateEvent} aria-label={t('calendar.addEvent')} />
      )}
    </div>
  );
}
