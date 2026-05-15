import { View } from '@tarojs/components';
import type React from 'react';
import { useAppStore } from '../store/appStore';

interface PageShellProps {
  children: React.ReactNode;
  className?: string;
}

export function PageShell({ children, className = '' }: PageShellProps) {
  const hasCoupleRelationship = useAppStore((state) => state.hasCoupleRelationship);
  return (
    <View className={`${hasCoupleRelationship ? 'theme-colorful' : 'theme-grayscale'} ${className}`}>
      {children}
    </View>
  );
}
