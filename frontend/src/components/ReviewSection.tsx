import React, { useState, useEffect, useCallback } from 'react';
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
} from 'lucide-react';
import { StarRating } from './StarRating';
import { ReviewSkeleton } from './ReviewSkeleton';
import { Review, ReviewTargetType, ReviewReply } from '../types/api';
import { reviewService } from '../services/reviewService';
import { useAuth } from '../context/AuthContext';
import { modalBackdropVariants, modalDialogVariants, cardEntranceVariants, staggerContainerVariants } from '../lib/motion';

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
  const [averageRating, setAverageRating] = useState(0);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // New review form modal
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

  // Selected image preview modal for viewing enlarged photos
  const [activePhotoUrl, setActivePhotoUrl] = useState<string | null>(null);

  const fetchReviews = useCallback(async () => {
    setLoading(true);
    try {
      const [res, avg] = await Promise.all([
        reviewService.getReviews(targetType, targetId, page, 5),
        reviewService.getAverageRating(targetType, targetId),
      ]);
      setReviews(res.content);
      setTotalCount(res.totalElements);
      setTotalPages(res.totalPages);
      setAverageRating(avg);

      // Fetch replies for each review
      for (const rev of res.content) {
        loadReplies(rev.id);
      }
    } catch (err) {
      console.error('Failed to load reviews', err);
    } finally {
      setLoading(false);
    }
  }, [targetType, targetId, page]);

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

    for (const f of files) {
      if (f.size > 5 * 1024 * 1024) {
        setError(`File ${f.name} exceeds the 5MB size limit.`);
        return;
      }
      if (!['image/jpeg', 'image/png', 'image/webp'].includes(f.type)) {
        setError(`File ${f.name} is not a valid format (JPEG, PNG, WebP only).`);
        return;
      }
      validFiles.push(f);
      newPreviews.push(URL.createObjectURL(f));
    }

    setSelectedPhotos((prev) => [...prev, ...validFiles]);
    setPhotoPreviews((prev) => [...prev, ...newPreviews]);
    setError(null);
  };

  const handleRemovePhoto = (index: number) => {
    setSelectedPhotos((prev) => prev.filter((_, i) => i !== index));
    setPhotoPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleHelpful = async (reviewId: string) => {
    if (!isAuthenticated) {
      alert('Please log in to vote on reviews.');
      return;
    }
    try {
      const updated = await reviewService.voteHelpful(reviewId);
      setReviews((prev) => prev.map((r) => (r.id === reviewId ? updated : r)));
    } catch (err: any) {
      alert(err.message || 'Failed to record vote');
    }
  };

  const handleFlag = async (reviewId: string) => {
    if (!isAuthenticated) {
      alert('Please log in to flag reviews.');
      return;
    }
    if (!confirm('Are you sure you want to flag this review for moderation?')) return;
    try {
      const updated = await reviewService.flagReview(reviewId);
      setReviews((prev) => prev.map((r) => (r.id === reviewId ? updated : r)));
      alert('Review flagged for administrative review.');
    } catch (err: any) {
      alert(err.message || 'Failed to flag review');
    }
  };

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated) return;
    if (title.trim().length < 5) {
      setError('Title must be at least 5 characters');
      return;
    }
    if (body.trim().length < 20) {
      setError('Review body must be at least 20 characters');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const createdReview = await reviewService.createReview({
        targetType,
        targetId,
        targetName,
        userDisplayName: user?.fullName || user?.email || 'Traveler',
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

      setSuccessMsg('Thank you! Your review with photos has been published.');
      setShowModal(false);
      setTitle('');
      setBody('');
      setSelectedPhotos([]);
      setPhotoPreviews([]);
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

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 backdrop-blur-md">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <h3 className="text-xl font-bold text-white flex items-center gap-2">
            <Star className="w-5 h-5 text-amber-400 fill-amber-400" />
            Guest Reviews & Ratings
          </h3>
          <p className="text-sm text-slate-400 mt-1">
            Real feedback and photos from verified travelers
          </p>
        </div>

        <div className="flex items-center gap-6">
          <div className="text-right">
            <div className="flex items-center gap-2">
              <span className="text-3xl font-extrabold text-white">
                {averageRating > 0 ? averageRating.toFixed(1) : '—'}
              </span>
              <div>
                <StarRating rating={averageRating} size="sm" />
                <span className="text-xs text-slate-400">
                  {totalCount} {totalCount === 1 ? 'review' : 'reviews'}
                </span>
              </div>
            </div>
          </div>

          <button
            onClick={() => {
              if (!isAuthenticated) {
                alert('Please sign in to write a review');
                return;
              }
              setShowModal(true);
            }}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition"
          >
            <MessageSquarePlus className="w-4 h-4" />
            Write a Review
          </button>
        </div>
      </div>

      {successMsg && (
        <div className="mt-4 p-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-xl text-sm flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
          {successMsg}
        </div>
      )}

      {/* Reviews List */}
      <div className="mt-6 space-y-4">
        {loading ? (
          <div className="space-y-4 py-2">
            {[1, 2, 3].map((i) => (
              <ReviewSkeleton key={i} />
            ))}
          </div>
        ) : reviews.length === 0 ? (
          <div className="py-12 text-center text-slate-400">
            <Star className="w-10 h-10 text-slate-700 mx-auto mb-2" />
            <p className="font-medium text-slate-300">No reviews yet</p>
            <p className="text-xs text-slate-500 mt-1">Be the first to share your experience!</p>
          </div>
        ) : (
          reviews.map((rev) => {
            const replies = repliesMap[rev.id] || [];
            const isRepliesExpanded = expandedReplies[rev.id] || false;

            return (
              <div
                key={rev.id}
                className="p-5 bg-slate-800/40 border border-slate-800/80 rounded-xl hover:border-slate-700 transition-colors"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-semibold text-white text-sm">
                        {rev.userFullName || 'Anonymous Traveler'}
                      </span>
                      {rev.verifiedPurchase && (
                        <span className="inline-flex items-center gap-1 text-[11px] font-medium text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
                          <ShieldCheck className="w-3 h-3" />
                          Verified Traveler
                        </span>
                      )}
                      <span className="text-xs text-slate-500">
                        {new Date(rev.createdAt).toLocaleDateString(undefined, {
                          year: 'numeric',
                          month: 'short',
                          day: 'numeric',
                        })}
                      </span>
                    </div>

                    <div className="mt-2 flex items-center gap-2">
                      <StarRating rating={rev.rating} size="sm" />
                      <h4 className="text-sm font-semibold text-slate-200">{rev.title}</h4>
                    </div>
                  </div>
                </div>

                <p className="mt-3 text-sm text-slate-300 leading-relaxed">{rev.body}</p>

                {/* Uploaded Photos Gallery */}
                {rev.photos && rev.photos.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-2.5">
                    {rev.photos.map((photoUrl, idx) => (
                      <div
                        key={idx}
                        onClick={() => setActivePhotoUrl(photoUrl)}
                        className="relative w-20 h-20 rounded-lg overflow-hidden border border-slate-700 cursor-pointer hover:border-slate-500 transition-all hover:scale-105 group"
                      >
                        <img
                          src={photoUrl}
                          alt={`Review photo ${idx + 1}`}
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            (e.currentTarget as HTMLImageElement).src =
                              'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=400';
                          }}
                        />
                        <div className="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                          <Camera className="w-4 h-4 text-white" />
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Sub-ratings */}
                {(rev.cleanlinessRating || rev.serviceRating || rev.valueRating) && (
                  <div className="mt-3 pt-3 border-t border-slate-800/60 flex flex-wrap gap-4 text-xs text-slate-400">
                    {rev.cleanlinessRating ? (
                      <div>
                        <span className="text-slate-500 mr-1.5">Cleanliness:</span>
                        <span className="text-amber-400 font-medium">
                          {rev.cleanlinessRating}/5
                        </span>
                      </div>
                    ) : null}
                    {rev.serviceRating ? (
                      <div>
                        <span className="text-slate-500 mr-1.5">Service:</span>
                        <span className="text-amber-400 font-medium">
                          {rev.serviceRating}/5
                        </span>
                      </div>
                    ) : null}
                    {rev.valueRating ? (
                      <div>
                        <span className="text-slate-500 mr-1.5">Value:</span>
                        <span className="text-amber-400 font-medium">
                          {rev.valueRating}/5
                        </span>
                      </div>
                    ) : null}
                  </div>
                )}

                {/* Action buttons */}
                <div className="mt-4 flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-800/40">
                  <div className="flex items-center gap-4">
                    <button
                      onClick={() => handleHelpful(rev.id)}
                      className={`flex items-center gap-1.5 px-2.5 py-1 rounded-lg transition-colors ${
                        rev.helpfulVoters?.includes(user?.id || '')
                          ? 'bg-blue-500/10 text-blue-400 font-medium'
                          : 'hover:bg-slate-800 text-slate-400 hover:text-slate-200'
                      }`}
                    >
                      <ThumbsUp className="w-3.5 h-3.5" />
                      Helpful ({rev.helpfulVoters?.length || 0})
                    </button>

                    <button
                      onClick={() =>
                        setExpandedReplies((prev) => ({
                          ...prev,
                          [rev.id]: !prev[rev.id],
                        }))
                      }
                      className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-slate-200 transition-colors"
                    >
                      <MessageCircle className="w-3.5 h-3.5" />
                      Replies ({replies.length})
                    </button>
                  </div>

                  <button
                    onClick={() => handleFlag(rev.id)}
                    className="flex items-center gap-1 text-slate-500 hover:text-rose-400 transition-colors"
                    title="Flag for moderation"
                  >
                    <Flag className="w-3 h-3" />
                    Flag
                  </button>
                </div>

                {/* Threaded Replies Section */}
                {isRepliesExpanded && (
                  <div className="mt-4 pt-3 border-t border-slate-800 pl-4 border-l-2 border-blue-500/30 space-y-3">
                    {replies.length > 0 ? (
                      replies.map((reply) => {
                        const isOwnReply = user?.id && reply.userId === user.id;
                        const isEditing = editingReplyId === reply.id;

                        return (
                          <div
                            key={reply.id}
                            className="bg-slate-900/60 rounded-xl p-3 border border-slate-800 text-xs"
                          >
                            <div className="flex items-center justify-between gap-2">
                              <div className="flex items-center gap-2">
                                <CornerDownRight className="w-3 h-3 text-blue-400" />
                                <span className="font-semibold text-slate-200">
                                  {reply.userName || 'Traveler'}
                                </span>
                                <span className="text-[10px] text-slate-500">
                                  {new Date(reply.createdAt).toLocaleDateString()}
                                </span>
                              </div>

                              {isOwnReply && !isEditing && (
                                <div className="flex items-center gap-2">
                                  <button
                                    onClick={() => {
                                      setEditingReplyId(reply.id);
                                      setEditReplyText(reply.content);
                                    }}
                                    className="text-slate-500 hover:text-blue-400 transition-colors"
                                    title="Edit reply"
                                  >
                                    <Edit2 className="w-3 h-3" />
                                  </button>
                                  <button
                                    onClick={() => handleDeleteReply(rev.id, reply.id)}
                                    className="text-slate-500 hover:text-rose-400 transition-colors"
                                    title="Delete reply"
                                  >
                                    <Trash2 className="w-3 h-3" />
                                  </button>
                                </div>
                              )}
                            </div>

                            {isEditing ? (
                              <div className="mt-2 space-y-2">
                                <input
                                  type="text"
                                  value={editReplyText}
                                  onChange={(e) => setEditReplyText(e.target.value)}
                                  className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:border-blue-500"
                                />
                                <div className="flex justify-end gap-2">
                                  <button
                                    onClick={() => setEditingReplyId(null)}
                                    className="px-2 py-1 text-slate-400 hover:text-white"
                                  >
                                    Cancel
                                  </button>
                                  <button
                                    onClick={() => handleEditReply(rev.id, reply.id)}
                                    className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium"
                                  >
                                    Save
                                  </button>
                                </div>
                              </div>
                            ) : (
                              <p className="mt-1.5 text-slate-300 pl-5">{reply.content}</p>
                            )}
                          </div>
                        );
                      })
                    ) : (
                      <p className="text-xs text-slate-500 italic">
                        No replies yet. Start the conversation below.
                      </p>
                    )}

                    {/* Add Reply Input */}
                    {isAuthenticated ? (
                      <div className="flex items-center gap-2 pt-2">
                        <input
                          type="text"
                          placeholder="Write a reply..."
                          value={replyInputMap[rev.id] || ''}
                          onChange={(e) =>
                            setReplyInputMap((prev) => ({
                              ...prev,
                              [rev.id]: e.target.value,
                            }))
                          }
                          onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                              e.preventDefault();
                              handleAddReply(rev.id);
                            }
                          }}
                          className="flex-1 px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-xs text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
                        />
                        <button
                          onClick={() => handleAddReply(rev.id)}
                          disabled={
                            replySubmittingMap[rev.id] ||
                            !replyInputMap[rev.id] ||
                            replyInputMap[rev.id].trim().length < 2
                          }
                          className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-40 text-white text-xs font-semibold rounded-lg transition flex items-center gap-1"
                        >
                          <Send className="w-3 h-3" />
                          Reply
                        </button>
                      </div>
                    ) : (
                      <p className="text-xs text-slate-500">
                        Please sign in to reply to this review.
                      </p>
                    )}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-2">
          <button
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-medium text-slate-300 rounded-lg transition-colors"
          >
            Previous
          </button>
          <span className="text-xs text-slate-400">
            Page {page + 1} of {totalPages}
          </span>
          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-medium text-slate-300 rounded-lg transition-colors"
          >
            Next
          </button>
        </div>
      )}

      {/* Review Creation Modal with Photo Upload */}
      <AnimatePresence>
        {showModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              variants={modalBackdropVariants}
              initial="hidden"
              animate="visible"
              exit="exit"
              onClick={() => {
                setShowModal(false);
                setSelectedPhotos([]);
                setPhotoPreviews([]);
              }}
              className="fixed inset-0 bg-black/75 backdrop-blur-md"
            />
            <motion.div
              variants={modalDialogVariants}
              initial="hidden"
              animate="visible"
              exit="exit"
              className="relative z-10 bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl max-h-[90vh] overflow-y-auto"
            >
              <h3 className="text-lg font-bold text-white mb-1">Share Your Experience</h3>
              <p className="text-xs text-slate-400 mb-4">
                Your feedback and photos help fellow travelers choose better trips.
              </p>

              {error && (
                <div className="mb-4 p-3 bg-rose-500/10 border border-rose-500/30 text-rose-400 rounded-xl text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmitReview} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    Overall Rating
                  </label>
                  <StarRating rating={rating} interactive={true} onChange={setRating} size="lg" />
                </div>

                <div className="grid grid-cols-3 gap-3 p-3 bg-slate-800/40 rounded-xl border border-slate-800">
                  <div>
                    <label className="block text-[11px] font-medium text-slate-400 mb-1">
                      Cleanliness
                    </label>
                    <StarRating rating={cleanliness} interactive={true} onChange={setCleanliness} size="sm" />
                  </div>
                  <div>
                    <label className="block text-[11px] font-medium text-slate-400 mb-1">
                      Service
                    </label>
                    <StarRating rating={service} interactive={true} onChange={setService} size="sm" />
                  </div>
                  <div>
                    <label className="block text-[11px] font-medium text-slate-400 mb-1">
                      Value
                    </label>
                    <StarRating rating={value} interactive={true} onChange={setValue} size="sm" />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Title</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Exceptional service and smooth flight"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    className="w-full px-3.5 py-2.5 bg-slate-800 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-400"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Review</label>
                  <textarea
                    required
                    rows={4}
                    placeholder="Describe your flight or stay in detail (minimum 20 characters)..."
                    value={body}
                    onChange={(e) => setBody(e.target.value)}
                    className="w-full px-3.5 py-2.5 bg-slate-800 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-400"
                  />
                </div>

                {/* Photo Upload Attachment Section */}
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Attach Photos (Max 5, up to 5MB each)
                  </label>
                  <div className="flex flex-wrap items-center gap-3">
                    <label className="cursor-pointer flex items-center gap-2 px-3 py-2 bg-slate-800 hover:bg-slate-700 border border-dashed border-slate-600 rounded-xl text-xs font-medium text-slate-300 transition-colors">
                      <Camera className="w-4 h-4 text-amber-400" />
                      <span>Choose Photos</span>
                      <input
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        multiple
                        onChange={handlePhotoSelect}
                        className="hidden"
                      />
                    </label>

                    {photoPreviews.map((preview, idx) => (
                      <div key={idx} className="relative w-14 h-14 rounded-lg overflow-hidden border border-slate-700">
                        <img src={preview} alt="Preview" className="w-full h-full object-cover" />
                        <button
                          type="button"
                          onClick={() => handleRemovePhoto(idx)}
                          className="absolute top-0.5 right-0.5 w-4 h-4 bg-rose-600/80 rounded-full text-white flex items-center justify-center hover:bg-rose-500"
                        >
                          <X className="w-2.5 h-2.5" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="flex items-center justify-end gap-3 pt-2">
                  <motion.button
                    whileTap={{ scale: 0.95 }}
                    type="button"
                    onClick={() => {
                      setShowModal(false);
                      setSelectedPhotos([]);
                      setPhotoPreviews([]);
                    }}
                    className="px-4 py-2 text-sm text-slate-400 hover:text-white transition-colors"
                  >
                    Cancel
                  </motion.button>
                  <motion.button
                    whileTap={{ scale: 0.96 }}
                    type="submit"
                    disabled={submitting}
                    className="px-5 py-2.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black font-bold disabled:opacity-50 text-sm rounded-xl transition shadow-glow-gold"
                  >
                    {submitting ? 'Submitting...' : 'Submit Review'}
                  </motion.button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Enlarged Photo Modal */}
      <AnimatePresence>
        {activePhotoUrl && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              variants={modalBackdropVariants}
              initial="hidden"
              animate="visible"
              exit="exit"
              onClick={() => setActivePhotoUrl(null)}
              className="fixed inset-0 bg-black/85 backdrop-blur-md"
            />
            <motion.div
              variants={modalDialogVariants}
              initial="hidden"
              animate="visible"
              exit="exit"
              className="relative z-10 max-w-2xl max-h-[85vh] rounded-2xl overflow-hidden shadow-2xl border border-slate-700 bg-slate-900"
            >
              <img src={activePhotoUrl} alt="Enlarged review photo" className="w-full h-full object-contain" />
              <motion.button
                whileTap={{ scale: 0.9 }}
                onClick={() => setActivePhotoUrl(null)}
                className="absolute top-3 right-3 p-2 bg-black/60 hover:bg-black/80 rounded-full text-white transition-colors"
              >
                <X className="w-5 h-5" />
              </motion.button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};
