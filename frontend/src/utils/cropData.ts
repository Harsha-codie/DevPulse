// Crop-specific data for yield and profit calculations
export interface CropInfo {
  name: string;
  avgYieldPerAcre: number; // quintals
  minYield: number;
  maxYield: number;
  imageUrl?: string;
}

export const CROP_DATABASE: Record<string, CropInfo> = {
  // Vegetables
  'CARROT': { name: 'Carrot', avgYieldPerAcre: 75, minYield: 70, maxYield: 80 },
  'TOMATO': { name: 'Tomato', avgYieldPerAcre: 175, minYield: 150, maxYield: 200 },
  'POTATO': { name: 'Potato', avgYieldPerAcre: 100, minYield: 90, maxYield: 110 },
  'ONION': { name: 'Onion', avgYieldPerAcre: 120, minYield: 100, maxYield: 140 },
  'CABBAGE': { name: 'Cabbage', avgYieldPerAcre: 150, minYield: 130, maxYield: 170 },
  'CAULIFLOWER': { name: 'Cauliflower', avgYieldPerAcre: 130, minYield: 120, maxYield: 140 },
  'BRINJAL': { name: 'Brinjal', avgYieldPerAcre: 140, minYield: 120, maxYield: 160 },
  'CAPSICUM': { name: 'Capsicum', avgYieldPerAcre: 90, minYield: 80, maxYield: 100 },
  
  // Cereals
  'RICE': { name: 'Rice', avgYieldPerAcre: 27, minYield: 25, maxYield: 30 },
  'WHEAT': { name: 'Wheat', avgYieldPerAcre: 22, minYield: 20, maxYield: 25 },
  'MAIZE': { name: 'Maize', avgYieldPerAcre: 30, minYield: 25, maxYield: 35 },
  'JOWAR': { name: 'Jowar', avgYieldPerAcre: 18, minYield: 15, maxYield: 20 },
  'BAJRA': { name: 'Bajra', avgYieldPerAcre: 20, minYield: 18, maxYield: 22 },
  'RAGI': { name: 'Ragi', avgYieldPerAcre: 16, minYield: 14, maxYield: 18 },
  
  // Pulses
  'CHICKPEA': { name: 'Chickpea', avgYieldPerAcre: 12, minYield: 10, maxYield: 14 },
  'PIGEON PEA': { name: 'Pigeon Pea', avgYieldPerAcre: 10, minYield: 8, maxYield: 12 },
  'GREEN GRAM': { name: 'Green Gram', avgYieldPerAcre: 6, minYield: 5, maxYield: 7 },
  'BLACK GRAM': { name: 'Black Gram', avgYieldPerAcre: 6, minYield: 5, maxYield: 7 },
  'REDGRAM': { name: 'Redgram', avgYieldPerAcre: 10, minYield: 8, maxYield: 12 },
  
  // Cash Crops
  'COTTON': { name: 'Cotton', avgYieldPerAcre: 12, minYield: 10, maxYield: 15 },
  'SUGARCANE': { name: 'Sugarcane', avgYieldPerAcre: 350, minYield: 300, maxYield: 400 },
  'GROUNDNUT': { name: 'Groundnut', avgYieldPerAcre: 18, minYield: 15, maxYield: 20 },
  'SUNFLOWER': { name: 'Sunflower', avgYieldPerAcre: 12, minYield: 10, maxYield: 14 },
  'SOYBEAN': { name: 'Soybean', avgYieldPerAcre: 15, minYield: 12, maxYield: 18 },
  
  // Fruits
  'BANANA': { name: 'Banana', avgYieldPerAcre: 200, minYield: 180, maxYield: 220 },
  'PAPAYA': { name: 'Papaya', avgYieldPerAcre: 150, minYield: 130, maxYield: 170 },
  'MANGO': { name: 'Mango', avgYieldPerAcre: 80, minYield: 70, maxYield: 90 },
  
  // Spices
  'TURMERIC': { name: 'Turmeric', avgYieldPerAcre: 80, minYield: 70, maxYield: 90 },
  'GINGER': { name: 'Ginger', avgYieldPerAcre: 100, minYield: 90, maxYield: 110 },
  'CHILLI': { name: 'Chilli', avgYieldPerAcre: 30, minYield: 25, maxYield: 35 },
};

// Fallback for crops not in database
export const DEFAULT_CROP_INFO: CropInfo = {
  name: 'Unknown Crop',
  avgYieldPerAcre: 25,
  minYield: 20,
  maxYield: 30,
};

// Get crop info by name (case-insensitive, handles variations)
export function getCropInfo(cropName: string): CropInfo {
  if (!cropName) return DEFAULT_CROP_INFO;
  
  // Normalize crop name
  const normalized = cropName.toUpperCase().trim();
  
  // Direct match
  if (CROP_DATABASE[normalized]) {
    return CROP_DATABASE[normalized];
  }
  
  // Partial match (e.g., "PADDY" -> "RICE")
  const partialMatch = Object.entries(CROP_DATABASE).find(([key, value]) => {
    return normalized.includes(key) || key.includes(normalized) || 
           value.name.toUpperCase().includes(normalized);
  });
  
  if (partialMatch) {
    return partialMatch[1];
  }
  
  // Return default with crop name
  return { ...DEFAULT_CROP_INFO, name: cropName };
}

// Standard labor cost per acre (₹)
export const LABOR_COST_PER_ACRE = 15000;
