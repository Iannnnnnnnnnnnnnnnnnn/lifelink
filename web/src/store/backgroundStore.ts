import { create } from 'zustand';
import {
  getUserBackgroundSetting,
  saveUserBackgroundSetting,
  UserBackgroundSetting,
} from '../api/userBackground';

export const DEFAULT_BACKGROUND_SETTING: UserBackgroundSetting = {
  enabled: false,
  imageUrl: null,
  objectKey: null,
  scale: 1,
  positionX: 50,
  positionY: 50,
  presetPosition: 'CENTER',
  opacity: 0.22,
  blur: 0,
  overlayOpacity: 0.35,
  scope: 'GLOBAL',
};

interface BackgroundState {
  setting: UserBackgroundSetting;
  loaded: boolean;
  loading: boolean;
  setSetting: (setting: UserBackgroundSetting) => void;
  updateSetting: (setting: Partial<UserBackgroundSetting>) => void;
  fetchSetting: (force?: boolean) => Promise<void>;
  saveSetting: () => Promise<UserBackgroundSetting>;
  resetLocalSetting: () => void;
}

export const useBackgroundStore = create<BackgroundState>((set, get) => ({
  setting: DEFAULT_BACKGROUND_SETTING,
  loaded: false,
  loading: false,
  setSetting: (setting) => {
    set({
      setting: { ...DEFAULT_BACKGROUND_SETTING, ...setting },
      loaded: true,
    });
  },
  updateSetting: (setting) => {
    set((state) => ({
      setting: { ...state.setting, ...setting },
    }));
  },
  fetchSetting: async (force = false) => {
    const state = get();
    if (state.loading || (state.loaded && !force)) {
      return;
    }
    set({ loading: true });
    try {
      const response = await getUserBackgroundSetting();
      set({
        setting: { ...DEFAULT_BACKGROUND_SETTING, ...response.data.data },
        loaded: true,
      });
    } finally {
      set({ loading: false });
    }
  },
  saveSetting: async () => {
    const setting = get().setting;
    const response = await saveUserBackgroundSetting({
      enabled: setting.enabled,
      objectKey: setting.objectKey,
      scale: setting.scale,
      positionX: setting.positionX,
      positionY: setting.positionY,
      presetPosition: setting.presetPosition,
      opacity: setting.opacity,
      blur: setting.blur,
      overlayOpacity: setting.overlayOpacity,
      scope: 'GLOBAL',
    });
    const nextSetting = { ...DEFAULT_BACKGROUND_SETTING, ...response.data.data };
    set({
      setting: nextSetting,
      loaded: true,
    });
    return nextSetting;
  },
  resetLocalSetting: () => {
    set({
      setting: DEFAULT_BACKGROUND_SETTING,
      loaded: false,
      loading: false,
    });
  },
}));
