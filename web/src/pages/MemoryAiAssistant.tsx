import { DatabaseOutlined, ReloadOutlined, RobotOutlined, SendOutlined, UserOutlined } from '@ant-design/icons';
import { Alert, Avatar, Button, Card, Input, List, Space, Spin, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { askMemoryAssistant, buildMemoryKnowledgeBase, type MemoryReference } from '../api/memoryAi';
import { RelationshipSubNav } from '../components/navigation/RelationshipSubNav';

interface ChatMessage {
  id: number;
  question: string;
  answer: string;
  references: MemoryReference[];
}

const copy = {
  title: '\u8bb0\u5fc6 AI \u52a9\u624b',
  subtitle: '\u53ea\u57fa\u4e8e\u8fd9\u4e2a\u5173\u7cfb\u7a7a\u95f4\u5df2\u4fdd\u5b58\u7684\u8bb0\u5fc6\u56de\u7b54\uff0c\u4e0d\u4f1a\u8865\u5145\u6216\u7f16\u9020\u5185\u5bb9\u3002',
  rebuild: '\u91cd\u65b0\u6784\u5efa\u8bb0\u5fc6\u5e93',
  buildHintTitle: '\u9996\u6b21\u4f7f\u7528\u8bf7\u5148\u6784\u5efa\u8bb0\u5fc6\u5e93',
  buildHint: '\u6784\u5efa\u4f1a\u8bfb\u53d6\u8fd9\u4e2a\u7a7a\u95f4\u7684\u65e5\u5e38\u3001\u7eaa\u5ff5\u65e5\u3001\u5f85\u529e\u3001\u8d26\u5355\u548c\u8bc4\u8bba\uff0c\u5e76\u66f4\u65b0\u4e3a\u53ef\u68c0\u7d22\u7684\u4e2a\u4eba\u8bb0\u5fc6\u3002',
  empty: '\u60f3\u4ece\u5171\u540c\u8bb0\u5fc6\u91cc\u4e86\u89e3\u4ec0\u4e48\uff1f',
  references: '\u5f15\u7528\u8bb0\u5fc6',
  placeholder: '\u4f8b\u5982\uff1a\u6211\u4eec\u7b2c\u4e00\u6b21\u65c5\u884c\u662f\u4ec0\u4e48\u65f6\u5019\uff1f',
  send: '\u53d1\u9001\u95ee\u9898',
  buildSuccessPrefix: '\u5df2\u6574\u7406 ',
  buildSuccessMiddle: ' \u6761\u8bb0\u5fc6\uff0c\u751f\u6210 ',
  buildSuccessSuffix: ' \u4e2a\u77e5\u8bc6\u7247\u6bb5\u3002',
};

const suggestions = [
  '\u6211\u4eec\u7b2c\u4e00\u6b21\u65c5\u884c\u662f\u4ec0\u4e48\u65f6\u5019\uff1f',
  '\u6211\u4eec\u6709\u54ea\u4e9b\u91cd\u8981\u7eaa\u5ff5\u65e5\uff1f',
  '\u6700\u8fd1\u4e00\u4e2a\u6708\u53d1\u751f\u4e86\u4ec0\u4e48\uff1f',
];

export function MemoryAiAssistant() {
  const { relationshipId: relationshipIdParam } = useParams();
  const relationshipId = Number(relationshipIdParam);
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [asking, setAsking] = useState(false);
  const [building, setBuilding] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const handleBuild = async () => {
    setBuilding(true);
    try {
      const response = await buildMemoryKnowledgeBase(relationshipId);
      const result = response.data.data;
      messageApi.success(`${copy.buildSuccessPrefix}${result.sourceCount}${copy.buildSuccessMiddle}${result.chunkCount}${copy.buildSuccessSuffix}`);
    } catch {
      // The shared request layer has already shown the server error.
    } finally {
      setBuilding(false);
    }
  };

  const handleAsk = async (value = question) => {
    const currentQuestion = value.trim();
    if (!currentQuestion || asking) return;
    setQuestion('');
    setAsking(true);
    try {
      const response = await askMemoryAssistant(relationshipId, currentQuestion);
      const result = response.data.data;
      setMessages((items) => [...items, {
        id: Date.now(),
        question: currentQuestion,
        answer: result.answer,
        references: result.references,
      }]);
    } catch {
      setQuestion(currentQuestion);
    } finally {
      setAsking(false);
    }
  };

  return (
    <Space direction="vertical" size={16} className="page-wide">
      {contextHolder}
      <RelationshipSubNav relationshipId={relationshipId} />
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{copy.title}</Typography.Title>
          <Typography.Text type="secondary">{copy.subtitle}</Typography.Text>
        </div>
        <Button icon={<ReloadOutlined />} loading={building} onClick={handleBuild}>{copy.rebuild}</Button>
      </div>

      <Alert type="info" showIcon message={copy.buildHintTitle} description={copy.buildHint} />

      <Card className="todo-list-card">
        {messages.length === 0 ? (
          <Space direction="vertical" size={16} className="full-width" align="center">
            <Avatar size={52} icon={<RobotOutlined />} />
            <Typography.Title level={4}>{copy.empty}</Typography.Title>
            <Space wrap>
              {suggestions.map((item) => <Button key={item} onClick={() => handleAsk(item)} disabled={asking}>{item}</Button>)}
            </Space>
          </Space>
        ) : (
          <List
            dataSource={messages}
            split={false}
            renderItem={(item) => (
              <List.Item key={item.id}>
                <Space direction="vertical" size={12} className="full-width">
                  <Card size="small"><Space align="start"><Avatar icon={<UserOutlined />} /><Typography.Text>{item.question}</Typography.Text></Space></Card>
                  <Card size="small" className="relationship-timeline-card">
                    <Space align="start" className="full-width">
                      <Avatar icon={<RobotOutlined />} />
                      <Space direction="vertical" size={10} className="full-width">
                        <Typography.Paragraph className="timeline-description">{item.answer}</Typography.Paragraph>
                        <div>
                          <Typography.Text type="secondary">{copy.references}</Typography.Text>
                          <List size="small" dataSource={item.references} renderItem={(reference) => (
                            <List.Item><Space direction="vertical" size={4}><Tag icon={<DatabaseOutlined />}>{reference.sourceType}</Tag><Typography.Text type="secondary">{reference.content}</Typography.Text></Space></List.Item>
                          )} />
                        </div>
                      </Space>
                    </Space>
                  </Card>
                </Space>
              </List.Item>
            )}
          />
        )}
        {asking && <Spin className="full-width" />}
      </Card>

      <Card>
        <Input.TextArea value={question} rows={3} maxLength={1000} showCount placeholder={copy.placeholder}
          onChange={(event) => setQuestion(event.target.value)}
          onPressEnter={(event) => {
            if (!event.shiftKey) {
              event.preventDefault();
              handleAsk();
            }
          }} />
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 12 }}>
          <Button type="primary" icon={<SendOutlined />} loading={asking} onClick={() => handleAsk()}>{copy.send}</Button>
        </div>
      </Card>
    </Space>
  );
}
