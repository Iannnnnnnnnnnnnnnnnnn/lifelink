import {
  BellOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  PauseOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  SettingOutlined,
  StarOutlined,
  StopOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { Avatar, Button, Card, Col, Form, Grid, Input, InputNumber, List, message, Modal, Progress, Row, Select, Skeleton, Space, Switch, Tag, Typography } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  abandonFocusRoom,
  abandonFocusSession,
  completeFocusRoom,
  completeFocusSession,
  createFocusRoom,
  declineFocusRoom,
  FocusRoom,
  FocusSession,
  FocusSettings,
  FocusStats,
  getCurrentFocusRoom,
  getCurrentFocusSession,
  getFocusSessions,
  getFocusSettings,
  getTodayFocusStats,
  joinFocusRoom,
  pauseFocusSession,
  resumeFocusSession,
  startFocusRoom,
  startFocusSession,
  updateFocusSettings,
} from '../../api/focus';
import { CoinAccount, getCoinAccount } from '../../api/rewards';
import { getRelationshipMembers, getRelationships, RelationshipMember, RelationshipSummary } from '../../api/relationship';
import { getSpaceTodos, SpaceTodo } from '../../api/spaceTodo';
import { FocusRewardsNav } from '../../components/navigation/FocusRewardsNav';
import { useAuthStore } from '../../store/authStore';

const phaseOptions = ['FOCUS', 'SHORT_BREAK', 'LONG_BREAK'] as const;

interface FocusSettingsFormValues {
  focusMinutes: number;
  shortBreakMinutes: number;
  longBreakMinutes: number;
  longBreakInterval: number;
  autoStartBreak: boolean;
  autoStartNextFocus: boolean;
  soundEnabled: boolean;
  notificationEnabled: boolean;
  strictModeEnabled: boolean;
}

