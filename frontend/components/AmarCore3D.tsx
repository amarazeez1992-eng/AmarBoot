'use client';

import { Canvas, useFrame } from '@react-three/fiber';
import { Float, OrbitControls, Sparkles, Sphere, Text } from '@react-three/drei';
import { useRef } from 'react';
import * as THREE from 'three';

function CoreMesh({ activity }: { activity: number }) {
  const group = useRef<THREE.Group>(null);
  useFrame(({ clock }) => {
    if (!group.current) return;
    const t = clock.getElapsedTime();
    group.current.rotation.y = t * (0.18 + activity * 0.28);
    group.current.rotation.x = Math.sin(t * 0.4) * 0.12;
    const s = 1 + Math.sin(t * (1.2 + activity * 2)) * (0.025 + activity * 0.05);
    group.current.scale.setScalar(s);
  });

  return (
    <group ref={group}>
      <Sphere args={[1.05, 64, 64]}>
        <meshStandardMaterial color="#67f5ff" emissive="#2563eb" emissiveIntensity={2.5 + activity * 3} roughness={0.18} metalness={0.72} transparent opacity={0.92} />
      </Sphere>
      <Sphere args={[1.34, 48, 48]}>
        <meshBasicMaterial color="#8b5cf6" transparent opacity={0.11 + activity * 0.08} wireframe />
      </Sphere>
      <Sphere args={[1.62, 32, 32]}>
        <meshBasicMaterial color="#22d3ee" transparent opacity={0.055 + activity * 0.04} wireframe />
      </Sphere>
      <pointLight color="#35e8ff" intensity={8 + activity * 10} distance={8} />
    </group>
  );
}

export default function AmarCore3D({ activity = 0.5 }: { activity?: number }) {
  return (
    <div className="core3d">
      <Canvas camera={{ position: [0, 0.1, 4.6], fov: 42 }} dpr={[1, 2]}>
        <ambientLight intensity={0.28} />
        <directionalLight position={[3, 4, 5]} intensity={2.2} color="#c4b5fd" />
        <Sparkles count={180} scale={[7, 5, 7]} size={2.1} speed={0.22 + activity * 0.55} color="#8be9ff" />
        <Float speed={1.4 + activity} rotationIntensity={0.15} floatIntensity={0.35}>
          <CoreMesh activity={activity} />
        </Float>
        <Text position={[0, -1.85, 0]} fontSize={0.22} color="#dffbff" anchorX="center">AMAR AI CORE</Text>
        <OrbitControls enableZoom={false} enablePan={false} autoRotate autoRotateSpeed={0.35} />
      </Canvas>
    </div>
  );
}
