import { Suspense, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { MainLayout } from './layouts/MainLayout';
import { AdminLayout } from './layouts/AdminLayout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { PageLoader } from './components/PageLoader';
import { ErrorBoundary } from './components/ErrorBoundary';
import { InAppNotificationToast } from './components/InAppNotificationToast';
import { startKeepAliveHeartbeat, stopKeepAliveHeartbeat } from './services/warmupService';
import { lazyWithRetry } from './utils/lazyWithRetry';

// Lazy-loaded Customer & Public Pages (with automatic chunk refresh retry)
const HomePage = lazyWithRetry(() => import('./pages/HomePage').then((m) => ({ default: m.HomePage })));
const FlightSearchPage = lazyWithRetry(() => import('./pages/FlightSearchPage').then((m) => ({ default: m.FlightSearchPage })));
const HotelSearchPage = lazyWithRetry(() => import('./pages/HotelSearchPage').then((m) => ({ default: m.HotelSearchPage })));
const HotelDetailsPage = lazyWithRetry(() => import('./pages/HotelDetailsPage').then((m) => ({ default: m.HotelDetailsPage })));
const TrackedFlightsPage = lazyWithRetry(() => import('./pages/TrackedFlightsPage').then((m) => ({ default: m.TrackedFlightsPage })));
const BookingPage = lazyWithRetry(() => import('./pages/BookingPage').then((m) => ({ default: m.BookingPage })));
const BookingConfirmationPage = lazyWithRetry(() => import('./pages/BookingConfirmationPage').then((m) => ({ default: m.BookingConfirmationPage })));
const MyBookingsPage = lazyWithRetry(() => import('./pages/MyBookingsPage').then((m) => ({ default: m.MyBookingsPage })));
const MyAccountPage = lazyWithRetry(() => import('./pages/MyAccountPage').then((m) => ({ default: m.MyAccountPage })));
const TicketPage = lazyWithRetry(() => import('./pages/TicketPage').then((m) => ({ default: m.TicketPage })));
const CheckInPage = lazyWithRetry(() => import('./pages/CheckInPage').then((m) => ({ default: m.CheckInPage })));
const BoardingPassPage = lazyWithRetry(() => import('./pages/BoardingPassPage').then((m) => ({ default: m.BoardingPassPage })));
const BoardingPassVerificationPage = lazyWithRetry(() => import('./pages/BoardingPassVerificationPage').then((m) => ({ default: m.BoardingPassVerificationPage })));
const LoginPage = lazyWithRetry(() => import('./pages/LoginPage').then((m) => ({ default: m.LoginPage })));
const RegisterPage = lazyWithRetry(() => import('./pages/RegisterPage').then((m) => ({ default: m.RegisterPage })));
const OffersPage = lazyWithRetry(() => import('./pages/OffersPage').then((m) => ({ default: m.OffersPage })));
const PrivacyPolicyPage = lazyWithRetry(() => import('./pages/PrivacyPolicyPage').then((m) => ({ default: m.PrivacyPolicyPage })));
const TermsAndConditionsPage = lazyWithRetry(() => import('./pages/TermsAndConditionsPage').then((m) => ({ default: m.TermsAndConditionsPage })));
const CookiePolicyPage = lazyWithRetry(() => import('./pages/CookiePolicyPage').then((m) => ({ default: m.CookiePolicyPage })));
const NotFoundPage = lazyWithRetry(() => import('./pages/NotFoundPage').then((m) => ({ default: m.NotFoundPage })));

// Lazy-loaded Admin Pages (with automatic chunk refresh retry)
const AdminDashboardPage = lazyWithRetry(() => import('./pages/admin/AdminDashboardPage').then((m) => ({ default: m.AdminDashboardPage })));
const AdminFlightsPage = lazyWithRetry(() => import('./pages/admin/AdminFlightsPage').then((m) => ({ default: m.AdminFlightsPage })));
const AdminFlightDetailPage = lazyWithRetry(() => import('./pages/admin/AdminFlightDetailPage').then((m) => ({ default: m.AdminFlightDetailPage })));
const AdminFlightFormPage = lazyWithRetry(() => import('./pages/admin/AdminFlightFormPage').then((m) => ({ default: m.AdminFlightFormPage })));
const AdminSeatMapPage = lazyWithRetry(() => import('./pages/admin/AdminSeatMapPage').then((m) => ({ default: m.AdminSeatMapPage })));
const AdminBookingsPage = lazyWithRetry(() => import('./pages/admin/AdminBookingsPage').then((m) => ({ default: m.AdminBookingsPage })));
const AdminBookingDetailPage = lazyWithRetry(() => import('./pages/admin/AdminBookingDetailPage').then((m) => ({ default: m.AdminBookingDetailPage })));
const AdminRefundsPage = lazyWithRetry(() => import('./pages/admin/AdminRefundsPage').then((m) => ({ default: m.AdminRefundsPage })));
const AdminTicketsPage = lazyWithRetry(() => import('./pages/admin/AdminTicketsPage').then((m) => ({ default: m.AdminTicketsPage })));
const AdminCheckInsPage = lazyWithRetry(() => import('./pages/admin/AdminCheckInsPage').then((m) => ({ default: m.AdminCheckInsPage })));
const AdminDisruptionsPage = lazyWithRetry(() => import('./pages/admin/AdminDisruptionsPage').then((m) => ({ default: m.AdminDisruptionsPage })));
const AdminNotificationsPage = lazyWithRetry(() => import('./pages/admin/AdminNotificationsPage').then((m) => ({ default: m.AdminNotificationsPage })));
const AdminReviewsPage = lazyWithRetry(() => import('./pages/admin/AdminReviewsPage').then((m) => ({ default: m.AdminReviewsPage })));
const AdminSystemPage = lazyWithRetry(() => import('./pages/admin/AdminSystemPage').then((m) => ({ default: m.AdminSystemPage })));

import { ScrollToTop } from './components/ScrollToTop';

export default function App() {
  useEffect(() => {
    startKeepAliveHeartbeat();
    return () => {
      stopKeepAliveHeartbeat();
    };
  }, []);

  return (
    <BrowserRouter>
      <ScrollToTop />
      <ErrorBoundary>
        <AuthProvider>
          <NotificationProvider>
            <InAppNotificationToast />
            <Suspense fallback={<PageLoader />}>
              <Routes>
              {/* Customer & Public Routes */}
              <Route path="/" element={<MainLayout />}>
                {/* Public Routes */}
                <Route index element={<HomePage />} />
                <Route path="flights" element={<FlightSearchPage />} />
                <Route path="hotels" element={<HotelSearchPage />} />
                <Route path="hotels/:hotelId" element={<HotelDetailsPage />} />
                
                {/* Live Flight Tracker & Radar Routes (Canonical: /live-tracker) */}
                <Route path="live-tracker" element={<TrackedFlightsPage />} />
                <Route path="live tracker" element={<TrackedFlightsPage />} />
                <Route path="live%20tracker" element={<TrackedFlightsPage />} />
                <Route path="live_tracker" element={<TrackedFlightsPage />} />
                <Route path="tracked-flights" element={<TrackedFlightsPage />} />
                <Route path="tracker" element={<TrackedFlightsPage />} />
                <Route path="radar" element={<TrackedFlightsPage />} />
                <Route path="live" element={<TrackedFlightsPage />} />
                <Route path="live-radar" element={<TrackedFlightsPage />} />
                <Route path="flight-radar" element={<TrackedFlightsPage />} />
                <Route path="flight-tracker" element={<TrackedFlightsPage />} />

                {/* Deals & Offers Routes (Canonical: /offers) */}
                <Route path="offers" element={<OffersPage />} />
                <Route path="deals" element={<OffersPage />} />
                <Route path="deals-and-offers" element={<OffersPage />} />

                <Route path="login" element={<LoginPage />} />
                <Route path="register" element={<RegisterPage />} />
                <Route path="book/:flightId" element={<BookingPage />} />
                <Route path="verify-pass" element={<BoardingPassVerificationPage />} />
                <Route path="verify-pass/:token" element={<BoardingPassVerificationPage />} />
                <Route path="verify-boarding-pass" element={<BoardingPassVerificationPage />} />
                <Route path="verify-boarding-pass/:token" element={<BoardingPassVerificationPage />} />
                <Route path="privacy-policy" element={<PrivacyPolicyPage />} />
                <Route path="privacy" element={<PrivacyPolicyPage />} />
                <Route path="terms-and-conditions" element={<TermsAndConditionsPage />} />
                <Route path="terms" element={<TermsAndConditionsPage />} />
                <Route path="cookie-policy" element={<CookiePolicyPage />} />
                <Route path="cookies" element={<CookiePolicyPage />} />

                {/* Protected Customer Routes */}
                <Route
                  path="confirmation/:bookingId"
                  element={
                    <ProtectedRoute>
                      <BookingConfirmationPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="booking-confirmation/:bookingId"
                  element={
                    <ProtectedRoute>
                      <BookingConfirmationPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="my-bookings"
                  element={
                    <ProtectedRoute>
                      <MyBookingsPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="account"
                  element={
                    <ProtectedRoute>
                      <MyAccountPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="profile"
                  element={
                    <ProtectedRoute>
                      <MyAccountPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="ticket/:bookingId"
                  element={
                    <ProtectedRoute>
                      <TicketPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="check-in/:bookingId"
                  element={
                    <ProtectedRoute>
                      <CheckInPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="boarding-pass/:bookingId"
                  element={
                    <ProtectedRoute>
                      <BoardingPassPage />
                    </ProtectedRoute>
                  }
                />

                {/* 404 Fallback for Customer App */}
                <Route path="*" element={<NotFoundPage />} />
              </Route>

              {/* Admin & Operations Routes (ROLE_ADMIN Protected) */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute requireAdmin={true}>
                    <AdminLayout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<AdminDashboardPage />} />
                <Route path="flights" element={<AdminFlightsPage />} />
                <Route path="flights/new" element={<AdminFlightFormPage />} />
                <Route path="flights/:flightId" element={<AdminFlightDetailPage />} />
                <Route path="flights/:flightId/edit" element={<AdminFlightFormPage />} />
                <Route path="flights/:flightId/seats" element={<AdminSeatMapPage />} />
                <Route path="bookings" element={<AdminBookingsPage />} />
                <Route path="bookings/:bookingId" element={<AdminBookingDetailPage />} />
                <Route path="refunds" element={<AdminRefundsPage />} />
                <Route path="tickets" element={<AdminTicketsPage />} />
                <Route path="checkins" element={<AdminCheckInsPage />} />
                <Route path="disruptions" element={<AdminDisruptionsPage />} />
                <Route path="notifications" element={<AdminNotificationsPage />} />
                <Route path="reviews" element={<AdminReviewsPage />} />
                <Route path="system" element={<AdminSystemPage />} />
                <Route path="*" element={<NotFoundPage />} />
              </Route>
            </Routes>
          </Suspense>
        </NotificationProvider>
      </AuthProvider>
    </ErrorBoundary>
  </BrowserRouter>
);
}
