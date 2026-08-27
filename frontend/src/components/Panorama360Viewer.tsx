import React, { useEffect, useRef, useState, useCallback } from 'react';
import * as THREE from 'three';
import {
  X,
  Maximize2,
  Minimize2,
  ZoomIn,
  ZoomOut,
  RotateCcw,
  Play,
  Pause,
  Compass,
  Sparkles,
  AlertCircle,
  Eye,
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export interface Panorama360ViewerProps {
  panoramaUrl: string;
  title?: string;
  subtitle?: string;
  isOpen: boolean;
  onClose: () => void;
}

export const Panorama360Viewer: React.FC<Panorama360ViewerProps> = ({
  panoramaUrl,
  title = '360° Interactive Virtual Tour',
  subtitle = 'Drag to explore in 360° • Pinch or scroll to zoom',
  isOpen,
  onClose,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isAutoRotating, setIsAutoRotating] = useState(true);
  const [currentFov, setCurrentFov] = useState(75);

  // Three.js instances ref
  const sceneRef = useRef<THREE.Scene | null>(null);
  const cameraRef = useRef<THREE.PerspectiveCamera | null>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  const sphereMeshRef = useRef<THREE.Mesh | null>(null);
  const textureRef = useRef<THREE.Texture | null>(null);
  const animFrameIdRef = useRef<number | null>(null);

  // Interaction State
  const isDraggingRef = useRef(false);
  const previousMousePositionRef = useRef({ x: 0, y: 0 });
  const touchStartDistRef = useRef(0);
  const lonRef = useRef(0);
  const latRef = useRef(0);
  const targetLonRef = useRef(0);
  const targetLatRef = useRef(0);

  // Trap scroll when modal is active
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  // Keybindings (ESC, Arrows, + / -)
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (document.fullscreenElement) {
          document.exitFullscreen().catch(console.error);
        } else {
          onClose();
        }
      } else if (e.key === 'ArrowLeft') {
        targetLonRef.current -= 10;
      } else if (e.key === 'ArrowRight') {
        targetLonRef.current += 10;
      } else if (e.key === 'ArrowUp') {
        targetLatRef.current = Math.min(85, targetLatRef.current + 8);
      } else if (e.key === 'ArrowDown') {
        targetLatRef.current = Math.max(-85, targetLatRef.current - 8);
      } else if (e.key === '+' || e.key === '=') {
        handleZoomIn();
      } else if (e.key === '-' || e.key === '_') {
        handleZoomOut();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  // Initialize Three.js Equirectangular Scene
  useEffect(() => {
    if (!isOpen || !containerRef.current || !canvasRef.current) return;

    setIsLoading(true);
    setHasError(false);

    const container = containerRef.current;
    const canvas = canvasRef.current;
    const width = container.clientWidth || 800;
    const height = container.clientHeight || 500;

    // 1. Scene & Camera
    const scene = new THREE.Scene();
    sceneRef.current = scene;

    const camera = new THREE.PerspectiveCamera(75, width / height, 1, 1100);
    cameraRef.current = camera;
    const cameraTarget = new THREE.Vector3(0, 0, 0);

    // 2. WebGL Renderer with High Precision & Antialiasing
    const renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: true,
      powerPreference: 'high-performance',
      alpha: false,
    });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setSize(width, height);
    rendererRef.current = renderer;

    // 3. Inverted Equirectangular Sphere (500 radius)
    const geometry = new THREE.SphereGeometry(500, 60, 40);
    geometry.scale(-1, 1, 1); // Invert normals so texture is visible inside

    // 4. Texture Loader with cross-origin support & graceful error handler
    const textureLoader = new THREE.TextureLoader();
    textureLoader.setCrossOrigin('anonymous');

    textureLoader.load(
      panoramaUrl,
      (texture) => {
        texture.colorSpace = THREE.SRGBColorSpace;
        texture.minFilter = THREE.LinearFilter;
        texture.generateMipmaps = false;
        textureRef.current = texture;

        const material = new THREE.MeshBasicMaterial({ map: texture });
        const mesh = new THREE.Mesh(geometry, material);
        scene.add(mesh);
        sphereMeshRef.current = mesh;

        setIsLoading(false);
      },
      undefined,
      (err) => {
        console.warn('360 panorama texture failed to load:', panoramaUrl, err);
        setHasError(true);
        setIsLoading(false);
      }
    );

    // 5. Render Loop with Smooth Damping & Auto-Rotation
    let isDestroyed = false;

    const animate = () => {
      if (isDestroyed) return;
      animFrameIdRef.current = requestAnimationFrame(animate);

      // Auto rotation when not dragging
      if (isAutoRotating && !isDraggingRef.current) {
        targetLonRef.current += 0.08;
      }

      // Smooth inertia damping towards target
      lonRef.current += (targetLonRef.current - lonRef.current) * 0.12;
      latRef.current += (targetLatRef.current - latRef.current) * 0.12;

      // Clamp latitude to prevent gimbal flips
      latRef.current = Math.max(-85, Math.min(85, latRef.current));

      const phi = THREE.MathUtils.degToRad(90 - latRef.current);
      const theta = THREE.MathUtils.degToRad(lonRef.current);

      cameraTarget.x = 500 * Math.sin(phi) * Math.cos(theta);
      cameraTarget.y = 500 * Math.cos(phi);
      cameraTarget.z = 500 * Math.sin(phi) * Math.sin(theta);

      camera.lookAt(cameraTarget);
      renderer.render(scene, camera);
    };

    animate();

    // 6. Resize Observer
    const handleResize = () => {
      if (!container || !camera || !renderer) return;
      const w = container.clientWidth;
      const h = container.clientHeight;
      if (w > 0 && h > 0) {
        camera.aspect = w / h;
        camera.updateProjectionMatrix();
        renderer.setSize(w, h);
      }
    };

    const resizeObserver = new ResizeObserver(handleResize);
    resizeObserver.observe(container);

    return () => {
      isDestroyed = true;
      if (animFrameIdRef.current) cancelAnimationFrame(animFrameIdRef.current);
      resizeObserver.disconnect();

      if (rendererRef.current) {
        rendererRef.current.dispose();
      }
      if (textureRef.current) {
        textureRef.current.dispose();
      }
      geometry.dispose();
    };
  }, [isOpen, panoramaUrl, isAutoRotating]);

  // Pointer / Mouse Event Handlers
  const handlePointerDown = (e: React.PointerEvent) => {
    isDraggingRef.current = true;
    previousMousePositionRef.current = { x: e.clientX, y: e.clientY };
    (e.target as HTMLElement).setPointerCapture(e.pointerId);
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!isDraggingRef.current) return;
    const deltaX = e.clientX - previousMousePositionRef.current.x;
    const deltaY = e.clientY - previousMousePositionRef.current.y;

    targetLonRef.current -= deltaX * 0.18;
    targetLatRef.current += deltaY * 0.18;

    previousMousePositionRef.current = { x: e.clientX, y: e.clientY };
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    isDraggingRef.current = false;
    try {
      (e.target as HTMLElement).releasePointerCapture(e.pointerId);
    } catch {
      // ignore
    }
  };

  // Wheel Zoom
  const handleWheel = (e: React.WheelEvent) => {
    e.preventDefault();
    if (!cameraRef.current) return;
    const newFov = Math.min(100, Math.max(35, cameraRef.current.fov + e.deltaY * 0.05));
    cameraRef.current.fov = newFov;
    cameraRef.current.updateProjectionMatrix();
    setCurrentFov(Math.round(newFov));
  };

  // Touch Pinch Zoom Handler
  const handleTouchMove = (e: React.TouchEvent) => {
    if (e.touches.length === 2 && cameraRef.current) {
      const dist = Math.hypot(
        e.touches[0].clientX - e.touches[1].clientX,
        e.touches[0].clientY - e.touches[1].clientY
      );
      if (touchStartDistRef.current > 0) {
        const delta = (touchStartDistRef.current - dist) * 0.1;
        const newFov = Math.min(100, Math.max(35, cameraRef.current.fov + delta));
        cameraRef.current.fov = newFov;
        cameraRef.current.updateProjectionMatrix();
        setCurrentFov(Math.round(newFov));
      }
      touchStartDistRef.current = dist;
    }
  };

  const handleTouchEnd = () => {
    touchStartDistRef.current = 0;
  };

  // Zoom Controls
  const handleZoomIn = useCallback(() => {
    if (!cameraRef.current) return;
    const newFov = Math.max(35, cameraRef.current.fov - 10);
    cameraRef.current.fov = newFov;
    cameraRef.current.updateProjectionMatrix();
    setCurrentFov(Math.round(newFov));
  }, []);

  const handleZoomOut = useCallback(() => {
    if (!cameraRef.current) return;
    const newFov = Math.min(100, cameraRef.current.fov + 10);
    cameraRef.current.fov = newFov;
    cameraRef.current.updateProjectionMatrix();
    setCurrentFov(Math.round(newFov));
  }, []);

  const handleReset = useCallback(() => {
    targetLonRef.current = 0;
    targetLatRef.current = 0;
    if (cameraRef.current) {
      cameraRef.current.fov = 75;
      cameraRef.current.updateProjectionMatrix();
      setCurrentFov(75);
    }
  }, []);

  const toggleFullscreen = () => {
    if (!containerRef.current) return;
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen().then(() => setIsFullscreen(true)).catch(console.error);
    } else {
      document.exitFullscreen().then(() => setIsFullscreen(false)).catch(console.error);
    }
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-xl p-2 sm:p-4 md:p-6"
        role="dialog"
        aria-modal="true"
        aria-label="360 Panorama Tour"
      >
        <motion.div
          ref={containerRef}
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.95, opacity: 0 }}
          transition={{ type: 'spring', damping: 25, stiffness: 300 }}
          className="relative w-full max-w-6xl h-[85vh] sm:h-[88vh] rounded-3xl overflow-hidden bg-[#0A0B0E] border border-white/15 shadow-2xl flex flex-col select-none"
        >
          {/* Header Bar */}
          <div className="absolute top-0 left-0 right-0 z-20 flex items-center justify-between p-4 sm:p-6 bg-gradient-to-b from-black/80 via-black/40 to-transparent pointer-events-none">
            <div className="flex items-center gap-3 pointer-events-auto">
              <div className="w-10 h-10 rounded-2xl bg-amber-400/20 border border-amber-400/40 flex items-center justify-center text-amber-400 shadow-glow-gold backdrop-blur-md">
                <Compass className="w-5 h-5 animate-spin-slow" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-[10px] sm:text-xs font-black uppercase tracking-widest px-2 py-0.5 rounded-full bg-amber-400 text-black shadow-glow-gold">
                    360° Virtual Experience
                  </span>
                </div>
                <h3 className="text-base sm:text-xl font-bold text-white tracking-tight drop-shadow-md">
                  {title}
                </h3>
              </div>
            </div>

            {/* Top Right Close Button */}
            <div className="flex items-center gap-2 pointer-events-auto">
              <button
                type="button"
                onClick={toggleFullscreen}
                className="hidden sm:flex p-2.5 rounded-xl bg-black/50 hover:bg-black/80 text-white border border-white/15 backdrop-blur-md transition hover:scale-105"
                title={isFullscreen ? 'Exit Fullscreen' : 'Fullscreen'}
                aria-label="Toggle Fullscreen"
              >
                {isFullscreen ? <Minimize2 className="w-5 h-5" /> : <Maximize2 className="w-5 h-5" />}
              </button>

              <button
                type="button"
                onClick={onClose}
                className="p-2.5 rounded-xl bg-black/50 hover:bg-rose-500/80 text-white border border-white/15 backdrop-blur-md transition hover:scale-105"
                title="Close 360 View (ESC)"
                aria-label="Close"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* Interactive WebGL Canvas */}
          <div
            className="relative flex-1 w-full h-full cursor-grab active:cursor-grabbing touch-none"
            onPointerDown={handlePointerDown}
            onPointerMove={handlePointerMove}
            onPointerUp={handlePointerUp}
            onPointerCancel={handlePointerUp}
            onWheel={handleWheel}
            onTouchMove={handleTouchMove}
            onTouchEnd={handleTouchEnd}
          >
            <canvas ref={canvasRef} className="w-full h-full block" />

            {/* Loading Indicator */}
            {isLoading && (
              <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-black/80 backdrop-blur-md text-white gap-3">
                <div className="w-12 h-12 rounded-full border-3 border-amber-400 border-t-transparent animate-spin" />
                <p className="text-xs sm:text-sm font-semibold tracking-wide text-slate-300 animate-pulse">
                  Rendering 360° Spherical Environment...
                </p>
              </div>
            )}

            {/* Error Fallback */}
            {hasError && (
              <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-[#12131A] text-white p-6 text-center">
                <AlertCircle className="w-12 h-12 text-amber-400 mb-3" />
                <h4 className="text-lg font-bold text-white mb-1">360° Panorama Unavailable</h4>
                <p className="text-xs text-slate-400 max-w-md mb-4">
                  We could not render the 360° equirectangular projection for this room. You can view the high-resolution photo gallery instead.
                </p>
                <button
                  type="button"
                  onClick={onClose}
                  className="px-5 py-2 rounded-xl bg-amber-400 text-black font-bold hover:bg-amber-500 transition"
                >
                  Return to Hotel Gallery
                </button>
              </div>
            )}
          </div>

          {/* Floating Cinematic HUD Controls */}
          <div className="absolute bottom-4 sm:bottom-6 left-4 right-4 z-20 flex flex-col sm:flex-row items-center justify-between gap-3 pointer-events-none">
            {/* Exploration Hint */}
            <div className="pointer-events-auto flex items-center gap-2 px-3.5 py-2 rounded-2xl bg-black/60 backdrop-blur-xl border border-white/10 text-xs text-slate-300">
              <Eye className="w-3.5 h-3.5 text-amber-400 animate-pulse" />
              <span>{subtitle}</span>
            </div>

            {/* Interactive Control Pill */}
            <div className="pointer-events-auto flex items-center gap-1.5 p-1.5 rounded-2xl bg-black/70 backdrop-blur-xl border border-white/15 shadow-2xl">
              <button
                type="button"
                onClick={() => setIsAutoRotating(!isAutoRotating)}
                className={`p-2 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
                  isAutoRotating
                    ? 'bg-amber-400 text-black shadow-glow-gold'
                    : 'bg-white/10 text-slate-300 hover:text-white hover:bg-white/20'
                }`}
                title={isAutoRotating ? 'Pause Auto-Rotate' : 'Play Auto-Rotate'}
              >
                {isAutoRotating ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                <span className="hidden sm:inline">{isAutoRotating ? 'Rotating' : 'Rotate'}</span>
              </button>

              <div className="w-px h-5 bg-white/15 mx-1" />

              <button
                type="button"
                onClick={handleZoomIn}
                className="p-2 rounded-xl bg-white/10 hover:bg-white/20 text-white transition hover:scale-105"
                title="Zoom In (+)"
                aria-label="Zoom In"
              >
                <ZoomIn className="w-4 h-4" />
              </button>

              <button
                type="button"
                onClick={handleZoomOut}
                className="p-2 rounded-xl bg-white/10 hover:bg-white/20 text-white transition hover:scale-105"
                title="Zoom Out (-)"
                aria-label="Zoom Out"
              >
                <ZoomOut className="w-4 h-4" />
              </button>

              <button
                type="button"
                onClick={handleReset}
                className="p-2 rounded-xl bg-white/10 hover:bg-white/20 text-white transition hover:scale-105"
                title="Reset Orientation"
                aria-label="Reset View"
              >
                <RotateCcw className="w-4 h-4" />
              </button>
            </div>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
};
