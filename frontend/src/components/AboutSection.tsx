import React from 'react';
import { useTranslation } from 'react-i18next';

const AboutSection: React.FC = () => {
  const { t } = useTranslation();
  
  return (
    <div className="mt-12 mb-8">
      <h2 className="text-2xl sm:text-3xl font-bold text-center mb-8 text-green-800">
        {t('aboutTitle')}
      </h2>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Goal Card */}
        <div className="bg-white rounded-xl shadow-lg p-6 border-t-4 border-green-500 hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-center mb-4">
            <span className="text-4xl mr-3">🎯</span>
            <h3 className="text-xl font-bold text-green-700">{t('aboutGoal')}</h3>
          </div>
          <p className="text-gray-700 leading-relaxed">
            {t('aboutGoalText')}
          </p>
        </div>
        
        {/* Mission Card */}
        <div className="bg-white rounded-xl shadow-lg p-6 border-t-4 border-blue-500 hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-center mb-4">
            <span className="text-4xl mr-3">🚀</span>
            <h3 className="text-xl font-bold text-blue-700">{t('aboutMission')}</h3>
          </div>
          <p className="text-gray-700 leading-relaxed">
            {t('aboutMissionText')}
          </p>
        </div>
        
        {/* Vision Card */}
        <div className="bg-white rounded-xl shadow-lg p-6 border-t-4 border-amber-500 hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-center mb-4">
            <span className="text-4xl mr-3">🌟</span>
            <h3 className="text-xl font-bold text-amber-700">{t('aboutVision')}</h3>
          </div>
          <p className="text-gray-700 leading-relaxed">
            {t('aboutVisionText')}
          </p>
        </div>
      </div>
    </div>
  );
};

export default AboutSection;
