import React, { useState } from 'react';

interface WeatherData {
  name: string;
  main: { temp: number; humidity: number };
  weather: { description: string; icon: string }[];
}

export default function WeatherPanel() {
  const [weather, setWeather] = useState<WeatherData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchWeather = (lat?: number, lon?: number) => {
    setLoading(true);
    setError(null);
    let url = '/api/weather';
    if (lat !== undefined && lon !== undefined) {
      url += `?lat=${lat}&lon=${lon}`;
    }
    fetch(url)
      .then(async (res) => {
        if (!res.ok) throw new Error(await res.text());
        return res.json();
      })
      .then((data) => setWeather(data))
      .catch((err) => setError(err.message))
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
        fetchWeather(pos.coords.latitude, pos.coords.longitude);
      },
      (err) => {
        setError('Geolocation error: ' + err.message);
        setLoading(false);
      }
    );
  };

  return (
    <div className="bg-white rounded-xl shadow-lg border-t-4 border-blue-500 overflow-hidden hover:shadow-2xl transition-shadow duration-300">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-500 to-blue-600 p-4 sm:p-6">
        <div className="flex items-center space-x-3">
          <span className="text-3xl sm:text-4xl">☁️</span>
          <h2 className="text-xl sm:text-2xl font-bold text-white">Weather</h2>
        </div>
      </div>
      
      {/* Content */}
      <div className="p-4 sm:p-6">
        <div className="mb-4">
          <button
            className="w-full px-4 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all duration-200 font-medium shadow-md text-sm sm:text-base"
            onClick={handleGeolocate}
            disabled={loading}
          >
            📍 Use My Location
          </button>
        </div>
        
        {loading && (
          <div className="text-center py-8">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
            <p className="mt-3 text-gray-600">Loading weather...</p>
          </div>
        )}
        
        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
            <p className="text-red-700 text-sm">⚠️ {error}</p>
          </div>
        )}
        
        {weather && (
          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-4 sm:p-6 mt-4">
            <div className="flex items-center gap-4">
              <img
                src={`https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png`}
                alt={weather.weather[0].description}
                className="w-16 h-16 sm:w-20 sm:h-20"
              />
              <div className="flex-1">
                <div className="text-lg sm:text-xl font-bold text-gray-800">{weather.name}</div>
                <div className="text-sm sm:text-base text-gray-600 capitalize">{weather.weather[0].description}</div>
                <div className="mt-2 grid grid-cols-2 gap-2 text-sm sm:text-base">
                  <div className="bg-white rounded-md px-3 py-2">
                    <span className="text-gray-500">Temp:</span>
                    <span className="font-bold text-blue-700 ml-1">{weather.main.temp}°C</span>
                  </div>
                  <div className="bg-white rounded-md px-3 py-2">
                    <span className="text-gray-500">Humidity:</span>
                    <span className="font-bold text-blue-700 ml-1">{weather.main.humidity}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
