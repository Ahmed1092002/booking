# Hotel Booking System - Frontend Integration Guide

## Table of Contents

1. [Overview](#overview)
2. [API Base URL & Authentication](#api-base-url--authentication)
3. [User Roles & Permissions](#user-roles--permissions)
4. [Page Structure](#page-structure)
5. [API Integration](#api-integration)
6. [UI/UX Specifications](#uiux-specifications)
7. [Component Examples](#component-examples)
8. [State Management](#state-management)
9. [Error Handling](#error-handling)
10. [Testing Checklist](#testing-checklist)

---

## 1. Overview

This guide provides everything needed to build the frontend for the Hotel Booking System. The backend API is fully functional and uses PostgreSQL database with all advanced features implemented.

**Tech Stack Recommendations:**

- **Framework**: React, Vue.js, or Angular
- **State Management**: Redux, Zustand, or Pinia
- **HTTP Client**: Axios or Fetch API
- **UI Library**: Material-UI, Ant Design, or Tailwind CSS
- **Form Validation**: Formik, React Hook Form, or Vuelidate
- **Date Picker**: react-datepicker or similar
- **Image Upload**: react-dropzone or similar

---

## 2. API Base URL & Authentication

### Base URL

```
Development: http://localhost:8080
Production: https://api.yourdomain.com
```

### Database

The backend uses **PostgreSQL** database named `booking`. All data persists across restarts.

### Authentication Flow

1. **Register/Login** → Get JWT token
2. **Store token** in localStorage or sessionStorage
3. **Include token** in all authenticated requests

```javascript
// Login Example
const login = async (email, password) => {
  const response = await fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  const data = await response.json();
  localStorage.setItem("token", data.token);
  localStorage.setItem("user", JSON.stringify(data));
  return data;
};

// Authenticated Request Example
const getMyBookings = async () => {
  const token = localStorage.getItem("token");
  const response = await fetch(
    "http://localhost:8080/api/bookings/my-bookings",
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.json();
};
```

---

## 3. User Roles & Permissions

### ROLE_CUSTOMER (Default)

- Search and view hotels
- Create bookings
- Write reviews
- View/redeem loyalty points
- Manage own bookings

### ROLE_SELLER

- All customer permissions
- Create and manage hotels
- Add rooms to hotels
- Upload hotel/room images
- Block dates for maintenance
- Set seasonal pricing
- Respond to reviews
- View seller dashboard

### ROLE_ADMIN

- All permissions
- Create discount codes
- View system statistics
- Manage users
- View audit logs

---

## 4. Page Structure

### Public Pages (No Auth Required)

#### 1. Home Page `/`

- Hero section with search
- Featured hotels
- Popular destinations
- How it works section

#### 2. Hotel Search `/search`

- Search filters (city, price, dates, amenities)
- Hotel cards with images, rating, price
- Sort options (price, rating, name)
- Pagination

#### 3. Hotel Details `/hotels/:id`

- Hotel images gallery
- Hotel information
- Amenities list
- Room list with prices
- Reviews section
- Location map (Google Maps)
- Booking form

#### 4. Login `/login`

- Email and password fields
- Remember me checkbox
- Forgot password link
- Register link

#### 5. Register `/register`

- Full name, email, password
- Role selection (Customer/Seller)
- Terms acceptance

---

### Customer Pages (Auth Required)

#### 6. My Bookings `/my-bookings`

- List of all bookings
- Status badges (Confirmed, Cancelled)
- Booking details
- Cancel booking button
- Leave review button (after checkout)

#### 7. My Reviews `/my-reviews`

- List of reviews written
- Edit/delete options
- Hotel information

#### 8. Loyalty Points `/loyalty`

- Points balance display
- Points history
- Redeem points form
- Points value calculator

---

### Seller Pages (Auth Required + SELLER Role)

#### 9. Seller Dashboard `/seller/dashboard`

- Revenue statistics
- Total bookings
- Average rating
- Recent bookings
- Hotel performance charts

#### 10. My Hotels `/seller/hotels`

- List of seller's hotels
- Add new hotel button
- Edit/delete hotel options

#### 11. Create/Edit Hotel `/seller/hotels/new` or `/seller/hotels/:id/edit`

- Hotel name, city, address
- Google Maps URL
- Amenities checkboxes
- Image upload (multiple)
- Save button

#### 12. Hotel Management `/seller/hotels/:id`

- Hotel details
- Room list
- Add room button
- Edit room options
- Image gallery management
- Set primary image

#### 13. Room Calendar `/seller/rooms/:id/calendar`

- Monthly calendar view
- Available/booked/blocked dates
- Block dates form
- Seasonal pricing form
- Color-coded availability

---

### Admin Pages (Auth Required + ADMIN Role)

#### 14. Admin Dashboard `/admin`

- System statistics
- Total users, hotels, bookings
- Revenue chart
- Recent activity

#### 15. User Management `/admin/users`

- User list with search
- Role badges
- User details
- Activate/deactivate users

#### 16. Discount Codes `/admin/discounts`

- List of discount codes
- Create new code form
- Active/inactive toggle
- Usage statistics

#### 17. Audit Logs `/admin/audit-logs`

- Paginated log list
- Filter by user, action, date
- Log details modal

---

## 5. API Integration

### Complete API Endpoints

#### Authentication

```javascript
// Register
POST /api/auth/register
Body: { email, password, fullName, roles: ["ROLE_CUSTOMER"] }

// Login
POST /api/auth/login
Body: { email, password }
Response: { token, type: "Bearer", email, roles }
```

#### Hotels

```javascript
// Get all hotels
GET /api/hotels

// Search by city
GET /api/hotels/search?city=New York

// Advanced search
POST /api/hotels/search/advanced
Body: {
  city: "New York",
  minPrice: 50,
  maxPrice: 300,
  amenities: ["Pool", "WiFi"],
  minCapacity: 2,
  checkInDate: "2025-02-15",
  checkOutDate: "2025-02-20",
  sortBy: "price",
  sortOrder: "asc"
}

// Create hotel (SELLER)
POST /api/hotels
Headers: { Authorization: "Bearer <token>" }
Body: { name, city, address, googleMapUrl, amenities: [] }

// Add room (SELLER)
POST /api/hotels/{hotelId}/rooms
Headers: { Authorization: "Bearer <token>" }
Body: { roomType, capacity, pricePerNight, available }
```

#### Bookings

```javascript
// Create booking
POST / api / bookings;
Headers: {
  Authorization: "Bearer <token>";
}
Body: {
  roomId, checkInDate, checkOutDate;
}

// Get my bookings
GET / api / bookings / my - bookings;
Headers: {
  Authorization: "Bearer <token>";
}
```

#### Reviews

```javascript
// Create review
POST /api/reviews
Headers: { Authorization: "Bearer <token>" }
Body: { bookingId, rating: 5, comment: "Great hotel!" }

// Get hotel reviews
GET /api/reviews/hotel/{hotelId}

// Seller respond (SELLER)
PUT /api/reviews/{reviewId}/respond
Headers: { Authorization: "Bearer <token>" }
Body: { response: "Thank you!" }

// Delete review
DELETE /api/reviews/{reviewId}
Headers: { Authorization: "Bearer <token>" }
```

#### Images

```javascript
// Upload hotel image (SELLER)
POST /api/images/hotels/{hotelId}
Headers: {
  Authorization: "Bearer <token>",
  Content-Type: "multipart/form-data"
}
Body: FormData with 'image' field

// Upload room image (SELLER)
POST /api/images/rooms/{roomId}
Headers: { Authorization: "Bearer <token>" }
Body: FormData with 'image' field

// Set primary image (SELLER)
PUT /api/images/hotels/{hotelId}/{imageId}/primary
Headers: { Authorization: "Bearer <token>" }

// Get hotel images
GET /api/images/hotels/{hotelId}

// Delete image (SELLER)
DELETE /api/images/hotels/{hotelId}/{imageId}
Headers: { Authorization: "Bearer <token>" }
```

#### Calendar

```javascript
// Get monthly calendar
GET /api/calendar/rooms/{roomId}?month=2025-02

// Block dates (SELLER)
POST /api/calendar/rooms/{roomId}/block
Headers: { Authorization: "Bearer <token>" }
Body: {
  startDate: "2025-03-01",
  endDate: "2025-03-05",
  reason: "MAINTENANCE",
  notes: "Annual maintenance"
}

// Add seasonal pricing (SELLER)
POST /api/calendar/rooms/{roomId}/seasonal-pricing
Headers: { Authorization: "Bearer <token>" }
Body: {
  startDate: "2025-12-20",
  endDate: "2025-12-31",
  pricePerNight: 300.00,
  seasonName: "Holiday Season"
}
```

#### Promotions

```javascript
// Validate discount code
POST /api/promotions/validate-code?code=SUMMER2025&amount=150.00

// Get loyalty points
GET /api/promotions/loyalty-points
Headers: { Authorization: "Bearer <token>" }

// Redeem points
POST /api/promotions/loyalty-points/redeem?points=100
Headers: { Authorization: "Bearer <token>" }
```

#### Admin

```javascript
// Get statistics (ADMIN)
GET /api/admin/statistics
Headers: { Authorization: "Bearer <token>" }

// Get all users (ADMIN)
GET /api/admin/users
Headers: { Authorization: "Bearer <token>" }

// Get audit logs (ADMIN)
GET /api/admin/audit-logs?page=0&size=50
Headers: { Authorization: "Bearer <token>" }
```

---

## 6. UI/UX Specifications

### Design Guidelines

#### Color Scheme

```css
--primary: #2563eb; /* Blue */
--secondary: #10b981; /* Green */
--danger: #ef4444; /* Red */
--warning: #f59e0b; /* Orange */
--success: #22c55e; /* Green */
--gray-50: #f9fafb;
--gray-100: #f3f4f6;
--gray-900: #111827;
```

#### Typography

- **Headings**: Inter, Poppins, or Montserrat
- **Body**: Inter or Roboto
- **Sizes**:
  - H1: 2.5rem
  - H2: 2rem
  - H3: 1.5rem
  - Body: 1rem
  - Small: 0.875rem

#### Spacing

- Use 8px grid system
- Padding: 8px, 16px, 24px, 32px
- Margins: 8px, 16px, 24px, 32px, 48px

### Component Specifications

#### Hotel Card

```
┌─────────────────────────────────┐
│  [Image - 300x200]              │
│                                 │
├─────────────────────────────────┤
│ Hotel Name              ⭐ 4.5  │
│ City, Country                   │
│ 📍 Distance from center         │
│ ✓ WiFi ✓ Pool ✓ Parking       │
│                                 │
│ From $150/night    [View Details]│
└─────────────────────────────────┘
```

#### Booking Form

```
┌─────────────────────────────────┐
│ Check-in Date:  [📅 Select]    │
│ Check-out Date: [📅 Select]    │
│ Guests:         [2 ▼]          │
│                                 │
│ Total: $450 (3 nights)         │
│                                 │
│ Discount Code: [___________]    │
│ [Apply]                         │
│                                 │
│ [Book Now]                      │
└─────────────────────────────────┘
```

#### Review Card

```
┌─────────────────────────────────┐
│ ⭐⭐⭐⭐⭐ 5.0                    │
│ John Doe - Feb 15, 2025         │
│                                 │
│ "Amazing hotel! Great service   │
│  and beautiful rooms."          │
│                                 │
│ Seller Response:                │
│ "Thank you for your feedback!"  │
└─────────────────────────────────┘
```

#### Calendar View

```
     February 2025
Su Mo Tu We Th Fr Sa
          1  2  3  4
 5  6  7  8  9 10 11
12 13 14 15 16 17 18
19 20 21 22 23 24 25
26 27 28

Legend:
🟢 Available
🔴 Booked
🟡 Blocked
🔵 Seasonal Price
```

---

## 7. Component Examples

### React Example - Hotel Search

```jsx
import React, { useState, useEffect } from "react";
import axios from "axios";

const HotelSearch = () => {
  const [filters, setFilters] = useState({
    city: "",
    minPrice: "",
    maxPrice: "",
    checkInDate: "",
    checkOutDate: "",
    amenities: [],
    sortBy: "price",
    sortOrder: "asc",
  });

  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(false);

  const searchHotels = async () => {
    setLoading(true);
    try {
      const response = await axios.post(
        "http://localhost:8080/api/hotels/search/advanced",
        filters
      );
      setHotels(response.data);
    } catch (error) {
      console.error("Search failed:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hotel-search">
      <div className="filters">
        <input
          type="text"
          placeholder="City"
          value={filters.city}
          onChange={(e) => setFilters({ ...filters, city: e.target.value })}
        />

        <input
          type="number"
          placeholder="Min Price"
          value={filters.minPrice}
          onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
        />

        <input
          type="number"
          placeholder="Max Price"
          value={filters.maxPrice}
          onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
        />

        <button onClick={searchHotels}>Search</button>
      </div>

      {loading ? (
        <div>Loading...</div>
      ) : (
        <div className="hotel-list">
          {hotels.map((hotel) => (
            <HotelCard key={hotel.id} hotel={hotel} />
          ))}
        </div>
      )}
    </div>
  );
};
```

### React Example - Image Upload

```jsx
const ImageUpload = ({ hotelId }) => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  const handleUpload = async () => {
    if (!file) return;

    const formData = new FormData();
    formData.append("image", file);

    setUploading(true);
    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        `http://localhost:8080/api/images/hotels/${hotelId}`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );
      alert("Image uploaded successfully!");
    } catch (error) {
      alert("Upload failed: " + error.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <input
        type="file"
        accept="image/*"
        onChange={(e) => setFile(e.target.files[0])}
      />
      <button onClick={handleUpload} disabled={uploading}>
        {uploading ? "Uploading..." : "Upload Image"}
      </button>
    </div>
  );
};
```

---

## 8. State Management

### Recommended State Structure

```javascript
{
  auth: {
    user: { id, email, fullName, roles },
    token: "jwt-token",
    isAuthenticated: true
  },
  hotels: {
    list: [],
    current: null,
    filters: {},
    loading: false,
    error: null
  },
  bookings: {
    list: [],
    current: null,
    loading: false
  },
  reviews: {
    list: [],
    loading: false
  },
  calendar: {
    month: "2025-02",
    days: [],
    loading: false
  },
  loyalty: {
    points: { total: 500, available: 300 },
    loading: false
  }
}
```

---

## 9. Error Handling

### Error Response Format

```javascript
{
  timestamp: "2025-12-25T15:00:00",
  status: 400,
  error: "Bad Request",
  message: "Validation failed",
  path: "/api/bookings"
}
```

### Handle Errors

```javascript
try {
  const response = await api.post("/api/bookings", data);
} catch (error) {
  if (error.response) {
    // Server responded with error
    const { status, data } = error.response;

    if (status === 401) {
      // Unauthorized - redirect to login
      logout();
      navigate("/login");
    } else if (status === 400) {
      // Bad request - show validation errors
      showError(data.message);
    } else if (status === 404) {
      // Not found
      showError("Resource not found");
    }
  } else {
    // Network error
    showError("Network error. Please try again.");
  }
}
```

---

## 10. Testing Checklist

### Functional Testing

#### Authentication

- [ ] Register new customer
- [ ] Register new seller
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Logout
- [ ] Token expiration handling

#### Hotel Search

- [ ] Search by city
- [ ] Filter by price range
- [ ] Filter by amenities
- [ ] Filter by dates
- [ ] Sort by price/rating/name
- [ ] View hotel details

#### Booking Flow

- [ ] Select dates
- [ ] Calculate total price
- [ ] Apply discount code
- [ ] Redeem loyalty points
- [ ] Create booking
- [ ] View booking confirmation
- [ ] Cancel booking

#### Reviews

- [ ] Write review after checkout
- [ ] Cannot review before checkout
- [ ] One review per booking
- [ ] Seller responds to review
- [ ] Delete own review

#### Seller Features

- [ ] Create hotel
- [ ] Add rooms
- [ ] Upload images
- [ ] Set primary image
- [ ] Block dates
- [ ] Set seasonal pricing
- [ ] View dashboard

#### Admin Features

- [ ] View statistics
- [ ] Manage users
- [ ] Create discount codes
- [ ] View audit logs

### UI/UX Testing

- [ ] Responsive design (mobile, tablet, desktop)
- [ ] Loading states
- [ ] Error messages
- [ ] Success messages
- [ ] Form validation
- [ ] Accessibility (WCAG 2.1)

---

## Quick Start Checklist

1. [ ] Set up development environment
2. [ ] Install dependencies
3. [ ] Configure API base URL
4. [ ] Implement authentication
5. [ ] Create routing structure
6. [ ] Build public pages
7. [ ] Build customer pages
8. [ ] Build seller pages
9. [ ] Build admin pages
10. [ ] Test all features
11. [ ] Deploy to staging
12. [ ] User acceptance testing
13. [ ] Deploy to production

---

## Backend Configuration

**Database**: PostgreSQL (database name: `booking`)
**Connection**: The backend connects to PostgreSQL on localhost:5432
**Migrations**: Flyway manages database schema automatically

**Important**: Ensure PostgreSQL is running and database `booking` exists before starting the backend.

---

## Support

**Backend API Documentation**: http://localhost:8080/swagger-ui.html

**Questions?** Contact backend team for API clarifications.

**Good luck building an amazing frontend! 🚀**
