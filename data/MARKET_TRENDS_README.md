# Market Trends CSV File Instructions

## File Location
Place your market trends CSV file at: `d:\CropPlannerApp\data\market_trends.csv`

## Required CSV Format

### Required Columns (case-insensitive)
The CSV file MUST contain these columns:
- `cmdty` - Crop/commodity name (e.g., RICE, WHEAT, MAIZE)
- `market_name` - Name of the market
- `district_name` - District name (used for geolocation mapping)
- `variety` - Crop variety name
- `p_min` - Minimum price (numeric)
- `p_max` - Maximum price (numeric)
- `p_model` - Model/predicted price (numeric)

### Sample CSV Structure
```csv
cmdty,market_name,district_name,variety,p_min,p_max,p_model,date,other_column
RICE,City Market,BANGALORE,Basmati,2500,3000,2750,2024-01-15,ignored
WHEAT,Rural Market,MYSORE,Lokwan,1800,2200,2000,2024-01-15,ignored
MAIZE,Central Market,BANGALORE,Hybrid,1500,1800,1650,2024-01-15,ignored
```

**Note**: The system will ONLY read the 7 required columns listed above. Any additional columns (like `date`, `other_column`, etc.) will be ignored during processing.

## API Endpoints Available

Once the CSV is loaded, you can access market trends via these endpoints:

### 1. Get All Districts
```
GET http://localhost:8080/api/market-trends/districts
```
Returns list of all available districts.

### 2. Get District Summary
```
GET http://localhost:8080/api/market-trends/summary?district=BANGALORE
```
Returns comprehensive summary with available crops, markets, and price statistics for the district.

### 3. Get Crops by District
```
GET http://localhost:8080/api/market-trends/crops?district=BANGALORE
```
Returns list of all crops available in the specified district.

### 4. Get Market Trends
```
GET http://localhost:8080/api/market-trends?district=BANGALORE
GET http://localhost:8080/api/market-trends?district=BANGALORE&crop=RICE
```
Returns all market trend records for a district, optionally filtered by crop.

### 5. Get Crop Analytics
```
GET http://localhost:8080/api/market-trends/analytics?district=BANGALORE&crop=RICE
```
Returns detailed analytics including price ranges, averages, and variety information for a specific crop in a district.

## District-Level Processing

All API responses are at the district level. The system:
- Maps user geolocation to district name
- Filters all data by district_name column
- Provides aggregated analytics per district
- Supports case-insensitive district matching

## Data Validation

The CSV loader will:
- Skip rows with missing `cmdty` or `district_name` (required fields)
- Accept null values for other fields
- Log warnings for invalid rows
- Continue processing valid rows even if some rows fail
