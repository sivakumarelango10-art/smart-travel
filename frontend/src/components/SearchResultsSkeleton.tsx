import React from 'react';
import { FlightCardSkeleton } from './FlightCardSkeleton';

export const SearchResultsSkeleton: React.FC<{ count?: number }> = ({ count = 4 }) => {
  return (
    <div className="space-y-4">
      {Array.from({ length: count }).map((_, i) => (
        <FlightCardSkeleton key={i} />
      ))}
    </div>
  );
};
