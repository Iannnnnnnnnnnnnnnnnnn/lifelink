import { Button, Image, Text, View } from '@tarojs/components';
import Taro, { useDidShow, useRouter } from '@tarojs/taro';
import { useState } from 'react';
import { getDailyPosts, likeDailyPost, unlikeDailyPost, type DailyPost } from '../../api/daily';
import { EmptyState } from '../../components/EmptyState';
import { PageShell } from '../../components/PageShell';
import { requireLogin } from '../../utils/auth';
import { formatDateTime, truncate } from '../../utils/format';
import { goDailyCreate } from '../../utils/navigation';
import './index.scss';

export default function DailyPage() {
  const router = useRouter();
  const relationshipId = router.params.relationshipId;
  const [posts, setPosts] = useState<DailyPost[]>([]);
  const [loading, setLoading] = useState(false);
  const [likingId, setLikingId] = useState<number>();

  async function loadPosts() {
    if (!requireLogin()) return;
    setLoading(true);
    try {
      const data = await getDailyPosts({ relationshipId, page: 1, size: 20 });
      setPosts(data);
    } finally {
      setLoading(false);
    }
  }

  async function toggleLike(item: DailyPost) {
    setLikingId(item.id);
    try {
      const result = item.likedByMe ? await unlikeDailyPost(item.id) : await likeDailyPost(item.id);
      setPosts((list) =>
        list.map((post) =>
          post.id === item.id
            ? { ...post, likeCount: result.likeCount, commentCount: result.commentCount, likedByMe: result.likedByMe }
            : post
        )
      );
    } finally {
      setLikingId(undefined);
    }
  }

  useDidShow(() => {
    loadPosts();
  });

  return (
    <PageShell>
      <View className="page daily-page">
        <View className="daily-toolbar">
          <Text className="daily-title">日常时间线</Text>
          <Button className="primary-button daily-create" onClick={() => goDailyCreate(relationshipId ? Number(relationshipId) : undefined)}>
            发布
          </Button>
        </View>

        {posts.length ? (
          posts.map((item) => (
            <View className="daily-card card" key={item.id}>
              <View className="daily-card__header">
                <Text className="daily-card__author">{item.username || '成员'}</Text>
                <Text className="muted">{formatDateTime(item.createdAt)}</Text>
              </View>
              <Text className="daily-card__space">{item.relationshipName || ''} {item.mood ? `· ${item.mood}` : ''}</Text>
              <Text className="daily-card__content">{truncate(item.content, 160)}</Text>
              {item.images?.length ? (
                <View className="daily-images">
                  {item.images.slice(0, 3).map((image) => (
                    <Image className="daily-image" key={image.fileId} src={image.url} mode="aspectFill" />
                  ))}
                </View>
              ) : null}
              <View className="daily-actions">
                <Button className="ghost-button daily-action" loading={likingId === item.id} onClick={() => toggleLike(item)}>
                  {item.likedByMe ? '已点赞' : '点赞'} {item.likeCount || 0}
                </Button>
                <Button className="ghost-button daily-action" onClick={() => Taro.showToast({ title: '评论详情后续完善', icon: 'none' })}>
                  评论 {item.commentCount || 0}
                </Button>
              </View>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '暂无日常'} actionText="发布日常" onAction={() => goDailyCreate()} />
        )}
      </View>
    </PageShell>
  );
}
