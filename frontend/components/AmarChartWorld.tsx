'use client';

import { useEffect, useRef } from 'react';
import { createChart, ColorType, type IChartApi, type ISeriesApi, type CandlestickData, type Time } from 'lightweight-charts';

const seed: CandlestickData[] = Array.from({ length: 64 }, (_, i) => {
  const base = 3460 + Math.sin(i / 5) * 10 + i * 0.16;
  const open = base + Math.sin(i * 1.7) * 1.4;
  const close = base + Math.cos(i * 1.1) * 1.8;
  return { time: (Date.now() / 1000 - (64 - i) * 300) as Time, open, high: Math.max(open, close) + 2.1, low: Math.min(open, close) - 2.0, close };
});

export default function AmarChartWorld() {
  const host = useRef<HTMLDivElement>(null);
  const chart = useRef<IChartApi>();
  const series = useRef<ISeriesApi<'Candlestick'>>();

  useEffect(() => {
    if (!host.current) return;
    const api = createChart(host.current, {
      layout: { background: { type: ColorType.Solid, color: 'transparent' }, textColor: '#8ca8b4' },
      grid: { vertLines: { color: '#ffffff08' }, horzLines: { color: '#ffffff08' } },
      rightPriceScale: { borderColor: '#ffffff12' },
      timeScale: { borderColor: '#ffffff12', timeVisible: true },
      crosshair: { mode: 1 },
      autoSize: true,
    });
    const candles = api.addCandlestickSeries({ upColor: '#4ee6a3', downColor: '#ff668e', borderVisible: false, wickUpColor: '#4ee6a3', wickDownColor: '#ff668e' });
    candles.setData(seed);
    api.timeScale().fitContent();
    chart.current = api;
    series.current = candles;
    const observer = new ResizeObserver(() => api.resize(host.current!.clientWidth, host.current!.clientHeight));
    observer.observe(host.current);
    return () => { observer.disconnect(); api.remove(); chart.current = undefined; series.current = undefined; };
  }, []);

  return <div className="amar-chart-world"><div className="chart-world-head"><span>XAUUSD · M5</span><b>DEMO MARKET</b></div><div ref={host} className="chart-world-canvas" /></div>;
}
