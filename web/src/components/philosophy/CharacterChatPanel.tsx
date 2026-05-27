import { HistoryOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Drawer, Grid, message, Space, Typography } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import type { Philosopher, PhilosophyChatSession } from '../../api/philosophy';
import {
  createChatSession,
  deleteChatSession,
  getChatSessionDetail,
  getChatSessions,
  sendChatMessage,
} from '../../api/philosophy';
import { ChatInputBox } from './ChatInputBox';
import { ChatMessageList } from './ChatMessageList';
import { ChatSessionList } from './ChatSessionList';
import { NewChatModal } from './NewChatModal';
import { PhilosopherProfileCard } from './PhilosopherProfileCard';

interface CharacterChatPanelProps {
  philosophers: Philosopher[];
  language: 'zh-CN' | 'en-US';
  t: (key: string, options?: Record<string, unknown>) => string;
}

function isHandledRequestError(error: unknown) {
  return Boolean((error as { __lifelinkHandled?: boolean })?.__lifelinkHandled);
}

export function CharacterChatPanel({ philosophers, language, t }: CharacterChatPanelProps) {
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.lg;
  const [messageApi, contextHolder] = message.useMessage();
  const [sessions, setSessions] = useState<PhilosophyChatSession[]>([]);
  const [currentSession, setCurrentSession] = useState<PhilosophyChatSession | null>(null);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [newChatOpen, setNewChatOpen] = useState(false);
  const [loadingSessions, setLoadingSessions] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [creating, setCreating] = useState(false);
  const [sending, setSending] = useState(false);

  const philosopherMap = useMemo(() => new Map(philosophers.map((item) => [item.code, item])), [philosophers]);
  const currentPhilosopher = currentSession ? philosopherMap.get(currentSession.philosopherCode) : undefined;
  const isCounselorChat = currentSession?.philosopherCode === 'PSYCHOLOGY_TEACHER'
    || currentPhilosopher?.responseLayout === 'COUNSELOR_CARD';

  const loadSessions = async () => {
    setLoadingSessions(true);
    try {
      const response = await getChatSessions({ page: 1, size: 20 });
      setSessions(response.data.data);
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('philosophy.loadChatFailed'));
      }
    } finally {
      setLoadingSessions(false);
    }
  };

  useEffect(() => {
    loadSessions();
  }, []);

  const openSession = async (id: number) => {
    setLoadingDetail(true);
    try {
      const response = await getChatSessionDetail(id);
      setCurrentSession(response.data.data);
      setHistoryOpen(false);
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('philosophy.loadChatFailed'));
      }
    } finally {
      setLoadingDetail(false);
    }
  };

  const startChat = async (philosopherCode: string) => {
    setCreating(true);
    try {
      const response = await createChatSession({ philosopherCode, language });
      setCurrentSession(response.data.data);
      setNewChatOpen(false);
      await loadSessions();
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('message.operationFailed'));
      }
    } finally {
      setCreating(false);
    }
  };

  const deleteSession = async (id: number) => {
    try {
      await deleteChatSession(id);
      messageApi.success(t('philosophy.deleteChatSuccess'));
      if (currentSession?.id === id) {
        setCurrentSession(null);
      }
      await loadSessions();
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('message.operationFailed'));
      }
    }
  };

  const sendMessage = async (content: string) => {
    if (!currentSession) {
      setNewChatOpen(true);
      return;
    }
    setSending(true);
    try {
      const response = await sendChatMessage(currentSession.id, { content });
      setCurrentSession((session) => {
        if (!session) {
          return session;
        }
        return {
          ...session,
          messages: [...session.messages, response.data.data.userMessage, response.data.data.assistantMessage],
          lastMessageAt: response.data.data.assistantMessage.createdAt,
          lastMessagePreview: response.data.data.assistantMessage.content,
        };
      });
      await loadSessions();
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('philosophy.sendFailed'));
      }
    } finally {
      setSending(false);
    }
  };

  const copyMessage = async (content: string) => {
    try {
      await navigator.clipboard.writeText(content);
      messageApi.success(t('philosophy.copySuccess'));
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    }
  };

  const formatDate = (value?: string) => {
    if (!value) {
      return '';
    }
    return dayjs(value).format(language === 'en-US' ? 'MMM D, HH:mm' : 'M月D日 HH:mm');
  };

  const historyList = (
    <ChatSessionList
      sessions={sessions}
      loading={loadingSessions}
      selectedId={currentSession?.id}
      onOpen={openSession}
      onDelete={deleteSession}
      t={t}
      formatDate={formatDate}
    />
  );

  return (
    <div className={isMobile ? 'character-chat-panel mobile' : 'character-chat-panel'}>
      {contextHolder}
      {!isMobile && (
        <Card className="chat-history-card" title={t('philosophy.chatHistory')}>
          {historyList}
        </Card>
      )}

      <Card className="chat-window-card">
        <div className="chat-window-header">
          <div>
            <Typography.Title level={4}>
              {currentSession?.title || t('philosophy.characterChat')}
            </Typography.Title>
            <Typography.Text type="secondary">
              {isCounselorChat
                ? t('philosophy.psychologyTeacherSubtitle')
                : currentSession?.philosopherName || t('philosophy.emptyChatTitle')}
            </Typography.Text>
          </div>
          <Space>
            {isMobile && (
              <Button icon={<HistoryOutlined />} onClick={() => setHistoryOpen(true)}>
                {t('philosophy.chatHistory')}
              </Button>
            )}
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setNewChatOpen(true)}>
              {t('philosophy.newChat')}
            </Button>
          </Space>
        </div>
        <div className="chat-disclaimer">
          {isCounselorChat ? t('philosophy.counselorDisclaimer') : t('philosophy.chatDisclaimer')}
        </div>
        <ChatMessageList
          messages={currentSession?.messages || []}
          loading={loadingDetail}
          thinking={sending}
          philosopherName={currentSession?.philosopherName}
          philosopherCode={currentSession?.philosopherCode}
          onCopy={copyMessage}
          t={t}
        />
        <ChatInputBox
          disabled={!currentSession}
          loading={sending}
          onSend={sendMessage}
          t={t}
        />
      </Card>

      {!isMobile && (
        <aside className="chat-profile-aside">
          <PhilosopherProfileCard philosopher={currentPhilosopher} t={t} />
        </aside>
      )}

      <Drawer
        title={t('philosophy.chatHistory')}
        placement="left"
        width={320}
        open={historyOpen}
        onClose={() => setHistoryOpen(false)}
      >
        {historyList}
      </Drawer>

      <NewChatModal
        open={newChatOpen}
        philosophers={philosophers}
        loading={creating}
        onCancel={() => setNewChatOpen(false)}
        onStart={startChat}
        t={t}
      />
    </div>
  );
}
