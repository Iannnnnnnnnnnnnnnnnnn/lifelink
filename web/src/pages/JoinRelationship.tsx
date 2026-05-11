import { Button, Card, Form, Input, message, Typography } from 'antd';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { joinRelationship } from '../api/relationship';
import { useRelationshipThemeStore } from '../store/relationshipThemeStore';

interface JoinRelationshipValues {
  inviteCode: string;
}

export function JoinRelationship() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();
  const fetchRelationshipThemeStatus = useRelationshipThemeStore((state) => state.fetchRelationshipThemeStatus);

  const handleSubmit = async (values: JoinRelationshipValues) => {
    try {
      const response = await joinRelationship({ inviteCode: values.inviteCode.trim().toUpperCase() });
      await fetchRelationshipThemeStatus().catch(() => undefined);
      messageApi.success(t('relationship.joinSuccess'));
      navigate(`/relationships/${response.data.data.id}`);
    } catch (error) {
      messageApi.error(t('relationship.joinFailed'));
    }
  };

  return (
    <div className="page-narrow">
      {contextHolder}
      <Typography.Title level={2}>{t('relationship.join')}</Typography.Title>
      <Card>
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="inviteCode" label={t('relationship.inviteCode')} rules={[{ required: true, message: t('relationship.inviteCodeRequired') }]}>
            <Input placeholder={t('relationship.inviteCodePlaceholder')} />
          </Form.Item>
          <Button type="primary" htmlType="submit">
            {t('relationship.join')}
          </Button>
        </Form>
      </Card>
    </div>
  );
}