function formatSeconds(value: number) {
  const seconds = Math.max(0, value);
  const minutes = Math.floor(seconds / 60);
  const rest = seconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(rest).padStart(2, '0')}`;
}

function getRemainingSeconds(session: FocusSession | null) {
  if (!session) return 0;
  if (session.status === 'PAUSED') return session.remainingSeconds || 0;
  return Math.max(0, dayjs(session.expectedEndAt).diff(dayjs(), 'second'));
}

export function FocusHome() {
  const { t, i18n } = useTranslation();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  const user = useAuthStore((state) => state.user);
  const [settings, setSettings] = useState<FocusSettings | null>(null);
  const [session, setSession] = useState<FocusSession | null>(null);
  const [stats, setStats] = useState<FocusStats | null>(null);
  const [coinAccount, setCoinAccount] = useState<CoinAccount | null>(null);
  const [recentSessions, setRecentSessions] = useState<FocusSession[]>([]);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [selectedRelationshipId, setSelectedRelationshipId] = useState<number | undefined>();
  const [todos, setTodos] = useState<SpaceTodo[]>([]);
  const [selectedTodoId, setSelectedTodoId] = useState<number | undefined>();
  const [members, setMembers] = useState<RelationshipMember[]>([]);
  const [inviteUserIds, setInviteUserIds] = useState<number[]>([]);
  const [room, setRoom] = useState<FocusRoom | null>(null);
  const [phase, setPhase] = useState<(typeof phaseOptions)[number]>('FOCUS');
  const [plannedMinutes, setPlannedMinutes] = useState(25);
  const [note, setNote] = useState('');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [roomOpen, setRoomOpen] = useState(false);
  const [settingsForm] = Form.useForm<FocusSettingsFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  const remainingSeconds = useMemo(() => getRemainingSeconds(session), [session]);
  const [displaySeconds, setDisplaySeconds] = useState(remainingSeconds);
  const totalSeconds = (session?.plannedMinutes || plannedMinutes) * 60;
  const progressPercent = totalSeconds > 0 ? Math.min(100, Math.max(0, ((totalSeconds - displaySeconds) / totalSeconds) * 100)) : 0;
  const currentMember = room?.members.find((item) => item.userId === user?.id);

  const loadFocusData = async () => {
    setLoading(true);
    try {
      const [settingsResult, currentResult, statsResult, sessionsResult, relationshipsResult, roomResult, coinResult] = await Promise.allSettled([
        getFocusSettings(),
        getCurrentFocusSession(),
        getTodayFocusStats(),
        getFocusSessions(),
        getRelationships(),
        getCurrentFocusRoom(),
        getCoinAccount(),
      ]);
      if (settingsResult.status === 'fulfilled') {
        const nextSettings = settingsResult.value.data.data;
        setSettings(nextSettings);
        setPlannedMinutes(nextSettings.focusMinutes);
        settingsForm.setFieldsValue(nextSettings);
      }
      if (currentResult.status === 'fulfilled') {
        setSession(currentResult.value.data.data);
      }
      if (statsResult.status === 'fulfilled') {
        setStats(statsResult.value.data.data);
      }
      if (sessionsResult.status === 'fulfilled') {
        setRecentSessions(sessionsResult.value.data.data.slice(0, 8));
      }
      if (relationshipsResult.status === 'fulfilled') {
        const items = relationshipsResult.value.data.data;
        setRelationships(items);
        if (!selectedRelationshipId && items[0]) {
          setSelectedRelationshipId(items[0].id);
        }
      }
      if (roomResult.status === 'fulfilled') {
        setRoom(roomResult.value.data.data);
      }
      if (coinResult.status === 'fulfilled') {
        setCoinAccount(coinResult.value.data.data);
      }
    } finally {
      setLoading(false);
    }
  };

  const loadTodos = async (relationshipId?: number) => {
    if (!relationshipId) {
      setTodos([]);
      setSelectedTodoId(undefined);
      return;
    }
    const response = await getSpaceTodos(relationshipId, { status: 'TODO', page: 1, size: 50 });
    setTodos(response.data.data);
  };

  const loadMembers = async (relationshipId?: number) => {
    if (!relationshipId) {
      setMembers([]);
      return;
    }
    const response = await getRelationshipMembers(relationshipId);
    setMembers(response.data.data.filter((item) => item.userId !== user?.id));
  };

  useEffect(() => {
    loadFocusData();
  }, []);

  useEffect(() => {
    loadTodos(selectedRelationshipId).catch(() => setTodos([]));
    loadMembers(selectedRelationshipId).catch(() => setMembers([]));
  }, [selectedRelationshipId]);

  useEffect(() => {
    setDisplaySeconds(getRemainingSeconds(session));
    if (!session || session.status === 'PAUSED') {
      return undefined;
    }
    const timer = window.setInterval(() => {
      setDisplaySeconds(getRemainingSeconds(session));
    }, 1000);
    return () => window.clearInterval(timer);
  }, [session]);

  useEffect(() => {
    if (!room || !['WAITING', 'RUNNING'].includes(room.status)) {
      return undefined;
    }
    const timer = window.setInterval(async () => {
      const response = await getCurrentFocusRoom();
      setRoom(response.data.data);
    }, 5000);
    return () => window.clearInterval(timer);
  }, [room?.id, room?.status]);

  useEffect(() => {
    if (!settings?.strictModeEnabled || !session || session.status !== 'RUNNING') {
      return undefined;
    }
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = '';
    };
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [settings?.strictModeEnabled, session?.status]);

  const refreshAfterAction = async (nextSession?: FocusSession | null) => {
    if (nextSession !== undefined) {
      setSession(nextSession);
    } else {
      const current = await getCurrentFocusSession();
      setSession(current.data.data);
    }
    const [statsResponse, sessionsResponse, coinResponse] = await Promise.all([getTodayFocusStats(), getFocusSessions(), getCoinAccount()]);
    setStats(statsResponse.data.data);
    setRecentSessions(sessionsResponse.data.data.slice(0, 8));
    setCoinAccount(coinResponse.data.data);
  };

  const handleStart = async () => {
    setActionLoading(true);
    try {
      const response = await startFocusSession({
        spaceId: selectedRelationshipId,
        todoId: selectedTodoId,
        plannedMinutes,
        phase,
        source: selectedTodoId ? 'TODO' : 'MANUAL',
        note: note.trim() || undefined,
      });
      await refreshAfterAction(response.data.data);
      messageApi.success(t('focus.startSuccess'));
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleSessionAction = async (action: 'pause' | 'resume' | 'complete' | 'abandon') => {
    if (!session) return;
    setActionLoading(true);
    try {
      const response = await ({
        pause: pauseFocusSession,
        resume: resumeFocusSession,
        complete: completeFocusSession,
        abandon: abandonFocusSession,
      }[action](session.sessionId));
      await refreshAfterAction(['complete', 'abandon'].includes(action) ? null : response.data.data);
      if (action === 'complete') {
        const coins = response.data.data.coinsAwarded || 0;
        messageApi.success(coins > 0 ? t('focus.coinsEarned', { coins }) : t('focus.noCoinsEarned'));
      } else {
        messageApi.success(t('common.success'));
      }
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleSaveSettings = async () => {
    const values = await settingsForm.validateFields();
    setActionLoading(true);
    try {
      const response = await updateFocusSettings(values);
      setSettings(response.data.data);
      setPlannedMinutes(response.data.data.focusMinutes);
      setSettingsOpen(false);
      messageApi.success(t('common.success'));
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleCreateRoom = async () => {
    if (!selectedRelationshipId) {
      messageApi.warning(t('focus.chooseSpaceFirst'));
      return;
    }
    setActionLoading(true);
    try {
      const response = await createFocusRoom({
        spaceId: selectedRelationshipId,
        title: t('focus.defaultRoomTitle', { minutes: plannedMinutes }),
        plannedMinutes,
        inviteUserIds,
      });
      setRoom(response.data.data);
      setRoomOpen(true);
      messageApi.success(t('focus.roomCreated'));
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleRoomAction = async (action: 'join' | 'decline' | 'start' | 'complete' | 'abandon') => {
    if (!room) return;
    setActionLoading(true);
    try {
      const response = await ({
        join: joinFocusRoom,
        decline: declineFocusRoom,
        start: startFocusRoom,
        complete: completeFocusRoom,
        abandon: abandonFocusRoom,
      }[action](room.id));
      setRoom(response.data.data);
      const current = await getCurrentFocusSession();
      setSession(current.data.data);
      messageApi.success(t('common.success'));
    } catch (error) {
      messageApi.error(t('message.operationFailed'));
    } finally {
      setActionLoading(false);
    }
  };

  if (loading && !settings) {
    return <Skeleton active paragraph={{ rows: 12 }} />;
  }

  return (
    <div className="page-wide focus-page">
      {contextHolder}
      <section className="focus-hero">
        <div className="focus-hero-copy">
          <Typography.Title level={1}>{t('focusRewards.title')}</Typography.Title>
          <Typography.Text>{t('focusRewards.subtitle')}</Typography.Text>
        </div>
        <Space wrap>
          <Button icon={<ReloadOutlined />} onClick={loadFocusData}>{t('common.refresh')}</Button>
          <Button icon={<SettingOutlined />} onClick={() => setSettingsOpen(true)}>{t('focus.settings')}</Button>
        </Space>
      </section>

      <FocusRewardsNav />

      <Row gutter={[18, 18]} align="stretch">
        <Col xs={24} xl={14}>
          <Card className="focus-timer-card">
            <div className="focus-phase-row">
              <Tag color={session ? 'processing' : 'default'}>{session ? t(`focus.status.${session.status}`) : t(`focus.phase.${phase}`)}</Tag>
              {session?.todoTitle && <Typography.Text type="secondary">{session.todoTitle}</Typography.Text>}
            </div>
            <div className="focus-timer-wrap">
              <Progress
                type="circle"
                percent={progressPercent}
                size={isMobile ? 260 : 320}
                strokeWidth={4}
                showInfo={false}
                strokeColor="#1d1d1f"
                trailColor="rgba(0,0,0,0.06)"
              />
              <div className="focus-countdown">
                <span>{session ? formatSeconds(displaySeconds) : `${plannedMinutes}:00`}</span>
                <small>{session ? t(`focus.phase.${session.phase}`) : t('focus.ready')}</small>
              </div>
            </div>
            <Space wrap className="focus-actions">
              {!session && (
                <Button type="primary" size="large" icon={<PlayCircleOutlined />} loading={actionLoading} onClick={handleStart}>
                  {t('focus.start')}
                </Button>
              )}
              {session?.status === 'RUNNING' && (
                <Button size="large" icon={<PauseOutlined />} loading={actionLoading} onClick={() => handleSessionAction('pause')}>
                  {t('focus.pause')}
                </Button>
              )}
              {session?.status === 'PAUSED' && (
                <Button type="primary" size="large" icon={<PlayCircleOutlined />} loading={actionLoading} onClick={() => handleSessionAction('resume')}>
                  {t('focus.resume')}
                </Button>
              )}
              {session && (
                <>
                  <Button size="large" icon={<CheckOutlined />} loading={actionLoading} onClick={() => handleSessionAction('complete')}>
                    {t('focus.complete')}
                  </Button>
                  <Button size="large" icon={<StopOutlined />} danger loading={actionLoading} onClick={() => handleSessionAction('abandon')}>
                    {t('focus.abandon')}
                  </Button>
                </>
              )}
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Space direction="vertical" size={18} className="full-width">
            <Card className="focus-panel">
              <Typography.Title level={4}>{t('focus.chooseTask')}</Typography.Title>
              <Space direction="vertical" size={12} className="full-width">
                <Select
                  allowClear
                  className="full-width"
                  placeholder={t('focus.chooseSpace')}
                  value={selectedRelationshipId}
                  options={relationships.map((item) => ({ value: item.id, label: item.name }))}
                  onChange={(value) => {
                    setSelectedRelationshipId(value);
                    setSelectedTodoId(undefined);
                  }}
                />
                <Select
                  allowClear
                  className="full-width"
                  placeholder={t('focus.noTask')}
                  value={selectedTodoId}
                  options={todos.map((item) => ({ value: item.id, label: item.title }))}
                  onChange={setSelectedTodoId}
                />
                <Select
                  value={phase}
                  options={phaseOptions.map((item) => ({ value: item, label: t(`focus.phase.${item}`) }))}
                  onChange={setPhase}
                />
                <InputNumber min={1} max={240} value={plannedMinutes} addonAfter={t('focus.minutes')} onChange={(value) => setPlannedMinutes(Number(value || 1))} />
                <Input.TextArea rows={3} value={note} maxLength={200} placeholder={t('focus.notePlaceholder')} onChange={(event) => setNote(event.target.value)} />
              </Space>
            </Card>

            <Card className="focus-panel">
              <div className="focus-panel-title">
                <Typography.Title level={4}>{t('focus.together')}</Typography.Title>
                <Button icon={<TeamOutlined />} onClick={handleCreateRoom} loading={actionLoading}>{t('focus.createRoom')}</Button>
              </div>
              <Select
                mode="multiple"
                className="full-width"
                placeholder={t('focus.inviteMembers')}
                value={inviteUserIds}
                options={members.map((item) => ({ value: item.userId, label: item.nickname || item.username }))}
                onChange={setInviteUserIds}
              />
              {room && (
                <div className="focus-room-mini" onClick={() => setRoomOpen(true)}>
                  <Typography.Text strong>{room.title}</Typography.Text>
                  <Tag>{t(`focus.roomStatus.${room.status}`)}</Tag>
                </div>
              )}
            </Card>
          </Space>
        </Col>
      </Row>

      <Row gutter={[18, 18]}>
        <Col xs={24} lg={12}>
          <Card className="focus-panel">
            <Typography.Title level={4}>{t('focus.today')}</Typography.Title>
            <div className="focus-stat-grid">
              <div><strong>{stats?.totalFocusMinutes || 0}</strong><span>{t('focus.totalMinutes')}</span></div>
              <div><strong>{stats?.completedPomodoros || 0}</strong><span>{t('focus.completedPomodoros')}</span></div>
              <div><strong>{stats?.currentStreak || 0}</strong><span>{t('focus.currentStreak')}</span></div>
              <div><strong>{coinAccount?.balance || 0}</strong><span>{t('rewards.myCoins')}</span></div>
            </div>
            <List
              size="small"
              dataSource={stats?.topTodos || []}
              locale={{ emptyText: t('focus.noTopTodo') }}
              renderItem={(item) => (
                <List.Item>
                  <Typography.Text>{item.todoTitle || t('focus.noTask')}</Typography.Text>
                  <Typography.Text type="secondary">{item.focusMinutes} {t('focus.minutes')}</Typography.Text>
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card className="focus-panel">
            <Typography.Title level={4}>{t('focus.recentSessions')}</Typography.Title>
            <List
              dataSource={recentSessions}
              locale={{ emptyText: t('focus.emptySessions') }}
              renderItem={(item) => (
                <List.Item>
                  <Space direction="vertical" size={2}>
                    <Typography.Text strong>{item.todoTitle || t(`focus.phase.${item.phase}`)}</Typography.Text>
                    <Typography.Text type="secondary">
                      {dayjs(item.startedAt).format(i18n.resolvedLanguage === 'en-US' ? 'MMM D HH:mm' : 'M月D日 HH:mm')} · {item.actualMinutes || item.plannedMinutes} {t('focus.minutes')}
                    </Typography.Text>
                  </Space>
                  <Space>
                    {Boolean(item.coinsAwarded) && <Tag icon={<StarOutlined />} color="gold">+{item.coinsAwarded}</Tag>}
                    <Tag>{t(`focus.status.${item.status}`)}</Tag>
                  </Space>
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>

      <Modal
        open={settingsOpen}
        title={t('focus.settings')}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
        onOk={handleSaveSettings}
        onCancel={() => setSettingsOpen(false)}
        confirmLoading={actionLoading}
      >
        <Form form={settingsForm} layout="vertical">
          <Row gutter={12}>
            <Col span={12}><Form.Item name="focusMinutes" label={t('focus.focusMinutes')}><InputNumber min={1} max={240} /></Form.Item></Col>
            <Col span={12}><Form.Item name="shortBreakMinutes" label={t('focus.shortBreakMinutes')}><InputNumber min={1} max={120} /></Form.Item></Col>
            <Col span={12}><Form.Item name="longBreakMinutes" label={t('focus.longBreakMinutes')}><InputNumber min={1} max={240} /></Form.Item></Col>
            <Col span={12}><Form.Item name="longBreakInterval" label={t('focus.longBreakInterval')}><InputNumber min={1} max={12} /></Form.Item></Col>
          </Row>
          <Form.Item name="autoStartBreak" label={t('focus.autoStartBreak')} valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="autoStartNextFocus" label={t('focus.autoStartNextFocus')} valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="soundEnabled" label={t('focus.soundEnabled')} valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="notificationEnabled" label={t('focus.notificationEnabled')} valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="strictModeEnabled" label={t('focus.strictModeEnabled')} valuePropName="checked"><Switch /></Form.Item>
        </Form>
      </Modal>

      <Modal open={roomOpen} title={t('focus.together')} footer={null} onCancel={() => setRoomOpen(false)}>
        {room && (
          <Space direction="vertical" size={16} className="full-width">
            <div className="focus-room-header">
              <ClockCircleOutlined />
              <div>
                <Typography.Title level={4}>{room.title}</Typography.Title>
                <Typography.Text type="secondary">{room.spaceName} · {room.plannedMinutes} {t('focus.minutes')}</Typography.Text>
              </div>
            </div>
            <List
              dataSource={room.members}
              renderItem={(member) => (
                <List.Item>
                  <Space>
                    <Avatar src={member.avatarUrl}>{member.username?.slice(0, 1)}</Avatar>
                    <Typography.Text>{member.username}</Typography.Text>
                  </Space>
                  <Tag>{t(`focus.memberStatus.${member.memberStatus}`)}</Tag>
                </List.Item>
              )}
            />
            <Space wrap>
              {currentMember?.memberStatus === 'INVITED' && <Button onClick={() => handleRoomAction('join')}>{t('focus.joinRoom')}</Button>}
              {currentMember?.memberStatus === 'INVITED' && <Button onClick={() => handleRoomAction('decline')}>{t('focus.declineRoom')}</Button>}
              {room.creatorUserId === user?.id && room.status === 'WAITING' && <Button type="primary" onClick={() => handleRoomAction('start')}>{t('focus.startTogether')}</Button>}
              {room.status === 'RUNNING' && <Button icon={<CheckOutlined />} onClick={() => handleRoomAction('complete')}>{t('focus.complete')}</Button>}
              {room.status === 'RUNNING' && <Button danger icon={<StopOutlined />} onClick={() => handleRoomAction('abandon')}>{t('focus.abandon')}</Button>}
            </Space>
            <Typography.Text type="secondary"><BellOutlined /> {t('focus.privacyHint')}</Typography.Text>
          </Space>
        )}
      </Modal>
    </div>
  );
}
