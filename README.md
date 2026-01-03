# 🌾 Neyogi - CropPlanner

[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Java](https://img.shields.io/badge/Java-22-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2-blue)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4-blue)](https://www.typescriptlang.org/)

An intelligent, bilingual (English/ಕನ್ನಡ) crop recommendation system powered by **Google Gemini AI**, designed specifically for Karnataka farmers. Get district-level crop recommendations, fertilizer schedules, seed dealer information with pricing, and cost-profit analysis - all backed by accurate agricultural data from 31 Karnataka districts.

**Built with ❤️ for Karnataka farmers**

---

## 🎯 Features

### 🤖 AI-Powered Recommendations
- **Gemini 2.0 Flash** integration for intelligent crop recommendations
- **Ultra-strict Kannada enforcement** with proper Unicode rendering
- Context-aware suggestions based on district, season, soil type, and weather
- District-specific crop suitability data (31 districts, 3 crops each)

### 🌱 Comprehensive Agricultural Planning

#### Quick Start Workflow
1. **One-click auto-detection** - GPS location → District → Soil & Weather analysis
2. **Top 3 crop recommendations** with confidence scores and Kannada explanations
3. **Dual action buttons** per crop:
   - 💰 Calculate profit estimate (yield, costs, revenue, margins)
   - 🧪 View fertilizer plan (NPK ratios, quantities, timing)
4. **Seed dealer recommendations** with:
   - 🏪 Dealer name, location, contact number
   - 💵 Seed prices by variety (₹/kg)
   - 🌱 Available seed varieties with detailed names
   - 🏆 Top 1/2/3 ranking badges

#### Manual Search Wizard
- **3-step progressive workflow**:
  1. District/Village selection with pincode lookup
  2. Crop recommendations with scores
  3. Fertilizer plans + Seed dealers with pricing

### 📊 District-Level Data Excellence
- **31 Karnataka districts** covered with precise agricultural data
- **93 crop records** (3 crops per district) with seasons and suitability scores
- **38 seed dealers** across all major districts (Bangalore, Mysore, Mangalore, Hubli, Belgaum, Uttara Kannada, Dakshina Kannada, Udupi, Hassan, Chikmagalur, Shimoga, Chitradurga, Davangere, Bellary, Mandya, Raichur, Bidar, Gulbarga, Yadgir, Chamarajanagar, Kodagu, Tumkur)
- **Market trends** with 900+ records for price analysis

### 💰 Cost-Profit Analysis
- Detailed input cost breakdown (seeds, fertilizer, labor, other)
- Expected yield estimates (min/avg/max quintals per acre)
- Revenue projections based on market prices
- Net profit calculation with margin percentage
- Profit range visualization (pessimistic/realistic/optimistic scenarios)

### 🌐 Bilingual Support
- Full **English** and **ಕನ್ನಡ (Kannada)** translations
- Navbar language toggle with instant UI updates
- Locale persistence across sessions
- Crop names and recommendations in user's selected language
- Translation validation with CI checks (65+ keys)

### 📄 Enhanced PDF Export
- **NEW**: html2canvas-based rendering for perfect Kannada Unicode display
- Professional layout with color-coded sections
- Includes all recommendations, fertilizer plans, and dealer details
- Seed prices and contact information in PDF
- Helpline email footer
- No more garbled text - proper font rendering guaranteed

### 🆘 Helpline Support
- Dedicated support email: **support.neyogi.cropplanner@gmail.com**
- Prominent footer display on all pages
- Available Monday - Saturday, 9 AM - 6 PM IST

### 🌦️ Real-Time Environmental Data
- **Weather Integration**: OpenWeatherMap API with graceful fallback to mock data
- **Soil Analysis**: SoilGrids API for composition (Clay, Sand, pH)
- **Location Services**: GPS-based district detection with Nominatim reverse geocoding

---

## 🏗️ Architecture

```
┌─────────────────────────┐         ┌─────────────────────────┐         ┌─────────────────────┐
│   React Frontend        │────────▶│  Spring Boot Backend    │────────▶│  External APIs      │
│   (Vite + TypeScript)   │  REST   │  (Java 22)              │         │                     │
│   Port: 5173            │◀────────│  Port: 8080             │         │  • Gemini 2.0 Flash │
│                         │         │                         │         │  • OpenWeather      │
│  • Tailwind CSS         │         │  • H2 Database          │         │  • SoilGrids (ISRIC)│
│  • react-i18next        │         │  • Spring Security      │         │  • Nominatim        │
│  • html2canvas          │         │  • CSV Data Loader      │         │                     │
│  • jsPDF                │         │  • RESTful APIs         │         └─────────────────────┘
│  • Language Toggle      │         │  • District Filtering   │
│                         │         │  • Season-aware Logic   │
└─────────────────────────┘         └─────────────────────────┘
                                                 │
                                                 ▼
                                    ┌─────────────────────────┐
                                    │   Data Sources          │
                                    │                         │
                                    │  • district_crop_       │
                                    │    suitability.csv      │
                                    │    (31 districts,       │
                                    │     93 records)         │
                                    │                         │
                                    │  • 38 Seed Dealers      │
                                    │    (In-memory DB)       │
                                    │                         │
                                    │  • Market Trends        │
                                    │    (900+ records)       │
                                    └─────────────────────────┘
```

---

## 📦 Installation & Setup

### Prerequisites
- **Java**: 22+ (JDK)
- **Node.js**: 18+
- **Maven**: 3.8+
- **Git**: Latest

### 1️⃣ Clone Repository
```bash
git clone https://github.com/yourusername/neyogi-cropplanner.git
cd neyogi-cropplanner
```

### 2️⃣ Backend Setup

#### Configure API Keys
The backend uses a Gemini API key. You can either:

**Option A: Environment Variables (Recommended)**
```bash
# Windows
set GEMINI_API_KEY=your_gemini_api_key_here

# Linux/Mac
export GEMINI_API_KEY=your_gemini_api_key_here
```

**Option B: application.yml**
Edit `backend/src/main/resources/application.yml`:
```yaml
gemini:
  api:
    key: ${GEMINI_API_KEY:your_default_key_here}
```

#### Install Dependencies & Run
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start at: **http://localhost:8080**

### 3️⃣ Frontend Setup

#### Install Dependencies
```bash
cd frontend
npm install
```

#### Run Development Server
```bash
npm run dev
```

Frontend will start at: **http://localhost:5173**

### 4️⃣ Quick Start (Windows)
Use the provided batch script to start both servers:
```cmd
START_SERVERS.bat
```

Wait 60 seconds, then open: **http://localhost:5173**

---

## 🔑 API Keys

### Required
| Service | Key Variable | Get Key From | Purpose |
|---------|--------------|--------------|---------|
| Google Gemini | `GEMINI_API_KEY` | [Google AI Studio](https://makersuite.google.com/app/apikey) | Crop recommendations |

### Optional (with Fallbacks)
| Service | Key Variable | Get Key From | Fallback Behavior |
|---------|--------------|--------------|-------------------|
| OpenWeather | `OPENWEATHER_API_KEY` | [OpenWeatherMap](https://openweathermap.org/api) | Returns mock data (25.5°C, 65% humidity) |

**Note**: Weather API failures are handled gracefully with mock data, so the app works even without a valid key.

---

## 🚀 API Endpoints

### Crop Recommendations
```http
POST /api/recommendations
Content-Type: application/json

{
  "district": "BANGALORE",
  "language": "en",          # "en" or "kn"
  "season": "KHARIF"         # Optional: KHARIF/RABI/ZAID
}
```

**Response**: Top 3 crops with scores (0-100) and reasons in selected language

### Fertilizer Recommendations
```http
POST /api/fertilizer/recommendations
Content-Type: application/json

{
  "crop": "Ragi",
  "district": "BANGALORE"
}
```

**Response**: Fertilizer plan with NPK ratios, quantities, timing

### Seed Dealers
```http
GET /api/seeds/dealers?district=BANGALORE&crop=Ragi&page=0&size=10
```

**Response**: Dealers with name, city, contact, available seeds, prices

### Weather & Soil
```http
GET /api/weather?lat=12.9716&lon=77.5946
GET /api/soil?lat=12.9716&lon=77.5946
```

### Location Services
```http
GET /api/location/pincode/{pincode}     # Resolve pincode to district
GET /api/location/reverse?lat=X&lon=Y   # Reverse geocode to district
```

---

## 📊 Dataset Structure

### District Crop Suitability (`district_crop_suitability.csv`)
**93 records** covering 31 Karnataka districts.

#### Format:
```csv
district,crop_1,season_1,score_1,crop_2,season_2,score_2,crop_3,season_3,score_3
```

#### Sample:
```csv
Bengaluru Urban,Ragi/Vegetables,Kharif/Rabi,75,Paddy,Kharif,60,Groundnut/Millet,Kharif,50
Mysuru,Paddy,Kharif,85,Ragi,Kharif,75,Maize/Pulses,Kharif/Rabi,65
```

**Key Points:**
- ✅ 3 crops per district with season and suitability score
- ✅ Backend filters crops by district+season before AI sees them
- ✅ Loaded automatically on startup into H2 database

### Seed Dealers (In-Memory Database)
**38 dealers** with complete information:
- Name, City, District, Contact (10-digit mobile)
- 2-4 available seed varieties per dealer
- Prices in ₹/kg format (e.g., "₹120/kg", "₹350/kg")
- Coverage: All major Karnataka districts

---

## 🧪 Testing

### Frontend Build
```bash
cd frontend
npm run build    # Includes i18n validation
```

### Backend Build
```bash
cd backend
mvn clean package -DskipTests
```

### Manual Testing Checklist
- [ ] Quick Start workflow completes successfully
- [ ] Manual Search wizard (3 steps) works end-to-end
- [ ] Language toggle EN ↔ KN updates all UI text
- [ ] Fertilizer updates when clicking different crops
- [ ] Seed dealers display with prices and contact
- [ ] PDF export shows proper Kannada text (no garbled characters)
- [ ] Profit calculator shows cost breakdown
- [ ] Weather panel displays data (or mock fallback)
- [ ] Helpline email visible in footer

---

## 🎨 UI Features

### Responsive Design
- **Mobile-first** approach with Tailwind CSS
- **Breakpoints**: 320px (mobile), 768px (tablet), 1024px (desktop)
- **Touch-friendly** buttons (min 44px tap targets)
- **Collapsible sections** for small screens

### Visual Components
- **Gradient backgrounds** with hover effects
- **Icon-based navigation** for intuitive UX
- **Loading animations** with spinners
- **Toast notifications** for user feedback
- **Smooth scrolling** to sections
- **Card-based layouts** with shadows and borders

### Dealer Cards
- **3-column grid** on desktop, single column on mobile
- **Store icons** with gradient backgrounds
- **Ranking badges** (Top 1/2/3)
- **Clickable phone numbers** with tel: links
- **Seed variety cards** with crop name, variety, and price
- **"+ more varieties" indicator**

---

## 🌍 Internationalization

### Locale Files
```
frontend/src/locales/
├── en.json    # English (65+ keys)
└── kn.json    # Kannada (65+ keys)
```

### Key Features
- Translation function: `t('key')` throughout React components
- Navbar shows "Neyogi - CropPlanner" (hardcoded for brand consistency)
- localStorage persistence with key: `i18nextLng`
- Instant language switching without page reload

---

## 📦 Dependencies

### Backend
- **Spring Boot 3.3.0** - REST API framework
- **Spring Boot Starter Web** - RESTful services
- **H2 Database 2.2.224** - In-memory data storage
- **OpenCSV** - CSV parsing
- **Spring Boot Starter Validation** - Input validation
- **Lombok** - Boilerplate reduction

### Frontend
- **React 18.2** - UI library
- **TypeScript 5.4** - Type safety
- **Vite 5.4** - Build tool & dev server
- **Tailwind CSS 3.4** - Utility-first CSS
- **react-i18next** - Internationalization
- **jsPDF** - PDF generation
- **html2canvas** - DOM to canvas rendering
- **Axios** - HTTP client (optional)

---

## 🔧 Development Scripts

### Frontend
```bash
npm run dev          # Start dev server (http://localhost:5173)
npm run build        # Production build with optimizations
npm run preview      # Preview production build
npm run check:i18n   # Validate translation completeness
```

### Backend
```bash
mvn spring-boot:run          # Run application
mvn clean install            # Build & install to local repo
mvn clean package            # Create JAR (backend-0.1.0.jar)
java -jar target/backend-0.1.0.jar    # Run standalone JAR
```

---

## 📂 Project Structure

```
CropPlannerApp/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ai/cropplanner/
│   │   │   │   ├── auth/           # JWT authentication
│   │   │   │   ├── crop/           # Crop suitability data
│   │   │   │   ├── fertilizer/     # NPK recommendations
│   │   │   │   ├── location/       # Pincode, GPS services
│   │   │   │   ├── market/         # Price trends
│   │   │   │   ├── recommendation/ # Gemini AI integration
│   │   │   │   ├── seed/           # Dealer services
│   │   │   │   ├── soil/           # SoilGrids API
│   │   │   │   └── weather/        # OpenWeather API
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── data/
│   │   │           ├── district_crop_suitability.csv
│   │   │           ├── karnataka_pincodes.csv
│   │   │           └── pincodes_clean.csv
│   │   └── test/               # Unit tests
│   ├── pom.xml
│   └── target/
│       └── backend-0.1.0.jar
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── AboutSection.tsx
│   │   │   ├── EnhancedPDFGeneratorNew.tsx  # html2canvas PDF
│   │   │   ├── Navbar.tsx
│   │   │   ├── QuickStartButton.tsx
│   │   │   ├── RecommendationWizard.tsx
│   │   │   ├── SoilPanel.tsx
│   │   │   ├── WeatherPanel.tsx
│   │   │   └── SmartTips.tsx
│   │   ├── locales/
│   │   │   ├── en.json
│   │   │   └── kn.json
│   │   ├── utils/
│   │   │   └── cropData.ts          # Profit calculation data
│   │   ├── App.tsx
│   │   ├── i18n.ts                  # i18next configuration
│   │   └── main.tsx
│   ├── package.json
│   ├── vite.config.mjs
│   └── tailwind.config.js
├── data/
│   ├── market_trends.csv            # 900+ records
│   └── karnataka_pincodes.csv
├── START_SERVERS.bat                # Windows quick start
├── README.md
└── LICENSE
```

---

## 🐛 Known Issues & Limitations

- **Weather API**: May return 401 if keys are invalid; app falls back to mock data gracefully
- **Gemini AI**: Occasional English words in Kannada responses despite strict prompts
- **First Load**: Initial AI inference may take 15-30 seconds
- **Mobile PDF**: PDF generation on mobile browsers may be slower due to html2canvas rendering

---

## 🚧 Future Enhancements

- [ ] User authentication with registration/login
- [ ] Historical crop data tracking per user
- [ ] Push notifications for weather alerts
- [ ] Multi-crop comparison side-by-side
- [ ] Dealer ratings and reviews
- [ ] Government subsidy information
- [ ] Soil testing booking system
- [ ] PostgreSQL/MySQL for production (currently H2)

---

## 📄 License

MIT License - See [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Google Gemini AI** - Intelligent crop recommendations
- **OpenWeatherMap** - Weather data
- **SoilGrids (ISRIC)** - Soil composition data
- **Karnataka Government** - Agricultural data and market trends
- **OpenStreetMap Nominatim** - Reverse geocoding services

---

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📧 Support

Need help? Contact us at: **support.neyogi.cropplanner@gmail.com**

Available Monday - Saturday, 9 AM - 6 PM IST

---

**Empowering Karnataka Farmers with AI-Powered Agricultural Insights** 🌾



![License](https://img.shields.io/badge/license-MIT-green)## Project Structure

![Java](https://img.shields.io/badge/Java-22-orange)- `backend/`: Spring Boot 3.3 (Java 22) API, MySQL, reads env via `application.yml`

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)- `frontend/`: React (Vite), Tailwind CSS, i18next (en/kn), responsive UI, language toggle

![React](https://img.shields.io/badge/React-18.2-blue)- `data/`: Datasets directory

![TypeScript](https://img.shields.io/badge/TypeScript-5.4-blue)- `.env.example`: Environment variable template



---## Environment Variables

Copy `.env.example` to `.env` and fill in your keys.

## 🎯 Features

## Backend Setup

### 🤖 AI-Powered Recommendations1. `cd backend`

- **Gemini 2.0 Flash** integration for intelligent crop recommendations2. Configure `application.yml` for MySQL and env variables

- Context-aware suggestions based on district, season, and farm size3. Build & test: `mvn verify`

- Market trend analysis with 900+ records from Karnataka districts

## Frontend Setup

### 🌱 Comprehensive Planning Tools1. `cd frontend`

- **3-Step Recommendation Wizard**:2. Install deps: `npm install`

  1. Crop recommendations with confidence scores3. Build: `npm run build`

  2. NPK-based fertilizer schedules4. Start: `npm run dev`

  3. Seed varieties and dealer listings

- **Cost-Profit Calculator**: Estimate input costs vs projected revenue per acre## Features

- **Weather Integration**: Real-time weather data via OpenWeatherMap- Bilingual UI (English/Kannada) with navbar language toggle

- **Soil Analysis**: SoilGrids API integration for composition analysis- Responsive design (Tailwind CSS)

- Auth (to be implemented)

### 📊 District-Level Data- Reads weather and location APIs

- **No village-level data required** - works at district granularity

- Market trends for all 30 Karnataka districts## CI

- 15+ seed dealers across major cities (Bangalore, Mysore, Mangalore, Hubli, etc.)- Backend: `mvn verify`

- District-specific market prices and crop suitability- Frontend: `npm run build`



### 🌐 Bilingual Support## Acceptance

- Full **English** and **ಕನ್ನಡ (Kannada)** translations- Both build commands succeed

- Locale persistence across sessions- Language toggle updates all visible strings

- i18n completeness validation with CI checks

### 📄 PDF Export
- Fertilizer application calendar
- Seed selection checklist
- Dealer contact information
- Bilingual PDF generation (EN/KN)

---

## 🏗️ Architecture

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│   React UI      │────────▶│  Spring Boot    │────────▶│  External APIs  │
│   (Vite + TS)   │  REST   │  Backend        │         │  - Gemini AI    │
│   Port: 5173    │◀────────│  Port: 8080     │         │  - Weather API  │
│                 │         │                 │         │  - SoilGrids    │
│  - i18n         │         │  - JWT Auth     │         └─────────────────┘
│  - Tailwind CSS │         │  - H2 Database  │
│  - jsPDF        │         │  - Spring       │
└─────────────────┘         │    Security     │
                            └─────────────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │   CSV Data      │
                            │  market_trends  │
                            │  (903 records)  │
                            └─────────────────┘
```

---

## 📦 Installation & Setup

### Prerequisites
- **Java**: 22+ (JDK)
- **Node.js**: 18+
- **Maven**: 3.8+
- **Git**: Latest

### 1️⃣ Clone Repository
```bash
git clone <repository-url>
cd CropPlannerApp
```

### 2️⃣ Backend Setup

#### Configure Environment Variables
Create `backend/.env` (or set system environment variables):
```env
GEMINI_API_KEY=your_gemini_api_key_here
JWT_SECRET=your_jwt_secret_key_min_256_bits
OPENWEATHER_API_KEY=your_openweather_key_here
```

#### Install Dependencies & Run
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start at: **http://localhost:8080**

### 3️⃣ Frontend Setup

#### Install Dependencies
```bash
cd frontend
npm install
```

#### Run Development Server
```bash
npm run dev
```

Frontend will start at: **http://localhost:5173**

### 4️⃣ Quick Start (Windows)
Use the provided batch script:
```cmd
START_ALL_FIXED.bat
```

---

## 🔑 Environment Variables

### Backend (`backend/.env` or system environment)
| Variable | Description | Required | Example |
|----------|-------------|----------|---------|
| `GEMINI_API_KEY` | Google Gemini API key | ✅ Yes | `AIzaSyC...` |
| `JWT_SECRET` | JWT signing secret (256+ bits) | ✅ Yes | `your-secret-key` |
| `OPENWEATHER_API_KEY` | OpenWeatherMap API key | ❌ Optional | `abc123...` |

### Frontend (build-time only, no runtime `.env` needed)
All API calls go through backend proxy - no frontend environment variables required.

---

## 📊 Dataset & Data Structure

### Market Trends CSV (`data/market_trends.csv`)
**903 records** covering Karnataka districts from 2021-2024.

#### Columns:
```
District, Crop, AvgPrice, MinPrice, MaxPrice, Season, Year
```

#### Sample Data:
```csv
BANGALORE,RICE,2500,2200,2800,MONSOON,2023
MYSORE,WHEAT,2100,1900,2300,WINTER,2023
HUBLI,COTTON,5500,5000,6000,MONSOON,2022
```

#### District Coverage:
- **30 districts**: Bangalore, Mysore, Mangalore, Hubli, Belgaum, Tumkur, Mandya, Shimoga, Davangere, Bellary, Gulbarga, Raichur, Bidar, Dharwad, Hassan, Chitradurga, Kolar, Chikmagalur, Udupi, Bagalkot, Vijayapura, Gadag, Haveri, Koppal, Yadgir, Chamarajanagar, Chickballapur, Kodagu, Ramanagara, Bijapur

#### Key Points:
- ✅ **District-level data only** (no village/taluk required)
- ✅ Historical price trends for seasonality analysis
- ✅ Multi-crop coverage (Rice, Wheat, Maize, Cotton, Sugarcane, etc.)
- ✅ Loaded automatically on backend startup

---

## 🚀 API Endpoints

### Authentication
```http
POST /api/auth/register    # Register new user
POST /api/auth/login       # Login (returns JWT)
```

### Crop Recommendations
```http
POST /api/recommendations
Content-Type: application/json

{
  "district": "BANGALORE",
  "season": "MONSOON",      # Optional: MONSOON/SUMMER/WINTER
  "farmSize": "MEDIUM"      # Optional: SMALL/MEDIUM/LARGE
}
```

### Fertilizer Recommendations
```http
POST /api/fertilizer/recommendations
Content-Type: application/json

{
  "crop": "RICE",
  "district": "BANGALORE",
  "soilNitrogen": 150,      # kg/ha
  "soilPhosphorus": 30,
  "soilPotassium": 200,
  "rainfall": 800,          # mm
  "season": "MONSOON"
}
```

### Seed Dealers
```http
GET /api/seeds/recommendations?crop=RICE

GET /api/seeds/dealers?district=BANGALORE&crop=RICE&page=0&size=10
```

### Weather & Soil
```http
GET /api/weather?lat=12.9716&lon=77.5946
GET /api/soil?lat=12.9716&lon=77.5946
```

### Market Trends
```http
GET /api/market-trends           # All trends
GET /api/market-trends/upload    # Upload CSV (multipart/form-data)
```

---

## 🧪 Testing & Validation

### Run i18n Check
```bash
cd frontend
npm run check:i18n
```

### Build with Pre-checks
```bash
npm run build    # Automatically runs i18n check first
```

### Manual Testing Checklist
- [ ] Recommendations load for all districts
- [ ] Fertilizer calculator shows NPK breakdown
- [ ] Cost-profit toggle works
- [ ] Seed dealers filter by district/crop
- [ ] PDF export includes calendar & checklist
- [ ] Language switch EN ↔ KN works
- [ ] Mobile responsive (320px+)
- [ ] Weather/Soil panels load data

---

## 🎨 UI Components

### Dashboard Cards
- **Gradient backgrounds** with hover effects
- **Responsive grid layouts** (1-2 columns)
- **Icon-based headers** for visual clarity
- **Loading animations** with spinners

### Recommendation Wizard
- **3-step progressive disclosure**
- **Cost-profit calculator** with expand/collapse
- **Real-time API integration**
- **Fallback data** for offline resilience

### Mobile Responsiveness
- **Breakpoints**: 320px (mobile), 768px (tablet), 1024px (desktop)
- **Touch-friendly buttons** (min 44px)
- **Collapsible sections** for small screens
- **Optimized font sizes** (rem-based)

---

## 🌍 Internationalization (i18n)

### File Structure
```
frontend/src/locales/
├── en.json    # English translations (65+ keys)
└── kn.json    # Kannada translations (65+ keys)
```

### CI Validation
GitHub Actions workflow (`.github/workflows/i18n-check.yml`) validates:
- ✅ All `t()` calls have keys in locale files
- ✅ `en.json` and `kn.json` have matching keys
- ✅ No missing translations
- ✅ Runs on every push/PR

### Locale Persistence
- Saved to `localStorage` with key: `cropPlanner_language`
- Restored on app load
- Sync across browser tabs

---

## 📦 Dependencies

### Backend
- **Spring Boot 3.3.0** - REST API framework
- **Spring Security** - JWT authentication
- **H2 Database** - In-memory data storage
- **OpenCSV** - CSV parsing for market data
- **Google Gemini SDK** - AI recommendations

### Frontend
- **React 18.2** - UI library
- **TypeScript 5.4** - Type safety
- **Vite** - Build tool
- **Tailwind CSS 3.4** - Utility-first CSS
- **i18next** - Internationalization
- **jsPDF** - PDF generation

---

## 🔧 Development Scripts

### Frontend
```bash
npm run dev          # Start dev server (Vite)
npm run build        # Production build with i18n check
npm run preview      # Preview production build
npm run lint         # ESLint check
npm run check:i18n   # Validate translations
```

### Backend
```bash
mvn spring-boot:run          # Run application
mvn clean install            # Build & test
mvn test                     # Run tests only
mvn clean package            # Create JAR
```

---

## 📄 License

MIT License - See [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Google Gemini AI** - Crop recommendations
- **OpenWeatherMap** - Weather data
- **SoilGrids (ISRIC)** - Soil composition data
- **Karnataka Government** - Market trend data

---

**Built with ❤️ for Karnataka farmers**
