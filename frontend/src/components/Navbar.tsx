import React from 'react';
import { useTranslation } from 'react-i18next';

const Navbar: React.FC = () => {
  const { i18n, t } = useTranslation();
  return (
    <nav className="bg-white shadow-md border-b-2 border-green-500">
      <div className="container mx-auto px-3 sm:px-4 md:px-6 py-3 sm:py-4 flex justify-between items-center">
        {/* Logo/Brand */}
        <div className="flex items-center space-x-2">
          <span className="text-2xl sm:text-3xl">🌾</span>
          <span className="font-bold text-lg sm:text-xl md:text-2xl bg-gradient-to-r from-green-600 to-green-800 bg-clip-text text-transparent">
            Neyogi - CropPlanner
          </span>
        </div>
        
        {/* Language Switcher */}
        <div className="flex gap-1 sm:gap-2">
          <button
            className={`px-2 sm:px-3 py-1 sm:py-2 rounded-lg text-xs sm:text-sm font-medium transition-all duration-200 ${
              i18n.language === 'en' 
                ? 'bg-gradient-to-r from-green-500 to-green-600 text-white shadow-md' 
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
            onClick={() => i18n.changeLanguage('en')}
          >
            English
          </button>
          <button
            className={`px-2 sm:px-3 py-1 sm:py-2 rounded-lg text-xs sm:text-sm font-medium transition-all duration-200 ${
              i18n.language === 'kn' 
                ? 'bg-gradient-to-r from-green-500 to-green-600 text-white shadow-md' 
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
            onClick={() => i18n.changeLanguage('kn')}
          >
            ಕನ್ನಡ
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
