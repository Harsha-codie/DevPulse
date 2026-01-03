import React, { useState } from 'react';

interface SoilData {
  clay: number;
  sand: number;
  silt: number;
  phh2o: number;
  source?: string; // Track data source from hybrid system
}

export default function SoilPanel() {
  const [soil, setSoil] = useState<SoilData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchSoil = (lat?: number, lon?: number) => {
    setLoading(true);
    setError(null);
    let url = '/api/soil';
    if (lat !== undefined && lon !== undefined) {
      url += `?lat=${lat}&lon=${lon}`;
    }
    
    // Add timeout to prevent hanging
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 20000); // 20 second timeout
    
    fetch(url, { signal: controller.signal })
      .then(async (res) => {
        clearTimeout(timeoutId);
        if (!res.ok) {
          const errorText = await res.text();
          throw new Error(errorText || 'Failed to fetch soil data');
        }
        return res.json();
      })
      .then((data) => {
        console.log('Soil data received:', data);
        setSoil(data);
      })
      .catch((err: any) => {
        clearTimeout(timeoutId);
        if (err.name === 'AbortError') {
          setError('Request timed out. The soil data service might be slow. Please try again.');
        } else {
          setError(err.message || 'Failed to fetch soil data');
        }
      })
      .finally(() => setLoading(false));
  };

  const handleGeolocate = () => {
    if (!navigator.geolocation) {
      setError('Geolocation not supported');
      return;
    }
    setLoading(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        fetchSoil(pos.coords.latitude, pos.coords.longitude);
      },
      (err) => {
        setError('Geolocation error: ' + err.message);
        setLoading(false);
      }
    );
  };

  return (
    <div className="bg-white rounded-xl shadow-lg border-t-4 border-amber-500 overflow-hidden hover:shadow-2xl transition-shadow duration-300">
      {/* Header */}
      <div className="bg-gradient-to-r from-amber-500 to-amber-600 p-4 sm:p-6">
        <div className="flex items-center space-x-3">
          <span className="text-3xl sm:text-4xl">🌱</span>
          <h2 className="text-xl sm:text-2xl font-bold text-white">Soil Analysis</h2>
        </div>
      </div>
      
      {/* Content */}
      <div className="p-4 sm:p-6">
        <div className="mb-4">
          <button
            className="w-full px-4 py-3 bg-gradient-to-r from-green-500 to-green-600 text-white rounded-lg hover:from-green-600 hover:to-green-700 transition-all duration-200 font-medium shadow-md text-sm sm:text-base"
            onClick={handleGeolocate}
            disabled={loading}
          >
            📍 Use My Location
          </button>
        </div>
        
        {loading && (
          <div className="text-center py-8">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-amber-500"></div>
            <p className="mt-3 text-gray-600">Analyzing soil...</p>
          </div>
        )}
        
        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
            <p className="text-red-700 text-sm">⚠️ {error}</p>
          </div>
        )}
        
        {soil && (
          <div className="bg-gradient-to-br from-amber-50 to-amber-100 rounded-lg p-4 sm:p-6 mt-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-gray-800 flex items-center text-base sm:text-lg">
                <span className="mr-2">📊</span>
                Composition
              </h3>
              {soil.source && (
                <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full font-medium">
                  {soil.source}
                </span>
              )}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-white rounded-lg p-3 shadow-sm">
                <div className="text-xs sm:text-sm text-gray-500 mb-1">Clay</div>
                <div className="text-lg sm:text-xl font-bold text-amber-700">{soil.clay.toFixed(1)}%</div>
              </div>
              <div className="bg-white rounded-lg p-3 shadow-sm">
                <div className="text-xs sm:text-sm text-gray-500 mb-1">Sand</div>
                <div className="text-lg sm:text-xl font-bold text-amber-700">{soil.sand.toFixed(1)}%</div>
              </div>
              <div className="bg-white rounded-lg p-3 shadow-sm">
                <div className="text-xs sm:text-sm text-gray-500 mb-1">Silt</div>
                <div className="text-lg sm:text-xl font-bold text-amber-700">{soil.silt.toFixed(1)}%</div>
              </div>
              <div className="bg-white rounded-lg p-3 shadow-sm">
                <div className="text-xs sm:text-sm text-gray-500 mb-1">pH Level</div>
                <div className="text-lg sm:text-xl font-bold text-amber-700">{soil.phh2o.toFixed(2)}</div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
