export type AmarMotionLevel = 'OFF' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CINEMATIC';

export const MOTION_LEVELS: Record<AmarMotionLevel, { multiplier: number; blur: number; particles: number }> = {
  OFF: { multiplier: 0, blur: 0, particles: 0 },
  LOW: { multiplier: 0.35, blur: 8, particles: 40 },
  MEDIUM: { multiplier: 0.65, blur: 14, particles: 90 },
  HIGH: { multiplier: 1, blur: 20, particles: 160 },
  CINEMATIC: { multiplier: 1.35, blur: 28, particles: 260 },
};

export function motionDuration(baseMs: number, level: AmarMotionLevel): number {
  const multiplier = MOTION_LEVELS[level].multiplier;
  if (multiplier === 0) return 0;
  return Math.max(80, Math.round(baseMs / multiplier));
}
