import { Button, Card, Form, Input, Select, message, Typography } from 'antd';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { createRelationship, RelationshipType } from '../api/relationship';
import { useRelationshipThemeStore } from '../store/relationshipThemeStore';

interface CreateRelationshipValues {
  name: string;
  type: RelationshipType;
  description?: string;
}

export function CreateRelationship() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [messageApi, contextHolder] = message.useMessage();
  const fetchRelationshipThemeStatus = useRelationshipThemeStore((state) => state.fetchRelationshipThemeStatus);

  const handleSubmit = async (values: CreateRelationshipValues) => {
    try {
      const response = await createRelationship(values);
      await fetchRelationshipThemeStatus().catch(() => undefined);
      messageApi.success(t('relationship.createSuccess'));
      navigate(`/relationships/${response.data.data.id}`);
    } catch (error) {
      messageApi.error(t('relationship.createFailed'));
    }
  };

  return (
    <SpaceShell title={t('relationship.create')}>
      {contextHolder}
      <Card>
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label={t('relationship.name')}
            rules={[
              { required: true, message: t('relationship.nameRequired') },
              { max: 100, message: t('relationship.nameLength') },
            ]}
          >
            <Input placeholder={t('relationship.name')} />
          </Form.Item>
          <Form.Item name="type" label={t('relationship.type')} rules={[{ required: true, message: t('relationship.typeRequired') }]}>
            <Select
              placeholder={t('relationship.selectType')}
              options={[
                { value: 'COUPLE', label: 'COUPLE' },
                { value: 'FAMILY', label: 'FAMILY' },
                { value: 'FRIEND', label: 'FRIEND' },
                { value: 'ROOMMATE', label: 'ROOMMATE' },
                { value: 'CUSTOM', label: 'CUSTOM' },
              ]}
            />
          </Form.Item>
          <Form.Item name="description" label={t('relationship.description')} rules={[{ max: 500 }]}>
            <Input.TextArea rows={4} placeholder={t('relationship.optionalDescription')} />
          </Form.Item>
          <Button type="primary" htmlType="submit">
            {t('common.create')}
          </Button>
        </Form>
      </Card>
    </SpaceShell>
  );
}

function SpaceShell({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="page-narrow">
      <Typography.Title level={2}>{title}</Typography.Title>
      {children}
    </div>
  );
}
