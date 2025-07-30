# 🚀 Deployment Guide for Scrum Poker

## Quick Deployment Options

### 🎯 Option 1: Railway (Recommended - Easiest)
**Time: 10 minutes | Cost: Free tier available**

1. **Sign up** at [railway.app](https://railway.app)
2. **Install Railway CLI**:
   ```bash
   brew install railway
   ```
3. **Deploy**:
   ```bash
   railway login
   railway init
   railway up
   ```
4. **Add environment variables** in Railway dashboard:
   - `PORT=8080`
   - `JAVA_OPTS=-Xmx512m`

### 🎯 Option 2: Render
**Time: 15 minutes | Cost: Free tier available**

1. **Push to GitHub** first
2. **Sign up** at [render.com](https://render.com)
3. **New Web Service** → Connect GitHub repo
4. **Configure**:
   - Build Command: `cd backend && ./gradlew build`
   - Start Command: `cd backend && java -jar build/libs/*.jar`
5. **Deploy Frontend** separately as Static Site

### 🎯 Option 3: Vercel + Railway (Split Deploy)
**Time: 20 minutes | Cost: Free**

**Frontend (Vercel):**
```bash
cd frontend
npm i -g vercel
vercel
```

**Backend (Railway):**
```bash
cd backend
railway init
railway up
```

**Update frontend WebSocket URL** to Railway backend URL.

### 🎯 Option 4: Docker + Google Cloud Run
**Time: 30 minutes | Cost: ~$5-10/month**

1. **Build Docker image**:
   ```bash
   docker build -t scrum-poker .
   docker tag scrum-poker gcr.io/YOUR-PROJECT/scrum-poker
   ```

2. **Push to registry**:
   ```bash
   docker push gcr.io/YOUR-PROJECT/scrum-poker
   ```

3. **Deploy to Cloud Run**:
   ```bash
   gcloud run deploy scrum-poker \
     --image gcr.io/YOUR-PROJECT/scrum-poker \
     --platform managed \
     --allow-unauthenticated \
     --port 8080
   ```

## 📝 Pre-Deployment Checklist

### Frontend Changes Needed:

1. **Update WebSocket URL** in `frontend/src/pages/PokerRoom.tsx`:
   ```typescript
   const wsUrl = process.env.NODE_ENV === 'production' 
     ? 'wss://your-backend-url.com/ws'
     : 'ws://localhost:8080/ws';
   ```

2. **Add production environment** file `frontend/.env.production`:
   ```
   VITE_WS_URL=wss://your-backend-url.com/ws
   VITE_API_URL=https://your-backend-url.com
   ```

### Backend Changes Needed:

1. **Update CORS** in `backend/src/main/kotlin/com/scrumpoker/websocket/WebSocketConfig.kt`:
   ```kotlin
   .setAllowedOrigins("https://your-frontend-url.com")
   ```

2. **Add application properties** for production in `backend/src/main/resources/application-prod.yml`:
   ```yaml
   server:
     port: ${PORT:8080}
   
   spring:
     profiles:
       active: prod
   ```

## 🔧 Environment Variables

### Backend:
- `PORT` - Server port (usually 8080)
- `JAVA_OPTS` - JVM options (e.g., `-Xmx512m`)

### Frontend:
- `VITE_WS_URL` - WebSocket URL
- `VITE_API_URL` - Backend API URL

## 🚨 Common Issues & Solutions

1. **WebSocket Connection Failed**
   - Ensure WSS (secure WebSocket) is used in production
   - Check CORS settings
   - Verify firewall/proxy allows WebSocket

2. **Memory Issues**
   - Adjust `JAVA_OPTS` to limit memory usage
   - Use `-Xmx256m` for free tiers

3. **Build Failures**
   - Ensure Node.js 18+ and Java 17+ in build environment
   - Check gradle wrapper permissions

## 📊 Comparison Table

| Platform | Setup Time | Cost | WebSocket | Auto-Deploy | Scale |
|----------|------------|------|-----------|-------------|-------|
| Railway | 10 min | Free/$5 | ✅ | ✅ | Auto |
| Render | 15 min | Free | ✅ | ✅ | Manual |
| Vercel+Railway | 20 min | Free | ✅ | ✅ | Auto |
| Cloud Run | 30 min | $5-10 | ✅ | ✅ | Auto |
| Heroku | 20 min | $7+ | ✅ | ✅ | Manual |

## 🎉 Post-Deployment

1. **Test WebSocket connection** with multiple users
2. **Monitor logs** for errors
3. **Set up custom domain** (optional)
4. **Configure SSL** (usually automatic)

Choose Railway for the quickest deployment, or Cloud Run for production-ready scaling! 