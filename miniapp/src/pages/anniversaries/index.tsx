import { Image, Text, View } from '@tarojs/components';
import { useDidShow } from '@tarojs/taro';
import { useState } from 'react';
import { getAnniversaries, type Anniversary } from '../../api/anniversary';
import { EmptyState } from '../../components/EmptyState';
import { PageShell } from '../../components/PageShell';
import { useAppStore } from '../../store/appStore';
import { requireLogin } from '../../utils/auth';
import { getAnniversaryDisplayText } from '../../utils/format';
import './index.scss';

export default function AnniversariesPage() {
  const refreshRelationshipsAndTheme = useAppStore((state) => state.refreshRelationshipsAndTheme);
  const [items, setItems] = useState<Anniversary[]>([]);
  const [loading, setLoading] = useState(false);

  async function loadItems() {
    if (!requireLogin()) return;
    setLoading(true);
    try {
      await refreshRelationshipsAndTheme().catch(() => undefined);
      const data = await getAnniversaries({ page: 1, size: 30 });
      setItems(data);
    } finally {
      setLoading(false);
    }
  }

  useDidShow(() => {
    loadItems();
  });

  return (
    <PageShell>
      <View className="page anniversaries-page">
        <Text className="anniversaries-title">纪念日</Text>
        {items.length ? (
          items.map((item) => (
            <View className={`anniversary-card card anniversary-card--${item.displayType || 'COUNTDOWN'}`} key={item.id}>
              {item.backgroundUrl ? <Image className="anniversary-card__image" src={item.backgroundUrl} mode="aspectFill" /> : null}
              <View className="anniversary-card__mask">
                <Text className="anniversary-card__name">{item.title}</Text>
                <Text className="anniversary-card__count">{item.dayCount || 0}</Text>
                <Text className="anniversary-card__text">{getAnniversaryDisplayText(item)}</Text>
                <Text className="anniversary-card__space">{item.relationshipName || ''}</Text>
              </View>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '暂无纪念日'} />
        )}
      </View>
    </PageShell>
  );
}
