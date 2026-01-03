import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

const SmartTips: React.FC = () => {
  const { t } = useTranslation();
  const [currentTip, setCurrentTip] = useState(0);
  
  const tips = [
    'tip1', 'tip2', 'tip3', 'tip4', 'tip5', 'tip6', 'tip7', 'tip8'
  ];
  
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTip((prev) => (prev + 1) % tips.length);
    }, 5000); // Rotate every 5 seconds
    
    return () => clearInterval(interval);
  }, [tips.length]);
  
  return (
    <div className="bg-gradient-to-r from-green-600 to-green-700 text-white py-3 px-4 rounded-lg shadow-md mb-6">
      <div className="flex items-center justify-center space-x-3">
        <span className="text-2xl">💡</span>
        <div className="flex-1">
          <p className="text-xs sm:text-sm font-semibold mb-1">{t('smartTips')}</p>
          <p className="text-sm sm:text-base transition-all duration-500">
            {t(tips[currentTip])}
          </p>
        </div>
        <div className="flex space-x-1">
          {tips.map((_, idx) => (
            <div
              key={idx}
              className={`h-2 w-2 rounded-full transition-all duration-300 ${
                idx === currentTip ? 'bg-white w-6' : 'bg-white/40'
              }`}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default SmartTips;
