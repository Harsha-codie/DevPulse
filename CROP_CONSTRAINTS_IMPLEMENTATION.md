# Crop Suitability Constraints Implementation

## Problem Solved
**Issue**: AI (Gemini) was recommending unsuitable crops for specific regions
- Example: Recommended **Cabbage** for **Honnavar** (coastal Karnataka)
- Reality: Cabbage doesn't grow well in coastal regions with high humidity
- Root Cause: Gemini is a general AI without Karnataka-specific agricultural knowledge

## Solution Architecture
Created a district-level crop suitability database to constrain AI recommendations based on:
1. **Agro-climatic zones** (Coastal, Hill Zone, Dry Zone, etc.)
2. **Suitable crops** for each zone
3. **Crops to avoid** in each zone
4. **Season-specific** constraints (Monsoon/Summer/Winter/ALL)

## Implementation Details

### 1. Database Schema
**Entity**: `DistrictCropSuitability`
- `district` - e.g., "UTTARA KANNADA", "BENGALURU URBAN"
- `agroClimaticZone` - e.g., "COASTAL", "SOUTHERN DRY", "HILL ZONE"
- `suitableCrops` - CSV list: "Rice,Coconut,Areca,Cashew,Mango"
- `avoidCrops` - CSV list: "Cabbage,Wheat,Potato,Barley"
- `season` - "Monsoon"/"Summer"/"Winter"/"ALL"
- `remarks` - Additional context

### 2. Data Coverage
**CSV File**: `backend/src/main/resources/data/district_crop_suitability.csv`
- **Districts Covered**: All 30 Karnataka districts
- **Agro-climatic Zones**: 7 zones
  * COASTAL (Uttara Kannada, Udupi, Dakshina Kannada)
  * MALNAD (Kodagu, Chikmagalur)
  * MALNAD_TRANSITION (Hassan, Shivamogga)
  * EASTERN DRY (Bengaluru, Kolar, Chikkaballapura)
  * CENTRAL DRY (Tumakuru, Chitradurga, Davanagere, Ballari)
  * SOUTHERN DRY (Mysuru, Mandya, Chamarajanagar, Ramanagara)
  * NORTHERN DRY (Vijayapura, Bagalkote, Kalaburagi, Yadgir, Koppal, Gadag)
  * NORTHERN TRANSITION (Belagavi, Dharwad, Haveri)
  * NORTH EASTERN DRY (Bidar, Raichur)

### 3. Example Constraints

#### Coastal Zone (e.g., Uttara Kannada/Honnavar)
- **Agro-climatic Zone**: COASTAL
- **Suitable Crops**: Rice, Coconut, Areca, Cashew, Mango, Banana, Spices
- **Avoid Crops**: Cabbage, Wheat, Potato, Barley, Chickpea
- **Reason**: High humidity, heavy monsoon rainfall - cool-season crops don't thrive

#### Hill Zone (e.g., Chikmagalur)
- **Agro-climatic Zone**: MALNAD
- **Suitable Crops**: Coffee, Cardamom, Areca, Pepper, Paddy, Orange
- **Avoid Crops**: Cotton, Sunflower, Jowar, Bajra
- **Reason**: High altitude, cool climate - suitable for plantation crops

#### Dry Zone (e.g., Kalaburagi)
- **Agro-climatic Zone**: NORTH EASTERN DRY
- **Suitable Crops**: Jowar, Cotton, Pulses, Sunflower, Tur, Chickpea
- **Avoid Crops**: Rice, Sugarcane, Ragi
- **Reason**: Very dry, deep black soil - water-intensive crops not viable

### 4. Code Changes

#### New Files Created:
1. **DistrictCropSuitability.java** - JPA entity for crop constraints
2. **DistrictCropSuitabilityRepository.java** - Repository with season-aware queries
3. **DistrictCropSuitabilityLoader.java** - Loads CSV data on startup
4. **district_crop_suitability.csv** - Data for 30 Karnataka districts

#### Modified Files:
1. **RecommendationService.java**
   - Added `cropSuitabilityRepository` dependency
   - Added `addCropConstraints()` method
   - Modified `buildComprehensivePrompt()` to inject constraints

### 5. How It Works

#### Before (Without Constraints):
```
User: "Recommend crops for Honnavar"
↓
System gathers: Soil + Weather + Market data
↓
Gemini AI: "Based on soil pH and market prices, I recommend Cabbage, Tomato, Potato"
↓
Result: ❌ UNREALISTIC (Cabbage doesn't grow in coastal Karnataka)
```

#### After (With Constraints):
```
User: "Recommend crops for Honnavar"
↓
System queries: district_crop_suitability WHERE district='UTTARA KANNADA'
↓
Constraints found:
  ✅ SUITABLE: Rice, Coconut, Areca, Cashew, Mango
  ❌ AVOID: Cabbage, Wheat, Potato, Barley
↓
Gemini Prompt: 
  "⚠️ AGRICULTURAL CONSTRAINTS (CRITICAL - MUST FOLLOW):
   District: UTTARA KANNADA (COASTAL)
   ✅ SUITABLE CROPS (recommend ONLY from this list): Rice,Coconut,Areca,Cashew,Mango
   ❌ AVOID CROPS (NEVER recommend these): Cabbage,Wheat,Potato,Barley
   
   ⚠️ IMPORTANT: You MUST ONLY recommend crops from the SUITABLE list above.
   NEVER recommend crops from the AVOID list, regardless of soil/weather/market data.
   
   [... soil, weather, market data ...]"
↓
Gemini AI: "I recommend Rice (95%), Coconut (90%), Areca (85%)"
↓
Result: ✅ AGRICULTURALLY ACCURATE (All crops suitable for coastal region)
```

