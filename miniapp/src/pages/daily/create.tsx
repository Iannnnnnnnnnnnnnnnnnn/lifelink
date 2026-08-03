import { Button, Image, Input, Picker, Text, Textarea, View } from '@tarojs/components';
import Taro, { useLoad, useRouter } from '@tarojs/taro';
import { useState } from 'react';
import { createDailyPost } from '../../api/daily';
import { uploadFile, type UploadFileResponse } from '../../api/file';
import { type RelationshipSummary } from '../../api/relationship';
import { PageShell } from '../../components/PageShell';
import { useAppStore } from '../../store/appStore';
import { requireLogin } from '../../utils/auth';
import './create.scss';

const moods = ['HAPPY', 'CALM', 'MISS', 'SAD', 'EXCITED'];

interface LocalImage {
  path: string;
  uploaded?: UploadFileResponse;
}

export default function DailyCreatePage() {
  const router = useRouter();
  const relationships = useAppStore((state) => state.relationships);
  const refreshRelationshipsAndTheme = useAppStore((state) => state.refreshRelationshipsAndTheme);
  const [relationshipList, setRelationshipList] = useState<RelationshipSummary[]>(relationships);
  const [relationshipIndex, setRelationshipIndex] = useState(0);
  const [content, setContent] = useState('');
  const [moodIndex, setMoodIndex] = useState(0);
  const [images, setImages] = useState<LocalImage[]>([]);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  async function loadRelationships() {
    if (!requireLogin()) return;
    const rels = relationships.length ? relationships : await refreshRelationshipsAndTheme();
    setRelationshipList(rels);
    const queryId = router.params.relationshipId;
    if (queryId) {
      const foundIndex = rels.findIndex((item) => String(item.id) === String(queryId));
      if (foundIndex >= 0) setRelationshipIndex(foundIndex);
    }
  }

  useLoad(() => {
    loadRelationships();
  });

  async function chooseImages() {
    const remain = 9 - images.length;
    if (remain <= 0) return;
    const result = await Taro.chooseImage({ count: remain, sizeType: ['compressed'], sourceType: ['album', 'camera'] });
    const accepted = result.tempFiles.filter((file) => file.size <= 10 * 1024 * 1024);
    if (accepted.length !== result.tempFiles.length) {
      Taro.showToast({ title: '单张图片不能超过 10MB', icon: 'none' });
    }

    setUploading(true);
    try {
      const uploaded = await Promise.all(accepted.map(async (file) => ({ path: file.path, uploaded: await uploadFile(file.path) })));
      setImages((list) => [...list, ...uploaded]);
    } finally {
      setUploading(false);
    }
  }

  async function submit() {
    const relationship = relationshipList[relationshipIndex];
    if (!relationship) {
      Taro.showToast({ title: '请选择关系空间', icon: 'none' });
      return;
    }
    if (!content.trim()) {
      Taro.showToast({ title: '请填写日常内容', icon: 'none' });
      return;
    }

    setSubmitting(true);
    try {
      await createDailyPost({
        relationshipId: relationship.id,
        content: content.trim(),
        mood: moods[moodIndex],
        visibility: 'RELATIONSHIP',
        imageIds: images.map((item) => item.uploaded?.fileId).filter((id): id is number => Boolean(id))
      });
      Taro.showToast({ title: '发布成功', icon: 'success' });
      setTimeout(() => Taro.switchTab({ url: '/pages/daily/index' }), 500);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PageShell>
      <View className="page daily-create-page">
        <View className="daily-form card">
          <Text className="daily-form__label">关系空间</Text>
          <Picker
            mode="selector"
            range={relationshipList.map((item) => item.name)}
            value={relationshipIndex}
            onChange={(event) => setRelationshipIndex(Number(event.detail.value))}
          >
            <View className="daily-form__picker">{relationshipList[relationshipIndex]?.name || '请选择关系空间'}</View>
          </Picker>

          <Text className="daily-form__label">心情</Text>
          <Picker mode="selector" range={moods} value={moodIndex} onChange={(event) => setMoodIndex(Number(event.detail.value))}>
            <View className="daily-form__picker">{moods[moodIndex]}</View>
          </Picker>

          <Text className="daily-form__label">内容</Text>
          <Textarea
            className="daily-form__textarea"
            value={content}
            maxlength={1000}
            placeholder="记录今天发生的事..."
            onInput={(event) => setContent(event.detail.value)}
          />

          <View className="daily-upload">
            {images.map((item, index) => (
              <View className="daily-upload__item" key={item.path}>
                <Image src={item.path} mode="aspectFill" className="daily-upload__image" />
                <Text className="daily-upload__remove" onClick={() => setImages((list) => list.filter((_, i) => i !== index))}>
                  ×
                </Text>
              </View>
            ))}
            {images.length < 9 ? (
              <Button className="daily-upload__add" loading={uploading} onClick={chooseImages}>
                +
              </Button>
            ) : null}
          </View>

          <Button className="primary-button daily-form__submit" loading={submitting} onClick={submit}>
            发布日常
          </Button>
        </View>
      </View>
    </PageShell>
  );
}
