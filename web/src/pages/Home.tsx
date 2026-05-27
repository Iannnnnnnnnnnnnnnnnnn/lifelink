import {
  CalendarOutlined,
  CheckSquareOutlined,
  HeartOutlined,
  PlusOutlined,
  ReadOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  UsergroupAddOutlined,
} from '@ant-design/icons';
import { Alert, Col, Row, Space, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { Anniversary, getAnniversaries } from '../api/anniversary';
import { getMyActivities, SpaceActivity } from '../api/activity';
import { DailyPost, getDailyPosts } from '../api/daily';
import { getRelationships, RelationshipSummary } from '../api/relationship';
import { getSpaceTodos, SpaceTodo, toggleSpaceTodo } from '../api/spaceTodo';
import { ActivityPreviewList } from '../components/dashboard/ActivityPreviewList';
import { AnniversaryPreview } from '../components/dashboard/AnniversaryPreview';
import { DashboardHero } from '../components/dashboard/DashboardHero';
import { DashboardSection } from '../components/dashboard/DashboardSection';
import { QuickActionCard } from '../components/dashboard/QuickActionCard';
import { RecentDailyList } from '../components/dashboard/RecentDailyList';
import { StatCard } from '../components/dashboard/StatCard';
import { TodoPreviewList } from '../components/dashboard/TodoPreviewList';
import { useAuthStore } from '../store/authStore';

type TodoPreview = SpaceTodo & { relationshipName?: string };
type DashboardModule = 'relationships' | 'daily' | 'todos' | 'anniversaries' | 'activities';

const initialLoading: Record<DashboardModule, boolean> = {
  relationships: true,
  daily: true,
  todos: true,
  anniversaries: true,
  activities: true,
};

export function Home() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const fetchCurrentUser = useAuthStore((state) => state.fetchCurrentUser);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [dailyPosts, setDailyPosts] = useState<DailyPost[]>([]);
  const [todos, setTodos] = useState<TodoPreview[]>([]);
  const [anniversaries, setAnniversaries] = useState<Anniversary[]>([]);
  const [activities, setActivities] = useState<SpaceActivity[]>([]);
  const [loading, setLoading] = useState(initialLoading);
  const [errors, setErrors] = useState<Partial<Record<DashboardModule, boolean>>>({});
  const [togglingIds, setTogglingIds] = useState<number[]>([]);
  const [messageApi, contextHolder] = message.useMessage();

  const unfinishedTodos = useMemo(() => todos.filter((todo) => todo.status === 'TODO'), [todos]);
  const upcomingAnniversaries = useMemo(
    () => anniversaries.filter((item) => item.displayType === 'TODAY' || item.displayType === 'COUNTDOWN'),
    [anniversaries],
  );

  const setModuleLoading = (module: DashboardModule, value: boolean) => {
    setLoading((current) => ({ ...current, [module]: value }));
  };

  const markModuleError = (module: DashboardModule) => {
    setErrors((current) => ({ ...current, [module]: true }));
  };

  const loadTodos = async (relationshipItems: RelationshipSummary[]) => {
    setModuleLoading('todos', true);
    try {
      const todoResponses = await Promise.allSettled(
        relationshipItems.slice(0, 5).map(async (relationship) => {
          const response = await getSpaceTodos(relationship.id, { status: 'TODO', page: 1, size: 5 });
          return response.data.data.map((todo) => ({ ...todo, relationshipName: relationship.name }));
        }),
      );
      setTodos(todoResponses.flatMap((result) => (result.status === 'fulfilled' ? result.value : [])));
      if (todoResponses.some((result) => result.status === 'rejected')) {
        markModuleError('todos');
      }
    } catch (error) {
      markModuleError('todos');
      setTodos([]);
    } finally {
      setModuleLoading('todos', false);
    }
  };

  const loadDashboard = async () => {
    setErrors({});
    setLoading(initialLoading);
    fetchCurrentUser().catch(() => undefined);

    const [relationshipResult, dailyResult, anniversaryResult, activityResult] = await Promise.allSettled([
      getRelationships(),
      getDailyPosts({ page: 1, size: 3 }),
      getAnniversaries({ page: 1, size: 3 }),
      getMyActivities({ page: 1, size: 5 }),
    ]);

    if (relationshipResult.status === 'fulfilled') {
      const relationshipItems = relationshipResult.value.data.data;
      setRelationships(relationshipItems);
      setModuleLoading('relationships', false);
      loadTodos(relationshipItems);
    } else {
      setRelationships([]);
      markModuleError('relationships');
      setModuleLoading('relationships', false);
      setModuleLoading('todos', false);
    }

    if (dailyResult.status === 'fulfilled') {
      setDailyPosts(dailyResult.value.data.data);
    } else {
      setDailyPosts([]);
      markModuleError('daily');
    }
    setModuleLoading('daily', false);

    if (anniversaryResult.status === 'fulfilled') {
      setAnniversaries(anniversaryResult.value.data.data);
    } else {
      setAnniversaries([]);
      markModuleError('anniversaries');
    }
    setModuleLoading('anniversaries', false);

    if (activityResult.status === 'fulfilled') {
      setActivities(activityResult.value.data.data);
    } else {
      setActivities([]);
      markModuleError('activities');
    }
    setModuleLoading('activities', false);
  };

  const handleToggleTodo = async (todo: TodoPreview) => {
    setTogglingIds((ids) => ids.concat(todo.id));
    try {
      const response = await toggleSpaceTodo(todo.relationshipId, todo.id);
      setTodos((items) => items.map((item) => (item.id === todo.id ? { ...response.data.data, relationshipName: item.relationshipName } : item)));
      messageApi.success(t('todo.toggleSuccess'));
    } catch (error) {
      messageApi.error(t('todo.toggleFailed'));
    } finally {
      setTogglingIds((ids) => ids.filter((id) => id !== todo.id));
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  return (
    <Space direction="vertical" size={20} className="home-page dashboard-page">
      {contextHolder}
      <DashboardHero
        username={user?.username}
        onCreateDaily={() => navigate('/daily/create')}
        onCreateSpace={() => navigate('/relationships/create')}
      />

      {Object.values(errors).some(Boolean) && (
        <Alert showIcon type="warning" className="dashboard-soft-alert" message={t('dashboard.partialLoadFailed')} />
      )}

      <section>
        <div className="dashboard-section-heading">
          <span>{t('dashboard.overview')}</span>
        </div>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <StatCard
              icon={<TeamOutlined />}
              title={t('dashboard.relationshipSpaces')}
              value={relationships.length}
              description={t('dashboard.relationshipSpacesDesc')}
              onClick={() => navigate('/relationships')}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatCard
              icon={<CheckSquareOutlined />}
              title={t('dashboard.unfinishedTodos')}
              value={unfinishedTodos.length}
              description={t('dashboard.unfinishedTodosDesc')}
              onClick={() => navigate(relationships[0] ? `/relationships/${relationships[0].id}/todos` : '/relationships')}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatCard
              icon={<CalendarOutlined />}
              title={t('dashboard.anniversaries')}
              value={upcomingAnniversaries.length}
              description={t('dashboard.anniversariesDesc')}
              onClick={() => navigate('/anniversaries')}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <StatCard
              icon={<ReadOutlined />}
              title={t('dashboard.recentDaily')}
              value={dailyPosts.length}
              description={t('dashboard.recentDailyDesc')}
              onClick={() => navigate('/daily')}
            />
          </Col>
        </Row>
      </section>

      <section>
        <div className="dashboard-section-heading">
          <span>{t('dashboard.quickActions')}</span>
        </div>
        <div className="dashboard-action-grid">
          <QuickActionCard icon={<ReadOutlined />} title={t('dashboard.createDaily')} description={t('dashboard.createDailyDesc')} onClick={() => navigate('/daily/create')} />
          <QuickActionCard icon={<CheckSquareOutlined />} title={t('dashboard.createTodo')} description={t('dashboard.createTodoDesc')} onClick={() => navigate(relationships[0] ? `/relationships/${relationships[0].id}/todos` : '/relationships')} />
          <QuickActionCard icon={<CalendarOutlined />} title={t('dashboard.createAnniversary')} description={t('dashboard.createAnniversaryDesc')} onClick={() => navigate('/anniversaries/create')} />
          <QuickActionCard icon={<HeartOutlined />} title={t('dashboard.cycleCare')} description={t('dashboard.cycleCareDesc')} onClick={() => navigate('/cycle-care')} />
          <QuickActionCard icon={<PlusOutlined />} title={t('dashboard.createSpace')} description={t('dashboard.createSpaceDesc')} onClick={() => navigate('/relationships/create')} />
          <QuickActionCard icon={<UsergroupAddOutlined />} title={t('dashboard.joinSpace')} description={t('dashboard.joinSpaceDesc')} onClick={() => navigate('/relationships/join')} />
          <QuickActionCard icon={<ThunderboltOutlined />} title={t('dashboard.viewActivities')} description={t('dashboard.viewActivitiesDesc')} onClick={() => navigate('/activities')} />
        </div>
      </section>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <DashboardSection title={t('dashboard.recentDaily')} onViewAll={() => navigate('/daily')}>
            <RecentDailyList items={dailyPosts} loading={loading.daily} onOpen={(id) => navigate(`/daily/${id}`)} onCreate={() => navigate('/daily/create')} />
          </DashboardSection>
        </Col>
        <Col xs={24} xl={10}>
          <DashboardSection title={t('dashboard.unfinishedTodos')}>
            <TodoPreviewList items={unfinishedTodos} loading={loading.todos} togglingIds={togglingIds} onToggle={handleToggleTodo} />
          </DashboardSection>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={12}>
          <DashboardSection title={t('dashboard.upcomingAnniversaries')} onViewAll={() => navigate('/anniversaries')}>
            <AnniversaryPreview items={upcomingAnniversaries.length ? upcomingAnniversaries : anniversaries} loading={loading.anniversaries} onOpen={(id) => navigate(`/anniversaries/${id}`)} />
          </DashboardSection>
        </Col>
        <Col xs={24} xl={12}>
          <DashboardSection title={t('dashboard.recentActivities')} onViewAll={() => navigate('/activities')}>
            <ActivityPreviewList items={activities} loading={loading.activities} onOpen={(relationshipId) => navigate(`/relationships/${relationshipId}/activities`)} />
          </DashboardSection>
        </Col>
      </Row>
    </Space>
  );
}
