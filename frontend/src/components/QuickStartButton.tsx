import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';

interface QuickStartProps {
  onComplete: (data: any) => void;
}

const QuickStartButton: React.FC<QuickStartProps> = ({ onComplete }) => {
  const { t, i18n } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleQuickStart = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Step 1: Get Location
      setStatus(t('autoDetecting'));
      const location = await getLocation();
      
      // Step 2: Get District from coordinates
      const district = await getDistrictFromCoords(location.lat, location.lon);
      
      // Step 3: Fetch Soil Data
      setStatus(t('fetchingSoil'));
      const soilData = await fetchSoilData(location.lat, location.lon);
      
      // Step 4: Fetch Weather Data
      setStatus(t('fetchingWeather'));
      const weatherData = await fetchWeatherData(location.lat, location.lon);
      
      // Step 5: Analyze Market (already loaded on backend)
      setStatus(t('analyzingMarket'));
      
      // Step 6: Generate Recommendations
      setStatus(t('generatingRecommendations'));
      const recommendationsData = await fetchRecommendations(district);
      const recommendations = recommendationsData.recommendations || [];
      
      // Step 7: Fetch Fertilizers for top crop
      setStatus('Fetching fertilizers...');
      const topCrop = recommendations[0]?.crop || 'RICE';
      const fertilizers = await fetchFertilizers(topCrop, district);
      
      // Step 8: Fetch Seed Dealers
      setStatus('Finding seed dealers...');
      const dealers = await fetchDealers(topCrop, district);
      
      // Step 9: Complete - pass all data to parent
      const completeData = {
        location,
        district,
        soil: soilData,
        weather: weatherData,
        recommendations,
        topCrop,
        fertilizers,
        dealers
      };
      
      onComplete(completeData);
      
    } catch (err: any) {
      console.error('Quick Start Error:', err);
      setError(err.message || 'Failed to auto-detect. Please use manual entry.');
    } finally {
      setLoading(false);
      setStatus('');
    }
  };

  const getLocation = (): Promise<{ lat: number; lon: number }> => {
    return new Promise((resolve) => {
      // Always resolve, never reject - use fallbacks
      const timeout = setTimeout(() => {
        // Default to Bangalore if everything times out
        resolve({ lat: 12.9716, lon: 77.5946 });
      }, 5000);

      if (!navigator.geolocation) {
        // Fallback to IP-based location
        fetch('https://ipapi.co/json/')
          .then(res => res.json())
          .then(data => {
            clearTimeout(timeout);
            resolve({ lat: data.latitude, lon: data.longitude });
          })
          .catch(() => {
            clearTimeout(timeout);
            resolve({ lat: 12.9716, lon: 77.5946 });
          });
        return;
      }
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          clearTimeout(timeout);
          resolve({
            lat: position.coords.latitude,
            lon: position.coords.longitude
          });
        },
        () => {
          // Fallback to IP-based location
          fetch('https://ipapi.co/json/')
            .then(res => res.json())
            .then(data => {
              clearTimeout(timeout);
              resolve({ lat: data.latitude, lon: data.longitude });
            })
            .catch(() => {
              clearTimeout(timeout);
              resolve({ lat: 12.9716, lon: 77.5946 });
            });
        },
        { timeout: 5000 }
      );
    });
  };

  const getDistrictFromCoords = async (lat: number, lon: number): Promise<string> => {
    try {
      // Simple Karnataka district mapping based on coordinates
      const districtMap = [
        { name: 'BANGALORE', lat: [12.7, 13.2], lon: [77.3, 77.9] },
        { name: 'MYSORE', lat: [12.0, 12.6], lon: [76.3, 77.0] },
        { name: 'MANGALORE', lat: [12.7, 13.2], lon: [74.7, 75.2] },
        { name: 'HUBLI', lat: [15.2, 15.5], lon: [74.9, 75.3] },
        { name: 'BELGAUM', lat: [15.7, 16.0], lon: [74.3, 74.7] },
        { name: 'TUMKUR', lat: [13.2, 13.6], lon: [76.8, 77.3] },
        { name: 'MANDYA', lat: [12.4, 12.8], lon: [76.7, 77.2] }
      ];

      // Check coordinate ranges - instant lookup
      for (const district of districtMap) {
        if (lat >= district.lat[0] && lat <= district.lat[1] && 
            lon >= district.lon[0] && lon <= district.lon[1]) {
          return district.name;
        }
      }

      // Try Nominatim as fallback (with timeout)
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000); // Increased to 5s
      
      try {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`,
          { 
            signal: controller.signal,
            headers: { 'User-Agent': 'CropPlanner/1.0' }
          }
        );
        clearTimeout(timeoutId);
        
        const data = await response.json();
        const district = data.address?.state_district || 
                        data.address?.county || 
                        data.address?.city || 
                        'BANGALORE';
        
        return district.toUpperCase().replace(' DISTRICT', '').replace(' URBAN', '').replace(' RURAL', '');
      } catch (nominatimErr) {
        clearTimeout(timeoutId);
        console.warn('Nominatim failed, using default:', nominatimErr);
        return 'BANGALORE';
      }
    } catch (err) {
      console.error('District detection error:', err);
      return 'BANGALORE';
    }
  };

  const fetchSoilData = async (lat: number, lon: number) => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 15000); // 15s timeout
    
    try {
      const response = await fetch(`/api/soil?lat=${lat}&lon=${lon}`, {
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      if (!response.ok) throw new Error('Soil data fetch failed');
      return response.json();
    } catch (err: any) {
      clearTimeout(timeoutId);
      if (err.name === 'AbortError') {
        throw new Error('Soil data request timed out');
      }
      throw err;
    }
  };

  const fetchWeatherData = async (lat: number, lon: number) => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 15000); // 15s timeout
    
    try {
      const response = await fetch(`/api/weather?lat=${lat}&lon=${lon}`, {
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      if (!response.ok) throw new Error('Weather data fetch failed');
      return response.json();
    } catch (err: any) {
      clearTimeout(timeoutId);
      if (err.name === 'AbortError') {
        throw new Error('Weather data request timed out');
      }
      throw err;
    }
  };

  const fetchRecommendations = async (district: string) => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 45000); // 45s timeout for AI
    
    try {
      const currentLanguage = i18n.language || 'en';
      const response = await fetch('/api/recommendations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          district,
          language: currentLanguage
        }),
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Recommendations failed: ${errorText}`);
      }
      return response.json();
    } catch (err: any) {
      clearTimeout(timeoutId);
      if (err.name === 'AbortError') {
        throw new Error('AI recommendation request timed out. Please try again.');
      }
      throw err;
    }
  };

  const fetchFertilizers = async (crop: string, district: string) => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 15000);
    
    try {
      const response = await fetch('/api/fertilizer/recommendations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ crop, district }),
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      if (!response.ok) throw new Error('Fertilizer fetch failed');
      const data = await response.json();
      return data.recommendations || [];
    } catch (err: any) {
      clearTimeout(timeoutId);
      console.warn('Fertilizer fetch error:', err);
      return [];
    }
  };

  const fetchDealers = async (crop: string, district: string) => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 15000);
    
    try {
      const params = new URLSearchParams();
      params.append('district', district);
      params.append('crop', crop);
      
      const response = await fetch(`/api/seeds/dealers?${params.toString()}`, {
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      if (!response.ok) throw new Error('Dealer fetch failed');
      const data = await response.json();
      return data.dealers || [];
    } catch (err: any) {
      clearTimeout(timeoutId);
      console.warn('Dealer fetch error:', err);
      return [];
    }
  };

  return (
    <div className="mb-8">
      <button
        onClick={handleQuickStart}
        disabled={loading}
        className="w-full sm:w-auto mx-auto block px-8 py-4 bg-gradient-to-r from-green-600 to-green-700 hover:from-green-700 hover:to-green-800 text-white text-lg sm:text-xl font-bold rounded-xl shadow-lg hover:shadow-2xl transform hover:scale-105 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
      >
        {loading ? (
          <span className="flex items-center justify-center">
            <svg className="animate-spin h-6 w-6 mr-3" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
            </svg>
            {status}
          </span>
        ) : (
          <>
            <span className="block sm:inline">{t('quickStart')}</span>
            <span className="block sm:inline sm:ml-2 text-sm font-normal opacity-90">
              {t('quickStartDesc')}
            </span>
          </>
        )}
      </button>
      
      {error && (
        <div className="mt-4 bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}
    </div>
  );
};

export default QuickStartButton;
