import { Button, Text, View } from '@tarojs/components';
import Taro, { useLoad, useRouter } from '@tarojs/taro';
import { useState } from 'react';
import { getRelationshipDetail, getRelationshipMembers, type RelationshipDetail } from '../../api/relationship';
import { EmptyState } from '../../components/EmptyState';
import { PageShell } from '../../components/PageShell';
import { requireLogin } from '../../utils/auth';
import { goDailyCreate, goTodos } from '../../utils/navigation';
import './detail.scss';

export default function RelationshipDetailPage() {
  const router = useRouter();
  const relationshipId = router.params.id || '';
  const [detail, setDetail] = useState<RelationshipDetail>();
  const [memberCount, setMemberCount] = useState(0);
  const [loading, setLoading] = useState(false);

  async function loadDetail() {
    if (!requireLogin() || !relationshipId) return;
    setLoading(true);
    try {
      const [detailResult, membersResult] = await Promise.allSettled([
        getRelationshipDetail(relationshipId),
        getRelationshipMembers(relationshipId)
      ]);
      if (detailResult.status === 'fulfilled') setDetail(detailResult.value);
      if (membersResult.status === 'fulfilled') setMemberCount(membersResult.value.length);
    } finally {
      setLoading(false);
    }
  }

  useLoad(() => {
    loadDetail();
  });

  if (!detail && !loading) {
    return (
      <PageShell>
        <View className="page">
          <EmptyState title="空间不存在或无权限访问" actionText="返回" onAction={() => Taro.navigateBack()} />
        </View>
      </PageShell>
    );
  }

  return (
    <PageShell>
      <View className="page relationship-detail-page">
        <View className="relationship-hero card">
          <Text className="relationship-hero__name">{detail?.name || '加载中...'}</Text>
          <Text className="relationship-hero__desc">{detail?.description || '暂无描述'}</Text>
          <View className="relationship-hero__meta">
            <Text>{detail?.type}</Text>
            <Text>{detail?.currentUserRole}</Text>
            <Text>{memberCount} 人</Text>
          </View>
        </View>

        <View className="section-title">
          <Text>功能入口</Text>
        </View>
        <View className="relationship-actions">
          <Button className="ghost-button" onClick={() => Taro.switchTab({ url: '/pages/daily/index' })}>
            日常
          </Button>
          <Button className="ghost-button" onClick={() => goTodos(Number(relationshipId))}>
            代办
          </Button>
          <Button className="ghost-button" onClick={() => Taro.switchTab({ url: '/pages/anniversaries/index' })}>
            纪念日
          </Button>
          <Button className="ghost-button" onClick={() => goDailyCreate(Number(relationshipId))}>
            发布日常
          </Button>
          <Button className="ghost-button" onClick={() => Taro.showToast({ title: '动态入口后续完善', icon: 'none' })}>
            动态
          </Button>
          <Button className="ghost-button" onClick={() => Taro.showToast({ title: '记账入口后续完善', icon: 'none' })}>
            记账
          </Button>
        </View>
      </View>
    </PageShell>
  );
}
