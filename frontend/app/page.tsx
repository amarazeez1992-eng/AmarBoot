'use client';

import { useMemo } from 'react';
import Item10Workspace from './amar-ui';
import { createAmarEngineBridge } from './amar-ui/amarEngineBridge';

export default function Page() {
  const bridge = useMemo(() => createAmarEngineBridge(), []);
  return <Item10Workspace bridge={bridge} />;
}
