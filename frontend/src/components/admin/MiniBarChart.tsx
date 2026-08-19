import React from 'react';
import { TrendDataPoint } from '../../types/analytics';

interface MiniBarChartProps {
  data: TrendDataPoint[];
  valueKey: keyof TrendDataPoint;
  label?: string;
  color?: string;
  formatValue?: (v: number) => string;
  height?: number;
}

/**
 * Lightweight pure SVG bar chart with tooltip.
 * No external charting library dependency needed — keep bundle lightweight.
 */
export const MiniBarChart: React.FC<MiniBarChartProps> = ({
  data,
  valueKey,
  label = 'Value',
  color = '#2563eb',
  formatValue = (v) => v.toLocaleString('en-IN'),
  height = 160,
}) => {
  const [hoveredIdx, setHoveredIdx] = React.useState<number | null>(null);

  if (!data || data.length === 0) {
    return (
      <div
        style={{ height }}
        className="flex items-center justify-center text-sm text-slate-400 dark:text-slate-500 bg-slate-50 dark:bg-slate-800/50 rounded-lg border border-dashed border-slate-200 dark:border-slate-700"
      >
        No trend data available for selected period
      </div>
    );
  }

  const values = data.map((d) => {
    const raw = d[valueKey];
    return typeof raw === 'number' ? raw : 0;
  });

  const maxVal = Math.max(...values, 1);
  const chartHeight = height - 40; // reserve space for bottom labels
  const barCount = data.length;
  const svgWidth = 600;
  const paddingX = 20;
  const availableWidth = svgWidth - paddingX * 2;
  const slotWidth = availableWidth / barCount;
  const barWidth = Math.max(Math.min(slotWidth * 0.6, 24), 4);

  return (
    <div className="relative w-full">
      <svg
        viewBox={`0 0 ${svgWidth} ${height}`}
        className="w-full overflow-visible"
        style={{ height }}
      >
        {/* Horizontal grid lines */}
        {[0, 0.25, 0.5, 0.75, 1].map((pct) => {
          const y = chartHeight - pct * chartHeight + 10;
          return (
            <line
              key={pct}
              x1={paddingX}
              y1={y}
              x2={svgWidth - paddingX}
              y2={y}
              stroke="currentColor"
              className="text-slate-100 dark:text-slate-700/50"
              strokeDasharray="3 3"
              strokeWidth="1"
            />
          );
        })}

        {/* Bars */}
        {data.map((item, idx) => {
          const val = values[idx];
          const barH = (val / maxVal) * chartHeight;
          const x = paddingX + idx * slotWidth + (slotWidth - barWidth) / 2;
          const y = chartHeight - barH + 10;
          const isHovered = hoveredIdx === idx;

          return (
            <g key={item.date || idx}>
              <rect
                x={x}
                y={y}
                width={barWidth}
                height={Math.max(barH, 2)}
                rx="3"
                fill={color}
                opacity={isHovered ? 1 : 0.8}
                className="transition-all duration-150 cursor-pointer"
                onMouseEnter={() => setHoveredIdx(idx)}
                onMouseLeave={() => setHoveredIdx(null)}
              />
              {/* Show date on bottom for every nth item */}
              {(idx === 0 || idx === barCount - 1 || idx === Math.floor(barCount / 2)) && (
                <text
                  x={x + barWidth / 2}
                  y={height - 5}
                  textAnchor="middle"
                  className="fill-slate-400 dark:fill-slate-500 text-[10px]"
                >
                  {item.date ? item.date.slice(5) : ''}
                </text>
              )}
            </g>
          );
        })}
      </svg>

      {/* Floating tooltip */}
      {hoveredIdx !== null && data[hoveredIdx] && (
        <div
          className="absolute z-10 bg-slate-900 text-white text-xs rounded-lg px-2.5 py-1.5 shadow-lg pointer-events-none transform -translate-x-1/2 -translate-y-full -mt-2"
          style={{
            left: `${((hoveredIdx + 0.5) / barCount) * 100}%`,
            top: '40px',
          }}
        >
          <div className="font-semibold text-slate-200">{data[hoveredIdx].date}</div>
          <div className="text-primary-300 font-bold">
            {label}: {formatValue(values[hoveredIdx])}
          </div>
        </div>
      )}
    </div>
  );
};
