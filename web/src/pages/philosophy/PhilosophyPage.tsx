import { BulbOutlined } from '@ant-design/icons';
import { Card, Grid, message, Typography } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router-dom';
import type { Philosopher, PhilosophyResponseItem, PhilosophySession } from '../../api/philosophy';
import {
  createPhilosophySession,
  deletePhilosophySession,
  getPhilosophers,
  getPhilosophySessionDetail,
  getPhilosophySessions,
} from '../../api/philosophy';
import { CharacterChatPanel } from '../../components/philosophy/CharacterChatPanel';
import { PhilosophyDisclaimer } from '../../components/philosophy/PhilosophyDisclaimer';
import { PhilosophyHistoryList } from '../../components/philosophy/PhilosophyHistoryList';
import { PhilosophyInputPanel } from '../../components/philosophy/PhilosophyInputPanel';
import { PhilosophyModeTabs } from '../../components/philosophy/PhilosophyModeTabs';
import { PhilosophyResultGrid } from '../../components/philosophy/PhilosophyResultGrid';

function isHandledRequestError(error: unknown) {
  return Boolean((error as { __lifelinkHandled?: boolean })?.__lifelinkHandled);
}

export function PhilosophyPage() {
  const { t, i18n } = useTranslation();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.lg;
  const [searchParams, setSearchParams] = useSearchParams();
  const [messageApi, contextHolder] = message.useMessage();
  const [philosophers, setPhilosophers] = useState<Philosopher[]>([]);
  const [question, setQuestion] = useState('');
  const [selectedCodes, setSelectedCodes] = useState<string[]>([]);
  const [currentSession, setCurrentSession] = useState<PhilosophySession | null>(null);
  const [history, setHistory] = useState<PhilosophySession[]>([]);
  const [loadingPhilosophers, setLoadingPhilosophers] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const activeMode = searchParams.get('mode') === 'chat' ? 'chat' : 'perspectives';
  const chatSessionIdValue = Number(searchParams.get('sessionId'));
  const selectedChatSessionId = Number.isInteger(chatSessionIdValue) && chatSessionIdValue > 0
    ? chatSessionIdValue
    : undefined;

  const language = i18n.resolvedLanguage === 'en-US' ? 'en-US' : 'zh-CN';
  const resultItems = currentSession?.responses || [];
  const tString = (key: string, options?: Record<string, unknown>) => String(t(key, options));

  const selectedSessionId = currentSession?.id;
  const defaultCodes = useMemo(() => philosophers.slice(0, 3).map((philosopher) => philosopher.code), [philosophers]);

  const loadPhilosophers = async () => {
    setLoadingPhilosophers(true);
    try {
      const response = await getPhilosophers(language);
      const items = response.data.data;
      setPhilosophers(items);
      setSelectedCodes((prev) => (prev.length ? prev : items.slice(0, 3).map((philosopher) => philosopher.code)));
    } finally {
      setLoadingPhilosophers(false);
    }
  };

  const loadHistory = async () => {
    setLoadingHistory(true);
    try {
      const response = await getPhilosophySessions({ page: 1, size: 10 });
      setHistory(response.data.data);
    } finally {
      setLoadingHistory(false);
    }
  };

  useEffect(() => {
    loadPhilosophers().catch((error) => {
      if (!isHandledRequestError(error)) {
        messageApi.error(tString('message.operationFailed'));
      }
    });
  }, [language]);

  useEffect(() => {
    loadHistory().catch(() => undefined);
  }, []);

  const handleGenerate = async () => {
    const trimmedQuestion = question.trim();
    if (!trimmedQuestion) {
      messageApi.warning(tString('validation.required', { field: tString('philosophy.question') }));
      return;
    }
    if (!selectedCodes.length) {
      messageApi.warning(tString('validation.selectRequired', { field: tString('philosophy.selectPhilosophers') }));
      return;
    }
    setGenerating(true);
    try {
      const response = await createPhilosophySession({
        question: trimmedQuestion,
        philosopherCodes: selectedCodes,
        language,
      });
      setCurrentSession(response.data.data);
      await loadHistory();
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(tString('philosophy.generateFailed'));
      }
    } finally {
      setGenerating(false);
    }
  };

  const handleClear = () => {
    setQuestion('');
    setCurrentSession(null);
    setSelectedCodes(defaultCodes);
  };

  const handleOpenHistory = async (id: number) => {
    try {
      const response = await getPhilosophySessionDetail(id);
      const session = response.data.data;
      setCurrentSession(session);
      setQuestion(session.question);
      setSelectedCodes(session.responses.map((item) => item.philosopherCode));
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(tString('message.operationFailed'));
      }
    }
  };

  const handleDeleteHistory = async (id: number) => {
    try {
      await deletePhilosophySession(id);
      messageApi.success(tString('philosophy.deleteSuccess'));
      if (currentSession?.id === id) {
        setCurrentSession(null);
      }
      await loadHistory();
    } catch (error) {
      if (!isHandledRequestError(error)) {
        messageApi.error(tString('message.operationFailed'));
      }
    }
  };

  const handleCopy = async (item: PhilosophyResponseItem) => {
    const isCounselor = item.responseLayout === 'COUNSELOR_CARD' || item.philosopherCode === 'PSYCHOLOGY_TEACHER';
    const content = isCounselor
      ? [
        `${item.philosopherName}`,
        `${tString('philosophy.understanding')}: ${item.understanding || item.viewpoint || ''}`,
        `${tString('philosophy.advice')}: ${item.advice || item.questionBack || ''}`,
        `${tString('philosophy.practice')}: ${item.practice || item.objection || ''}`,
        `${tString('philosophy.support')}: ${item.support || item.summary || ''}`,
      ].join('\n')
      : [
        `${item.philosopherName}`,
        `${tString('philosophy.viewpoint')}: ${item.viewpoint || ''}`,
        `${tString('philosophy.questionBack')}: ${item.questionBack || ''}`,
        `${tString('philosophy.objection')}: ${item.objection || ''}`,
        `${tString('philosophy.summary')}: ${item.summary || ''}`,
      ].join('\n');
    try {
      await navigator.clipboard.writeText(content);
      messageApi.success(tString('philosophy.copySuccess'));
    } catch (error) {
      messageApi.error(tString('message.operationFailed'));
    }
  };

  const formatDate = (value: string) => {
    return dayjs(value).format(language === 'en-US' ? 'MMM D, YYYY HH:mm' : 'YYYY年M月D日 HH:mm');
  };

  const handleModeChange = (mode: string) => {
    const nextParams = new URLSearchParams(searchParams);
    nextParams.set('mode', mode === 'chat' ? 'chat' : 'perspectives');
    if (mode !== 'chat') {
      nextParams.delete('sessionId');
    }
    setSearchParams(nextParams, { replace: true });
  };

  const handleChatSessionChange = (sessionId?: number) => {
    const nextParams = new URLSearchParams(searchParams);
    nextParams.set('mode', 'chat');
    if (sessionId) {
      nextParams.set('sessionId', String(sessionId));
    } else {
      nextParams.delete('sessionId');
    }
    setSearchParams(nextParams, { replace: true });
  };

  return (
    <div className={`philosophy-page${activeMode === 'chat' ? ' chat-mode' : ' page-wide'}`}>
      {contextHolder}
      {activeMode === 'perspectives' && (
        <>
          <div className="philosophy-page-toolbar">
            <PhilosophyModeTabs activeKey={activeMode} onChange={handleModeChange} t={tString} />
          </div>
          <Card className="philosophy-hero-card">
            <div className="philosophy-hero-content">
              <span className="philosophy-hero-icon">
                <BulbOutlined />
              </span>
              <div>
                <Typography.Title level={2}>{t('philosophy.title')}</Typography.Title>
                <Typography.Paragraph>{t('philosophy.subtitle')}</Typography.Paragraph>
              </div>
            </div>
            <PhilosophyDisclaimer />
          </Card>
        </>
      )}

      {activeMode === 'perspectives' ? (
        <div className={isMobile ? 'philosophy-layout mobile' : 'philosophy-layout'}>
          <main className="philosophy-main">
            <PhilosophyInputPanel
              question={question}
              selectedCodes={selectedCodes}
              philosophers={philosophers}
              loading={generating || loadingPhilosophers}
              hasResult={!!resultItems.length}
              onQuestionChange={setQuestion}
              onSelectedCodesChange={setSelectedCodes}
              onGenerate={handleGenerate}
              onClear={handleClear}
              t={tString}
            />
            <Typography.Text className="philosophy-generating-text">
              {generating ? t('philosophy.generating') : ' '}
            </Typography.Text>
            <PhilosophyResultGrid
              items={resultItems}
              philosophers={philosophers}
              loading={generating}
              onCopy={handleCopy}
              t={tString}
            />
          </main>
          <aside className="philosophy-aside">
            <PhilosophyHistoryList
              sessions={history}
              loading={loadingHistory}
              selectedId={selectedSessionId}
              onOpen={handleOpenHistory}
              onDelete={handleDeleteHistory}
              t={tString}
              formatDate={formatDate}
            />
          </aside>
        </div>
      ) : (
        <CharacterChatPanel
          philosophers={philosophers}
          language={language}
          selectedSessionId={selectedChatSessionId}
          onSessionChange={handleChatSessionChange}
          activeMode={activeMode}
          onModeChange={handleModeChange}
          t={tString}
        />
      )}
    </div>
  );
}
