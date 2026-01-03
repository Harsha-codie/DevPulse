import React from 'react';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

interface PDFData {
  district: string;
  location: { lat: number; lon: number };
  soil: any;
  weather: any;
  recommendations: any[];
  fertilizers?: any[];
  dealers?: any[];
  topCrop?: string;
  language?: string;
}

export async function generateEnhancedPDF(data: PDFData) {
  // Create a hidden container for rendering
  const container = document.createElement('div');
  container.style.position = 'absolute';
  container.style.left = '-9999px';
  container.style.width = '794px'; // A4 width in pixels at 96 DPI
  container.style.backgroundColor = 'white';
  container.style.padding = '40px';
  container.style.fontFamily = 'Arial, sans-serif';
  document.body.appendChild(container);

  // Build the HTML content with proper Kannada rendering
  container.innerHTML = `
    <div style="width: 100%; background: white;">
      <!-- Header -->
      <div style="background: linear-gradient(135deg, #228B22, #006400); padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px;">
        <h1 style="color: white; font-size: 32px; margin: 0 0 10px 0; font-weight: bold;">
          🌾 Neyogi - CropPlanner Report
        </h1>
        <p style="color: #E8F5E9; font-size: 16px; margin: 5px 0;">
          District: ${data.district}
        </p>
        <p style="color: #E8F5E9; font-size: 14px; margin: 5px 0;">
          Generated: ${new Date().toLocaleString()}
        </p>
      </div>

      <!-- Location Info -->
      <div style="background: #E3F2FD; padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #2196F3;">
        <p style="margin: 0; color: #1565C0; font-size: 14px;">
          📍 Location: ${data.location.lat.toFixed(4)}, ${data.location.lon.toFixed(4)}
        </p>
      </div>

      <!-- Soil & Weather Section -->
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 25px;">
        <!-- Soil Box -->
        <div style="background: #FFFACD; padding: 20px; border-radius: 10px; border: 2px solid #FFD700;">
          <h3 style="color: #FF8C00; font-size: 18px; margin: 0 0 15px 0; font-weight: bold;">
            🌱 Soil Analysis
          </h3>
          <p style="margin: 8px 0; color: #333; font-size: 14px;">
            Clay: <strong>${data.soil?.clay?.toFixed(1)}%</strong>
          </p>
          <p style="margin: 8px 0; color: #333; font-size: 14px;">
            Sand: <strong>${data.soil?.sand?.toFixed(1)}%</strong>
          </p>
          <p style="margin: 8px 0; color: #333; font-size: 14px;">
            pH: <strong>${data.soil?.phh2o?.toFixed(2)}</strong>
          </p>
          ${data.soil?.source ? `<p style="margin: 8px 0; color: #666; font-size: 11px;">Source: ${data.soil.source}</p>` : ''}
        </div>

        <!-- Weather Box -->
        <div style="background: #B0E0E6; padding: 20px; border-radius: 10px; border: 2px solid #4682B4;">
          <h3 style="color: #1E90FF; font-size: 18px; margin: 0 0 15px 0; font-weight: bold;">
            ☀️ Weather
          </h3>
          <p style="margin: 8px 0; color: #333; font-size: 14px;">
            Temperature: <strong>${data.weather?.main?.temp}°C</strong>
          </p>
          <p style="margin: 8px 0; color: #333; font-size: 14px;">
            Humidity: <strong>${data.weather?.main?.humidity}%</strong>
          </p>
          <p style="margin: 8px 0; color: #333; font-size: 14px;">
            Location: <strong>${data.weather?.name || 'N/A'}</strong>
          </p>
        </div>
      </div>

      <!-- Crop Recommendations Section -->
      <div style="background: linear-gradient(135deg, #228B22, #32CD32); padding: 15px; border-radius: 8px; margin-bottom: 15px;">
        <h2 style="color: white; font-size: 22px; margin: 0; font-weight: bold;">
          🌾 Recommended Crops
        </h2>
      </div>

      ${data.recommendations && data.recommendations.length > 0 ? data.recommendations.map((rec: any, idx: number) => `
        <div style="background: #F0FFF0; padding: 20px; border-radius: 10px; margin-bottom: 15px; border: 2px solid #90EE90;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <h3 style="color: #228B22; font-size: 20px; margin: 0; font-weight: bold;">
              ${idx + 1}. ${rec.crop}
            </h3>
            <div style="background: #228B22; color: white; padding: 8px 16px; border-radius: 20px; font-size: 16px; font-weight: bold;">
              ${rec.score || rec.confidence}%
            </div>
          </div>
          <p style="color: #333; font-size: 14px; line-height: 1.6; margin: 10px 0;">
            ${rec.reason}
          </p>
        </div>
      `).join('') : '<p style="color: #666; text-align: center;">No recommendations available</p>'}

      <!-- Fertilizer Section -->
      ${data.fertilizers && data.fertilizers.length > 0 ? `
        <div style="background: linear-gradient(135deg, #8A2BE2, #9370DB); padding: 15px; border-radius: 8px; margin: 25px 0 15px 0;">
          <h2 style="color: white; font-size: 22px; margin: 0; font-weight: bold;">
            🧪 Fertilizer Plan - ${data.topCrop || ''}
          </h2>
        </div>

        ${data.fertilizers.map((fert: any, idx: number) => `
          <div style="background: #F0E6FA; padding: 18px; border-radius: 10px; margin-bottom: 12px; border: 2px solid #DDA0DD;">
            <h4 style="color: #8A2BE2; font-size: 16px; margin: 0 0 10px 0; font-weight: bold;">
              ${idx + 1}. ${fert.type}
            </h4>
            <p style="color: #333; font-size: 13px; margin: 5px 0;">
              <strong>Quantity:</strong> ${fert.quantity}
            </p>
            <p style="color: #333; font-size: 13px; margin: 5px 0;">
              <strong>Timing:</strong> ${fert.timing}
            </p>
            ${fert.npkRatio ? `<p style="color: #333; font-size: 13px; margin: 5px 0;"><strong>NPK Ratio:</strong> ${fert.npkRatio}</p>` : ''}
          </div>
        `).join('')}
      ` : ''}

      <!-- Seed Dealers Section -->
      ${data.dealers && data.dealers.length > 0 ? `
        <div style="background: linear-gradient(135deg, #4682B4, #5F9EA0); padding: 15px; border-radius: 8px; margin: 25px 0 15px 0;">
          <h2 style="color: white; font-size: 22px; margin: 0; font-weight: bold;">
            🏪 Recommended Seed Dealers
          </h2>
        </div>

        ${data.dealers.slice(0, 6).map((dealer: any, idx: number) => `
          <div style="background: #E6F3FF; padding: 18px; border-radius: 10px; margin-bottom: 12px; border: 2px solid #87CEEB;">
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 8px;">
              <h4 style="color: #4682B4; font-size: 16px; margin: 0; font-weight: bold; flex: 1;">
                ${dealer.name}
              </h4>
              <div style="background: #228B22; color: white; padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: bold;">
                TOP ${idx + 1}
              </div>
            </div>
            <p style="color: #333; font-size: 13px; margin: 5px 0;">
              <strong>📍 Location:</strong> ${dealer.city}, ${dealer.district}
            </p>
            <p style="color: #333; font-size: 13px; margin: 5px 0;">
              <strong>☎️ Contact:</strong> ${dealer.contact}
            </p>
            ${dealer.availableSeeds && dealer.availableSeeds.length > 0 ? `
              <div style="margin-top: 10px;">
                <p style="color: #666; font-size: 12px; margin: 0 0 5px 0; font-weight: bold;">Available Seeds:</p>
                ${dealer.availableSeeds.slice(0, 3).map((seed: string) => {
                  const cropName = seed.split(' - ')[0];
                  const variety = seed.split(' - ')[1] || '';
                  const price = dealer.seedPrices?.[cropName];
                  return `
                    <div style="background: white; padding: 8px; border-radius: 6px; margin: 4px 0; border: 1px solid #90EE90;">
                      <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                          <span style="color: #228B22; font-weight: bold; font-size: 12px;">${cropName}</span>
                          ${variety ? `<span style="color: #666; font-size: 11px; margin-left: 5px;">${variety}</span>` : ''}
                        </div>
                        ${price ? `<span style="color: #228B22; font-weight: bold; font-size: 12px;">${price}</span>` : ''}
                      </div>
                    </div>
                  `;
                }).join('')}
              </div>
            ` : ''}
          </div>
        `).join('')}
      ` : ''}

      <!-- Footer -->
      <div style="background: #F5F5F5; padding: 20px; border-radius: 8px; text-align: center; margin-top: 30px; border-top: 3px solid #228B22;">
        <p style="color: #666; font-size: 14px; margin: 5px 0;">
          <strong>📧 Need Help?</strong>
        </p>
        <p style="color: #228B22; font-size: 14px; margin: 5px 0; font-weight: bold;">
          support.neyogi.cropplanner@gmail.com
        </p>
        <p style="color: #999; font-size: 11px; margin: 15px 0 5px 0;">
          © 2026 Neyogi - CropPlanner | Empowering Karnataka Farmers
        </p>
        <p style="color: #999; font-size: 10px; margin: 5px 0;">
          Page generated: ${new Date().toLocaleString()}
        </p>
      </div>
    </div>
  `;

  try {
    // Use html2canvas to capture the rendered content with proper fonts
    const canvas = await html2canvas(container, {
      scale: 2, // Higher quality
      useCORS: true,
      logging: false,
      backgroundColor: '#ffffff'
    });

    // Remove the temporary container
    document.body.removeChild(container);

    // Create PDF
    const pdf = new jsPDF('p', 'mm', 'a4');
    const imgWidth = 210; // A4 width in mm
    const pageHeight = 297; // A4 height in mm
    const imgHeight = (canvas.height * imgWidth) / canvas.width;
    let heightLeft = imgHeight;
    let position = 0;

    // Add image to PDF
    const imgData = canvas.toDataURL('image/png');
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;

    // Add extra pages if content is longer than one page
    while (heightLeft > 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }

    // Save the PDF
    const langSuffix = data.language === 'kn' ? 'KN' : 'EN';
    pdf.save(`Neyogi-CropPlanner-${data.district}-${langSuffix}-${Date.now()}.pdf`);
  } catch (error) {
    console.error('PDF generation error:', error);
    // Remove container on error
    if (container.parentNode) {
      document.body.removeChild(container);
    }
    throw error;
  }
}