## Testing Checklist

### Test Case 1: Honnavar (Coastal)
- **District**: Uttara Kannada
- **Expected**: Rice, Coconut, Areca, Cashew, Mango
- **NOT Expected**: Cabbage, Wheat, Potato
- **Status**: Ready to test

### Test Case 2: Chikmagalur (Hill Zone)
- **District**: Chikmagalur
- **Expected**: Coffee, Cardamom, Pepper, Areca
- **NOT Expected**: Cotton, Sunflower, Jowar
- **Status**: Ready to test

### Test Case 3: Kalaburagi (Very Dry)
- **District**: Kalaburagi
- **Expected**: Jowar, Cotton, Pulses, Sunflower
- **NOT Expected**: Rice, Sugarcane, Ragi
- **Status**: Ready to test

### Test Case 4: Bengaluru Urban (Eastern Dry)
- **District**: Bengaluru Urban
- **Expected**: Ragi, Groundnut, Vegetables, Flowers
- **NOT Expected**: Rice, Cotton, Sugarcane
- **Status**: Ready to test

## Data Loader Behavior

The `DistrictCropSuitabilityLoader` runs on application startup:
1. Checks if data already loaded (`repository.count() > 0`)
2. If data exists, skips loading
3. If data doesn't exist, loads CSV file
4. Parses each row and saves to database
5. Logs: "Successfully loaded {count} district crop suitability records"

**Expected on first startup**: 
```
Loading district crop suitability data from CSV...
Successfully loaded 32 district crop suitability records
```

**Expected on subsequent startups**:
```
District crop suitability data already loaded. Total records: 32
```

## Integration with Existing System

### Safety Measures:
1. **Graceful Degradation**: If constraints not found, system continues without them
2. **No Breaking Changes**: All existing location/weather/soil/market logic unchanged
3. **Additive Only**: Only adding new constraints, not modifying existing features

### Compatibility:
- ✅ Pincode-based location detection
- ✅ Village+Taluk manual entry
- ✅ GPS coordinates reverse geocoding
- ✅ Taluk fallback logic
- ✅ Fuzzy matching for typos
- ✅ Kannada language support
- ✅ Season-aware recommendations
- ✅ Market price integration
- ✅ Soil and weather data

## Database Updates

### H2 Database Changes:
New table created automatically by JPA: `district_crop_suitability`

**Schema**:
```sql
CREATE TABLE district_crop_suitability (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    district VARCHAR(255),
    agro_climatic_zone VARCHAR(255),
    suitable_crops VARCHAR(1000),
    avoid_crops VARCHAR(1000),
    season VARCHAR(50),
    remarks VARCHAR(500)
);
```

**Data Volume**: 32 records (30 districts, with some having multiple seasonal entries)

## Impact on User Experience

### Before:
- User gets unrealistic crop recommendations
- Example: Cabbage for coastal regions, Rice for very dry regions
- Farmer might waste resources planting unsuitable crops

### After:
- User gets agriculturally accurate recommendations
- Recommendations constrained by local agricultural science
- Higher chance of successful farming and profit

## Maintenance

### To Add New District:
1. Open `backend/src/main/resources/data/district_crop_suitability.csv`
2. Add new row with format:
   ```
   DISTRICT_NAME,ZONE,suitable_crops,avoid_crops,season,remarks
   ```
3. Restart backend (will detect changes and reload)

### To Update Crop List:
1. Modify existing row in CSV
2. Delete H2 database file (forces reload):
   ```
   backend/data/cropplanner.mv.db
   ```
3. Restart backend

## Verification Steps

1. **Check Data Loaded**:
   - Look for log: "Successfully loaded 32 district crop suitability records"
   
2. **Test Honnavar**:
   - Enter "Honnavar" or "Uttara Kannada" in village/district field
   - Verify recommendations are ONLY: Rice, Coconut, Areca, Cashew, Mango
   - Verify Cabbage is NOT recommended
   
3. **Check Constraint Injection**:
   - Backend logs should show: "Added crop constraints for UTTARA KANNADA: 1 constraint(s)"
   
4. **Verify Graceful Degradation**:
   - Test with unknown district (e.g., "Test District")
   - Should still get recommendations (without constraints)
   - Log: "No crop constraints found for district: TEST DISTRICT"

## Files Modified

### New Files:
```
backend/src/main/java/ai/cropplanner/crop/DistrictCropSuitability.java
backend/src/main/java/ai/cropplanner/crop/DistrictCropSuitabilityRepository.java
backend/src/main/java/ai/cropplanner/crop/DistrictCropSuitabilityLoader.java
backend/src/main/resources/data/district_crop_suitability.csv
```

### Modified Files:
```
backend/src/main/java/ai/cropplanner/recommendation/RecommendationService.java
  - Added cropSuitabilityRepository dependency
  - Added addCropConstraints() method
  - Modified buildComprehensivePrompt() to inject constraints BEFORE soil/weather data
```

## Summary

✅ **Problem**: AI recommending unsuitable crops (Cabbage in coastal Honnavar)
✅ **Solution**: District-level crop suitability constraints database
✅ **Coverage**: All 30 Karnataka districts with agro-climatic zones
✅ **Integration**: Injected into Gemini prompt BEFORE soil/weather/market data
✅ **Safety**: Graceful degradation if constraints not found
✅ **Compatibility**: No breaking changes to existing features
✅ **Status**: Ready to test

**Next Step**: Test with Honnavar to verify Cabbage is NOT recommended anymore!
