import { Button, Text, View } from '@tarojs/components';
import './EmptyState.scss';

interface EmptyStateProps {
  title?: string;
  description?: string;
  actionText?: string;
  onAction?: () => void;
}

export function EmptyState({ title = '这里还没有内容', description, actionText, onAction }: EmptyStateProps) {
  return (
    <View className="empty-state">
      <Text className="empty-state__icon">☁️</Text>
      <Text className="empty-state__title">{title}</Text>
      {description ? <Text className="empty-state__description">{description}</Text> : null}
      {actionText ? (
        <Button className="ghost-button empty-state__button" onClick={onAction}>
          {actionText}
        </Button>
      ) : null}
    </View>
  );
}
