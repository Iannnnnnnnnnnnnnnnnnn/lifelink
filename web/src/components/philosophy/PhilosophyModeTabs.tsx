import { Tabs } from 'antd';

interface PhilosophyModeTabsProps {
  activeKey: string;
  onChange: (key: string) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function PhilosophyModeTabs({ activeKey, onChange, t }: PhilosophyModeTabsProps) {
  return (
    <Tabs
      className="philosophy-mode-tabs"
      activeKey={activeKey}
      onChange={onChange}
      items={[
        { key: 'perspectives', label: t('philosophy.modePerspectives') },
        { key: 'chat', label: t('philosophy.modeChat') },
      ]}
    />
  );
}
