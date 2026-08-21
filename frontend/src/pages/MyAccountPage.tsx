import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  User as UserIcon,
  Mail,
  Phone,
  Plane,
  MapPin,
  Lock,
  Trash2,
  CheckCircle2,
  AlertTriangle,
  AlertCircle,
  Save,
  KeyRound,
  Eye,
  EyeOff,
  Compass,
  BookmarkCheck,
  Check
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { UserPreferences } from '../types/auth';

export const MyAccountPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, updateProfile, changePassword, deleteAccount, logout, isAdmin } = useAuth();

  const [activeTab, setActiveTab] = useState<'PROFILE' | 'PREFERENCES' | 'SECURITY' | 'DANGER'>('PROFILE');

  // Profile fields state
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '');

  // Travel preferences state
  const [preferredClass, setPreferredClass] = useState(user?.preferences?.preferredClass || 'ECONOMY');
  const [preferredSeatType, setPreferredSeatType] = useState(user?.preferences?.preferredSeatType || 'WINDOW');
  const [dietaryPreference, setDietaryPreference] = useState(user?.preferences?.dietaryPreference || 'VEGETARIAN');
  const [homeAirport, setHomeAirport] = useState(user?.preferences?.homeAirport || 'DEL');
  const [addressLine1, setAddressLine1] = useState(user?.preferences?.addressLine1 || '');
  const [addressLine2, setAddressLine2] = useState(user?.preferences?.addressLine2 || '');
  const [city, setCity] = useState(user?.preferences?.city || '');
  const [state, setState] = useState(user?.preferences?.state || '');
  const [postalCode, setPostalCode] = useState(user?.preferences?.postalCode || '');
  const [country, setCountry] = useState(user?.preferences?.country || 'India');
  const [passportNumber, setPassportNumber] = useState(user?.preferences?.passportNumber || '');
  const [nationality, setNationality] = useState(user?.preferences?.nationality || 'Indian');

  // Password update state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Status & feedback state
  const [profileSaving, setProfileSaving] = useState(false);
  const [passwordSaving, setPasswordSaving] = useState(false);
  const [profileSuccessMsg, setProfileSuccessMsg] = useState<string | null>(null);
  const [passwordSuccessMsg, setPasswordSuccessMsg] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Delete modal state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteReason, setDeleteReason] = useState('No longer using the service');
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  // Sync state if user updates in context
  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setPhoneNumber(user.phoneNumber || '');
      if (user.preferences) {
        setPreferredClass(user.preferences.preferredClass || 'ECONOMY');
        setPreferredSeatType(user.preferences.preferredSeatType || 'WINDOW');
        setDietaryPreference(user.preferences.dietaryPreference || 'VEGETARIAN');
        setHomeAirport(user.preferences.homeAirport || 'DEL');
        setAddressLine1(user.preferences.addressLine1 || '');
        setAddressLine2(user.preferences.addressLine2 || '');
        setCity(user.preferences.city || '');
        setState(user.preferences.state || '');
        setPostalCode(user.preferences.postalCode || '');
        setCountry(user.preferences.country || 'India');
        setPassportNumber(user.preferences.passportNumber || '');
        setNationality(user.preferences.nationality || 'Indian');
      }
    }
  }, [user]);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setProfileSuccessMsg(null);

    if (!fullName.trim()) {
      setErrorMessage('Full name cannot be empty.');
      return;
    }

    try {
      setProfileSaving(true);
      const preferences: UserPreferences = {
        preferredClass,
        preferredSeatType,
        dietaryPreference,
        homeAirport: homeAirport.toUpperCase().trim(),
        addressLine1: addressLine1.trim(),
        addressLine2: addressLine2.trim(),
        city: city.trim(),
        state: state.trim(),
        postalCode: postalCode.trim(),
        country: country.trim(),
        passportNumber: passportNumber.trim(),
        nationality: nationality.trim(),
      };

      await updateProfile({
        fullName: fullName.trim(),
        phoneNumber: phoneNumber.trim() || undefined,
        preferences,
      });

      setProfileSuccessMsg('Profile and travel preferences updated successfully!');
      setTimeout(() => setProfileSuccessMsg(null), 4000);
    } catch (err: any) {
      setErrorMessage(err?.message || 'Failed to update profile. Please try again.');
    } finally {
      setProfileSaving(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setPasswordSuccessMsg(null);

    if (!currentPassword || !newPassword || !confirmPassword) {
      setErrorMessage('Please fill in all password fields.');
      return;
    }

    if (newPassword.length < 8) {
      setErrorMessage('New password must be at least 8 characters long.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage('New password and confirmation do not match.');
      return;
    }

    try {
      setPasswordSaving(true);
      await changePassword({
        currentPassword,
        newPassword,
        confirmPassword,
      });

      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setPasswordSuccessMsg('Your account password has been updated successfully!');
      setTimeout(() => setPasswordSuccessMsg(null), 4000);
    } catch (err: any) {
      setErrorMessage(err?.message || 'Failed to change password. Please check your current password.');
    } finally {
      setPasswordSaving(false);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      setDeleteLoading(true);
      setDeleteError(null);
      await deleteAccount({
        password: deletePassword || undefined,
        reason: deleteReason,
      });
      setShowDeleteModal(false);
      navigate('/login');
    } catch (err: any) {
      setDeleteError(err?.message || 'Failed to delete account. Please verify credentials.');
      setDeleteLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto py-8 space-y-8 animate-fade-in pb-16">
      {/* 1. TOP HERO PROFILE BANNER */}
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 sm:p-8 shadow-lg relative">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 relative z-10">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-xl bg-blue-600 flex items-center justify-center text-white font-bold text-2xl sm:text-3xl shrink-0">
              {user?.fullName?.charAt(0).toUpperCase() || user?.email?.charAt(0).toUpperCase() || 'U'}
            </div>

            <div className="space-y-1">
              <div className="flex flex-wrap items-center gap-2.5">
                <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                  {user?.fullName || 'Traveler Account'}
                </h1>
                {isAdmin ? (
                  <span className="px-2.5 py-0.5 rounded bg-rose-500/15 text-rose-400 border border-rose-500/30 text-[10px] font-bold uppercase tracking-wider">
                    ADMINISTRATOR
                  </span>
                ) : (
                  <span className="px-2.5 py-0.5 rounded bg-slate-800 text-blue-400 border border-slate-700 text-[10px] font-bold uppercase tracking-wider">
                    VERIFIED TRAVELER
                  </span>
                )}
              </div>

              <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400">
                <span className="flex items-center gap-1.5">
                  <Mail className="w-3.5 h-3.5 text-slate-500" />
                  {user?.email}
                </span>
                {user?.phoneNumber && (
                  <span className="flex items-center gap-1.5">
                    <Phone className="w-3.5 h-3.5 text-slate-500" />
                    {user.phoneNumber}
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/my-bookings"
              className="px-4 py-2.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 hover:text-white border border-slate-700 text-xs font-semibold transition flex items-center gap-2"
            >
              <BookmarkCheck className="w-4 h-4 text-blue-400" />
              <span>My Bookings</span>
            </Link>

            <button
              type="button"
              onClick={logout}
              className="px-4 py-2.5 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 text-xs font-semibold transition"
            >
              Sign Out
            </button>
          </div>
        </div>
      </div>

      {/* 2. NAVIGATION TABS */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-3 overflow-x-auto">
        <button
          type="button"
          onClick={() => setActiveTab('PROFILE')}
          className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition flex items-center gap-2 shrink-0 ${
            activeTab === 'PROFILE'
              ? 'bg-blue-600 text-white'
              : 'bg-slate-900 text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-800'
          }`}
        >
          <UserIcon className="w-3.5 h-3.5" />
          <span>Profile & Address</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('PREFERENCES')}
          className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition flex items-center gap-2 shrink-0 ${
            activeTab === 'PREFERENCES'
              ? 'bg-blue-600 text-white'
              : 'bg-slate-900 text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-800'
          }`}
        >
          <Compass className="w-3.5 h-3.5" />
          <span>Flight & Travel Preferences</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('SECURITY')}
          className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition flex items-center gap-2 shrink-0 ${
            activeTab === 'SECURITY'
              ? 'bg-blue-600 text-white'
              : 'bg-slate-900 text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-800'
          }`}
        >
          <KeyRound className="w-3.5 h-3.5" />
          <span>Security & Password</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('DANGER')}
          className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition flex items-center gap-2 shrink-0 ${
            activeTab === 'DANGER'
              ? 'bg-rose-600 text-white'
              : 'bg-slate-900 text-rose-400 hover:bg-rose-500/10 border border-slate-800'
          }`}
        >
          <Trash2 className="w-3.5 h-3.5" />
          <span>Danger Zone</span>
        </button>
      </div>

      {/* FEEDBACK TOASTS */}
      {errorMessage && (
        <div className="p-4 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2.5 animate-slide-up">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {profileSuccessMsg && (
        <div className="p-4 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-semibold flex items-center gap-2.5 animate-slide-up">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{profileSuccessMsg}</span>
        </div>
      )}

      {passwordSuccessMsg && (
        <div className="p-4 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-semibold flex items-center gap-2.5 animate-slide-up">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{passwordSuccessMsg}</span>
        </div>
      )}

      {/* 3. TAB CONTENT: PROFILE & ADDRESS */}
      {activeTab === 'PROFILE' && (
        <form onSubmit={handleSaveProfile} className="space-y-6 animate-fade-in">
          <div className="p-6 sm:p-8 rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl space-y-6 backdrop-blur-xl">
            <div className="border-b border-slate-800 pb-4">
              <h2 className="text-lg font-black text-white flex items-center gap-2">
                <UserIcon className="w-5 h-5 text-sky-400" />
                <span>Personal Information</span>
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">Manage your identity and verified contact information</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Full Name *</label>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  required
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Email Address (Read-only)</label>
                <div className="relative">
                  <input
                    type="email"
                    value={user?.email || ''}
                    disabled
                    className="w-full bg-slate-950/60 border border-slate-800/80 rounded-xl px-4 py-3 text-sm text-slate-400 cursor-not-allowed font-medium"
                  />
                  <span className="absolute right-3.5 top-3.5 text-[10px] font-bold text-emerald-400 flex items-center gap-1 bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
                    <Check className="w-3 h-3" />
                    Verified
                  </span>
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Phone Number</label>
                <input
                  type="tel"
                  placeholder="+91 98765 43210"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Nationality</label>
                <input
                  type="text"
                  placeholder="Indian"
                  value={nationality}
                  onChange={(e) => setNationality(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-medium"
                />
              </div>
            </div>

            <div className="border-t border-slate-800 pt-6 space-y-4">
              <h3 className="text-sm font-black text-white flex items-center gap-2">
                <MapPin className="w-4 h-4 text-emerald-400" />
                <span>Saved Billing & Travel Address</span>
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5 sm:col-span-2">
                  <label className="text-xs font-bold text-slate-300">Address Line 1</label>
                  <input
                    type="text"
                    placeholder="Suite 402, Highline Towers"
                    value={addressLine1}
                    onChange={(e) => setAddressLine1(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition"
                  />
                </div>

                <div className="space-y-1.5 sm:col-span-2">
                  <label className="text-xs font-bold text-slate-300">Address Line 2 (Optional)</label>
                  <input
                    type="text"
                    placeholder="MG Road, Sector 2"
                    value={addressLine2}
                    onChange={(e) => setAddressLine2(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-300">City</label>
                  <input
                    type="text"
                    placeholder="Mumbai"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-300">State / Province</label>
                  <input
                    type="text"
                    placeholder="Maharashtra"
                    value={state}
                    onChange={(e) => setState(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-300">Postal / Zip Code</label>
                  <input
                    type="text"
                    placeholder="400001"
                    value={postalCode}
                    onChange={(e) => setPostalCode(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition font-mono"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-300">Country</label>
                  <input
                    type="text"
                    placeholder="India"
                    value={country}
                    onChange={(e) => setCountry(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-sky-500 transition"
                  />
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800 flex justify-end">
              <button
                type="submit"
                disabled={profileSaving}
                className="px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs transition flex items-center gap-2 disabled:opacity-50 cursor-pointer"
              >
                {profileSaving ? (
                  <>
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    <span>Saving Changes...</span>
                  </>
                ) : (
                  <>
                    <Save className="w-4 h-4" />
                    <span>Save Profile Changes</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </form>
      )}

      {/* 4. TAB CONTENT: FLIGHT PREFERENCES */}
      {activeTab === 'PREFERENCES' && (
        <form onSubmit={handleSaveProfile} className="space-y-6 animate-fade-in">
          <div className="p-6 sm:p-8 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-6">
            <div className="border-b border-slate-800 pb-4">
              <h2 className="text-lg font-bold text-white flex items-center gap-2">
                <Plane className="w-5 h-5 text-blue-400" />
                <span>Flight & In-Flight Preferences</span>
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">Customize your default flight bookings and seat selections</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Preferred Cabin Class</label>
                <select
                  value={preferredClass}
                  onChange={(e) => setPreferredClass(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-blue-500 transition font-medium"
                >
                  <option value="ECONOMY">Economy Class</option>
                  <option value="PREMIUM_ECONOMY">Premium Economy</option>
                  <option value="BUSINESS">Business Class</option>
                  <option value="FIRST">First Class</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Preferred Seat Position</label>
                <select
                  value={preferredSeatType}
                  onChange={(e) => setPreferredSeatType(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-blue-500 transition font-medium"
                >
                  <option value="WINDOW">Window Seat</option>
                  <option value="AISLE">Aisle Seat</option>
                  <option value="EXTRA_LEGROOM">Extra Legroom (Exit Row)</option>
                  <option value="MIDDLE">Middle Seat</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">In-Flight Meal Preference</label>
                <select
                  value={dietaryPreference}
                  onChange={(e) => setDietaryPreference(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-blue-500 transition font-medium"
                >
                  <option value="VEGETARIAN">Vegetarian (Hindu / Asian)</option>
                  <option value="NON_VEG">Standard Non-Vegetarian</option>
                  <option value="VEGAN">Strict Vegan</option>
                  <option value="JAIN">Jain Meal (No Root Veg)</option>
                  <option value="GLUTEN_FREE">Gluten Friendly Meal</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Home / Primary Airport Code</label>
                <input
                  type="text"
                  placeholder="DEL / BOM / BLR"
                  maxLength={3}
                  value={homeAirport}
                  onChange={(e) => setHomeAirport(e.target.value.toUpperCase())}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-blue-500 transition font-mono font-bold uppercase"
                />
              </div>

              <div className="space-y-1.5 sm:col-span-2">
                <label className="text-xs font-bold text-slate-300">Passport Number (For International Flights)</label>
                <input
                  type="text"
                  placeholder="L1234567"
                  value={passportNumber}
                  onChange={(e) => setPassportNumber(e.target.value.toUpperCase())}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-blue-500 transition font-mono"
                />
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800 flex justify-end">
              <button
                type="submit"
                disabled={profileSaving}
                className="px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs transition flex items-center gap-2 disabled:opacity-50 cursor-pointer"
              >
                {profileSaving ? (
                  <>
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    <span>Saving Preferences...</span>
                  </>
                ) : (
                  <>
                    <Save className="w-4 h-4" />
                    <span>Save Travel Preferences</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </form>
      )}

      {/* 5. TAB CONTENT: SECURITY & PASSWORD */}
      {activeTab === 'SECURITY' && (
        <form onSubmit={handleChangePassword} className="space-y-6 animate-fade-in">
          <div className="p-6 sm:p-8 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-6">
            <div className="border-b border-slate-800 pb-4">
              <h2 className="text-lg font-bold text-white flex items-center gap-2">
                <KeyRound className="w-5 h-5 text-blue-400" />
                <span>Security & Password Management</span>
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">Ensure your account uses a strong password with at least 8 characters</p>
            </div>

            <div className="max-w-md space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Current Password *</label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
                  <input
                    type={showCurrentPassword ? 'text' : 'password'}
                    placeholder="••••••••••••"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    required
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-11 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-blue-500 transition font-medium"
                  />
                  <button
                    type="button"
                    onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                    className="absolute right-3.5 top-3.5 text-slate-500 hover:text-slate-300 transition"
                  >
                    {showCurrentPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">New Password *</label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
                  <input
                    type={showNewPassword ? 'text' : 'password'}
                    placeholder="••••••••••••"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-11 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-blue-500 transition font-medium"
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPassword(!showNewPassword)}
                    className="absolute right-3.5 top-3.5 text-slate-500 hover:text-slate-300 transition"
                  >
                    {showNewPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-300">Confirm New Password *</label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-500 absolute left-4 top-3.5" />
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    placeholder="••••••••••••"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-11 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-blue-500 transition font-medium"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3.5 top-3.5 text-slate-500 hover:text-slate-300 transition"
                  >
                    {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <div className="pt-3">
                <button
                  type="submit"
                  disabled={passwordSaving}
                  className="w-full py-3 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
                >
                  {passwordSaving ? (
                    <>
                      <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                      <span>Updating Password...</span>
                    </>
                  ) : (
                    <>
                      <Lock className="w-4 h-4" />
                      <span>Update Account Password</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </form>
      )}

      {/* 6. TAB CONTENT: DANGER ZONE & DELETE ACCOUNT */}
      {activeTab === 'DANGER' && (
        <div className="p-6 sm:p-8 rounded-3xl bg-slate-900/90 border border-rose-500/30 shadow-2xl space-y-6 backdrop-blur-xl animate-fade-in">
          <div className="flex items-center gap-3 pb-4 border-b border-rose-500/20">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-lg font-black text-rose-400">Danger Zone</h2>
              <p className="text-xs text-slate-400">Permanent and irreversible account actions</p>
            </div>
          </div>

          <div className="p-5 rounded-2xl bg-rose-500/10 border border-rose-500/20 space-y-3">
            <h3 className="font-bold text-white text-sm">Delete This Account</h3>
            <p className="text-xs text-slate-300 leading-relaxed">
              Once you delete your account, your personal details, travel preferences, and active authentication sessions will be permanently deactivated.
              Existing booked tickets remain legally preserved for airline and tax records, but your account login will be revoked immediately.
            </p>
            <div className="pt-2">
              <button
                type="button"
                onClick={() => setShowDeleteModal(true)}
                className="px-5 py-3 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-black text-xs shadow-xl shadow-rose-600/30 transition flex items-center gap-2 cursor-pointer"
              >
                <Trash2 className="w-4 h-4" />
                <span>Delete My Account</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DESTRUCTIVE DELETE ACCOUNT CONFIRMATION MODAL */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md rounded-3xl bg-slate-900 border border-rose-500/30 p-6 sm:p-8 shadow-2xl space-y-5 relative overflow-hidden">
            <div className="absolute top-0 left-0 right-0 h-1.5 bg-rose-500"></div>

            <div className="w-14 h-14 rounded-2xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto shadow-lg shadow-rose-500/20">
              <AlertTriangle className="w-7 h-7" />
            </div>

            <div className="text-center space-y-1.5">
              <h3 className="text-xl font-black text-white">Delete Account?</h3>
              <p className="text-xs text-slate-300">
                This action cannot be undone. You will immediately be signed out and lose access to this profile.
              </p>
            </div>

            {deleteError && (
              <div className="p-3.5 rounded-2xl bg-rose-500/20 border border-rose-500/40 text-rose-300 text-xs font-semibold flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{deleteError}</span>
              </div>
            )}

            <div className="space-y-3 text-xs">
              <div className="space-y-1">
                <label className="font-bold text-slate-300">Optional Reason for Leaving</label>
                <input
                  type="text"
                  value={deleteReason}
                  onChange={(e) => setDeleteReason(e.target.value)}
                  placeholder="Tell us why you are leaving..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-rose-500"
                />
              </div>

              <div className="space-y-1">
                <label className="font-bold text-slate-300">Enter Password to Confirm</label>
                <input
                  type="password"
                  value={deletePassword}
                  onChange={(e) => setDeletePassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-rose-500"
                />
              </div>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                onClick={() => setShowDeleteModal(false)}
                className="flex-1 py-3 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white text-xs font-bold transition border border-slate-700 cursor-pointer"
              >
                Cancel
              </button>

              <button
                type="button"
                disabled={deleteLoading}
                onClick={handleDeleteAccount}
                className="flex-1 py-3 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-black text-xs shadow-xl shadow-rose-600/30 transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
              >
                {deleteLoading ? (
                  <>
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    <span>Deleting...</span>
                  </>
                ) : (
                  <>
                    <Trash2 className="w-4 h-4" />
                    <span>Delete Account</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
