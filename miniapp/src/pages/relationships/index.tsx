import { Button, Text, View } from '@tarojs/components';
import Taro, { useDidShow } from '@tarojs/taro';
import { useState } from 'react';
import { EmptyState } from '../../components/EmptyState';
import { PageShell } from '../../components/PageShell';
import { useAppStore } from '../../store/appStore';
import { requireLogin } from '../../utils/auth';
import { formatDateTime } from '../../utils/format';
import { goRelationshipDetail } from '../../utils/navigation';
import './index.scss';

export default function RelationshipsPage() {
  const relationships = useAppStore((state) => state.relationships);
  const refreshRelationshipsAndTheme = useAppStore((state) => state.refreshRelationshipsAndTheme);
  const [loading, setLoading] = useState(false);

  async function loadRelationships() {
    if (!requireLogin()) return;
    setLoading(true);
    try {
      await refreshRelationshipsAndTheme();
    } finally {
      setLoading(false);
    }
  }

  useDidShow(() => {
    loadRelationships();
  });

  return (
    <PageShell>
      <View className="page relationships-page">
        <View className="relationships-header">
          <Text className="relationships-title">关系空间</Text>
          <Text className="relationships-desc">查看你加入的关系空间。</Text>
        </View>

        {relationships.length ? (
          relationships.map((item) => (
            <View className="relationship-card card" key={item.id} onClick={() => goRelationshipDetail(item.id)}>
              <View className="relationship-card__top">
                <Text className="relationship-card__name">{item.name}</Text>
                <Text className="relationship-card__tag">{item.type}</Text>
              </View>
              <Text className="relationship-card__desc">{item.description || '暂无描述'}</Text>
              <View className="relationship-card__meta">
                <Text>{item.currentUserRole || 'MEMBER'}</Text>
                <Text>{formatDateTime(item.createdAt)}</Text>
              </View>
            </View>
          ))
        ) : (
          <EmptyState
            title={loading ? '加载中...' : '还没有关系空间'}
            description="可以先在 Web 端创建或加入关系空间。"
          />
        )}

        <Button className="primary-button relationships-create" onClick={() => Taro.showToast({ title: '小程序创建/加入入口后续完善', icon: 'none' })}>
          创建或加入空间
        </Button>
      </View>
    </PageShell>
  );
}
