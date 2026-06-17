import { create } from 'zustand';
import { getCurrentUser, LoginResponse, UserProfile } from '../api/auth';
import { TOKEN_STORAGE_KEY } from '../api/request';
import { useBackgroundStore } from './backgroundStore';
import { useRelationshipThemeStore } from './relationshipThemeStore';

interface AuthState {
  token: string | null;
  user: UserProfile | null;
  isAuthenticated: boolean;
  login: (payload: LoginResponse) => void;
  logout: () => void;
  fetchCurrentUser: () => Promise<void>;
  setUser: (user: UserProfile) => void;
  updateUser: (user: Partial<UserProfile>) => void;
}

const initialToken = localStorage.getItem(TOKEN_STORAGE_KEY);

export const useAuthStore = create<AuthState>((set) => ({
  token: initialToken,
  user: null,
  isAuthenticated: Boolean(initialToken),
  login: (payload) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, payload.token);
    set({
      token: payload.token,
      user: payload.user,
      isAuthenticated: true,
    });
  },
  logout: () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    useBackgroundStore.getState().resetLocalSetting();
    useRelationshipThemeStore.getState().resetRelationshipThemeStatus();
    set({
      token: null,
      user: null,
      isAuthenticated: false,
    });
  },
  fetchCurrentUser: async () => {
    const response = await getCurrentUser();
    set({
      user: response.data.data,
      isAuthenticated: true,
    });
  },
  setUser: (user) => {
    set({
      user,
      isAuthenticated: true,
    });
  },
  updateUser: (user) => {
    set((state) => ({
      user: state.user ? { ...state.user, ...user } : null,
    }));
  },
}));

window.addEventListener('lifelink:unauthorized', () => {
  useAuthStore.getState().logout();
});
