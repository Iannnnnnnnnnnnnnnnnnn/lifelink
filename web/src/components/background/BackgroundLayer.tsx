import type { CSSProperties } from 'react';
import { useBackgroundStore } from '../../store/backgroundStore';

type BackgroundLayerStyle = CSSProperties & {
  '--app-background-image'?: string;
  '--app-background-position'?: string;
  '--app-background-size'?: string;
  '--app-background-opacity'?: number;
  '--app-background-blur'?: string;
  '--app-background-overlay-opacity'?: number;
};

export function BackgroundLayer() {
  const setting = useBackgroundStore((state) => state.setting);
  const showImage = setting.enabled && Boolean(setting.imageUrl);
  const style: BackgroundLayerStyle = showImage
    ? {
        '--app-background-image': `url("${setting.imageUrl}")`,
        '--app-background-position': `${setting.positionX}% ${setting.positionY}%`,
        '--app-background-size': `${Math.round(setting.scale * 100)}% auto`,
        '--app-background-opacity': setting.opacity,
        '--app-background-blur': `${setting.blur}px`,
        '--app-background-overlay-opacity': setting.overlayOpacity,
      }
    : {};

  return (
    <div className={`app-background-layer page-background ${showImage ? 'has-user-background' : ''}`} style={style} aria-hidden="true">
      <div className="app-background-image" />
      <div className="app-background-overlay" />
    </div>
  );
}
