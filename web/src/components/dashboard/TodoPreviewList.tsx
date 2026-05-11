import { Checkbox, List, Skeleton, Space, Tag, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { SpaceTodo } from '../../api/spaceTodo';
import { EmptyState } from '../decorations/EmptyState';

interface TodoPreviewListProps {
  items: Array<SpaceTodo & { relationshipName?: string }>;
  loading: boolean;
  togglingIds: number[];
  onToggle: (todo: SpaceTodo & { relationshipName?: string }) => void;
}

export function TodoPreviewList({ items, loading, togglingIds, onToggle }: TodoPreviewListProps) {
  const { t } = useTranslation();

  if (loading) {
    return <Skeleton active paragraph={{ rows: 4 }} />;
  }

  if (items.length === 0) {
    return <EmptyState description={t('dashboard.noTodos')} />;
  }

  return (
    <List
      className="dashboard-list"
      dataSource={items.slice(0, 5)}
      renderItem={(todo) => (
        <List.Item className="dashboard-list-item">
          <List.Item.Meta
            avatar={<Checkbox checked={todo.status === 'DONE'} disabled={togglingIds.includes(todo.id)} onChange={() => onToggle(todo)} />}
            title={
              <Space wrap>
                <Typography.Text strong>{todo.title}</Typography.Text>
                <Tag color={todo.priority === 'HIGH' ? 'red' : todo.priority === 'LOW' ? 'default' : 'blue'}>{todo.priority}</Tag>
              </Space>
            }
            description={`${todo.relationshipName || '-'} · ${t('common.due')}: ${todo.dueTime || '-'}`}
          />
        </List.Item>
      )}
    />
  );
}
