import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';

interface CropRecommendation {
  crop: string;
  score: number;
  reason: string;
}

interface FertilizerRecommendation {
  type: string;
  quantity: string;
  timing: string;
  schedule?: string;
  npkRatio?: string;
  justification?: string;
}

interface SeedDealer {
  name: string;
  city: string;
  district: string;
  contact: string;
  address: string;
  availableSeeds: string[];
  seedPrices?: { [key: string]: string };
}

// Auto-detect current season based on month
const getCurrentSeason = (): string => {
  const month = new Date().getMonth() + 1; // 1-12
  
  if (month >= 6 && month <= 10) {
    return 'KHARIF';  // June–October: Monsoon season
  } else if (month >= 11 || month <= 2) {
    return 'RABI';    // November–February: Winter season
  } else if (month >= 3 && month <= 5) {
    return 'ZAID';    // March–May: Summer season
  }
  return 'KHARIF';    // Default
};

export default function RecommendationWizard() {
  const { t, i18n } = useTranslation();
  
  // Step state
  const [currentStep, setCurrentStep] = useState(1);
  
  // Step 1: Recommendation inputs & results
  const [district, setDistrict] = useState('');
  const [village, setVillage] = useState('');
  const [taluk, setTaluk] = useState('');
  const [pincode, setPincode] = useState('');
  const [season, setSeason] = useState(getCurrentSeason()); // Auto-detect season
  const [farmSize, setFarmSize] = useState('');
  const [recommendations, setRecommendations] = useState<CropRecommendation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showExplanation, setShowExplanation] = useState(false);
  
  // Location input mode: 'pincode' | 'village' | 'coordinates'
  const [locationMode, setLocationMode] = useState<'pincode' | 'village' | 'coordinates'>('pincode');
  const [locationMessage, setLocationMessage] = useState<string | null>(null);
  const [villageSuggestions, setVillageSuggestions] = useState<string[]>([]);
  
  // GPS Coordinates
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  
  // Step 2: Selected crop & fertilizer
  const [selectedCrop, setSelectedCrop] = useState<string | null>(null);
  const [fertilizerRecs, setFertilizerRecs] = useState<FertilizerRecommendation[]>([]);
  
  // Step 3: Seed dealers
  const [seedDealers, setSeedDealers] = useState<SeedDealer[]>([]);
  
  // Cost-Profit Calculator
  const [showCalculator, setShowCalculator] = useState(false);
  const [costProfit, setCostProfit] = useState<any>(null);

  // Calculate cost-profit for a crop
  const calculateCostProfit = (crop: string) => {
    // Simple estimation - can be enhanced with backend API
    const baseCosts = {
      RICE: { seed: 2000, fertilizer: 3500, labor: 8000, other: 2500 },
      WHEAT: { seed: 1800, fertilizer: 3000, labor: 7000, other: 2200 },
      MAIZE: { seed: 2500, fertilizer: 4000, labor: 6000, other: 2000 },
      COTTON: { seed: 3000, fertilizer: 5000, labor: 10000, other: 3000 }
    };
    
    const cropKey = crop.toUpperCase() as keyof typeof baseCosts;
    const costs = baseCosts[cropKey] || baseCosts.RICE;
    const totalCost = Object.values(costs).reduce((a, b) => a + b, 0);
    
    // Get market price from recommendations context (simplified)
    const avgPrice = 2000; // ₹ per quintal
    const estimatedYield = 50; // quintals per acre
    const revenue = avgPrice * estimatedYield;
    const profit = revenue - totalCost;
    const profitMargin = ((profit / revenue) * 100).toFixed(1);
    
    return {
      crop,
      costs,
      totalCost,
      revenue,
      profit,
      profitMargin,
      breakeven: (totalCost / avgPrice).toFixed(1)
    };
  };

  // Auto-resolve location from pincode
  const handlePincodeInput = async (value: string) => {
    setPincode(value);
    
    if (value.length === 6 && /^\d{6}$/.test(value)) {
      try {
        const response = await fetch(`http://localhost:8080/api/location/pincode/${value}`);
        const data = await response.json();
        
        if (data.success) {
          setVillage(data.village);
          setTaluk(data.taluk);
          setDistrict(data.district);
          setLocationMessage(`📍 ${data.fullAddress}`);
          setError(null);
        } else {
          setLocationMessage('⚠️ Pincode not found in Karnataka database');
          setVillage('');
          setTaluk('');
          setDistrict('');
        }
      } catch (err) {
        setLocationMessage('Failed to resolve pincode');
      }
    } else {
      setLocationMessage(null);
    }
  };

  // Get village autocomplete suggestions
  const handleVillageInput = async (value: string) => {
    setVillage(value);
    
    if (value.length >= 2) {
      try {
        const response = await fetch(
          `http://localhost:8080/api/location/suggestions?q=${encodeURIComponent(value)}`
        );
        const data = await response.json();
        
        if (data.success && data.suggestions) {
          setVillageSuggestions(data.suggestions);
        }
      } catch (err) {
        console.error('Failed to fetch suggestions:', err);
      }
    } else {
      setVillageSuggestions([]);
    }
  };

  // Select village from suggestions
  const handleVillageSuggestionClick = (suggestion: string) => {
    // Parse: "Malur, Kolar, Kolar"
    const parts = suggestion.split(', ');
    if (parts.length >= 3) {
      setVillage(parts[0]);
      setTaluk(parts[1]);
      setDistrict(parts[2]);
    } else if (parts.length >= 2) {
      setVillage(parts[0]);
      setDistrict(parts[1]);
    }
    setVillageSuggestions([]);
    setLocationMessage(`✓ Selected: ${suggestion}`);
  };

  // Step 1: Fetch recommendations
  const fetchRecommendations = () => {
    // Validate location input based on mode
    if (locationMode === 'pincode' && !pincode) {
      setError('Please enter a pincode');
      return;
    }
    if (locationMode === 'village' && (!village || !taluk)) {
      setError('Please enter village name and taluk (both required)');
      return;
    }
    if (locationMode === 'coordinates' && (!latitude || !longitude)) {
      setError('Please enter both latitude and longitude');
      return;
    }

    setLoading(true);
    setError(null);

    const requestBody: any = { 
      language: i18n.language 
    };
    
    // Add location based on selected mode
    if (locationMode === 'pincode' && pincode) {
      requestBody.pincode = pincode;
    } else if (locationMode === 'village') {
      requestBody.village = village;
      requestBody.taluk = taluk; // Now mandatory
      if (district) requestBody.district = district.toUpperCase();
    } else if (locationMode === 'coordinates' && latitude && longitude) {
      requestBody.latitude = parseFloat(latitude);
      requestBody.longitude = parseFloat(longitude);
    }
    
    if (season) requestBody.season = season;
    if (farmSize) requestBody.farmSize = farmSize;
    
    // Add language parameter for multilingual responses
    requestBody.language = i18n.language || 'en';

    fetch('http://localhost:8080/api/recommendations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody),
    })
      .then(async (res) => {
        if (!res.ok) {
          const errorData = await res.json().catch(() => null);
          throw new Error(errorData?.message || t('failedToGetRecommendations'));
        }
        return res.json();
      })
      .then((data) => {
        if (data.success && data.recommendations?.length > 0) {
          setRecommendations(data.recommendations);
          setError(null);
        } else {
          throw new Error(data.message || t('noRecommendations'));
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  // Step 2: Select crop and generate fertilizer recommendations
  const handleCropSelection = (crop: string) => {
    setSelectedCrop(crop);
    setLoading(true);
    
    // Call fertilizer API
    const fertRequest = {
      crop: crop,
      district: district,
      season: season,
      farmSize: farmSize
    };
    
    fetch('http://localhost:8080/api/fertilizer/recommendations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(fertRequest),
    })
      .then(res => res.json())
      .then((data) => {
        if (data.success && data.recommendations) {
          setFertilizerRecs(data.recommendations);
          setCurrentStep(2);
        } else {
          // Fallback to sample data if API fails
          const fertilizers: FertilizerRecommendation[] = [
            {
              type: t('urea') || 'Urea',
              quantity: '50 kg/acre',
              timing: t('duringPlanting') || 'During planting',
              npkRatio: '46-0-0'
            }
          ];
          setFertilizerRecs(fertilizers);
          setCurrentStep(2);
        }
      })
      .catch(() => {
        // Fallback on error
        const fertilizers: FertilizerRecommendation[] = [
          {
            type: t('urea') || 'Urea',
            quantity: '50 kg/acre',
            timing: t('duringPlanting') || 'During planting',
            npkRatio: '46-0-0'
          }
        ];
        setFertilizerRecs(fertilizers);
        setCurrentStep(2);
      })
      .finally(() => setLoading(false));
  };

  // Step 3: Generate seed dealers from API
  const proceedToSeedDealers = () => {
    setLoading(true);
    
    // Fetch dealers from API
    const params = new URLSearchParams();
    if (district) params.append('district', district);
    if (selectedCrop) params.append('crop', selectedCrop);
    
    fetch(`http://localhost:8080/api/seeds/dealers?${params.toString()}`)
      .then(res => res.json())
      .then((data) => {
        if (data.success && data.dealers && data.dealers.length > 0) {
          // Store full dealer objects with all details
          setSeedDealers(data.dealers.slice(0, 6));
          setCurrentStep(3);
        } else {
          // Fallback to sample data
          const dealers: any[] = [
            {
              name: 'Karnataka Agro Traders',
              city: district || 'Bangalore',
              district: district || 'BANGALORE',
              contact: '9845012345',
              address: `${district || 'Bangalore'}, Karnataka`,
              availableSeeds: [selectedCrop ? `${selectedCrop} - Premium Variety` : 'Rice - BPT 5204', 'Maize - Super 900M'],
              seedPrices: { [selectedCrop || 'Rice']: '₹120/kg', 'Maize': '₹200/kg' }
            },
            {
              name: 'Farmers Seed Centre',
              city: district || 'Bangalore',
              district: district || 'BANGALORE',
              contact: '9880123456',
              address: `${district || 'Bangalore'}, Karnataka`,
              availableSeeds: [selectedCrop ? `${selectedCrop} - Local Variety` : 'Wheat - HD 2967', 'Chickpea - JG 11'],
              seedPrices: { [selectedCrop || 'Wheat']: '₹65/kg', 'Chickpea': '₹175/kg' }
            },
            {
              name: 'Green Fields Seeds',
              city: district || 'Bangalore',
              district: district || 'BANGALORE',
              contact: '9449876543',
              address: `${district || 'Bangalore'}, Karnataka`,
              availableSeeds: [selectedCrop ? `${selectedCrop} - Hybrid` : 'Cotton - Bt Bunny', 'Groundnut - TMV 2'],
              seedPrices: { [selectedCrop || 'Cotton']: '₹650/pkt', 'Groundnut': '₹140/kg' }
            }
          ];
          setSeedDealers(dealers);
          setCurrentStep(3);
        }
      })
      .catch(() => {
        // Fallback on error
        const dealers: any[] = [
          {
            name: 'Karnataka Agro Traders',
            city: district || 'Bangalore',
            district: district || 'BANGALORE',
            contact: '9845012345',
            address: `${district || 'Bangalore'}, Karnataka`,
            availableSeeds: [selectedCrop ? `${selectedCrop} - Premium Variety` : 'Rice - BPT 5204', 'Maize - Super 900M'],
            seedPrices: { [selectedCrop || 'Rice']: '₹120/kg', 'Maize': '₹200/kg' }
          },
          {
            name: 'Farmers Seed Centre',
            city: district || 'Bangalore',
            district: district || 'BANGALORE',
            contact: '9880123456',
            address: `${district || 'Bangalore'}, Karnataka`,
            availableSeeds: [selectedCrop ? `${selectedCrop} - Local Variety` : 'Wheat - HD 2967', 'Chickpea - JG 11'],
            seedPrices: { [selectedCrop || 'Wheat']: '₹65/kg', 'Chickpea': '₹175/kg' }
          }
        ];
        setSeedDealers(dealers);
        setCurrentStep(3);
      })
      .finally(() => setLoading(false));
  };

  // Export PDF using html2canvas for proper Kannada rendering
  const exportPDF = async () => {
    const pdfData = {
      district: district || 'Unknown',
      location: { 
        lat: latitude ? parseFloat(latitude) : 0, 
        lon: longitude ? parseFloat(longitude) : 0 
      },
      soil: {},
      weather: {},
      recommendations,
      fertilizers: fertilizerRecs,
      dealers: seedDealers,
      topCrop: selectedCrop || '',
      language: i18n.language
    };
    
    try {
      // Import dynamically
      const { generateEnhancedPDF } = await import('./EnhancedPDFGeneratorNew');
      await generateEnhancedPDF(pdfData);
    } catch (error) {
      console.error('Failed to generate PDF:', error);
      alert('Failed to generate PDF. Please try again.');
    }
  };

  return (
    <div className="max-w-5xl mx-auto p-6 sm:p-8 bg-white rounded-2xl shadow-2xl mt-8 border border-green-100">
      {/* Enhanced Step Indicator */}
      <div className="flex items-center justify-center mb-10">
        {/* Step 1 */}
        <div className={`flex items-center transition-all duration-300 ${currentStep >= 1 ? 'text-emerald-600' : 'text-gray-400'}`}>
          <div className={`w-12 h-12 rounded-full flex items-center justify-center border-2 font-bold text-lg shadow-md transition-all duration-300 ${
            currentStep >= 1 
              ? 'bg-gradient-to-br from-emerald-600 to-green-600 text-white border-emerald-600 scale-110' 
              : 'border-gray-300 bg-gray-50'
          }`}>
            {currentStep > 1 ? '✓' : '1'}
          </div>
          <span className="ml-3 font-semibold text-sm sm:text-base hidden sm:inline">{t('recommendations') || 'Recommendations'}</span>
        </div>
        
        {/* Connector 1-2 */}
        <div className={`w-16 sm:w-24 h-1.5 mx-3 rounded-full transition-all duration-500 ${
          currentStep >= 2 ? 'bg-gradient-to-r from-emerald-600 to-green-600' : 'bg-gray-200'
        }`}></div>
        
        {/* Step 2 */}
        <div className={`flex items-center transition-all duration-300 ${currentStep >= 2 ? 'text-emerald-600' : 'text-gray-400'}`}>
          <div className={`w-12 h-12 rounded-full flex items-center justify-center border-2 font-bold text-lg shadow-md transition-all duration-300 ${
            currentStep >= 2 
              ? 'bg-gradient-to-br from-emerald-600 to-green-600 text-white border-emerald-600 scale-110' 
              : 'border-gray-300 bg-gray-50'
          }`}>
            {currentStep > 2 ? '✓' : '2'}
          </div>
          <span className="ml-3 font-semibold text-sm sm:text-base hidden sm:inline">{t('fertilizer') || 'Fertilizer'}</span>
        </div>
        
        {/* Connector 2-3 */}
        <div className={`w-16 sm:w-24 h-1.5 mx-3 rounded-full transition-all duration-500 ${
          currentStep >= 3 ? 'bg-gradient-to-r from-emerald-600 to-green-600' : 'bg-gray-200'
        }`}></div>
        
        {/* Step 3 */}
        <div className={`flex items-center transition-all duration-300 ${currentStep >= 3 ? 'text-emerald-600' : 'text-gray-400'}`}>
          <div className={`w-12 h-12 rounded-full flex items-center justify-center border-2 font-bold text-lg shadow-md transition-all duration-300 ${
            currentStep >= 3 
              ? 'bg-gradient-to-br from-emerald-600 to-green-600 text-white border-emerald-600 scale-110' 
              : 'border-gray-300 bg-gray-50'
          }`}>
            3
          </div>
          <span className="ml-3 font-semibold text-sm sm:text-base hidden sm:inline">{t('seeds') || 'Seeds'}</span>
        </div>
      </div>

      {/* Step 1: Recommendations */}
      {currentStep === 1 && (
        <div>
          <h2 className="text-2xl font-bold mb-6 text-green-700">
            {t('cropRecommendations') || 'Crop Recommendations'}
          </h2>
          
          {/* Location Input Tabs */}
          <div className="mb-6">
            <div className="flex border-b border-gray-300 mb-4">
              <button
                className={`px-6 py-2 font-medium transition ${
                  locationMode === 'pincode'
                    ? 'text-green-600 border-b-2 border-green-600'
                    : 'text-gray-600 hover:text-green-600'
                }`}
                onClick={() => {
                  setLocationMode('pincode');
                  setLocationMessage(null);
                }}
              >
                📍 {t('pincode') || 'Pincode'}
              </button>
              <button
                className={`px-6 py-2 font-medium transition ${
                  locationMode === 'village'
                    ? 'text-green-600 border-b-2 border-green-600'
                    : 'text-gray-600 hover:text-green-600'
                }`}
                onClick={() => {
                  setLocationMode('village');
                  setLocationMessage(null);
                }}
              >
                🏘️ {t('village') || 'Village'}
              </button>
              <button
                className={`px-6 py-2 font-medium transition ${
                  locationMode === 'coordinates'
                    ? 'text-purple-600 border-b-2 border-purple-600'
                    : 'text-gray-600 hover:text-purple-600'
                }`}
                onClick={() => {
                  setLocationMode('coordinates');
                  setLocationMessage(null);
                }}
              >
                📍 {t('coordinates') || 'GPS Coordinates'}
              </button>
            </div>

            {/* Pincode Input Panel */}
            {locationMode === 'pincode' && (
              <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
                <label className="block text-sm font-medium mb-2 text-gray-700">
                  {t('enterPincode') || 'Enter Pincode'} (6 digits)
                </label>
                <input
                  type="text"
                  maxLength={6}
                  className="w-full px-4 py-2 border-2 border-blue-300 rounded-md focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                  placeholder="563130"
                  value={pincode}
                  onChange={(e) => handlePincodeInput(e.target.value)}
                />
                {locationMessage && (
                  <div className="mt-2 text-sm text-gray-700 bg-white p-2 rounded border border-gray-200">
                    {locationMessage}
                  </div>
                )}
                {village && taluk && district && (
                  <div className="mt-3 grid grid-cols-3 gap-2">
                    <div className="bg-white p-2 rounded border border-gray-200">
                      <span className="text-xs text-gray-500">Village:</span>
                      <div className="font-medium text-sm">{village}</div>
                    </div>
                    <div className="bg-white p-2 rounded border border-gray-200">
                      <span className="text-xs text-gray-500">Taluk:</span>
                      <div className="font-medium text-sm">{taluk}</div>
                    </div>
                    <div className="bg-white p-2 rounded border border-gray-200">
                      <span className="text-xs text-gray-500">District:</span>
                      <div className="font-medium text-sm">{district}</div>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Village Input Panel */}
            {locationMode === 'village' && (
              <div className="bg-green-50 p-4 rounded-lg border border-green-200">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="relative">
                    <label className="block text-sm font-medium mb-2 text-gray-700">
                      {t('village') || 'Village'} *
                    </label>
                    <input
                      type="text"
                      className="w-full px-4 py-2 border-2 border-green-300 rounded-md focus:border-green-500 focus:ring-2 focus:ring-green-200"
                      placeholder={t('enterVillage') || 'Type village name...'}
                      value={village}
                      onChange={(e) => handleVillageInput(e.target.value)}
                    />
                    {villageSuggestions.length > 0 && (
                      <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-y-auto">
                        {villageSuggestions.map((suggestion, idx) => (
                          <div
                            key={idx}
                            className="px-4 py-2 hover:bg-green-100 cursor-pointer text-sm"
                            onClick={() => handleVillageSuggestionClick(suggestion)}
                          >
                            {suggestion}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium mb-2 text-gray-700">
                      {t('taluk') || 'Taluk'} <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      required
                      className="w-full px-4 py-2 border-2 border-green-300 rounded-md focus:border-green-500 focus:ring-2 focus:ring-green-200"
                      placeholder={t('enterTaluk') || 'Enter taluk... (required)'}
                      value={taluk}
                      onChange={(e) => setTaluk(e.target.value)}
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium mb-2 text-gray-700">
                      {t('district') || 'District'} (optional)
                    </label>
                    <input
                      type="text"
                      className="w-full px-4 py-2 border-2 border-green-300 rounded-md focus:border-green-500 focus:ring-2 focus:ring-green-200"
                      placeholder={t('enterDistrict') || 'Enter district...'}
                      value={district}
                      onChange={(e) => setDistrict(e.target.value)}
                    />
                  </div>
                </div>
                {locationMessage && (
                  <div className="mt-3 text-sm text-gray-700 bg-white p-2 rounded border border-gray-200">
                    {locationMessage}
                  </div>
                )}
                <div className="mt-3 text-xs text-gray-600">
                  💡 {t('villageHint') || 'Start typing to see suggestions. Typos will be auto-corrected!'}
                </div>
              </div>
            )}

            {/* GPS Coordinates Input Panel */}
            {locationMode === 'coordinates' && (
              <div className="bg-purple-50 p-4 rounded-lg border border-purple-200">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-2 text-gray-700">
                      {t('latitude') || 'Latitude'} *
                    </label>
                    <input
                      type="number"
                      step="0.0001"
                      className="w-full px-4 py-2 border-2 border-purple-300 rounded-md focus:border-purple-500 focus:ring-2 focus:ring-purple-200"
                      placeholder="13.0804"
                      value={latitude}
                      onChange={(e) => setLatitude(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-2 text-gray-700">
                      {t('longitude') || 'Longitude'} *
                    </label>
                    <input
                      type="number"
                      step="0.0001"
                      className="w-full px-4 py-2 border-2 border-purple-300 rounded-md focus:border-purple-500 focus:ring-2 focus:ring-purple-200"
                      placeholder="77.5526"
                      value={longitude}
                      onChange={(e) => setLongitude(e.target.value)}
                    />
                  </div>
                </div>
                <div className="mt-3 text-xs text-gray-600 bg-white p-2 rounded border border-gray-200">
                  💡 {t('coordinatesHint') || 'Enter GPS coordinates. District will be auto-detected for market prices.'}
                </div>
              </div>
            )}
          </div>

          {/* Other Input Fields */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            <div>
              <label className="block text-sm font-medium mb-2">
                {t('season') || 'Season'} <span className="text-xs text-green-600">({t('autoDetected') || 'Auto-detected'})</span>
              </label>
              <select
                className="w-full px-3 py-2 border rounded-md bg-green-50"
                value={season}
                onChange={(e) => setSeason(e.target.value)}
              >
                <option value="KHARIF">{t('kharif') || 'Kharif'} (Jun-Oct)</option>
                <option value="RABI">{t('rabi') || 'Rabi'} (Nov-Feb)</option>
                <option value="ZAID">{t('zaid') || 'Zaid'} (Mar-May)</option>
              </select>
              <div className="mt-1 text-xs text-gray-500">
                ℹ️ {season === 'KHARIF' ? 'Monsoon season' : season === 'RABI' ? 'Winter season' : 'Summer season'}
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium mb-2">
                {t('farmSize') || 'Farm Size'}
              </label>
              <select
                className="w-full px-3 py-2 border rounded-md"
                value={farmSize}
                onChange={(e) => setFarmSize(e.target.value)}
              >
                <option value="">{t('selectFarmSize') || 'Select Size'}</option>
                <option value="SMALL">{t('small') || 'Small'} (&lt; 2 acres)</option>
                <option value="MEDIUM">{t('medium') || 'Medium'} (2-10 acres)</option>
                <option value="LARGE">{t('large') || 'Large'} (&gt; 10 acres)</option>
              </select>
            </div>
          </div>

          <button
            className="w-full px-4 py-3 bg-green-600 text-white rounded-md hover:bg-green-700 transition font-semibold disabled:bg-gray-400 disabled:cursor-not-allowed"
            onClick={fetchRecommendations}
            disabled={loading}
          >
            {loading ? t('loading') || 'Loading...' : t('getRecommendations') || 'Get Recommendations'}
          </button>

          {error && (
            <div className="mt-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
              {error}
            </div>
          )}

          {recommendations.length > 0 && (
            <div className="mt-6">
              {/* Explain Toggle */}
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-semibold text-gray-800">
                  {t('recommendedCrops') || 'Recommended Crops'}:
                </h3>
                <button
                  onClick={() => setShowExplanation(!showExplanation)}
                  className="px-4 py-2 bg-blue-100 text-blue-700 rounded-md hover:bg-blue-200 transition text-sm font-medium"
                >
                  {showExplanation 
                    ? (t('hideExplanation') || '🔽 Hide Explanation') 
                    : (t('explainRecommendation') || '🔼 Explain my recommendation')}
                </button>
              </div>

              <div className="space-y-4">
                {recommendations.map((rec, index) => (
                  <div
                    key={index}
                    className="border-2 border-gray-200 rounded-lg p-5 hover:shadow-lg transition cursor-pointer hover:border-green-500"
                    onClick={() => handleCropSelection(rec.crop)}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <h4 className="text-xl font-bold text-green-700">{rec.crop}</h4>
                      <span className="bg-green-100 text-green-800 px-4 py-2 rounded-full text-sm font-bold">
                        {rec.score}/100
                      </span>
                    </div>
                    
                    {showExplanation && (
                      <p className="text-gray-700 leading-relaxed mt-3 bg-blue-50 p-3 rounded-md">
                        <strong className="text-blue-700">{t('why') || 'Why'}:</strong> {rec.reason}
                      </p>
                    )}
                    
                    {/* Cost-Profit Calculator Toggle */}
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        const calc = calculateCostProfit(rec.crop);
                        setCostProfit(costProfit?.crop === rec.crop ? null : calc);
                      }}
                      className="mt-3 px-3 py-1 text-sm bg-green-100 text-green-700 rounded-md hover:bg-green-200 transition"
                    >
                      {costProfit?.crop === rec.crop ? '📊 Hide' : '📊 Show'} {t('costProfit') || 'Cost & Profit'}
                    </button>
                    
                    {costProfit?.crop === rec.crop && (
                      <div className="mt-3 p-4 bg-green-50 rounded-md border-l-4 border-green-500">
                        <h5 className="font-bold text-green-800 mb-2">{t('estimatedCostProfit') || 'Cost-Profit (per acre)'}</h5>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                          <div><strong>{t('seeds') || 'Seeds'}:</strong> ₹{costProfit.costs.seed}</div>
                          <div><strong>{t('fertilizer') || 'Fertilizer'}:</strong> ₹{costProfit.costs.fertilizer}</div>
                          <div><strong>{t('labor') || 'Labor'}:</strong> ₹{costProfit.costs.labor}</div>
                          <div><strong>{t('other') || 'Other'}:</strong> ₹{costProfit.costs.other}</div>
                          <div className="col-span-2 border-t pt-2 font-bold">
                            <strong>{t('totalCost') || 'Total Cost'}:</strong> ₹{costProfit.totalCost}
                          </div>
                          <div className="col-span-2 text-blue-700">
                            <strong>{t('revenue') || 'Revenue'}:</strong> ₹{costProfit.revenue}
                          </div>
                          <div className="col-span-2 text-green-700 font-bold text-lg">
                            <strong>{t('profit') || 'Profit'}:</strong> ₹{costProfit.profit} ({costProfit.profitMargin}%)
                          </div>
                        </div>
                      </div>
                    )}
                    
                    <div className="mt-3 text-sm text-gray-500 flex items-center">
                      <span className="mr-2">👆</span>
                      <span>{t('clickToSelectCrop') || 'Click to select this crop and see fertilizer recommendations'}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Step 2: Fertilizer Recommendations */}
      {currentStep === 2 && (
        <div>
          <h2 className="text-2xl font-bold mb-6 text-green-700">
            {t('fertilizerFor') || 'Fertilizer for'} {selectedCrop}
          </h2>

          <div className="bg-green-50 border-l-4 border-green-600 p-4 mb-6">
            <p className="text-green-800">
              <strong>{t('selectedCrop') || 'Selected Crop'}:</strong> {selectedCrop}
            </p>
            <p className="text-sm text-green-700 mt-1">
              {t('basedOnSoilMarket') || 'Based on your soil and market analysis'}
            </p>
          </div>

          <div className="space-y-4 mb-6">
            {fertilizerRecs.map((fert, index) => (
              <div key={index} className="border border-gray-200 rounded-lg p-5 bg-white shadow-md hover:shadow-lg transition">
                <div className="flex justify-between items-start mb-3">
                  <h4 className="text-lg font-bold text-gray-800">{fert.type}</h4>
                  {fert.npkRatio && (
                    <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-semibold">
                      NPK: {fert.npkRatio}
                    </span>
                  )}
                </div>
                <div className="space-y-2">
                  <p className="text-gray-700">
                    <strong>{t('quantity') || 'Quantity'}:</strong> {fert.quantity}
                  </p>
                  <p className="text-gray-700">
                    <strong>{t('timing') || 'Timing'}:</strong> {fert.timing}
                  </p>
                  {fert.schedule && (
                    <p className="text-gray-700">
                      <strong>{t('schedule') || 'Schedule'}:</strong> {fert.schedule}
                    </p>
                  )}
                  {fert.justification && (
                    <div className="mt-3 p-3 bg-blue-50 rounded-md border-l-4 border-blue-500">
                      <p className="text-sm text-blue-900">
                        <strong className="text-blue-700">{t('justification') || 'Why'}:</strong>{' '}
                        {fert.justification}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          <div className="flex gap-3">
            <button
              onClick={() => setCurrentStep(1)}
              className="px-6 py-3 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 transition font-semibold"
            >
              ← {t('back') || 'Back'}
            </button>
            <button
              onClick={proceedToSeedDealers}
              className="flex-1 px-6 py-3 bg-green-600 text-white rounded-md hover:bg-green-700 transition font-semibold"
            >
              {t('nextSeeds') || 'Next: Seed Dealers'} →
            </button>
          </div>
        </div>
      )}

      {/* Step 3: Seed Dealers */}
      {currentStep === 3 && (
        <div>
          <h2 className="text-2xl font-bold mb-6 text-green-700">
            {t('seedDealersFor') || 'Seed Dealers for'} {selectedCrop}
          </h2>

          <div className="bg-blue-50 border-l-4 border-blue-600 p-4 mb-6">
            <p className="text-blue-800">
              <strong>{t('district') || 'District'}:</strong> {district}
            </p>
            <p className="text-sm text-blue-700 mt-1">
              {t('contactBeforePurchase') || 'Please contact dealers before visiting'}
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
            {seedDealers.map((dealer, index) => (
              <div key={index} className="bg-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all duration-300 hover:scale-105 border-2 border-green-100">
                {/* Dealer Image */}
                <div className="relative mb-4">
                  <div className="bg-gradient-to-br from-green-100 to-emerald-100 rounded-lg p-4 flex items-center justify-center h-32">
                    <span className="text-6xl">🏪</span>
                  </div>
                  <div className="absolute top-2 right-2 bg-green-500 text-white px-3 py-1 rounded-full text-xs font-bold shadow-md">
                    Top {index + 1}
                  </div>
                </div>
                
                {/* Dealer Name */}
                <h4 className="font-bold text-xl text-gray-900 mb-2 leading-tight">{dealer.name}</h4>
                
                {/* Location */}
                <div className="flex items-center text-sm text-gray-600 mb-3">
                  <span className="mr-2">📍</span>
                  <span>{dealer.city}, {dealer.district}</span>
                </div>
                
                {/* Contact */}
                <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-3 rounded-lg mb-4 border border-blue-200">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-gray-600">Contact</span>
                    <a href={`tel:${dealer.contact}`} className="font-bold text-blue-600 hover:text-blue-800 transition-colors">
                      ☎️ {dealer.contact}
                    </a>
                  </div>
                </div>
                
                {/* Seed Categories */}
                {dealer.availableSeeds && dealer.availableSeeds.length > 0 && (
                  <div>
                    <div className="flex items-center mb-3">
                      <span className="text-xs font-bold text-gray-700 uppercase tracking-wide">Available Seeds</span>
                    </div>
                    <div className="space-y-2">
                      {dealer.availableSeeds.slice(0, 3).map((seed: string, sidx: number) => {
                        const cropName = seed.split(' - ')[0];
                        const variety = seed.split(' - ')[1] || '';
                        const price = dealer.seedPrices?.[cropName];
                        return (
                          <div key={sidx} className="bg-gradient-to-r from-green-50 to-emerald-50 p-3 rounded-lg border border-green-200">
                            <div className="flex items-start justify-between mb-1">
                              <div className="flex-1">
                                <div className="font-bold text-gray-800 text-sm">{cropName}</div>
                                {variety && <div className="text-xs text-gray-600 mt-0.5">{variety}</div>}
                              </div>
                              {price && (
                                <div className="text-green-600 font-bold text-sm ml-2">{price}</div>
                              )}
                            </div>
                            <div className="text-xs text-gray-500">Category: Seeds</div>
                          </div>
                        );
                      })}
                      {dealer.availableSeeds.length > 3 && (
                        <div className="text-center pt-2">
                          <span className="text-xs text-gray-500 bg-gray-100 px-3 py-1 rounded-full">
                            +{dealer.availableSeeds.length - 3} more varieties
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>

          <div className="flex gap-3">
            <button
              onClick={() => setCurrentStep(2)}
              className="px-6 py-3 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 transition font-semibold"
            >
              ← {t('back') || 'Back'}
            </button>
            <button
              onClick={exportPDF}
              className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition font-semibold"
            >
              📄 {t('exportPDF') || 'Export PDF Report'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
