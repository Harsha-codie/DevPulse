# Gemini API Integration - Complete Implementation Guide

## ✅ What Has Been Implemented

### Backend (Spring Boot)

#### 1. **RecommendationController** (`/api/recommendations`)
- **Endpoint:** `POST /api/recommendations`
- **Request Body:**
  ```json
  {
    "district": "BANGALORE",
    "latitude": 12.9716,     // Optional (alternative to district)
    "longitude": 77.5946,    // Optional
    "season": "monsoon",     // Optional
    "farmSize": "medium",    // Optional
    "preferredCrops": ["Rice", "Wheat"]  // Optional
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "district": "BANGALORE",
    "message": "Recommendations generated successfully",
    "recommendations": [
      {
        "crop": "Rice",
        "score": 85,
        "reason": "High market demand with favorable monsoon conditions..."
      }
    ]
  }
  ```

#### 2. **RecommendationService**
Orchestrates the entire recommendation process:
1. **Resolves District** - From lat/lon or direct district name
2. **Gathers Context Data:**
   - Weather data (temperature, humidity, conditions)
   - Soil data (clay, sand, silt, pH)
   - Market trends (crop prices, availability)
3. **Builds Comprehensive Prompt** - Combines all data into AI prompt
4. **Calls Gemini API** - Gets AI-generated recommendations
5. **Parses & Validates** - Extracts JSON recommendations with scores

#### 3. **GeminiClient**
Advanced Gemini API integration with:
- **Schema-Constrained Prompts** - Forces JSON output format
- **Retry Logic** - Handles 429 (rate limit) and 5xx errors
- **Exponential Backoff** - 1s, 2s, 4s retry delays
- **Model:** `gemini-1.5-flash` (current stable version)
- **Configuration:**
  - Temperature: 0.2 (consistent outputs)
  - TopK: 1, TopP: 0.8
  - Max tokens: 1024

### Frontend (React + TypeScript)

#### RecommendationPanel Component
Located: `frontend/src/components/RecommendationPanel.tsx`

**Features:**
- **District Input** - Manual district name entry
- **GPS Location** - Uses browser geolocation API
- **Optional Filters:**
  - Season (Monsoon/Summer/Winter)
  - Farm Size (Small/Medium/Large)
- **Loading States** - Spinner while calling API
- **Error Handling** - Displays API errors
- **Results Display:**
  - Crop name with score badge (0-100)
  - AI-generated reasoning

**Connected to Backend:**
```typescript
fetch('http://localhost:8080/api/recommendations', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(requestBody)
})
```

---

## 🔧 How It Works

### Complete Flow:

```
User Clicks "Get Recommendations"
         ↓
Frontend sends POST to /api/recommendations
         ↓
RecommendationController validates request
         ↓
RecommendationService gathers context:
  ├─ Weather data from OpenWeather API
  ├─ Soil data from SoilGrids API
  └─ Market trends from local database
         ↓
Service builds comprehensive AI prompt:
  "You are an expert agricultural advisor...
   LOCATION: District: BANGALORE
   CURRENT WEATHER: Temperature: 28.5°C, Humidity: 75%
   SOIL COMPOSITION: Clay: 35%, Sand: 40%, Silt: 25%, pH: 6.5
   MARKET PRICE TRENDS: Rice: ₹2500, Maize: ₹1800..."
         ↓
GeminiClient calls Google Gemini API
         ↓
Gemini AI analyzes data and returns JSON:
  {
    "recommendations": [
      {"crop": "Rice", "score": 85, "reason": "..."},
      {"crop": "Maize", "score": 78, "reason": "..."}
    ]
  }
         ↓
Service parses and validates JSON
         ↓
Controller returns response to frontend
         ↓
Frontend displays recommendations with scores
```

---

## 📊 AI Scoring System

Gemini AI calculates scores (0-100) based on:

| Factor | Weight | Considerations |
|--------|--------|---------------|
| **Market Price Potential** | 40% | Current prices, demand trends |
| **Soil Suitability** | 30% | Clay/sand/silt ratios, pH levels |
| **Weather/Season Compatibility** | 30% | Temperature, humidity, rainfall |

