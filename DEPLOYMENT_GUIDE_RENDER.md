# Deploying to Render 🚀

This guide explains how to deploy your Hotel Booking System to [Render](https://render.com).

> [!TIP] > **Free Tier Architecture**
> We are using a "Sidecar" approach where **Redis runs inside the Java Container**.
> This allows you to deploy just **1 Web Service** + **1 Database** for a 100% Free setup.

---

## Option 1: The "Blueprint" Way (Recommended & Easiest)

This automatically creates the Database and Web Service for you.

1.  **Push Code**: Ensure `git push` is done.
2.  **New Blueprint**: Go to Dashboard -> New + -> Blueprint -> Select Repo.
3.  **Apply**: Render detects `render.yaml` and sets everything up.
4.  **Done!**

---

## Option 2: The "Manual" Way (If you prefer control)

If you want to create the services yourself, follow these steps exactly:

### Step 1: Create the Database

1.  Click **New +** -> **PostgreSQL**.
2.  **Name**: `booking-db`
3.  **Plan**: Free.
4.  **Create**.
5.  Wait for creation, then copy the **"Internal Database URL"**.

### Step 2: Create the Web Service

1.  Click **New +** -> **Web Service**.
2.  **Source**: "Build and deploy from a Git repository".
3.  **Connect**: Select your `booking` repository.
4.  **Name**: `booking-api`
5.  **Runtime**: **Docker** (Very Important!)
6.  **Plan**: Free.
7.  **Environment Variables**:
    You MUST add these variables manually.

    | Key                      | Value                                 |
    | ------------------------ | ------------------------------------- |
    | `SPRING_PROFILES_ACTIVE` | `prod`                                |
    | `SPRING_DATASOURCE_URL`  | _(Paste Internal DB URL from Step 1)_ |
    | `DB_USERNAME`            | _(Copy "Username" from DB info)_      |
    | `DB_PASSWORD`            | _(Copy "Password" from DB info)_      |
    | `CLOUDINARY_CLOUD_NAME`  | _(Your Cloudinary Name)_              |
    | `CLOUDINARY_API_KEY`     | _(Your Cloudinary Key)_               |
    | `CLOUDINARY_API_SECRET`  | _(Your Cloudinary Secret)_            |

### Step 3: Deploy

1.  Click **Create Web Service**.
2.  Render will start the Docker build.
3.  It will install Redis + Java and start them both.
4.  You will see logs: `Starting Redis...` then `Starting Spring Boot App...`.

---

## ⚠️ Troubleshooting

- **"Unable to connect to Redis"**:
  - Ensure your `Dockerfile` includes the Redis installation steps.
  - Ensure your `start.sh` is starting `redis-server`.
  - This project is configured to look for Redis at `localhost:6379`, which works because of the Sidecar setup.
