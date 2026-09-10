import './globals.css';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'عمار — المحور الذكي للتداول',
  description: 'واجهة عمار الحية — مركز قيادة تداول تجريبي آمن',
  manifest: '/manifest.webmanifest',
  themeColor: '#dff7fb',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return <html lang="ar" dir="rtl"><body>{children}</body></html>;
}
