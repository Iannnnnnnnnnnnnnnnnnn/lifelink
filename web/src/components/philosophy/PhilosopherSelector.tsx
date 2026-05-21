import { Select, Space, Tag, Typography } from 'antd';
import type { Philosopher } from '../../api/philosophy';

interface PhilosopherSelectorProps {
  philosophers: Philosopher[];
  value: string[];
  onChange: (value: string[]) => void;
  placeholder: string;
}

export function PhilosopherSelector({ philosophers, value, onChange, placeholder }: PhilosopherSelectorProps) {
  return (
    <Select
      mode="multiple"
      className="philosopher-selector"
      value={value}
      onChange={onChange}
      maxCount={8}
      placeholder={placeholder}
      optionLabelProp="label"
      options={philosophers.map((philosopher) => ({
        value: philosopher.code,
        label: philosopher.name,
        searchText: `${philosopher.name} ${philosopher.nameZh} ${philosopher.nameEn}`,
        title: philosopher.name,
        option: philosopher,
      }))}
      optionRender={(option) => {
        const philosopher = option.data.option as Philosopher;
        return (
          <div className="philosopher-option">
            <Space direction="vertical" size={2}>
              <Typography.Text strong>{philosopher.name}</Typography.Text>
              <Typography.Text type="secondary">{philosopher.era}</Typography.Text>
              <div className="philosopher-option-tags">
                {philosopher.tags.slice(0, 3).map((tag) => (
                  <Tag key={tag}>{tag}</Tag>
                ))}
              </div>
            </Space>
          </div>
        );
      }}
      filterOption={(input, option) => {
        const text = String(option?.searchText || '').toLowerCase();
        return text.includes(input.toLowerCase());
      }}
    />
  );
}
