import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { MainLayout } from './layouts/MainLayout';
import { AdminLayout } from './layouts/AdminLayout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { PageLoader } from './components/PageLoader';

// Lazy-loaded Customer & Public Pages
const HomePage = lazy(() => import('./pages/HomePage').then((m) => ({ default: m.HomePage })));
const FlightSearchPage = lazy(() => import('./pages/FlightSearchPage').then((m) => ({ default: m.FlightSearchPage })));
const BookingPage = lazy(() => import('./pages/BookingPage').then((m) => ({ default: m.BookingPage })));
const BookingConfirmationPage = lazy(() => import('./pages/BookingConfirmationPage').then((m) => ({ default: m.BookingConfirmationPage })));
const MyBookingsPage = lazy(() => import('./pages/MyBookingsPage').then((m) => ({ default: m.MyBookingsPage })));
const TicketPage = lazy(() => import('./pages/TicketPage').then((m) => ({ default: m.TicketPage })));
const CheckInPage = lazy(() => import('./pages/CheckInPage').then((m) => ({ default: m.CheckInPage })));
const BoardingPassPage = lazy(() => import('./pages/BoardingPassPage').then((m) => ({ default: m.BoardingPassPage })));
const LoginPage = lazy(() => import('./pages/LoginPage').then((m) => ({ default: m.LoginPage })));
const RegisterPage = lazy(() => import('./pages/RegisterPage').then((m) => ({ default: m.RegisterPage })));
const NotFoundPage = lazy(() => import('./pages/NotFoundPage').then((m) => ({ default: m.NotFoundPage })));

// Lazy-loaded Admin Pages (loaded only on demand by administrators)
const AdminDashboardPage = lazy(() => import('./pages/admin/AdminDashboardPage').then((m) => ({ default: m.AdminDashboardPage })));
const AdminFlightsPage = lazy(() => import('./pages/admin/AdminFlightsPage').then((m) => ({ default: m.AdminFlightsPage })));
const AdminFlightDetailPage = lazy(() => import('./pages/admin/AdminFlightDetailPage').then((m) => ({ default: m.AdminFlightDetailPage })));
const AdminFlightFormPage = lazy(() => import('./pages/admin/AdminFlightFormPage').then((m) => ({ default: m.AdminFlightFormPage })));
const AdminSeatMapPage = lazy(() => import('./pages/admin/AdminSeatMapPage').then((m) => ({ default: m.AdminSeatMapPage })));
const AdminBookingsPage = lazy(() => import('./pages/admin/AdminBookingsPage').then((m) => ({ default: m.AdminBookingsPage })));
const AdminBookingDetailPage = lazy(() => import('./pages/admin/AdminBookingDetailPage').then((m) => ({ default: m.AdminBookingDetailPage })));
const AdminRefundsPage = lazy(() => import('./pages/admin/AdminRefundsPage').then((m) => ({ default: m.AdminRefundsPage })));
const AdminTicketsPage = lazy(() => import('./pages/admin/AdminTicketsPage').then((m) => ({ default: m.AdminTicketsPage })));
const AdminCheckInsPage = lazy(() => import('./pages/admin/AdminCheckInsPage').then((m) => ({ default: m.AdminCheckInsPage })));
const AdminDisruptionsPage = lazy(() => import('./pages/admin/AdminDisruptionsPage').then((m) => ({ default: m.AdminDisruptionsPage })));
const AdminNotificationsPage = lazy(() => import('./pages/admin/AdminNotificationsPage').then((m) => ({ default: m.AdminNotificationsPage })));
const AdminSystemPage = lazy(() => import('./pages/admin/AdminSystemPage').then((m) => ({ default: m.AdminSystemPage })));

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NotificationProvider>
          <Suspense fallback={<PageLoader />}>
            <Routes>
              {/* Customer & Public Routes */}
              <Route path="/" element={<MainLayout />}>
                {/* Public Routes */}
                <Route index element={<HomePage />} />
                <Route path="flights" element={<FlightSearchPage />} />
                <Route path="login" element={<LoginPage />} />
                <Route path="register" element={<RegisterPage />} />
                <Route path="book/:flightId" element={<BookingPage />} />

                {/* Protected Customer Routes */}
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
                <Route path="system" element={<AdminSystemPage />} />
                <Route path="*" element={<NotFoundPage />} />
              </Route>
            </Routes>
          </Suspense>
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
