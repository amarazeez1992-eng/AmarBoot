import './globals.css';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'AMAR AI — Trading Command OS',
  description: 'AMAR Aurora Nexus — demo command interface'
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return <html lang="ar" dir="rtl"><body>{children}</body></html>;
}