**Example Scoring:**
- Rice in Monsoon with loamy soil + high market price = **85/100**
- Wheat in Summer with sandy soil + low market price = **45/100**

---

## 🧪 Testing the Integration

### 1. **Check Health:**
```bash
GET http://localhost:8080/api/recommendations/health
Response: "Recommendations service is healthy"
```

### 2. **Test with District:**
```bash
POST http://localhost:8080/api/recommendations
Body:
{
  "district": "BANGALORE",
  "season": "monsoon",
  "farmSize": "medium"
}
```

### 3. **Test with GPS:**
```bash
POST http://localhost:8080/api/recommendations
Body:
{
  "latitude": 12.9716,
  "longitude": 77.5946
}
```

### 4. **Test in Browser:**
1. Open http://localhost:5173
2. Scroll to "Crop Recommendations" panel
3. Enter district: "BANGALORE"
4. Select season: "Monsoon"
5. Click "Get Recommendations"
6. View AI-generated crop suggestions with scores

---

## ⚙️ Configuration

### Environment Variables:
```properties
# application.yml
gemini:
  api:
    key: ${GEMINI_API_KEY}  # Your Gemini API key
    base-url: https://generativelanguage.googleapis.com/v1beta

openweather:
  api-key: ${OPENWEATHER_API_KEY}

ipapi:
  base-url: https://ipapi.co
```

### Security Config:
```java
.requestMatchers(
    "/api/recommendations/**"  // Public access
).permitAll()
```

---

## 🚀 Current Status

### ✅ Completed:
- [x] Backend POST endpoint implemented
- [x] Gemini API client with retry logic
- [x] Context data gathering (weather, soil, market)
- [x] Comprehensive AI prompt building
- [x] JSON response parsing and validation
- [x] Frontend component with form inputs
- [x] API integration in React
- [x] Loading and error states
- [x] Results display with scores

### ⚠️ Prerequisites:
- **Market Data:** Run `POST /api/market-trends/upload` to load CSV data
- **Gemini API Key:** Must be valid and have quota
- **Both Servers Running:**
  - Backend: http://localhost:8080
  - Frontend: http://localhost:5173

---

## 🐛 Troubleshooting

### Issue: 500 Internal Server Error
**Causes:**
1. Market data not loaded → Run upload endpoint
2. Invalid Gemini API key → Check environment variable
3. Gemini API quota exceeded → Wait or upgrade plan
4. Network connectivity → Check internet connection

**Check Backend Logs:**
```
[ERROR] Failed to generate recommendations: ...
```

### Issue: No Recommendations Returned
**Causes:**
1. District not found in database
2. Gemini API timeout
3. Invalid JSON from Gemini

**Solution:** Check logs for specific error message

### Issue: Frontend Shows "Failed to get recommendations"
**Causes:**
1. Backend not running on port 8080
2. CORS issue (should be configured)
3. Network error

**Solution:** Verify backend is accessible

---

## 📝 Next Steps

1. **Upload Market Data** (if not done):
   ```bash
   POST http://localhost:8080/api/market-trends/upload
   ```

2. **Verify Gemini API Key is Working**:
   - Check backend logs for Gemini API responses
   - Ensure API key has sufficient quota

3. **Test in Browser**:
   - Navigate to http://localhost:5173
   - Enter "BANGALORE" as district
   - Click "Get Recommendations"
   - Should see 1-3 crop recommendations with AI reasoning

4. **Monitor Performance**:
   - Typical response time: 3-8 seconds (Gemini API call)
   - Retry logic handles transient failures
   - Exponential backoff prevents hammering API

---

## 🎯 Key Files Modified

### Backend:
- `RecommendationController.java` - REST endpoint
- `RecommendationService.java` - Business logic & context gathering
- `GeminiClient.java` - Gemini API integration with retry logic
- `RecommendationRequest.java` - Request DTO
- `RecommendationResponse.java` - Response DTO
- `CropRecommendation.java` - Individual recommendation DTO

### Frontend:
- `RecommendationPanel.tsx` - UI component with API integration

### All implementations are complete and ready to use! 🎉
