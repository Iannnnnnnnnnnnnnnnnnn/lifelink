import { create } from 'zustand';
import { getUnreadCount } from '../api/notification';
import { getRelationships, type RelationshipSummary } from '../api/relationship';

interface AppState {
  selectedRelationshipId?: number;
  unreadNotificationCount: number;
  hasCoupleRelationship: boolean;
  relationships: RelationshipSummary[];
  setSelectedRelationshipId: (id?: number) => void;
  refreshUnreadCount: () => Promise<void>;
  refreshRelationshipsAndTheme: () => Promise<RelationshipSummary[]>;
  resetAppState: () => void;
}

export const useAppStore = create<AppState>((set) => ({
  selectedRelationshipId: undefined,
  unreadNotificationCount: 0,
  hasCoupleRelationship: false,
  relationships: [],
  setSelectedRelationshipId: (id) => set({ selectedRelationshipId: id }),
  refreshUnreadCount: async () => {
    const result = await getUnreadCount();
    set({ unreadNotificationCount: result.count || 0 });
  },
  refreshRelationshipsAndTheme: async () => {
    const relationships = await getRelationships();
    const hasCoupleRelationship = relationships.some(
      (item) => item.type === 'COUPLE' && item.status === 'ACTIVE'
    );
    set({ relationships, hasCoupleRelationship });
    return relationships;
  },
  resetAppState: () => {
    set({
      selectedRelationshipId: undefined,
      unreadNotificationCount: 0,
      hasCoupleRelationship: false,
      relationships: []
    });
  }
}));
