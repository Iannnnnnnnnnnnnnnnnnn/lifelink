import {
  HistoryOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  PlusOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Button, Card, Drawer, Grid, message, Space, Typography } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useRef, useState } from 'react';
import type { Philosopher, PhilosophyChatMessage, PhilosophyChatSession } from '../../api/philosophy';
import {
  createChatSession,
  deleteChatSession,
  getChatSessionDetail,
  getChatSessions,
  sendChatMessage,
  updateChatSessionTitle,
} from '../../api/philosophy';
import { ChatInputBox } from './ChatInputBox';
import type { ChatDisplayMessage } from './ChatMessageBubble';
import { ChatMessageList } from './ChatMessageList';
import { ChatSessionList } from './ChatSessionList';
import { NewChatModal } from './NewChatModal';
import { PhilosopherProfileCard } from './PhilosopherProfileCard';
import { PhilosophyModeTabs } from './PhilosophyModeTabs';

interface CharacterChatPanelProps {
  philosophers: Philosopher[];
  language: 'zh-CN' | 'en-US';
  selectedSessionId?: number;
  onSessionChange: (sessionId?: number) => void;
  activeMode: string;
  onModeChange: (mode: string) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

function isHandledRequestError(error: unknown) {
  return Boolean((error as { __lifelinkHandled?: boolean })?.__lifelinkHandled);
}

const CHAT_PAGE_SIZE = 20;

export function CharacterChatPanel({
  philosophers,
  language,
  selectedSessionId,
  onSessionChange,
  activeMode,
  onModeChange,
  t,
}: CharacterChatPanelProps) {
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.lg;
  const [messageApi, contextHolder] = message.useMessage();
  const [sessions, setSessions] = useState<PhilosophyChatSession[]>([]);
  const [currentSession, setCurrentSession] = useState<PhilosophyChatSession | null>(null);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [historyCollapsed, setHistoryCollapsed] = useState(false);
  const [newChatOpen, setNewChatOpen] = useState(false);
  const [loadingSessions, setLoadingSessions] = useState(false);
  const [loadingMoreSessions, setLoadingMoreSessions] = useState(false);
  const [sessionPage, setSessionPage] = useState(1);
  const [hasMoreSessions, setHasMoreSessions] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [creating, setCreating] = useState(false);
  const [sending, setSending] = useState(false);
  const optimisticIdRef = useRef(-1);
  const detailRequestIdRef = useRef(0);
  const openingSessionIdRef = useRef<number | undefined>(undefined);

  const philosopherMap = useMemo(() => new Map(philosophers.map((item) => [item.code, item])), [philosophers]);
  const currentPhilosopher = currentSession ? philosopherMap.get(currentSession.philosopherCode) : undefined;
  const isCounselorChat = currentSession?.philosopherCode === 'PSYCHOLOGY_TEACHER'
    || currentPhilosopher?.responseLayout === 'COUNSELOR_CARD';

  const loadSessions = async (page = 1, append = false) => {
    append ? setLoadingMoreSessions(true) : setLoadingSessions(true);
    try {
      const response = await getChatSessions({ page, size: CHAT_PAGE_SIZE });
      const items = response.data.data;
      setSessions((previous) => {
        if (!append) {
          return items;
        }
        const knownIds = new Set(previous.map((session) => session.id));
        return [...previous, ...items.filter((session) => !knownIds.has(session.id))];
      });
      setSessionPage(page);
      setHasMoreSessions(items.length === CHAT_PAGE_SIZE);
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('philosophy.loadChatFailed'));
      }
    } finally {
      setLoadingSessions(false);
      setLoadingMoreSessions(false);
    }
  };

  useEffect(() => {
    void loadSessions();
  }, []);

  const openSession = async (id: number, syncUrl = true) => {
    if (currentSession?.id === id) {
      setHistoryOpen(false);
      if (syncUrl) {
        onSessionChange(id);
      }
      return;
    }
    if (openingSessionIdRef.current === id) {
      return;
    }
    openingSessionIdRef.current = id;
    const requestId = ++detailRequestIdRef.current;
    setLoadingDetail(true);
    try {
      const response = await getChatSessionDetail(id);
      if (requestId !== detailRequestIdRef.current) {
        return;
      }
      setCurrentSession(response.data.data);
      setHistoryOpen(false);
      if (syncUrl) {
        onSessionChange(id);
      }
    } catch (error) {
      if (requestId !== detailRequestIdRef.current) {
        return;
      }
      if (!isHandledRequestError(error)) {
        messageApi.error(t('philosophy.loadChatFailed'));
      }
      if (!syncUrl) {
        onSessionChange(undefined);
      }
    } finally {
      if (requestId === detailRequestIdRef.current) {
        openingSessionIdRef.current = undefined;
        setLoadingDetail(false);
      }
    }
  };

  useEffect(() => {
    if (!selectedSessionId) {
      detailRequestIdRef.current += 1;
      openingSessionIdRef.current = undefined;
      setLoadingDetail(false);
      setCurrentSession(null);
      return;
    }
    if (currentSession?.id !== selectedSessionId) {
      void openSession(selectedSessionId, false);
    }
  }, [selectedSessionId]);

  const startChat = async (philosopherCode: string) => {
    setCreating(true);
    try {
      const response = await createChatSession({ philosopherCode, language });
      const session = response.data.data;
      setCurrentSession(session);
      setNewChatOpen(false);
      onSessionChange(session.id);
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
        onSessionChange(undefined);
      }
      await loadSessions();
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('message.operationFailed'));
      }
    }
  };

  const renameSession = async (id: number, title: string) => {
    try {
      const response = await updateChatSessionTitle(id, { title });
      setSessions((previous) => previous.map((session) => session.id === id ? response.data.data : session));
      setCurrentSession((session) => session?.id === id ? { ...session, title: response.data.data.title } : session);
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(t('message.operationFailed'));
      }
      throw error;
    }
  };

  const sendMessage = async (content: string) => {
    if (!currentSession) {
      setNewChatOpen(true);
      return;
    }
    if (sending) {
      return;
    }

    const sessionId = currentSession.id;
    const userMessageId = optimisticIdRef.current--;
    const pendingMessageId = optimisticIdRef.current--;
    const createdAt = new Date().toISOString();
    const optimisticUserMessage: ChatDisplayMessage = {
      id: userMessageId,
      role: 'USER',
      content,
      createdAt,
    };
    const pendingMessage: ChatDisplayMessage = {
      id: pendingMessageId,
      role: 'ASSISTANT',
      content: t('philosophy.thinking'),
      createdAt,
      deliveryState: 'pending',
      retryContent: content,
    };

    setCurrentSession((session) => session?.id === sessionId
      ? { ...session, messages: [...session.messages, optimisticUserMessage, pendingMessage] }
      : session);
    setSending(true);
    try {
      const response = await sendChatMessage(sessionId, { content });
      const { userMessage, assistantMessage } = response.data.data;
      setCurrentSession((session) => {
        if (!session || session.id !== sessionId) {
          return session;
        }
        const nextMessages = (session.messages as ChatDisplayMessage[]).map((chatMessage) => {
          if (chatMessage.id === userMessageId) {
            return userMessage;
          }
          if (chatMessage.id === pendingMessageId) {
            return assistantMessage;
          }
          return chatMessage;
        });
        return {
          ...session,
          messages: nextMessages,
          lastMessageAt: assistantMessage.createdAt,
          lastMessagePreview: assistantMessage.content,
        };
      });
      await loadSessions();
    } catch (error) {
      setCurrentSession((session) => {
        if (!session || session.id !== sessionId) {
          return session;
        }
        return {
          ...session,
          messages: (session.messages as ChatDisplayMessage[]).map((chatMessage) => chatMessage.id === pendingMessageId
            ? {
              ...chatMessage,
              content: t('philosophy.sendFailed'),
              deliveryState: 'failed',
              retryContent: content,
            }
            : chatMessage) as PhilosophyChatMessage[],
        };
      });
      if (!isHandledRequestError(error)) {
        messageApi.error(t('philosophy.sendFailed'));
      }
    } finally {
      setSending(false);
    }
  };

  const retryMessage = async (content: string, failedMessageId: number) => {
    setCurrentSession((session) => {
      if (!session) {
        return session;
      }
      const failedIndex = session.messages.findIndex((chatMessage) => chatMessage.id === failedMessageId);
      const optimisticUserId = failedIndex > 0 && session.messages[failedIndex - 1].role === 'USER'
        && session.messages[failedIndex - 1].id < 0
        ? session.messages[failedIndex - 1].id
        : undefined;
      return {
        ...session,
        messages: session.messages.filter((chatMessage) => chatMessage.id !== failedMessageId
          && chatMessage.id !== optimisticUserId),
      };
    });
    await sendMessage(content);
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
      loadingMore={loadingMoreSessions}
      hasMore={hasMoreSessions}
      selectedId={currentSession?.id}
      onOpen={(id) => void openSession(id)}
      onDelete={(id) => void deleteSession(id)}
      onRename={renameSession}
      onLoadMore={() => void loadSessions(sessionPage + 1, true)}
      t={t}
      formatDate={formatDate}
    />
  );

  const title = currentSession?.title || t('philosophy.characterChat');

  return (
    <div className={`character-chat-panel${isMobile ? ' mobile' : ''}${historyCollapsed ? ' history-collapsed' : ''}`}>
      {contextHolder}
      {!isMobile && !historyCollapsed && (
        <aside className="chat-history-sidebar">
          <div className="chat-history-heading">
            <Typography.Text strong>{t('philosophy.chatHistory')}</Typography.Text>
            <Button
              type="text"
              icon={<MenuFoldOutlined />}
              aria-label={t('menu.collapseSidebar')}
              onClick={() => setHistoryCollapsed(true)}
            />
          </div>
          {historyList}
        </aside>
      )}

      <Card className="chat-window-card">
        <div className="chat-workspace-toolbar">
          <div className="chat-toolbar-start">
            {isMobile ? (
              <Button
                type="text"
                icon={<HistoryOutlined />}
                aria-label={t('philosophy.chatHistory')}
                onClick={() => setHistoryOpen(true)}
              />
            ) : historyCollapsed ? (
              <Button
                type="text"
                icon={<MenuUnfoldOutlined />}
                aria-label={t('menu.expandSidebar')}
                onClick={() => setHistoryCollapsed(false)}
              />
            ) : null}
            <PhilosophyModeTabs activeKey={activeMode} onChange={onModeChange} t={t} />
          </div>

          <div className="chat-window-identity">
            <Avatar className="philosopher-avatar" src={currentPhilosopher?.avatarUrl || undefined}>
              {(currentSession?.philosopherName || 'AI').slice(0, 1).toUpperCase()}
            </Avatar>
            <div>
              <Typography.Title level={4} ellipsis={{ tooltip: title }}>{title}</Typography.Title>
              <Typography.Text type="secondary">
                {isCounselorChat
                  ? t('philosophy.psychologyTeacherSubtitle')
                  : currentSession?.philosopherName || t('philosophy.emptyChatTitle')}
              </Typography.Text>
            </div>
          </div>

          <Space className="chat-toolbar-actions" size={6}>
            <Button
              type="text"
              icon={<UserOutlined />}
              disabled={!currentPhilosopher}
              aria-label={t('philosophy.characterProfile')}
              onClick={() => setProfileOpen(true)}
            >
              <span className="chat-toolbar-button-label">{t('philosophy.characterProfile')}</span>
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setNewChatOpen(true)}>
              <span className="chat-toolbar-button-label">{t('philosophy.newChat')}</span>
            </Button>
          </Space>
        </div>

        <div className="chat-disclaimer">
          {isCounselorChat ? t('philosophy.counselorDisclaimer') : t('philosophy.chatDisclaimer')}
        </div>
        <ChatMessageList
          messages={(currentSession?.messages || []) as ChatDisplayMessage[]}
          loading={loadingDetail}
          sessionId={currentSession?.id}
          philosopherName={currentSession?.philosopherName}
          philosopherCode={currentSession?.philosopherCode}
          onCopy={copyMessage}
          onRetry={(content, failedMessageId) => void retryMessage(content, failedMessageId)}
          onStartChat={() => setNewChatOpen(true)}
          retrying={sending}
          t={t}
        />
        <ChatInputBox
          disabled={!currentSession}
          loading={sending}
          sessionId={currentSession?.id}
          onSend={sendMessage}
          t={t}
        />
      </Card>

      <Drawer
        title={t('philosophy.chatHistory')}
        placement="left"
        width={320}
        open={historyOpen}
        onClose={() => setHistoryOpen(false)}
      >
        {historyList}
      </Drawer>

      <Drawer
        title={t('philosophy.characterProfile')}
        placement="right"
        width={360}
        open={profileOpen}
        onClose={() => setProfileOpen(false)}
      >
        <PhilosopherProfileCard philosopher={currentPhilosopher} t={t} />
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
