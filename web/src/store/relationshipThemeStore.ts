import { create } from 'zustand';
import { getRelationships } from '../api/relationship';
import { TOKEN_STORAGE_KEY } from '../api/request';

interface RelationshipThemeState {
  hasCoupleRelationship: boolean;
  relationshipThemeLoaded: boolean;
  fetchRelationshipThemeStatus: () => Promise<void>;
  resetRelationshipThemeStatus: () => void;
}

export const useRelationshipThemeStore = create<RelationshipThemeState>((set) => ({
  hasCoupleRelationship: false,
  relationshipThemeLoaded: false,
  fetchRelationshipThemeStatus: async () => {
    if (!localStorage.getItem(TOKEN_STORAGE_KEY)) {
      set({
        hasCoupleRelationship: false,
        relationshipThemeLoaded: false,
      });
      return;
    }

    try {
      const response = await getRelationships();
      const hasActiveCouple = response.data.data.some((item) => item.type === 'COUPLE' && item.status === 'ACTIVE');
      set({
        hasCoupleRelationship: hasActiveCouple,
        relationshipThemeLoaded: true,
      });
    } catch (error) {
      console.warn('Failed to load relationship theme status.', error);
      set({
        hasCoupleRelationship: false,
        relationshipThemeLoaded: true,
      });
    }
  },
  resetRelationshipThemeStatus: () => {
    set({
      hasCoupleRelationship: false,
      relationshipThemeLoaded: false,
    });
  },
}));
