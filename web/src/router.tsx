import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './components/AppLayout';
import { ActivityTimeline } from './pages/ActivityTimeline';
import { AnniversaryDetail } from './pages/AnniversaryDetail';
import { AnniversaryList } from './pages/AnniversaryList';
import { ProtectedRoute } from './components/ProtectedRoute';
import { PublicOnlyRoute } from './components/PublicOnlyRoute';
import { CreateAnniversary } from './pages/CreateAnniversary';
import { CreateRelationship } from './pages/CreateRelationship';
import { CreateDailyPost } from './pages/CreateDailyPost';
import { DailyPostDetail } from './pages/DailyPostDetail';
import { DailyTimeline } from './pages/DailyTimeline';
import { FinanceCreateTransaction } from './pages/FinanceCreateTransaction';
import { FinanceDashboard } from './pages/FinanceDashboard';
import { FinanceTransactionList } from './pages/FinanceTransactionList';
import { Home } from './pages/Home';
import { JoinRelationship } from './pages/JoinRelationship';
import { RelationshipFinance } from './pages/RelationshipFinance';
import { CalendarPage } from './pages/calendar/CalendarPage';
import { Login } from './pages/Login';
import { PhilosophyPage } from './pages/philosophy/PhilosophyPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { RelationshipDetail } from './pages/RelationshipDetail';
import { RelationshipList } from './pages/RelationshipList';
import { RelationshipTimelinePage } from './pages/RelationshipTimelinePage';
import { Register } from './pages/Register';
import { SearchPage } from './pages/SearchPage';
import { SpaceTodoList } from './pages/SpaceTodoList';
import { ForbiddenPage } from './pages/error/ForbiddenPage';
import { NotFoundPage } from './pages/error/NotFoundPage';

export const router = createBrowserRouter([
  {
    element: <ProtectedRoute />,
    children: [
      {
        path: '/',
        element: <AppLayout />,
        children: [
          {
            index: true,
            element: <Home />,
          },
          {
            path: 'relationships',
            element: <RelationshipList />,
          },
          {
            path: 'search',
            element: <SearchPage />,
          },
          {
            path: 'profile',
            element: <ProfilePage />,
          },
          {
            path: '403',
            element: <ForbiddenPage />,
          },
          {
            path: '404',
            element: <NotFoundPage />,
          },
          {
            path: 'relationships/create',
            element: <CreateRelationship />,
          },
          {
            path: 'relationships/join',
            element: <JoinRelationship />,
          },
          {
            path: 'relationships/:id',
            element: <RelationshipDetail />,
          },
          {
            path: 'relationships/:relationshipId/todos',
            element: <SpaceTodoList />,
          },
          {
            path: 'relationships/:relationshipId/activities',
            element: <ActivityTimeline />,
          },
          {
            path: 'relationships/:relationshipId/timeline',
            element: <RelationshipTimelinePage />,
          },
          {
            path: 'relationships/:relationshipId/calendar',
            element: <CalendarPage />,
          },
          {
            path: 'relationships/:relationshipId/anniversaries',
            element: <AnniversaryList />,
          },
          {
            path: 'activities',
            element: <ActivityTimeline />,
          },
          {
            path: 'relationships/:relationshipId/finance',
            element: <RelationshipFinance />,
          },
          {
            path: 'anniversaries',
            element: <AnniversaryList />,
          },
          {
            path: 'anniversaries/create',
            element: <CreateAnniversary />,
          },
          {
            path: 'anniversaries/:id',
            element: <AnniversaryDetail />,
          },
          {
            path: 'daily',
            element: <DailyTimeline />,
          },
          {
            path: 'daily/create',
            element: <CreateDailyPost />,
          },
          {
            path: 'daily/:id',
            element: <DailyPostDetail />,
          },
          {
            path: 'finance',
            element: <FinanceDashboard />,
          },
          {
            path: 'finance/transactions',
            element: <FinanceTransactionList />,
          },
          {
            path: 'finance/create',
            element: <FinanceCreateTransaction />,
          },
          {
            path: 'philosophy',
            element: <PhilosophyPage />,
          },
          {
            path: '*',
            element: <NotFoundPage />,
          },
        ],
      },
    ],
  },
  {
    element: <PublicOnlyRoute />,
    children: [
      {
        path: '/login',
        element: <Login />,
      },
      {
        path: '/register',
        element: <Register />,
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);
