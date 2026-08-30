import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Star,
  ThumbsUp,
  Flag,
  MessageSquarePlus,
  CheckCircle2,
  ShieldCheck,
  AlertCircle,
  Camera,
  X,
  MessageCircle,
  CornerDownRight,
  Trash2,
  Edit2,
  Send,
  Filter,
  ArrowUpDown,
  Sparkles,
  Image as ImageIcon,
  Check,
} from 'lucide-react';
import { StarRating } from './StarRating';
import { ReviewSkeleton } from './ReviewSkeleton';
import {
  Review,
  ReviewTargetType,
  ReviewReply,
  ReviewSortOption,
  ReviewStats,
} from '../types/review';
import { reviewService } from '../services/reviewService';
import { useAuth } from '../context/AuthContext';
import {
  modalBackdropVariants,
  modalDialogVariants,
  cardEntranceVariants,
  staggerContainerVariants,
} from '../lib/motion';

interface ReviewSectionProps {
  targetType: ReviewTargetType;
  targetId: string;
  targetName?: string;
  bookingId?: string;
}

export const ReviewSection: React.FC<ReviewSectionProps> = ({
  targetType,
  targetId,
  targetName,
  bookingId,
}) => {
  const { user, isAuthenticated } = useAuth();
  const [reviews, setReviews] = useState<Review[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Sorting & Filtering State
  const [sortBy, setSortBy] = useState<ReviewSortOption>('NEWEST');
  const [ratingFilter, setRatingFilter] = useState<number | undefined>(undefined);
  const [verifiedOnly, setVerifiedOnly] = useState<boolean>(false);
  const [withPhotosOnly, setWithPhotosOnly] = useState<boolean>(false);

  // Aggregate stats
  const [stats, setStats] = useState<ReviewStats | null>(null);

  // Write Review Modal
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Form fields
  const [rating, setRating] = useState(5);
  const [cleanliness, setCleanliness] = useState(5);
  const [service, setService] = useState(5);
  const [value, setValue] = useState(5);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [selectedPhotos, setSelectedPhotos] = useState<File[]>([]);
  const [photoPreviews, setPhotoPreviews] = useState<string[]>([]);

  // Reply threads state: reviewId -> list of replies
  const [repliesMap, setRepliesMap] = useState<Record<string, ReviewReply[]>>({});
  const [expandedReplies, setExpandedReplies] = useState<Record<string, boolean>>({});
  const [replyInputMap, setReplyInputMap] = useState<Record<string, string>>({});
  const [replySubmittingMap, setReplySubmittingMap] = useState<Record<string, boolean>>({});
  const [editingReplyId, setEditingReplyId] = useState<string | null>(null);
  const [editReplyText, setEditReplyText] = useState('');

  // Photo viewer modal
  const [activePhotoUrl, setActivePhotoUrl] = useState<string | null>(null);

  // Flag confirmation state
  const [flaggingReviewId, setFlaggingReviewId] = useState<string | null>(null);

  // Load reviews with current sort & filters
  const fetchReviews = useCallback(async () => {
    setLoading(true);
    try {
      const [res, statData] = await Promise.all([
        reviewService.getReviews({
          targetType,
          targetId,
          page,
          size: 6,
          sortBy,
          rating: ratingFilter,
          verifiedOnly,
          withPhotosOnly,
        }),
        reviewService.getReviewStats(targetType, targetId),
      ]);
      setReviews(res.content);
      setTotalCount(res.totalElements);
      setTotalPages(res.totalPages);
      setStats(statData);

      // Fetch replies for each review
      for (const rev of res.content) {
        loadReplies(rev.id);
      }
    } catch (err) {
      console.error('Failed to load reviews', err);
    } finally {
      setLoading(false);
    }
  }, [targetType, targetId, page, sortBy, ratingFilter, verifiedOnly, withPhotosOnly]);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  const loadReplies = async (reviewId: string) => {
    try {
      const replies = await reviewService.getReplies(reviewId);
      setRepliesMap((prev) => ({ ...prev, [reviewId]: replies }));
    } catch (err) {
      console.error('Failed to load replies for review ' + reviewId, err);
    }
  };

  const handlePhotoSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files) return;
    const files = Array.from(e.target.files);

    if (selectedPhotos.length + files.length > 5) {
      setError('You can upload a maximum of 5 photos per review.');
      return;
    }

    const validFiles: File[] = [];
    const newPreviews: string[] = [];

    for (const file of files) {
      if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
        setError('Only JPG, PNG, and WebP formats are supported.');
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        setError('Each photo must be smaller than 5MB.');
        return;
      }
      validFiles.push(file);
      newPreviews.push(URL.createObjectURL(file));
    }

    setError(null);
    setSelectedPhotos((prev) => [...prev, ...validFiles]);
    setPhotoPreviews((prev) => [...prev, ...newPreviews]);
  };

  const handleRemovePhoto = (index: number) => {
    setSelectedPhotos((prev) => prev.filter((_, i) => i !== index));
    setPhotoPreviews((prev) => {
      URL.revokeObjectURL(prev[index]);
      return prev.filter((_, i) => i !== index);
    });
  };

  const handleVoteHelpful = async (reviewId: string) => {
    if (!isAuthenticated) {
      alert('Please sign in to vote.');
      return;
    }
    try {
      const updated = await reviewService.voteHelpful(reviewId);
      setReviews((prev) =>
        prev.map((r) =>
          r.id === reviewId
            ? {
                ...r,
                helpfulVoters: updated.helpfulVoters,
                helpfulCount: updated.helpfulCount ?? updated.helpfulVoters?.length ?? 0,
              }
            : r
        )
      );
    } catch (err: any) {
      alert(err.message || 'Failed to vote');
    }
  };

  const handleConfirmFlag = async (reviewId: string) => {
    try {
      await reviewService.flagReview(reviewId);
      setSuccessMsg('Review has been reported to moderators for safety review.');
      setFlaggingReviewId(null);
      setTimeout(() => setSuccessMsg(null), 5000);
      fetchReviews();
    } catch (err: any) {
      alert(err.message || 'Failed to report review');
      setFlaggingReviewId(null);
    }
  };

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !body.trim()) {
      setError('Please provide a title and detailed review body.');
      return;
    }
    if (body.trim().length < 20) {
      setError('Review body must be at least 20 characters.');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const createdReview = await reviewService.createReview({
        targetType,
        targetId,
        targetName,
        userDisplayName: user?.fullName || 'Traveler',
        rating,
        cleanlinessRating: cleanliness,
        serviceRating: service,
        valueRating: value,
        title: title.trim(),
        body: body.trim(),
        bookingId,
      });

      // Upload attached photos
      if (selectedPhotos.length > 0) {
        for (const file of selectedPhotos) {
          try {
            await reviewService.uploadPhoto(createdReview.id, file);
          } catch (uploadErr) {
            console.error('Failed to upload photo', uploadErr);
          }
        }
      }

      setSuccessMsg('Thank you! Your verified review and photos have been published.');
      setShowModal(false);
      setTitle('');
      setBody('');
      setSelectedPhotos([]);
      setPhotoPreviews([]);
      setPage(0);
      fetchReviews();
    } catch (err: any) {
      setError(err.message || 'Failed to submit review. You may have already reviewed this.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddReply = async (reviewId: string) => {
    const text = replyInputMap[reviewId];
    if (!text || text.trim().length < 2) return;

    setReplySubmittingMap((prev) => ({ ...prev, [reviewId]: true }));
    try {
      const newReply = await reviewService.createReply(
        reviewId,
        text.trim(),
        user?.fullName || user?.email || 'Traveler'
      );
      setRepliesMap((prev) => ({
        ...prev,
        [reviewId]: [...(prev[reviewId] || []), newReply],
      }));
      setReplyInputMap((prev) => ({ ...prev, [reviewId]: '' }));
      setExpandedReplies((prev) => ({ ...prev, [reviewId]: true }));
    } catch (err: any) {
      alert(err.message || 'Failed to post reply');
    } finally {
      setReplySubmittingMap((prev) => ({ ...prev, [reviewId]: false }));
    }
  };

  const handleEditReply = async (reviewId: string, replyId: string) => {
    if (!editReplyText || editReplyText.trim().length < 2) return;
    try {
      const updated = await reviewService.updateReply(reviewId, replyId, editReplyText.trim());
      setRepliesMap((prev) => ({
        ...prev,
        [reviewId]: (prev[reviewId] || []).map((r) => (r.id === replyId ? updated : r)),
      }));
      setEditingReplyId(null);
      setEditReplyText('');
    } catch (err: any) {
      alert(err.message || 'Failed to update reply');
    }
  };

  const handleDeleteReply = async (reviewId: string, replyId: string) => {
    if (!confirm('Are you sure you want to delete this reply?')) return;
    try {
      await reviewService.deleteReply(reviewId, replyId);
      setRepliesMap((prev) => ({
        ...prev,
        [reviewId]: (prev[reviewId] || []).filter((r) => r.id !== replyId),
      }));
    } catch (err: any) {
      alert(err.message || 'Failed to delete reply');
    }
  };

  // Safe star percentages for distribution breakdown
  const starPercentages = useMemo(() => {
    if (!stats || stats.totalReviews === 0) return { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
    const total = stats.totalReviews;
    return {
      5: Math.round((stats.count5Stars / total) * 100),
      4: Math.round((stats.count4Stars / total) * 100),
      3: Math.round((stats.count3Stars / total) * 100),
      2: Math.round((stats.count2Stars / total) * 100),
      1: Math.round((stats.count1Star / total) * 100),
    };
  }, [stats]);

  return (
    <div className="bg-[#12141C] border border-white/10 rounded-3xl p-6 sm:p-8 backdrop-blur-xl shadow-2xl space-y-8">
      {/* Header & Main Rating Snapshot */}
      <div className="flex flex-wrap items-center justify-between gap-6 pb-6 border-b border-white/10">
        <div>
          <h3 className="text-2xl font-black text-white flex items-center gap-2.5">
            <Star className="w-6 h-6 text-amber-400 fill-amber-400" />
            <span>Verified Guest Reviews & Ratings</span>
          </h3>
          <p className="text-xs text-slate-400 mt-1">
            Real feedback and verified traveler photos for {targetName || (targetType === 'HOTEL' ? 'this property' : 'this flight')}
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            if (!isAuthenticated) {
              alert('Please sign in to write a review');
              return;
            }
            setShowModal(true);
          }}
          className="flex items-center gap-2 px-5 py-3 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold text-xs rounded-xl shadow-glow-gold transition-all hover:scale-105 cursor-pointer"
        >
          <MessageSquarePlus className="w-4 h-4 text-black" />
          <span>Write a Review</span>
        </button>
      </div>

      {successMsg && (
        <div className="p-3.5 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-2xl text-xs font-semibold flex items-center gap-2.5 animate-fade-in shadow-glow-emerald">
          <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Aggregate Rating Scorecard & Star Breakdown */}
      {stats && stats.totalReviews > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 p-6 rounded-2xl bg-[#181A24] border border-white/5">
          {/* Overall Score */}
          <div className="flex flex-col justify-center items-center text-center p-4 border-b md:border-b-0 md:border-r border-white/10 space-y-2">
            <span className="text-5xl font-black text-white tracking-tight">
              {stats.averageRating.toFixed(1)}
            </span>
            <StarRating rating={stats.averageRating} size="md" />
            <span className="text-xs text-slate-400 font-medium">
              Based on {stats.totalReviews.toLocaleString()} verified {stats.totalReviews === 1 ? 'review' : 'reviews'}
            </span>
          </div>

          {/* Star Distribution Breakdown */}
          <div className="space-y-2 py-1">
            {[5, 4, 3, 2, 1].map((stars) => {
              const pct = starPercentages[stars as 5 | 4 | 3 | 2 | 1];
              const isSelected = ratingFilter === stars;
              return (
                <button
                  key={stars}
                  type="button"
                  onClick={() => {
                    setRatingFilter(isSelected ? undefined : stars);
                    setPage(0);
                  }}
                  className={`w-full flex items-center gap-3 text-xs p-1 rounded-lg transition-colors cursor-pointer ${
                    isSelected ? 'bg-amber-400/15 text-amber-400 font-bold' : 'text-slate-300 hover:bg-white/5'
                  }`}
                >
                  <span className="w-10 text-left font-medium">{stars} ★</span>
                  <div className="flex-1 h-2 rounded-full bg-white/10 overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-amber-400 to-amber-500 rounded-full transition-all duration-500"
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                  <span className="w-10 text-right text-slate-400 font-mono text-[11px]">{pct}%</span>
                </button>
              );
            })}
          </div>

          {/* Sub-Category Averages */}
          <div className="flex flex-col justify-center space-y-3.5 p-2">
            <div>
              <div className="flex justify-between text-xs text-slate-300 mb-1">
                <span>Cleanliness & Hygiene</span>
                <strong className="text-amber-400">{stats.averageCleanliness.toFixed(1)} / 5</strong>
              </div>
              <div className="h-1.5 rounded-full bg-white/10 overflow-hidden">
                <div
                  className="h-full bg-emerald-400 rounded-full"
                  style={{ width: `${(stats.averageCleanliness / 5) * 100}%` }}
                />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs text-slate-300 mb-1">
                <span>Staff & Service</span>
                <strong className="text-amber-400">{stats.averageService.toFixed(1)} / 5</strong>
              </div>
              <div className="h-1.5 rounded-full bg-white/10 overflow-hidden">
                <div
                  className="h-full bg-sky-400 rounded-full"
                  style={{ width: `${(stats.averageService / 5) * 100}%` }}
                />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs text-slate-300 mb-1">
                <span>Value for Money</span>
                <strong className="text-amber-400">{stats.averageValue.toFixed(1)} / 5</strong>
              </div>
              <div className="h-1.5 rounded-full bg-white/10 overflow-hidden">
                <div
                  className="h-full bg-amber-400 rounded-full"
                  style={{ width: `${(stats.averageValue / 5) * 100}%` }}
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Sorting & Filter Controls Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-2xl bg-[#151722] border border-white/10">
        {/* Star Rating & Attribute Filter Chips */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => {
              setRatingFilter(undefined);
              setVerifiedOnly(false);
              setWithPhotosOnly(false);
              setPage(0);
            }}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
              ratingFilter === undefined && !verifiedOnly && !withPhotosOnly
                ? 'bg-amber-400 text-black shadow-glow-gold'
                : 'bg-[#1C1F2C] text-slate-300 hover:bg-[#25293A] border border-white/5'
            }`}
          >
            All Reviews ({stats?.totalReviews || totalCount})
          </button>

          {[5, 4, 3, 2, 1].map((s) => (
            <button
              key={s}
              type="button"
              onClick={() => {
                setRatingFilter(ratingFilter === s ? undefined : s);
                setPage(0);
              }}
              className={`px-2.5 py-1.5 rounded-xl text-xs font-bold transition flex items-center gap-1 ${
                ratingFilter === s
                  ? 'bg-amber-400 text-black shadow-glow-gold'
                  : 'bg-[#1C1F2C] text-slate-300 hover:bg-[#25293A] border border-white/5'
              }`}
            >
              <span>{s}</span>
              <Star className={`w-3 h-3 ${ratingFilter === s ? 'fill-black text-black' : 'fill-amber-400 text-amber-400'}`} />
            </button>
          ))}

          <button
            type="button"
            onClick={() => {
              setVerifiedOnly(!verifiedOnly);
              setPage(0);
            }}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
              verifiedOnly
                ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 shadow-glow-emerald'
                : 'bg-[#1C1F2C] text-slate-300 hover:bg-[#25293A] border border-white/5'
            }`}
          >
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Verified Only</span>
          </button>

          <button
            type="button"
            onClick={() => {
              setWithPhotosOnly(!withPhotosOnly);
              setPage(0);
            }}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
              withPhotosOnly
                ? 'bg-sky-500/20 text-sky-400 border border-sky-500/40'
                : 'bg-[#1C1F2C] text-slate-300 hover:bg-[#25293A] border border-white/5'
            }`}
          >
            <Camera className="w-3.5 h-3.5" />
            <span>With Photos</span>
          </button>
        </div>

        {/* Sort Select Dropdown */}
        <div className="flex items-center gap-2 text-xs">
          <ArrowUpDown className="w-3.5 h-3.5 text-slate-400" />
          <span className="text-slate-400 font-medium">Sort By:</span>
          <select
            value={sortBy}
            onChange={(e) => {
              setSortBy(e.target.value as ReviewSortOption);
              setPage(0);
            }}
            className="bg-[#1C1F2C] border border-white/10 rounded-xl px-3 py-1.5 text-xs text-white font-bold focus:outline-none focus:border-amber-400 cursor-pointer"
          >
            <option value="NEWEST">Newest First</option>
            <option value="MOST_HELPFUL">Most Helpful</option>
            <option value="HIGHEST_RATED">Highest Rating</option>
            <option value="LOWEST_RATED">Lowest Rating</option>
            <option value="OLDEST">Oldest First</option>
          </select>
        </div>
      </div>

      {/* Review Cards List */}
      <div className="space-y-4">
        {loading ? (
          <div className="space-y-4 py-2">
            {[1, 2, 3].map((i) => (
              <ReviewSkeleton key={i} />
            ))}
          </div>
        ) : reviews.length === 0 ? (
          <div className="py-14 text-center text-slate-400 bg-[#181A24] border border-white/5 rounded-2xl space-y-3">
            <Star className="w-10 h-10 text-slate-700 mx-auto" />
            <p className="font-bold text-white text-base">No matching reviews found</p>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              {ratingFilter || verifiedOnly || withPhotosOnly
                ? 'Try resetting the filters to view all reviews.'
                : 'Be the first traveler to share your verified experience!'}
            </p>
          </div>
        ) : (
          reviews.map((rev) => {
            const replies = repliesMap[rev.id] || [];
            const isRepliesExpanded = expandedReplies[rev.id] || false;
            const userHasVoted = Boolean(user && rev.helpfulVoters?.includes(user.id || user.email));
            const helpfulCount = rev.helpfulCount ?? rev.helpfulVoters?.length ?? 0;

            return (
              <div
                key={rev.id}
                className="bg-[#151722] border border-white/10 hover:border-white/20 rounded-2xl p-6 transition-all duration-200 space-y-4"
              >
                {/* Review Header */}
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-amber-400 to-amber-500 text-black font-black flex items-center justify-center text-sm shadow-glow-gold">
                      {rev.userFullName ? rev.userFullName.charAt(0).toUpperCase() : 'T'}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-white">{rev.userFullName || 'Traveler'}</span>
                        {rev.verifiedPurchase && (
                          <span className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded-full">
                            <ShieldCheck className="w-3 h-3" />
                            <span>{targetType === 'HOTEL' ? 'Verified Stay' : 'Verified Flight'}</span>
                          </span>
                        )}
                      </div>
                      <span className="text-[10px] text-slate-400">
                        {new Date(rev.createdAt).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'short',
                          day: 'numeric',
                        })}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <StarRating rating={rev.rating} size="sm" />
                    <span className="text-xs font-bold text-amber-400">{rev.rating.toFixed(1)}</span>
                  </div>
                </div>

                {/* Title & Body */}
                <div className="space-y-1.5">
                  <h4 className="text-base font-bold text-white">{rev.title}</h4>
                  <p className="text-xs text-slate-300 leading-relaxed whitespace-pre-line">{rev.body}</p>
                </div>

                {/* Sub-ratings Tags */}
                {(rev.cleanlinessRating || rev.serviceRating || rev.valueRating) && (
                  <div className="flex flex-wrap gap-2 text-[10px] text-slate-400 pt-1">
                    {rev.cleanlinessRating && (
                      <span className="px-2 py-1 rounded bg-[#1C1F2C] border border-white/5">
                        Cleanliness: <strong className="text-white">{rev.cleanlinessRating}/5</strong>
                      </span>
                    )}
                    {rev.serviceRating && (
                      <span className="px-2 py-1 rounded bg-[#1C1F2C] border border-white/5">
                        Service: <strong className="text-white">{rev.serviceRating}/5</strong>
                      </span>
                    )}
                    {rev.valueRating && (
                      <span className="px-2 py-1 rounded bg-[#1C1F2C] border border-white/5">
                        Value: <strong className="text-white">{rev.valueRating}/5</strong>
                      </span>
                    )}
                  </div>
                )}

                {/* Photos Gallery */}
                {rev.photos && rev.photos.length > 0 && (
                  <div className="pt-2">
                    <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider block mb-2">
                      Traveler Photos ({rev.photos.length})
                    </span>
                    <div className="flex flex-wrap gap-2.5">
                      {rev.photos.map((pUrl, idx) => (
                        <button
                          key={idx}
                          type="button"
                          onClick={() => setActivePhotoUrl(pUrl)}
                          className="relative w-16 h-16 sm:w-20 sm:h-20 rounded-xl overflow-hidden border border-white/10 hover:border-amber-400 transition-all hover:scale-105 group cursor-pointer"
                        >
                          <img
                            src={pUrl}
                            alt={`Review photo ${idx + 1}`}
                            className="w-full h-full object-cover"
                            loading="lazy"
                          />
                          <div className="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                            <Camera className="w-4 h-4 text-white" />
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {/* Review Action Toolbar */}
                <div className="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-white/10 text-xs">
                  <div className="flex items-center gap-3">
                    <button
                      type="button"
                      onClick={() => handleVoteHelpful(rev.id)}
                      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-lg transition font-medium cursor-pointer ${
                        userHasVoted
                          ? 'bg-amber-400/20 text-amber-400 border border-amber-400/40'
                          : 'bg-[#1C1F2C] text-slate-400 hover:text-white border border-white/5'
                      }`}
                    >
                      <ThumbsUp className="w-3.5 h-3.5" />
                      <span>Helpful ({helpfulCount})</span>
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        setExpandedReplies((prev) => ({ ...prev, [rev.id]: !prev[rev.id] }))
                      }
                      className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-[#1C1F2C] hover:bg-[#25293A] text-slate-400 hover:text-white border border-white/5 transition font-medium cursor-pointer"
                    >
                      <MessageCircle className="w-3.5 h-3.5" />
                      <span>Replies ({replies.length})</span>
                    </button>
                  </div>

                  <button
                    type="button"
                    onClick={() => setFlaggingReviewId(rev.id)}
                    className="inline-flex items-center gap-1 text-[11px] text-slate-500 hover:text-rose-400 transition cursor-pointer"
                  >
                    <Flag className="w-3 h-3" />
                    <span>Report</span>
                  </button>
                </div>

                {/* Threaded Replies Section */}
                {isRepliesExpanded && (
                  <div className="mt-4 pt-4 border-t border-white/5 space-y-3 pl-4 sm:pl-6 border-l-2 border-amber-400/30 bg-[#12141C]/50 p-4 rounded-xl">
                    <span className="text-[11px] font-bold text-slate-300 block">Conversation</span>

                    {replies.length === 0 ? (
                      <p className="text-xs text-slate-500 italic">No replies yet. Join the conversation!</p>
                    ) : (
                      replies.map((reply) => (
                        <div key={reply.id} className="bg-[#1A1D2A] p-3 rounded-xl border border-white/5 space-y-1">
                          <div className="flex items-center justify-between text-[11px]">
                            <span className="font-bold text-amber-400">{reply.userName}</span>
                            <span className="text-slate-500">
                              {new Date(reply.createdAt).toLocaleDateString()}
                            </span>
                          </div>
                          {editingReplyId === reply.id ? (
                            <div className="space-y-2 pt-1">
                              <input
                                type="text"
                                value={editReplyText}
                                onChange={(e) => setEditReplyText(e.target.value)}
                                className="w-full bg-[#12141C] border border-white/15 rounded-lg px-3 py-1.5 text-xs text-white"
                              />
                              <div className="flex gap-2">
                                <button
                                  type="button"
                                  onClick={() => handleEditReply(rev.id, reply.id)}
                                  className="px-2.5 py-1 bg-amber-400 text-black text-[10px] font-bold rounded-md"
                                >
                                  Save
                                </button>
                                <button
                                  type="button"
                                  onClick={() => setEditingReplyId(null)}
                                  className="px-2.5 py-1 bg-white/10 text-slate-300 text-[10px] rounded-md"
                                >
                                  Cancel
                                </button>
                              </div>
                            </div>
                          ) : (
                            <p className="text-xs text-slate-200">{reply.content}</p>
                          )}
                          {user && (user.id === reply.userId || user.email === reply.userId) && (
                            <div className="flex items-center gap-2 pt-1">
                              <button
                                type="button"
                                onClick={() => {
                                  setEditingReplyId(reply.id);
                                  setEditReplyText(reply.content);
                                }}
                                className="text-[10px] text-slate-400 hover:text-white inline-flex items-center gap-1"
                              >
                                <Edit2 className="w-2.5 h-2.5" /> Edit
                              </button>
                              <button
                                type="button"
                                onClick={() => handleDeleteReply(rev.id, reply.id)}
                                className="text-[10px] text-rose-400 hover:text-rose-300 inline-flex items-center gap-1"
                              >
                                <Trash2 className="w-2.5 h-2.5" /> Delete
                              </button>
                            </div>
                          )}
                        </div>
                      ))
                    )}

                    {/* Inline Reply Input */}
                    {isAuthenticated ? (
                      <div className="flex gap-2 pt-2">
                        <input
                          type="text"
                          placeholder="Write a helpful reply..."
                          value={replyInputMap[rev.id] || ''}
                          onChange={(e) =>
                            setReplyInputMap((prev) => ({ ...prev, [rev.id]: e.target.value }))
                          }
                          onKeyDown={(e) => {
                            if (e.key === 'Enter') handleAddReply(rev.id);
                          }}
                          className="flex-1 bg-[#1A1D2A] border border-white/10 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-400"
                        />
                        <button
                          type="button"
                          disabled={replySubmittingMap[rev.id]}
                          onClick={() => handleAddReply(rev.id)}
                          className="px-3 py-2 bg-gradient-to-r from-amber-400 to-amber-500 text-black rounded-xl font-bold text-xs flex items-center gap-1 shadow-glow-gold hover:scale-105 transition"
                        >
                          <Send className="w-3 h-3" />
                          <span>Reply</span>
                        </button>
                      </div>
                    ) : (
                      <p className="text-[11px] text-slate-400 pt-1">
                        Sign in to reply to this review.
                      </p>
                    )}
                  </div>
                )}
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
              className="px-3 py-1.5 rounded-xl bg-[#1C1F2C] hover:bg-[#25293A] disabled:opacity-40 text-xs font-bold text-white border border-white/10"
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
              className="px-3 py-1.5 rounded-xl bg-[#1C1F2C] hover:bg-[#25293A] disabled:opacity-40 text-xs font-bold text-white border border-white/10"
            >
              Next
            </button>
          </div>
        )}
      </div>

      {/* MODAL: WRITE A REVIEW */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in overflow-y-auto">
          <div className="relative w-full max-w-xl bg-[#141620] border border-white/15 rounded-3xl p-6 sm:p-8 shadow-2xl space-y-6 my-8">
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-5 right-5 p-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition"
            >
              <X className="w-5 h-5" />
            </button>

            <div>
              <h3 className="text-xl font-bold text-white flex items-center gap-2">
                <Star className="w-5 h-5 text-amber-400 fill-amber-400" />
                Write Your Review
              </h3>
              <p className="text-xs text-slate-400 mt-1">
                Share your authentic experience for <strong>{targetName || 'this travel booking'}</strong>
              </p>
            </div>

            {error && (
              <div className="p-3 bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs rounded-xl flex items-center gap-2">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmitReview} className="space-y-4 text-xs">
              {/* Overall Rating Picker */}
              <div className="space-y-1.5">
                <label className="text-slate-300 font-bold block">Overall Rating (1–5 Stars)</label>
                <div className="flex items-center gap-2">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <button
                      key={s}
                      type="button"
                      onClick={() => setRating(s)}
                      className="p-1 cursor-pointer transition hover:scale-110"
                    >
                      <Star
                        className={`w-7 h-7 ${
                          s <= rating ? 'text-amber-400 fill-amber-400 shadow-glow-gold' : 'text-slate-700'
                        }`}
                      />
                    </button>
                  ))}
                  <span className="ml-2 font-black text-amber-400 text-sm">{rating}.0 / 5.0</span>
                </div>
              </div>

              {/* Sub-Ratings Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-1">
                <div>
                  <label className="text-slate-400 text-[11px] block mb-1">Cleanliness</label>
                  <select
                    value={cleanliness}
                    onChange={(e) => setCleanliness(Number(e.target.value))}
                    className="w-full bg-[#1C1F2C] border border-white/10 rounded-xl px-2.5 py-1.5 text-white font-bold"
                  >
                    {[5, 4, 3, 2, 1].map((s) => (
                      <option key={s} value={s}>{s} Stars</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="text-slate-400 text-[11px] block mb-1">Service</label>
                  <select
                    value={service}
                    onChange={(e) => setService(Number(e.target.value))}
                    className="w-full bg-[#1C1F2C] border border-white/10 rounded-xl px-2.5 py-1.5 text-white font-bold"
                  >
                    {[5, 4, 3, 2, 1].map((s) => (
                      <option key={s} value={s}>{s} Stars</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="text-slate-400 text-[11px] block mb-1">Value</label>
                  <select
                    value={value}
                    onChange={(e) => setValue(Number(e.target.value))}
                    className="w-full bg-[#1C1F2C] border border-white/10 rounded-xl px-2.5 py-1.5 text-white font-bold"
                  >
                    {[5, 4, 3, 2, 1].map((s) => (
                      <option key={s} value={s}>{s} Stars</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Title Input */}
              <div className="space-y-1">
                <label className="text-slate-300 font-bold block">Review Headline</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Exceptional service and stunning room views!"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full bg-[#1C1F2C] border border-white/10 rounded-xl px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-amber-400"
                />
              </div>

              {/* Body Textarea */}
              <div className="space-y-1">
                <label className="text-slate-300 font-bold block">Detailed Feedback</label>
                <textarea
                  required
                  rows={4}
                  placeholder="Tell future travelers about check-in, amenities, dining, location, and tips..."
                  value={body}
                  onChange={(e) => setBody(e.target.value)}
                  className="w-full bg-[#1C1F2C] border border-white/10 rounded-xl px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 leading-relaxed"
                />
              </div>

              {/* Photo Uploads Gallery */}
              <div className="space-y-2 pt-1">
                <label className="text-slate-300 font-bold flex items-center justify-between">
                  <span>Attach Photos (Optional, max 5)</span>
                  <span className="text-[10px] text-slate-400">{selectedPhotos.length} / 5</span>
                </label>
                <div className="flex flex-wrap gap-2.5 items-center">
                  {photoPreviews.map((src, idx) => (
                    <div key={idx} className="relative w-16 h-16 rounded-xl overflow-hidden border border-white/15">
                      <img src={src} alt="Preview" className="w-full h-full object-cover" />
                      <button
                        type="button"
                        onClick={() => handleRemovePhoto(idx)}
                        className="absolute top-1 right-1 p-0.5 rounded-full bg-black/70 text-rose-400 hover:text-white"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  ))}

                  {selectedPhotos.length < 5 && (
                    <label className="w-16 h-16 rounded-xl border-2 border-dashed border-white/20 hover:border-amber-400 flex flex-col items-center justify-center text-slate-400 hover:text-amber-400 transition cursor-pointer">
                      <Camera className="w-5 h-5" />
                      <span className="text-[9px] font-bold mt-0.5">Add</span>
                      <input
                        type="file"
                        multiple
                        accept="image/jpeg,image/png,image/webp"
                        onChange={handlePhotoSelect}
                        className="hidden"
                      />
                    </label>
                  )}
                </div>
              </div>

              {/* Submit Buttons */}
              <div className="flex items-center justify-end gap-3 pt-4 border-t border-white/10">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 font-bold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-6 py-2.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-extrabold rounded-xl shadow-glow-gold transition-all hover:scale-105 flex items-center gap-2 cursor-pointer"
                >
                  {submitting ? 'Publishing...' : 'Publish Review'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: PHOTO FULLSCREEN VIEWER */}
      {activePhotoUrl && (
        <div
          onClick={() => setActivePhotoUrl(null)}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-lg animate-fade-in cursor-zoom-out"
        >
          <div className="relative max-w-4xl max-h-[85vh] rounded-2xl overflow-hidden border border-white/20 shadow-2xl">
            <img src={activePhotoUrl} alt="Enlarged review photo" className="w-full h-full object-contain" />
            <button
              onClick={() => setActivePhotoUrl(null)}
              className="absolute top-4 right-4 p-2 rounded-xl bg-black/60 hover:bg-black text-white transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>
      )}

      {/* MODAL: REPORT REVIEW CONFIRMATION */}
      {flaggingReviewId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="relative w-full max-w-md bg-[#141620] border border-white/15 rounded-3xl p-6 shadow-2xl space-y-4 text-center">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto">
              <Flag className="w-6 h-6" />
            </div>
            <h4 className="text-lg font-bold text-white">Report Inappropriate Content</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Are you sure you want to flag this review? Flagged content will be sent to the moderation team for review and potential removal.
            </p>
            <div className="flex gap-2.5 pt-2">
              <button
                type="button"
                onClick={() => setFlaggingReviewId(null)}
                className="flex-1 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 font-bold text-xs"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => handleConfirmFlag(flaggingReviewId)}
                className="flex-1 py-2.5 rounded-xl bg-rose-500 hover:bg-rose-600 text-white font-bold text-xs transition"
              >
                Confirm Report
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
