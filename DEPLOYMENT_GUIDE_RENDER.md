# Deploying to Render 🚀

This guide explains how to deploy your Hotel Booking System to [Render](https://render.com) using the configurations we've created.

## 📋 Prerequisites

1. **GitHub Account**: Your project code **must** be pushed to a GitHub repository.
2. **Render Account**: Create a free account at https://dashboard.render.com.
3. **Cloudinary Account**: You need your Cloud API keys handy.

---

## 🚀 Deployment Steps (Using Blueprint)

The easiest way to deploy is using the `render.yaml` "Blueprint" file I created. This automatically sets up:

- The Web Service (Spring Boot App)
- PostgreSQL Database
- Redis Cache

### Step 1: Push Code to GitHub

Ensure your latest changes (including `Dockerfile` and `render.yaml`) are pushed to your GitHub repository.

### Step 2: Create Blueprint on Render

1. Go to your [Render Dashboard](https://dashboard.render.com).
2. Click **New +** button → select **Blueprint**.
3. Connect your GitHub repository.
4. Render will detect the `render.yaml` file.
5. You will be prompted to enter values for **Service Name** and environment variables.

### Step 3: Configure Environment Variables

The `render.yaml` will automatically link the database and Redis for you! However, you must manually provide the **Cloudinary** keys when prompted (or in the dashboard):

| Variable                | Value             |
| ----------------------- | ----------------- |
| `CLOUDINARY_CLOUD_NAME` | (Your Cloud Name) |
| `CLOUDINARY_API_KEY`    | (Your API Key)    |
| `CLOUDINARY_API_SECRET` | (Your API Secret) |

> **Note**: Variables like `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `REDIS_URL` are **automatically injected** by the Blueprint. You don't need to set them manually!

---

## 🐳 Manual Deployment (Alternative)

If you prefer to set up services manually without a Blueprint:

1. **Create PostgreSQL Database**:

   - New + → PostgreSQL
   - Name: `booking-db`
   - Copy the `Internal Connection URL`.

2. **Create Redis**:

   - New + → Redis
   - Name: `booking-redis`
   - Copy the `Internal Connection URL` (e.g., `redis://...`).

3. **Create Web Service**:
   - New + → Web Service
   - **Environment**: Docker
   - **Environment Variables**:
     - `SPRING_DATASOURCE_URL`: (Paste Postgres Internal URL)
     - `DB_USERNAME`: (Postgres User)
     - `DB_PASSWORD`: (Postgres/Render Password)
     - `REDIS_URL`: (Paste Redis Internal URL)
     - `CLOUDINARY_...`: (Your Cloudinary keys)

---

## 🔍 Verification

Once deployed, Render will provide a public URL (e.g., `https://booking-api.onrender.com`).

1. **Check Health**: POST to `/api/auth/login` needed? Or check Swagger: `https://<your-app>.onrender.com/swagger-ui.html`
2. **View Logs**: Check "Logs" tab in Render dashboard to ensure the app started and connected to DB/Redis.

## ⚠️ Free Tier Limitations

- **Spin Down**: Free web services spin down after 15 minutes of inactivity. The first request may take 50+ seconds.
- **Database**: Free Postgres expires after 90 days (use backups!).
