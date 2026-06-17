import type { CSSProperties } from 'react';
import { useBackgroundStore } from '../../store/backgroundStore';

type BackgroundLayerStyle = CSSProperties & {
  '--user-background-image'?: string;
  '--user-background-position-x'?: string;
  '--user-background-position-y'?: string;
  '--user-background-size'?: string;
  '--user-background-opacity'?: number;
  '--user-background-blur'?: string;
  '--user-background-overlay-opacity'?: number;
};

export function BackgroundLayer() {
  const setting = useBackgroundStore((state) => state.setting);
  const showImage = setting.enabled && Boolean(setting.imageUrl);
  const style: BackgroundLayerStyle = showImage
    ? {
        '--user-background-image': `url("${setting.imageUrl}")`,
        '--user-background-position-x': `${setting.positionX}%`,
        '--user-background-position-y': `${setting.positionY}%`,
        '--user-background-size': `${Math.round(setting.scale * 100)}% auto`,
        '--user-background-opacity': setting.opacity,
        '--user-background-blur': `${setting.blur}px`,
        '--user-background-overlay-opacity': setting.overlayOpacity,
      }
    : {};

  return (
    <div className={`app-background-layer page-background ${showImage ? 'has-user-background' : ''}`} style={style} aria-hidden="true">
      <div className="app-background-image" />
      <div className="app-background-overlay" />
    </div>
  );
}
