import { Segmented } from 'antd';

interface PhilosophyModeTabsProps {
  activeKey: string;
  onChange: (key: string) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function PhilosophyModeTabs({ activeKey, onChange, t }: PhilosophyModeTabsProps) {
  return (
    <Segmented<string>
      className="philosophy-mode-tabs"
      value={activeKey}
      onChange={onChange}
      options={[
        { key: 'perspectives', label: t('philosophy.modePerspectives') },
        { key: 'chat', label: t('philosophy.modeChat') },
      ].map(({ key, label }) => ({ value: key, label }))}
    />
  );
}
