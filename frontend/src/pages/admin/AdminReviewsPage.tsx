import React, { useState, useEffect, useCallback } from 'react';
import {
  Star,
  Flag,
  ShieldCheck,
  CheckCircle2,
  EyeOff,
  Trash2,
  RotateCcw,
  AlertTriangle,
  MessageSquare,
  Search,
  Filter,
  Camera,
  ExternalLink,
  Users,
  Building2,
  Plane,
  X,
  Clock,
} from 'lucide-react';
import { Review, ReviewStatus, ReviewTargetType } from '../../types/review';
import { reviewService } from '../../services/reviewService';
import { StarRating } from '../../components/StarRating';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { ConfirmModal } from '../../components/admin/ConfirmModal';

export const AdminReviewsPage: React.FC = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  // Filter tabs: 'FLAGGED' | 'ALL' | 'PUBLISHED' | 'HIDDEN' | 'REMOVED'
  const [statusFilter, setStatusFilter] = useState<ReviewStatus | 'ALL'>('FLAGGED');
  const [targetTypeFilter, setTargetTypeFilter] = useState<ReviewTargetType | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Moderation Action Modal States
  const [selectedReview, setSelectedReview] = useState<Review | null>(null);
  const [actionType, setActionType] = useState<'APPROVE' | 'HIDE' | 'REMOVE' | 'RESTORE' | null>(null);
  const [removalReason, setRemovalReason] = useState('Violates community guidelines on safety and authenticity');

  // Photo viewer
  const [previewPhoto, setPreviewPhoto] = useState<string | null>(null);

  const fetchAdminReviews = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const effectiveStatus = statusFilter === 'ALL' ? undefined : statusFilter;
      const effectiveType = targetTypeFilter === 'ALL' ? undefined : targetTypeFilter;

      const res = await reviewService.getAdminReviews(effectiveStatus, effectiveType, page, 10);
      setReviews(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load reviews for moderation');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, targetTypeFilter, page]);

  useEffect(() => {
    fetchAdminReviews();
  }, [fetchAdminReviews]);

  const handleExecuteAction = async () => {
    if (!selectedReview || !actionType) return;
    try {
      if (actionType === 'APPROVE') {
        await reviewService.approveReview(selectedReview.id);
        setActionSuccess(`Review #${selectedReview.id} approved and published.`);
      } else if (actionType === 'HIDE') {
        await reviewService.hideReview(selectedReview.id);
        setActionSuccess(`Review #${selectedReview.id} hidden from public catalog.`);
      } else if (actionType === 'REMOVE') {
        await reviewService.removeReview(selectedReview.id, removalReason);
        setActionSuccess(`Review #${selectedReview.id} removed.`);
      } else if (actionType === 'RESTORE') {
        await reviewService.restoreReview(selectedReview.id);
        setActionSuccess(`Review #${selectedReview.id} restored to published status.`);
      }
      setSelectedReview(null);
      setActionType(null);
      setTimeout(() => setActionSuccess(null), 4000);
      fetchAdminReviews();
    } catch (err: any) {
      alert(err.message || 'Moderation action failed');
    }
  };

  const filteredReviews = reviews.filter((r) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      r.userFullName?.toLowerCase().includes(q) ||
      r.title?.toLowerCase().includes(q) ||
      r.body?.toLowerCase().includes(q) ||
      r.targetName?.toLowerCase().includes(q) ||
      r.targetId?.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2.5">
            <ShieldCheck className="w-7 h-7 text-sky-400" />
            <span>Review & Content Moderation Hub</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Audit user-generated feedback, inspect safety flags, remove abusive submissions, and ensure authentic travel experiences.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="px-3.5 py-1.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-bold flex items-center gap-1.5">
            <Flag className="w-4 h-4" />
            <span>Moderation Queue Active</span>
          </div>
        </div>
      </div>

      {actionSuccess && (
        <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-xl text-xs font-semibold flex items-center gap-2 animate-fade-in shadow-glow-emerald">
          <CheckCircle2 className="w-4 h-4" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* Filter Tabs & Search Bar */}
      <div className="p-4 rounded-2xl bg-[#141622] border border-white/10 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          {/* Status Tabs */}
          <div className="flex flex-wrap items-center gap-1.5">
            {(['FLAGGED', 'ALL', 'PUBLISHED', 'HIDDEN', 'REMOVED'] as const).map((st) => (
              <button
                key={st}
                type="button"
                onClick={() => {
                  setStatusFilter(st);
                  setPage(0);
                }}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition cursor-pointer ${
                  statusFilter === st
                    ? st === 'FLAGGED'
                      ? 'bg-rose-500 text-white shadow-glow-rose'
                      : 'bg-sky-500 text-white shadow-glow-sky'
                    : 'bg-[#1C1F2E] text-slate-400 hover:text-white border border-white/5'
                }`}
              >
                {st === 'FLAGGED' ? '🚩 Flagged Queue' : st}
              </button>
            ))}
          </div>

          {/* Target Type Filter */}
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 font-medium">Category:</span>
            <select
              value={targetTypeFilter}
              onChange={(e) => {
                setTargetTypeFilter(e.target.value as any);
                setPage(0);
              }}
              className="bg-[#1C1F2E] border border-white/10 rounded-xl px-3 py-1.5 text-xs text-white font-bold focus:outline-none focus:border-sky-400 cursor-pointer"
            >
              <option value="ALL">All Categories</option>
              <option value="HOTEL">Hotels Only</option>
              <option value="FLIGHT">Flights Only</option>
            </select>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by reviewer name, title, body content, or entity ID..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-[#1C1F2E] border border-white/10 rounded-xl pl-10 pr-4 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-sky-400"
          />
        </div>
      </div>

      {/* Reviews Moderation Table/Cards */}
      <div className="space-y-4">
        {loading ? (
          <div className="p-12 text-center text-slate-400 bg-[#141622] rounded-2xl border border-white/10">
            <p className="text-xs font-semibold animate-pulse">Loading moderation records...</p>
          </div>
        ) : filteredReviews.length === 0 ? (
          <div className="p-12 text-center text-slate-400 bg-[#141622] rounded-2xl border border-white/10 space-y-2">
            <CheckCircle2 className="w-8 h-8 text-emerald-400 mx-auto" />
            <p className="text-sm font-bold text-white">No reviews found in this moderation queue</p>
            <p className="text-xs text-slate-500">All submissions are clean or match the selected filters.</p>
          </div>
        ) : (
          filteredReviews.map((rev) => {
            const flagCount = rev.flagCount ?? rev.flaggedBy?.length ?? 0;
            const isFlagged = rev.status === 'FLAGGED' || flagCount > 0;

            return (
              <div
                key={rev.id}
                className={`p-6 rounded-2xl border transition-all ${
                  isFlagged
                    ? 'bg-[#1C1620] border-rose-500/40 shadow-lg shadow-rose-950/20'
                    : 'bg-[#141622] border-white/10'
                } space-y-4`}
              >
                {/* Header Row */}
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-[#1C1F2E] flex items-center justify-center text-slate-300 font-bold border border-white/10">
                      {rev.targetType === 'HOTEL' ? (
                        <Building2 className="w-5 h-5 text-amber-400" />
                      ) : (
                        <Plane className="w-5 h-5 text-sky-400" />
                      )}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-white">
                          {rev.targetName || rev.targetId}
                        </span>
                        <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded bg-white/5 text-slate-400 border border-white/5">
                          {rev.targetType}
                        </span>
                      </div>
                      <p className="text-xs text-slate-400">
                        Review ID: <span className="font-mono text-slate-300">{rev.id}</span> • By{' '}
                        <strong className="text-white">{rev.userFullName}</strong> ({rev.userId})
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <div className="flex items-center gap-1.5">
                      <StarRating rating={rev.rating} size="sm" />
                      <span className="text-xs font-bold text-amber-400">{rev.rating.toFixed(1)}</span>
                    </div>

                    <span
                      className={`text-[10px] font-extrabold px-2.5 py-1 rounded-full uppercase tracking-wider ${
                        rev.status === 'PUBLISHED'
                          ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                          : rev.status === 'FLAGGED'
                          ? 'bg-rose-500/20 text-rose-400 border border-rose-500/40 animate-pulse'
                          : rev.status === 'HIDDEN'
                          ? 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                          : 'bg-slate-500/15 text-slate-400 border border-slate-500/30'
                      }`}
                    >
                      {rev.status}
                    </span>
                  </div>
                </div>

                {/* Content */}
                <div className="space-y-1.5 bg-[#181A28]/60 p-4 rounded-xl border border-white/5">
                  <h4 className="text-sm font-bold text-white">{rev.title}</h4>
                  <p className="text-xs text-slate-300 leading-relaxed whitespace-pre-line">{rev.body}</p>
                </div>

                {/* Sub-Ratings & Photos */}
                <div className="flex flex-wrap items-center justify-between gap-4">
                  <div className="flex flex-wrap gap-2 text-[10px] text-slate-400">
                    {rev.cleanlinessRating && (
                      <span className="px-2 py-0.5 bg-[#1C1F2E] rounded border border-white/5">
                        Cleanliness: <strong className="text-white">{rev.cleanlinessRating}/5</strong>
                      </span>
                    )}
                    {rev.serviceRating && (
                      <span className="px-2 py-0.5 bg-[#1C1F2E] rounded border border-white/5">
                        Service: <strong className="text-white">{rev.serviceRating}/5</strong>
                      </span>
                    )}
                    {rev.valueRating && (
                      <span className="px-2 py-0.5 bg-[#1C1F2E] rounded border border-white/5">
                        Value: <strong className="text-white">{rev.valueRating}/5</strong>
                      </span>
                    )}
                    {rev.verifiedPurchase && (
                      <span className="px-2 py-0.5 bg-emerald-500/10 text-emerald-400 rounded border border-emerald-500/20 font-bold">
                        ✓ Verified Booking
                      </span>
                    )}
                  </div>

                  {rev.photos && rev.photos.length > 0 && (
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] text-slate-400 font-bold">Photos ({rev.photos.length}):</span>
                      <div className="flex gap-1.5">
                        {rev.photos.map((url, i) => (
                          <button
                            key={i}
                            type="button"
                            onClick={() => setPreviewPhoto(url)}
                            className="w-8 h-8 rounded-lg overflow-hidden border border-white/15 hover:border-amber-400 transition"
                          >
                            <img src={url} alt="Review" className="w-full h-full object-cover" />
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                {/* Moderation Notes & Flags Info */}
                {flagCount > 0 && (
                  <div className="flex items-center gap-2 text-xs text-rose-400 bg-rose-500/10 border border-rose-500/20 p-2.5 rounded-xl font-medium">
                    <AlertTriangle className="w-4 h-4 flex-shrink-0" />
                    <span>Reported by {flagCount} user{flagCount > 1 ? 's' : ''} for potential policy violation.</span>
                  </div>
                )}

                {rev.moderationNote && (
                  <p className="text-[11px] text-slate-400 italic">
                    Moderator Note: "{rev.moderationNote}" (by {rev.moderatedBy || 'Admin'})
                  </p>
                )}

                {/* Moderation Action Buttons */}
                <div className="flex flex-wrap items-center justify-end gap-2 pt-2 border-t border-white/10">
                  {rev.status !== 'PUBLISHED' && (
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedReview(rev);
                        setActionType('APPROVE');
                      }}
                      className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 transition"
                    >
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      <span>Approve / Publish</span>
                    </button>
                  )}

                  {rev.status === 'PUBLISHED' && (
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedReview(rev);
                        setActionType('HIDE');
                      }}
                      className="px-3.5 py-1.5 bg-amber-600 hover:bg-amber-500 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 transition"
                    >
                      <EyeOff className="w-3.5 h-3.5" />
                      <span>Hide Review</span>
                    </button>
                  )}

                  {rev.status !== 'REMOVED' && (
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedReview(rev);
                        setActionType('REMOVE');
                      }}
                      className="px-3.5 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 transition"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span>Remove Review</span>
                    </button>
                  )}

                  {rev.status === 'REMOVED' && (
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedReview(rev);
                        setActionType('RESTORE');
                      }}
                      className="px-3.5 py-1.5 bg-sky-600 hover:bg-sky-500 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 transition"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      <span>Restore Review</span>
                    </button>
                  )}
                </div>
              </div>
            );
          })
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 pt-4">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="px-3 py-1.5 rounded-xl bg-[#1C1F2E] hover:bg-[#25293A] disabled:opacity-40 text-xs font-bold text-white border border-white/10"
            >
              Previous
            </button>
            <span className="text-xs text-slate-400 font-medium px-2">
              Page {page + 1} of {totalPages}
            </span>
            <button
              type="button"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="px-3 py-1.5 rounded-xl bg-[#1C1F2E] hover:bg-[#25293A] disabled:opacity-40 text-xs font-bold text-white border border-white/10"
            >
              Next
            </button>
          </div>
        )}
      </div>

      {/* CONFIRMATION & REMOVAL REASON MODAL */}
      {selectedReview && actionType && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="relative w-full max-w-md bg-[#141620] border border-white/15 rounded-3xl p-6 shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">
              {actionType === 'APPROVE' && 'Approve & Publish Review'}
              {actionType === 'HIDE' && 'Hide Review from Public Listings'}
              {actionType === 'REMOVE' && 'Remove Review for Policy Violation'}
              {actionType === 'RESTORE' && 'Restore Removed Review'}
            </h3>
            <p className="text-xs text-slate-400">
              Review ID: <span className="font-mono text-white">{selectedReview.id}</span> by{' '}
              <strong className="text-white">{selectedReview.userFullName}</strong>
            </p>

            {actionType === 'REMOVE' && (
              <div className="space-y-1.5 pt-2">
                <label className="text-xs font-bold text-slate-300 block">Moderation Removal Reason:</label>
                <textarea
                  rows={3}
                  value={removalReason}
                  onChange={(e) => setRemovalReason(e.target.value)}
                  className="w-full bg-[#1C1F2E] border border-white/10 rounded-xl p-3 text-xs text-white focus:outline-none focus:border-rose-400"
                />
              </div>
            )}

            <div className="flex gap-2.5 pt-4">
              <button
                type="button"
                onClick={() => {
                  setSelectedReview(null);
                  setActionType(null);
                }}
                className="flex-1 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 font-bold text-xs"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleExecuteAction}
                className={`flex-1 py-2.5 rounded-xl text-white font-bold text-xs transition ${
                  actionType === 'REMOVE' ? 'bg-rose-600 hover:bg-rose-500' : 'bg-sky-600 hover:bg-sky-500'
                }`}
              >
                Confirm {actionType}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* PHOTO PREVIEW MODAL */}
      {previewPhoto && (
        <div
          onClick={() => setPreviewPhoto(null)}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-lg animate-fade-in cursor-zoom-out"
        >
          <div className="relative max-w-4xl max-h-[85vh] rounded-2xl overflow-hidden border border-white/20">
            <img src={previewPhoto} alt="Review attachment" className="w-full h-full object-contain" />
            <button
              onClick={() => setPreviewPhoto(null)}
              className="absolute top-4 right-4 p-2 rounded-xl bg-black/60 text-white"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
