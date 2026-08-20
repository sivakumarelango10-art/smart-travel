export interface UserPreferences {
  preferredSeatType?: string; // WINDOW, AISLE, EXTRA_LEGROOM
  preferredRoomType?: string; // DELUXE, SUITE, STANDARD
  preferredClass?: string;    // ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST
  homeAirport?: string;       // e.g. DEL, BOM, BLR
  dietaryPreference?: string; // VEGETARIAN, NON_VEG, VEGAN
  favoriteDestinations?: string[];
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  passportNumber?: string;
  nationality?: string;
}

export interface User {
  id: string;
  email: string;
  fullName: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  roles: string[];
  accountStatus?: string;
  emailVerified?: boolean;
  preferences?: UserPreferences;
  createdAt?: string;
  updatedAt?: string;
  lastLoginAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn?: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  confirmPassword?: string;
  phoneNumber?: string;
  firstName?: string;
  lastName?: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  preferences?: UserPreferences;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword?: string;
}

export interface DeleteAccountRequest {
  password?: string;
  reason?: string;
}

