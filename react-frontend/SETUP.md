# BanditGames Frontend - Setup Guide

This React application provides user management functionality with Keycloak JWT authentication for the BanditGames platform.

## Features

- User Registration (creates both Keycloak account + database profile)
- User Login (JWT token-based authentication)
- View User Profile
- Edit User Profile
- Protected Routes (JWT token required)
- Token Management (automatic storage and refresh)

## Prerequisites

Before running this application, ensure you have:

1. **Node.js** (v16 or higher)
2. **Keycloak Server** running on `http://localhost:8081`
   - Realm: `banditgames`
   - Client ID: `banditgames-frontend` (public client)
3. **Backend API** running on `http://localhost:8080`
   - Backend creates both Keycloak account and database user on registration

## Installation

1. Install dependencies:
```bash
npm install
```

2. Configure environment variables:
   - Copy `.env.example` to `.env`
   - Update values if your backend/Keycloak URLs differ

```bash
cp .env.example .env
```

## Running the Application

Start the development server:

```bash
npm run dev
```

The application will be available at `http://localhost:5173`

## Application Flow

### 1. Registration Flow

1. User visits the application
2. Clicks "Create Account" button
3. Fills in registration form:
   - Username
   - Email
   - Player Tag
   - **Password** (minimum 8 characters)
4. Submits the form (calls `POST /api/users`)
5. Backend creates BOTH:
   - Keycloak account (with password)
   - Database user profile
6. On success, redirected to login page

### 2. Login Flow

1. User clicks "Login" button or navigates to `/login`
2. Enters username and password
3. Frontend calls Keycloak token endpoint directly
4. Receives JWT access token and refresh token
5. Tokens stored in localStorage
6. User redirected to profile page

### 3. View Profile

1. Protected route checks for JWT token
2. If no token → redirect to login
3. App extracts email from JWT token
4. Fetches user data from `GET /api/users/email/{email}` with Bearer token
5. Displays user information:
   - Username
   - Email
   - Player Tag
   - Avatar (if set)
   - Status (ONLINE/OFFLINE/IN_GAME/AWAY)
   - Registration date
   - Last login date

### 4. Edit Profile

1. User clicks "Edit Profile" from profile page
2. Protected route checks JWT token
3. Pre-filled form with current user data
4. User can update:
   - Username
   - Email
   - Player Tag
   - Avatar URL
5. Submits changes with Bearer token (calls `PUT /api/users/{userId}`)
6. Redirected back to profile page

### 5. Logout

1. User clicks "Logout"
2. Tokens cleared from localStorage
3. Redirected to home page

## Project Structure

```
src/
├── components/
│   └── ProtectedRoute.tsx   # JWT token-based route protection
├── pages/
│   ├── Home.tsx             # Landing page
│   ├── Login.tsx            # Login page with JWT authentication
│   ├── Register.tsx         # Registration form (includes password)
│   ├── Profile.tsx          # User profile view
│   └── EditProfile.tsx      # Profile editing form
├── services/
│   ├── authService.ts       # JWT authentication & token management
│   └── userService.ts       # User API service layer
├── types/
│   └── user.ts              # TypeScript interfaces
└── App.tsx                  # Main app with routing
```

## API Endpoints Used

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/api/users` | POST | No | Create new user |
| `/api/users/email/{email}` | GET | No | Get user by email |
| `/api/users/{userId}` | GET | Yes | Get user by ID |
| `/api/users/{userId}` | PUT | Yes | Update user |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Backend API URL |
| `VITE_KEYCLOAK_URL` | `http://localhost:8081` | Keycloak server URL |
| `VITE_KEYCLOAK_REALM` | `banditgames` | Keycloak realm name |
| `VITE_KEYCLOAK_CLIENT_ID` | `banditgames-frontend` | Keycloak client ID |

## Protected Routes

The following routes require authentication:
- `/profile` - View user profile
- `/edit-profile` - Edit user profile

If a user tries to access these routes without being authenticated, they will be redirected to the home page.

## Troubleshooting

### "Failed to fetch user"
- Ensure the backend API is running on `http://localhost:8080`
- Check that the user exists in the database

### Keycloak Login Issues
- Verify Keycloak is running on `http://localhost:8081`
- Check that the `banditgames` realm exists
- Ensure `banditgames-frontend` client is configured correctly
- Verify redirect URIs include `http://localhost:5173/*`

### CORS Errors
- Backend must allow CORS from `http://localhost:5173`
- Check backend CORS configuration

## Building for Production

```bash
npm run build
```

The optimized production build will be in the `dist/` directory.

## Technologies Used

- **React 19** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router** - Client-side routing
- **Keycloak** - Authentication server (JWT tokens)
- **localStorage** - Token storage

## Authentication Details

### Token Management

- **Access Token**: Stored in localStorage, used for API calls
- **Refresh Token**: Stored in localStorage, used to get new access tokens
- **Token Extraction**: Email and username extracted from JWT payload
- **Protected Routes**: Automatically check for valid token before rendering

### Security Notes

- Tokens stored in localStorage (consider httpOnly cookies for production)
- No password ever stored client-side
- All API calls to protected endpoints include `Authorization: Bearer {token}` header
- Invalid/expired tokens trigger redirect to login page