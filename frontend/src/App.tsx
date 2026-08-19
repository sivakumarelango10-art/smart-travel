import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { MainLayout } from './layouts/MainLayout';
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

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NotificationProvider>
          <Routes>
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

              {/* 404 Fallback */}
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
