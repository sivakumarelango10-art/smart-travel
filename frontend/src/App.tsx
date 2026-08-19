import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { MainLayout } from './layouts/MainLayout';
import { AdminLayout } from './layouts/AdminLayout';
import { ProtectedRoute } from './components/ProtectedRoute';

import { HomePage } from './pages/HomePage';
import { FlightSearchPage } from './pages/FlightSearchPage';
import { BookingPage } from './pages/BookingPage';
import { BookingConfirmationPage } from './pages/BookingConfirmationPage';
import { MyBookingsPage } from './pages/MyBookingsPage';
import { TicketPage } from './pages/TicketPage';
import { CheckInPage } from './pages/CheckInPage';
import { BoardingPassPage } from './pages/BoardingPassPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { NotFoundPage } from './pages/NotFoundPage';

// Admin Pages
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminFlightsPage } from './pages/admin/AdminFlightsPage';
import { AdminFlightDetailPage } from './pages/admin/AdminFlightDetailPage';
import { AdminFlightFormPage } from './pages/admin/AdminFlightFormPage';
import { AdminSeatMapPage } from './pages/admin/AdminSeatMapPage';
import { AdminBookingsPage } from './pages/admin/AdminBookingsPage';
import { AdminBookingDetailPage } from './pages/admin/AdminBookingDetailPage';
import { AdminRefundsPage } from './pages/admin/AdminRefundsPage';
import { AdminTicketsPage } from './pages/admin/AdminTicketsPage';
import { AdminCheckInsPage } from './pages/admin/AdminCheckInsPage';
import { AdminDisruptionsPage } from './pages/admin/AdminDisruptionsPage';
import { AdminNotificationsPage } from './pages/admin/AdminNotificationsPage';
import { AdminSystemPage } from './pages/admin/AdminSystemPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NotificationProvider>
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
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
