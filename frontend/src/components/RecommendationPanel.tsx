import React, { useState } from 'react';

interface CropRecommendation {
  crop: string;
  score: number;
  reason: string;
}

interface RecommendationResponse {
  recommendations: CropRecommendation[];
  district?: string;
  message?: string;
  success: boolean;
}

export default function RecommendationPanel() {
  const [recommendations, setRecommendations] = useState<CropRecommendation[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [district, setDistrict] = useState('');
  const [season, setSeason] = useState('');
  const [farmSize, setFarmSize] = useState('');

  const fetchRecommendations = (lat?: number, lon?: number) => {
    setLoading(true);
    setError(null);
    
    const requestBody: any = {};
    
    if (lat !== undefined && lon !== undefined) {
      requestBody.latitude = lat;
      requestBody.longitude = lon;
    } else if (district) {
      requestBody.district = district.toUpperCase();
    } else {
      setError('Please provide either location or district name');
      setLoading(false);
      return;
    }

    if (season) requestBody.season = season;
    if (farmSize) requestBody.farmSize = farmSize;

    fetch('http://localhost:8080/api/recommendations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    })
      .then(async (res) => {
        if (!res.ok) {
          const errorData = await res.json().catch(() => null);
          throw new Error(errorData?.message || 'Failed to get recommendations');
        }
        return res.json();
      })
      .then((data: RecommendationResponse) => {
        if (data.success && data.recommendations && data.recommendations.length > 0) {
          setRecommendations(data.recommendations);
        } else {
          throw new Error(data.message || 'No recommendations available');
        }
      })
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
        fetchRecommendations(pos.coords.latitude, pos.coords.longitude);
      },
      (err) => {
        setError('Geolocation error: ' + err.message);
        setLoading(false);
      }
    );
  };

  const handleGetRecommendations = () => {
    fetchRecommendations();
  };

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-lg mt-8">
      <h2 className="text-2xl font-bold mb-6 text-green-700">Crop Recommendations</h2>
      
      {/* Input Form */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div>
          <label className="block text-sm font-medium mb-2">District (e.g., BANGALORE)</label>
          <input
            type="text"
            className="w-full px-3 py-2 border rounded-md"
            placeholder="Enter district name"
            value={district}
            onChange={(e) => setDistrict(e.target.value)}
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium mb-2">Season (Optional)</label>
          <select
            className="w-full px-3 py-2 border rounded-md"
            value={season}
            onChange={(e) => setSeason(e.target.value)}
          >
            <option value="">Select Season</option>
            <option value="monsoon">Monsoon</option>
            <option value="summer">Summer</option>
            <option value="winter">Winter</option>
          </select>
        </div>
        
        <div>
          <label className="block text-sm font-medium mb-2">Farm Size (Optional)</label>
          <select
            className="w-full px-3 py-2 border rounded-md"
            value={farmSize}
            onChange={(e) => setFarmSize(e.target.value)}
          >
            <option value="">Select Farm Size</option>
            <option value="small">Small (&lt; 2 acres)</option>
            <option value="medium">Medium (2-10 acres)</option>
            <option value="large">Large (&gt; 10 acres)</option>
          </select>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 mb-6">
        <button
          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition"
          onClick={handleGeolocate}
          disabled={loading}
        >
          Use My Location
        </button>
        <button
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
          onClick={handleGetRecommendations}
          disabled={loading || !district}
        >
          Get Recommendations
        </button>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="text-center py-4">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
          <p className="mt-2 text-gray-600">Getting AI recommendations...</p>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
          {error}
        </div>
      )}

      {/* Recommendations Display */}
      {recommendations && recommendations.length > 0 && (
        <div className="mt-6">
          <h3 className="text-xl font-semibold mb-4 text-gray-800">Recommended Crops:</h3>
          <div className="space-y-4">
            {recommendations.map((rec, index) => (
              <div
                key={index}
                className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition"
              >
                <div className="flex justify-between items-start mb-2">
                  <h4 className="text-lg font-bold text-green-700">{rec.crop}</h4>
                  <span className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-semibold">
                    Score: {rec.score}/100
                  </span>
                </div>
                <p className="text-gray-700 leading-relaxed">{rec.reason}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
