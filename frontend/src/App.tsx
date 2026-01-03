import React, { useState } from 'react';
import Navbar from './components/Navbar';
import { useTranslation } from 'react-i18next';
import WeatherPanel from './components/WeatherPanel';
import SoilPanel from './components/SoilPanel';
import RecommendationWizard from './components/RecommendationWizard';
import QuickStartButton from './components/QuickStartButton';
import SmartTips from './components/SmartTips';
import AboutSection from './components/AboutSection';
import { generateEnhancedPDF } from './components/EnhancedPDFGeneratorNew';
import { getCropInfo, LABOR_COST_PER_ACRE } from './utils/cropData';

function App() {
  const { t, i18n } = useTranslation();
  const [quickStartData, setQuickStartData] = useState<any>(null);
  const [showQuickResults, setShowQuickResults] = useState(false);
  const [profitData, setProfitData] = useState<any>(null);
  const [loadingProfit, setLoadingProfit] = useState(false);
  const [selectedCropForFertilizer, setSelectedCropForFertilizer] = useState<string | null>(null);
  const [fertilizerData, setFertilizerData] = useState<any>(null);
  const [loadingFertilizer, setLoadingFertilizer] = useState(false);

  // Generate Enhanced PDF with Kannada support and crop images
  const generateQuickStartPDF = (data: any) => {
    generateEnhancedPDF(data);
  };

  const handleQuickStartComplete = (data: any) => {
    // Add current language to data
    const dataWithLanguage = {
      ...data,
      language: i18n.language || 'en'
    };
    
    setQuickStartData(dataWithLanguage);
    setShowQuickResults(true);
    
    // Auto-download PDF with all data including language
    setTimeout(() => {
      generateQuickStartPDF(dataWithLanguage);
    }, 500);
    
    // Scroll to results
    window.scrollTo({ top: 400, behavior: 'smooth' });
  };

  // Calculate profit estimate for a crop with realistic yield data
  const calculateProfit = async (crop: string, district: string) => {
    setLoadingProfit(true);
    setProfitData(null);
    
    try {
      // Fetch live market price
      const priceResponse = await fetch(
        `http://localhost:8080/api/market/live-price?crop=${encodeURIComponent(crop)}&district=${encodeURIComponent(district)}`
      );
      const priceData = await priceResponse.json();
      
      // Get crop-specific yield data
      const cropInfo = getCropInfo(crop);
      const avgYield = cropInfo.avgYieldPerAcre;
      const minYield = cropInfo.minYield;
      const maxYield = cropInfo.maxYield;
      const costPerAcre = LABOR_COST_PER_ACRE;
      const marketPrice = priceData.modalPrice || 2500;
      
      const revenue = avgYield * marketPrice;
      const minRevenue = minYield * marketPrice;
      const maxRevenue = maxYield * marketPrice;
      const profit = revenue - costPerAcre;
      const minProfit = minRevenue - costPerAcre;
      const maxProfit = maxRevenue - costPerAcre;
      const profitMargin = ((profit / revenue) * 100).toFixed(1);
      
      setProfitData({
        crop,
        cropInfo,
        district,
        marketPrice,
        priceSource: priceData.source,
        avgYield,
        minYield,
        maxYield,
        costPerAcre,
        revenue,
        minRevenue,
        maxRevenue,
        profit,
        minProfit,
        maxProfit,
        profitMargin,
        cached: priceData.cached
      });
      
      // Scroll to profit display
      setTimeout(() => {
        document.getElementById('profit-estimate')?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
      
    } catch (error) {
      console.error('Error calculating profit:', error);
      alert('Failed to calculate profit. Please try again.');
    } finally {
      setLoadingProfit(false);
    }
  };

  // Fetch fertilizer recommendations for selected crop
  const fetchFertilizerForCrop = async (crop: string, district: string) => {
    setLoadingFertilizer(true);
    setSelectedCropForFertilizer(crop);
    try {
      const response = await fetch('/api/fertilizer/recommendations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ crop, district })
      });
      if (!response.ok) throw new Error('Fertilizer fetch failed');
      const data = await response.json();
      setFertilizerData({
        crop,
        recommendations: data.recommendations || []
      });
      // Scroll to fertilizer section
      setTimeout(() => {
        document.getElementById('fertilizer-section')?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } catch (error) {
      console.error('Error fetching fertilizer:', error);
      setFertilizerData(null);
    } finally {
      setLoadingFertilizer(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-emerald-50 via-blue-50 to-teal-50">
      <Navbar />
      <main className="flex-1 container mx-auto px-3 sm:px-4 md:px-6 lg:px-8 py-6 sm:py-8 md:py-10">
        {/* Hero Section - Enhanced */}
        <div className="text-center mb-8 sm:mb-12 animate-fadeIn">
          <div className="inline-block mb-4">
            <span className="text-5xl sm:text-6xl">🌾</span>
          </div>
          <h1 className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-extrabold bg-gradient-to-r from-emerald-600 via-green-600 to-teal-600 bg-clip-text text-transparent mb-4 sm:mb-5 leading-tight">
            Crop Planner
          </h1>
          <p className="text-xl sm:text-2xl md:text-3xl text-green-700 font-bold mb-3 tracking-wide">
            {t('tagline')}
          </p>
          <p className="text-base sm:text-lg text-gray-600 max-w-3xl mx-auto px-4 leading-relaxed">
            🌱 AI-powered, village-level crop recommendations for smart farming
          </p>
        </div>

        {/* Quick Start Button */}
        <QuickStartButton onComplete={handleQuickStartComplete} />

        {/* Smart Tips Banner */}
        <SmartTips />

        {/* Quick Results Display - Enhanced Modern Design */}
        {showQuickResults && quickStartData && (
          <div className="mb-10 bg-white rounded-2xl shadow-2xl overflow-hidden border border-green-100 animate-fadeIn">
            {/* Premium Header */}
            <div className="bg-gradient-to-r from-emerald-600 to-green-600 px-6 sm:px-8 py-5">
              <h2 className="text-2xl sm:text-3xl font-bold text-white flex items-center">
                <span className="mr-3 text-3xl">✨</span>
                <span className="flex-1">Quick Start Results</span>
                <span className="text-sm font-normal bg-white/20 px-3 py-1 rounded-full backdrop-blur-sm">
                  PDF Downloaded ✓
                </span>
              </h2>
            </div>

            <div className="p-6 sm:p-8">
              {/* Location Info - Enhanced */}
              <div className="bg-gradient-to-r from-blue-50 to-cyan-50 p-5 rounded-xl mb-6 border border-blue-100 shadow-sm">
                <div className="flex items-center text-sm text-gray-700">
                  <span className="text-2xl mr-3">📍</span>
                  <div>
                    <p className="font-semibold text-base text-gray-800 mb-1">
                      {quickStartData.district} District
                    </p>
                    <p className="text-xs text-gray-600">
                      Coordinates: {quickStartData.location.lat.toFixed(4)}, {quickStartData.location.lon.toFixed(4)}
                    </p>
                  </div>
                </div>
              </div>

              {/* Soil & Weather Summary - Enhanced Cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5 mb-8">
                {/* Soil Card */}
                <div className="bg-gradient-to-br from-amber-50 to-orange-50 p-6 rounded-xl border border-amber-200 shadow-md hover:shadow-lg transition-shadow duration-300">
                  <div className="flex items-center mb-4">
                    <span className="text-3xl mr-3">🌱</span>
                    <h4 className="font-bold text-lg text-amber-900">Soil Analysis</h4>
                  </div>
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                      <span className="text-gray-700 font-medium">Clay</span>
                      <span className="font-bold text-amber-800">{quickStartData.soil?.clay?.toFixed(1)}%</span>
                    </div>
                    <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                      <span className="text-gray-700 font-medium">Sand</span>
                      <span className="font-bold text-amber-800">{quickStartData.soil?.sand?.toFixed(1)}%</span>
                    </div>
                    <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                      <span className="text-gray-700 font-medium">pH Level</span>
                      <span className="font-bold text-amber-800">{quickStartData.soil?.phh2o?.toFixed(2)}</span>
                    </div>
                    {quickStartData.soil?.source && (
                      <div className="mt-3 pt-3 border-t border-amber-200">
                        <span className="inline-block bg-emerald-100 text-emerald-700 px-3 py-1 rounded-full text-xs font-semibold">
                          📊 {quickStartData.soil.source}
                        </span>
                      </div>
                    )}
                  </div>
                </div>

                {/* Weather Card */}
                <div className="bg-gradient-to-br from-blue-50 to-cyan-50 p-6 rounded-xl border border-blue-200 shadow-md hover:shadow-lg transition-shadow duration-300">
                  <div className="flex items-center mb-4">
                    <span className="text-3xl mr-3">☁️</span>
                    <h4 className="font-bold text-lg text-blue-900">Weather Conditions</h4>
                  </div>
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                      <span className="text-gray-700 font-medium">Location</span>
                      <span className="font-bold text-blue-800">{quickStartData.weather?.name}</span>
                    </div>
                    <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                      <span className="text-gray-700 font-medium">Temperature</span>
                      <span className="font-bold text-blue-800">{quickStartData.weather?.main?.temp}°C</span>
                    </div>
                    <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                      <span className="text-gray-700 font-medium">Humidity</span>
                      <span className="font-bold text-blue-800">{quickStartData.weather?.main?.humidity}%</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Crop Recommendations - Enhanced Modern Cards */}
              {quickStartData.recommendations && quickStartData.recommendations.length > 0 && (
                <div className="space-y-5 mb-8">
                  <h3 className="text-2xl font-bold text-gray-800 flex items-center">
                    <span className="text-3xl mr-3">🌾</span>
                    Recommended Crops
                  </h3>
                  {quickStartData.recommendations.map((rec: any, idx: number) => (
                    <div key={idx} className="bg-gradient-to-r from-emerald-50 to-green-50 p-6 rounded-xl border-l-4 border-emerald-500 shadow-md hover:shadow-xl transition-all duration-300 hover:scale-[1.02]">
                      <div className="flex justify-between items-start mb-4">
                        <div className="flex-1 pr-4">
                          <div className="flex items-center mb-2">
                            <span className="text-2xl mr-2">🌱</span>
                            <h4 className="text-xl font-bold text-emerald-700">{rec.crop}</h4>
                          </div>
                          <p className="text-gray-700 leading-relaxed text-sm sm:text-base">{rec.reason}</p>
                        </div>
                        <div className="flex-shrink-0">
                          <div className="bg-gradient-to-r from-emerald-600 to-green-600 text-white px-4 py-2 rounded-full text-sm font-bold shadow-md">
                            {rec.score || rec.confidence}% match
                          </div>
                        </div>
                      </div>
                      <div className="mt-4 pt-4 border-t border-emerald-200 flex gap-3">
                        <button
                          onClick={() => calculateProfit(rec.crop, quickStartData.district)}
                          className="flex-1 inline-flex items-center justify-center bg-gradient-to-r from-blue-500 to-blue-600 text-white px-4 py-2.5 rounded-lg font-semibold hover:from-blue-600 hover:to-blue-700 transition-all shadow-md hover:shadow-lg transform hover:scale-105"
                        >
                          <span className="mr-2">💰</span>
                          Profit Estimate
                        </button>
                        <button
                          onClick={() => fetchFertilizerForCrop(rec.crop, quickStartData.district)}
                          className="flex-1 inline-flex items-center justify-center bg-gradient-to-r from-purple-500 to-purple-600 text-white px-4 py-2.5 rounded-lg font-semibold hover:from-purple-600 hover:to-purple-700 transition-all shadow-md hover:shadow-lg transform hover:scale-105"
                        >
                          <span className="mr-2">🧪</span>
                          View Fertilizer
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Fertilizer Recommendations - Dynamic based on selected crop */}
              {fertilizerData && fertilizerData.recommendations && fertilizerData.recommendations.length > 0 && (
                <div id="fertilizer-section" className="mb-8">
                  <h3 className="text-2xl font-bold text-gray-800 mb-5 flex items-center">
                    <span className="text-3xl mr-3">🧪</span>
                    Fertilizer Plan for {fertilizerData.crop}
                  </h3>
                  {loadingFertilizer && (
                    <div className="text-center py-8">
                      <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
                      <p className="mt-3 text-gray-600">Loading fertilizer recommendations...</p>
                    </div>
                  )}
                  {!loadingFertilizer && (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                      {fertilizerData.recommendations.map((fert: any, idx: number) => (
                        <div key={idx} className="bg-gradient-to-br from-purple-50 to-pink-50 p-6 rounded-xl border border-purple-200 shadow-md hover:shadow-lg transition-all duration-300">
                          <div className="flex items-center mb-4 pb-3 border-b border-purple-200">
                            <span className="text-2xl mr-3">⚗️</span>
                            <h4 className="font-bold text-lg text-purple-800">{fert.type}</h4>
                          </div>
                          <div className="space-y-3 text-sm">
                            <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                              <span className="text-gray-700 font-medium">Quantity</span>
                              <span className="font-bold text-purple-700">{fert.quantity}</span>
                            </div>
                            <div className="flex justify-between items-center bg-white/60 px-3 py-2 rounded-lg">
                              <span className="text-gray-700 font-medium">Timing</span>
                              <span className="font-bold text-purple-700">{fert.timing}</span>
                            </div>
                            {fert.npkRatio && (
                              <div className="bg-gradient-to-r from-purple-100 to-pink-100 px-3 py-2 rounded-lg">
                                <span className="text-gray-700 font-medium mr-2">NPK Ratio:</span>
                                <span className="font-bold text-purple-800">{fert.npkRatio}</span>
                              </div>
                            )}
                            {fert.schedule && (
                              <div className="mt-3 pt-3 border-t border-purple-100">
                                <p className="text-xs text-gray-600 leading-relaxed">{fert.schedule}</p>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Seed Dealers - Fertilizer-style Panel with Images */}
              {quickStartData.dealers && quickStartData.dealers.length > 0 && (
                <div className="bg-gradient-to-br from-green-50 to-emerald-50 p-8 rounded-2xl shadow-xl border-2 border-green-200">
                  <h3 className="text-3xl font-bold text-gray-800 mb-6 flex items-center">
                    <span className="text-4xl mr-3">🌱</span>
                    Recommended Seed Dealers
                  </h3>
                  <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {quickStartData.dealers.slice(0, 3).map((dealer: any, idx: number) => (
                      <div key={idx} className="bg-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all duration-300 hover:scale-105 border-2 border-green-100">
                        {/* Dealer Image */}
                        <div className="relative mb-4">
                          <div className="bg-gradient-to-br from-green-100 to-emerald-100 rounded-lg p-4 flex items-center justify-center h-32">
                            <span className="text-6xl">🏪</span>
                          </div>
                          <div className="absolute top-2 right-2 bg-green-500 text-white px-3 py-1 rounded-full text-xs font-bold shadow-md">
                            Top {idx + 1}
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
                </div>
              )}
            </div>
          </div>
        )}

        {/* Profit Estimate Display - Enhanced Premium Design */}
        {profitData && (
          <div id="profit-estimate" className="mb-8 bg-white rounded-2xl shadow-2xl overflow-hidden border border-yellow-200">
            {/* Header with Gradient */}
            <div className="bg-gradient-to-r from-yellow-500 via-amber-500 to-orange-500 px-8 py-5">
              <h2 className="text-3xl font-extrabold text-white flex items-center">
                <span className="text-4xl mr-3">💰</span>
                Profit Estimate for {profitData.crop}
              </h2>
            </div>

            <div className="p-8">
              {/* Main Metrics Grid */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                {/* Market Price Card */}
                <div className="bg-gradient-to-br from-blue-50 to-cyan-50 p-6 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 border border-blue-200">
                  <div className="flex items-center mb-3">
                    <span className="text-3xl mr-2">💵</span>
                    <p className="text-sm font-semibold text-gray-700">Market Price</p>
                  </div>
                  <p className="text-3xl font-bold text-blue-600 mb-2">₹{profitData.marketPrice.toLocaleString()}</p>
                  <p className="text-sm text-gray-600 mb-3">per quintal</p>
                  <div className="bg-white/60 px-3 py-2 rounded-lg">
                    <p className="text-xs text-gray-600">
                      Source: <span className="font-semibold text-blue-700">{profitData.priceSource}</span>
                      {profitData.cached && <span className="ml-1 text-gray-500">(cached)</span>}
                    </p>
                  </div>
                </div>
                
                {/* Revenue Card */}
                <div className="bg-gradient-to-br from-emerald-50 to-green-50 p-6 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 border border-emerald-200">
                  <div className="flex items-center mb-3">
                    <span className="text-3xl mr-2">📈</span>
                    <p className="text-sm font-semibold text-gray-700">Expected Revenue</p>
                  </div>
                  <p className="text-3xl font-bold text-emerald-600 mb-2">₹{profitData.revenue.toLocaleString()}</p>
                  <p className="text-sm text-gray-600 mb-3">per acre (avg)</p>
                  <div className="space-y-1">
                    <div className="bg-white/60 px-3 py-1.5 rounded-lg">
                      <p className="text-xs text-gray-600">
                        Yield: <span className="font-bold text-emerald-700">{profitData.avgYield} quintals/acre</span>
                      </p>
                    </div>
                    <div className="bg-white/60 px-3 py-1.5 rounded-lg">
                      <p className="text-xs text-gray-500">
                        Range: {profitData.minYield}-{profitData.maxYield} quintals
                      </p>
                    </div>
                  </div>
                </div>
                
                {/* Net Profit Card */}
                <div className={`bg-gradient-to-br ${profitData.profit > 0 ? 'from-green-50 to-emerald-50 border-green-200' : 'from-red-50 to-orange-50 border-red-200'} p-6 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 border`}>
                  <div className="flex items-center mb-3">
                    <span className="text-3xl mr-2">💎</span>
                    <p className="text-sm font-semibold text-gray-700">Net Profit</p>
                  </div>
                  <p className={`text-3xl font-bold mb-2 ${profitData.profit > 0 ? 'text-green-600' : 'text-red-600'}`}>
                    ₹{profitData.profit.toLocaleString()}
                  </p>
                  <p className="text-sm text-gray-600 mb-3">per acre (avg)</p>
                  <div className="bg-white/60 px-3 py-2 rounded-lg">
                    <p className="text-xs text-gray-600">
                      Margin: <span className={`font-bold ${profitData.profit > 0 ? 'text-green-700' : 'text-red-700'}`}>{profitData.profitMargin}%</span>
                    </p>
                  </div>
                </div>
              </div>
              
              {/* Profit Range - Enhanced Visual Display */}
              <div className="bg-gradient-to-r from-green-50 via-emerald-50 to-teal-50 p-6 rounded-xl shadow-md border border-green-200">
                <div className="flex items-center mb-4">
                  <span className="text-2xl mr-2">📊</span>
                  <p className="text-base font-bold text-gray-800">Profit Range Estimate</p>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {/* Minimum */}
                  <div className="bg-white/80 p-4 rounded-lg text-center shadow-sm hover:shadow-md transition-shadow">
                    <p className="text-xs font-semibold text-gray-600 mb-2">Minimum</p>
                    <p className={`text-2xl font-bold mb-1 ${profitData.minProfit > 0 ? 'text-orange-600' : 'text-red-600'}`}>
                      ₹{profitData.minProfit.toLocaleString()}
                    </p>
                    <p className="text-xs text-gray-500">({profitData.minYield} quintals)</p>
                  </div>
                  
                  {/* Average */}
                  <div className="bg-gradient-to-br from-emerald-100 to-green-100 p-4 rounded-lg text-center shadow-md transform scale-105">
                    <p className="text-xs font-semibold text-gray-700 mb-2">Average ⭐</p>
                    <p className={`text-2xl font-bold mb-1 ${profitData.profit > 0 ? 'text-green-600' : 'text-red-600'}`}>
                      ₹{profitData.profit.toLocaleString()}
                    </p>
                    <p className="text-xs text-gray-600">({profitData.avgYield} quintals)</p>
                  </div>
                  
                  {/* Maximum */}
                  <div className="bg-white/80 p-4 rounded-lg text-center shadow-sm hover:shadow-md transition-shadow">
                    <p className="text-xs font-semibold text-gray-600 mb-2">Maximum</p>
                    <p className="text-2xl font-bold text-green-700 mb-1">
                      ₹{profitData.maxProfit.toLocaleString()}
                    </p>
                    <p className="text-xs text-gray-500">({profitData.maxYield} quintals)</p>
                  </div>
                </div>
              </div>
              
              {/* Cost Breakdown - Enhanced */}
              <div className="mt-6 bg-gradient-to-r from-amber-50 to-yellow-50 p-6 rounded-xl shadow-md border border-amber-200">
                <div className="flex items-center mb-4">
                  <span className="text-2xl mr-2">💡</span>
                  <p className="text-base font-bold text-gray-800">Cost Breakdown</p>
                </div>
                <div className="bg-white/80 p-4 rounded-lg mb-4">
                  <p className="text-sm text-gray-700 mb-2">
                    <span className="font-semibold text-amber-700">Investment:</span> ₹{profitData.costPerAcre.toLocaleString()} per acre
                  </p>
                  <p className="text-xs text-gray-600">Includes: Seeds, Fertilizer, Labor</p>
                </div>
                <div className="space-y-2">
                  <p className="text-xs text-gray-600 flex items-start">
                    <span className="mr-2">📌</span>
                    <span>Based on realistic yield data for {profitData.cropInfo.name} and current market prices in {profitData.district}</span>
                  </p>
                  <p className="text-xs text-gray-600 flex items-start">
                    <span className="mr-2">⚠️</span>
                    <span>Actual profits may vary based on farming practices, weather conditions, and market fluctuations</span>
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {loadingProfit && (
          <div className="mb-8 bg-white rounded-xl shadow-lg p-6 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p className="text-gray-600">Calculating profit estimate...</p>
          </div>
        )}
        
        <div className="space-y-6 sm:space-y-8">
          {/* Main Recommendation Wizard */}
          <div className="transform transition-all duration-300">
            <RecommendationWizard />
          </div>
          
          {/* Weather and Soil Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6 md:gap-8">
            <div className="transform transition-all duration-300 hover:scale-[1.02] hover:shadow-xl">
              <WeatherPanel />
            </div>
            <div className="transform transition-all duration-300 hover:scale-[1.02] hover:shadow-xl">
              <SoilPanel />
            </div>
          </div>
        </div>

        {/* About Section */}
        <AboutSection />
        
        {/* Helpline Footer */}
        <div className="mt-12 mb-8">
          <div className="bg-gradient-to-r from-green-50 to-emerald-50 p-6 rounded-xl shadow-md border border-green-200 text-center">
            <h3 className="text-lg font-bold text-gray-800 mb-3 flex items-center justify-center">
              <span className="text-2xl mr-2">📧</span>
              Need Help?
            </h3>
            <p className="text-gray-700 mb-2">Contact our helpline for support and guidance</p>
            <a href="mailto:support.neyogi.cropplanner@gmail.com" className="text-green-600 hover:text-green-800 font-bold text-lg transition-colors">
              support.neyogi.cropplanner@gmail.com
            </a>
            <p className="text-sm text-gray-500 mt-3">Available Monday - Saturday, 9 AM - 6 PM IST</p>
          </div>
        </div>
        
        {/* Footer */}
        <div className="mt-8 pt-8 border-t border-gray-200 text-center text-sm text-gray-600 pb-6">
          <p className="font-semibold text-gray-700 mb-2">© 2026 Neyogi - CropPlanner. Empowering Karnataka Farmers.</p>
          <p className="text-xs">{t('poweredBy')}</p>
        </div>
      </main>
    </div>
  );
}

export default App;
