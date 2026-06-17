import { Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { PhilosophyPage } from '../pages/philosophy/PhilosophyPage';
import { useAuthStore } from '../store/authStore';

export function PhilosophyFeatureRoute() {
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const fetchCurrentUser = useAuthStore((state) => state.fetchCurrentUser);
  const [loading, setLoading] = useState(isAuthenticated && !user);

  useEffect(() => {
    if (!isAuthenticated || user) {
      setLoading(false);
      return;
    }
    setLoading(true);
    fetchCurrentUser()
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, [fetchCurrentUser, isAuthenticated, user]);

  if (loading) {
    return (
      <div className="page-wide">
        <Spin />
      </div>
    );
  }

  if (!user?.features?.philosophyEnabled) {
    return <Navigate to="/403" replace />;
  }

  return <PhilosophyPage />;
}
