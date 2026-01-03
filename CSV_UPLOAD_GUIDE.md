# Market Trends CSV Upload Guide

## Overview

The market trends CSV loading system has been refactored to **prevent automatic loading on every application startup**. This prevents duplicate data insertion and improves application performance.

## Key Changes

### ❌ What Was Removed
- **Auto-loading on startup**: CSV file is NO LONGER loaded automatically when the application starts
- **Repository.deleteAll()**: No more clearing of existing data
- **@EventListener(ApplicationReadyEvent.class)**: Startup event listener removed

### ✅ What Was Added
- **Manual upload endpoint**: `POST /api/market-trends/upload`
- **Duplicate detection**: Automatic checking before inserting records
- **Upload statistics**: Detailed summary of inserted, skipped, and error records
- **Database stats endpoint**: `GET /api/market-trends/stats`

## How to Use

### 1. Check Current Database Status

```bash
GET http://localhost:8080/api/market-trends/stats
```

Response:
```json
{
  "totalRecords": 0,
  "distinctDistricts": 0,
  "dataLoaded": false
}
```

### 2. Upload CSV Data (Option A: Use Default File)

Place your `market_trends.csv` in the `/data` folder, then:

```bash
POST http://localhost:8080/api/market-trends/upload
```

Response:
```json
{
  "success": true,
  "message": "CSV processing completed",
  "statistics": {
    "totalRows": 97046,
    "successfulInserts": 97046,
    "duplicatesSkipped": 0,
    "invalidRowsSkipped": 0,
    "errors": 0
  },
  "databaseStatistics": {
    "totalRecordsInDatabase": 97046,
    "distinctDistricts": 30
  }
}
```

### 3. Upload CSV Data (Option B: Upload File)

```bash
POST http://localhost:8080/api/market-trends/upload
Content-Type: multipart/form-data

file: [your-csv-file.csv]
```

### 4. Re-running Upload (Duplicate Prevention)

If you run the upload again with the same data:

```bash
POST http://localhost:8080/api/market-trends/upload
```

Response:
```json
{
  "success": true,
  "message": "CSV processing completed",
  "statistics": {
    "totalRows": 97046,
    "successfulInserts": 0,
    "duplicatesSkipped": 97046,  // ← All records skipped!
    "invalidRowsSkipped": 0,
    "errors": 0
  },
  "databaseStatistics": {
    "totalRecordsInDatabase": 97046,  // ← Same count as before
    "distinctDistricts": 30
  }
}
```

## Duplicate Detection Logic

Records are considered duplicates if they have the same combination of:
- `cmdty` (commodity/crop name)
- `district_name`
- `market_name`
- `variety`

If a record with this exact combination already exists, it will be **skipped** (not inserted again).

## API Endpoints

### Upload CSV Data
```
POST /api/market-trends/upload
```

**Parameters:**
- `file` (optional): CSV file to upload (multipart/form-data)
- If no file provided, uses default CSV from `/data/market_trends.csv`

**Response:**
```json
{
  "success": true,
  "message": "CSV processing completed",
  "statistics": {
    "totalRows": 100000,
    "successfulInserts": 50000,
    "duplicatesSkipped": 45000,
    "invalidRowsSkipped": 5000,
    "errors": 0
  },
  "databaseStatistics": {
    "totalRecordsInDatabase": 150000,
    "distinctDistricts": 30
  },
  "errors": []  // Only included if errors.size() > 0
}
```

### Get Database Statistics
```
GET /api/market-trends/stats
```

**Response:**
```json
{
  "totalRecords": 97046,
  "distinctDistricts": 30,
  "dataLoaded": true
}
```

## Curl Examples

### Upload default CSV file:
```bash
curl -X POST http://localhost:8080/api/market-trends/upload
```

### Upload custom CSV file:
```bash
curl -X POST http://localhost:8080/api/market-trends/upload \
  -F "file=@/path/to/your/market_trends.csv"
```

### Check database stats:
```bash
curl http://localhost:8080/api/market-trends/stats
```

## Postman Usage

### 1. Upload Default CSV
- Method: `POST`
- URL: `http://localhost:8080/api/market-trends/upload`
- Body: (none - leave empty)
- Send

### 2. Upload Custom CSV
- Method: `POST`
- URL: `http://localhost:8080/api/market-trends/upload`
- Body: `form-data`
  - Key: `file` (change type to "File")
  - Value: Select your CSV file
- Send

## Application Startup Behavior

### Before (Old Behavior)
```
[INFO] Loading market trends from: ../data/market_trends.csv
[INFO] Successfully loaded 97046 market trend records from CSV
[INFO] Data covers 30 distinct districts
```
**Problem:** This happened on EVERY startup, causing duplicate inserts or unnecessary deleteAll() operations.

### After (New Behavior)
```
[INFO] Started BackendApplication in 8.234 seconds
```
**Solution:** No CSV processing on startup. Database remains unchanged. Use the upload endpoint when you need to load data.

## Best Practices

1. **First-time setup:**
   - Start the application
   - Check database stats (should show 0 records)
   - Upload CSV via the endpoint
   - Verify records were inserted

2. **Subsequent startups:**
   - Just start the application
   - No need to upload again
   - Data persists in database

3. **Updating data:**
   - Upload new CSV file
   - Duplicates are automatically skipped
   - Only new records are inserted

4. **Clearing data:**
   - If you need to clear all data and start fresh:
   ```sql
   DELETE FROM market_trends;
   ```
   - Then upload CSV again

## Error Handling

### File Not Found
```json
{
  "success": false,
  "error": "Default CSV file not found at: ../data/market_trends.csv. Please upload a CSV file or place market_trends.csv in the /data folder."
}
```

### Invalid File Type
```json
{
  "success": false,
  "error": "Only CSV files are supported"
}
```

### CSV Parsing Errors
```json
{
  "success": true,
  "message": "CSV processing completed",
  "statistics": {
    "totalRows": 1000,
    "successfulInserts": 950,
    "duplicatesSkipped": 30,
    "invalidRowsSkipped": 20,
    "errors": 5
  },
  "errors": [
    "Line 123: Invalid number format for price",
    "Line 456: Missing required field: cmdty",
    ...
  ]
}
```

## Configuration

The default CSV path can be configured in `application.yml`:

```yaml
market:
  csv:
    path: ../data/market_trends.csv
```

## Summary

✅ **CSV data is NOT loaded automatically on startup**  
✅ **Use POST /api/market-trends/upload to load data manually**  
✅ **Duplicate records are automatically skipped**  
✅ **Detailed statistics provided for every upload**  
✅ **Application startup is now faster**  
✅ **No risk of duplicate data on restart**
