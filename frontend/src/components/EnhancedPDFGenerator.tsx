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

// Helper to detect if text contains Kannada characters
function containsKannada(text: string): boolean {
  // Kannada Unicode range: 0C80-0CFF
  return /[\u0C80-\u0CFF]/.test(text);
}

export async function generateEnhancedPDF(data: PDFData) {
  const doc = new jsPDF('p', 'mm', 'a4');
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const margin = 15;
  const contentWidth = pageWidth - (2 * margin);
  const footerHeight = 20;
  let y = margin;

  // Check if content is in Kannada
  const isKannada = data.language === 'kn' || data.language === 'kannada';

  // Helper to add page if needed with better spacing
  const checkPageBreak = (requiredSpace: number) => {
    if (y + requiredSpace > pageHeight - footerHeight - margin) {
      doc.addPage();
      y = margin;
      return true;
    }
    return false;
  };

  // === HEADER with solid background ===
  doc.setFillColor(34, 139, 34); // Green
  doc.rect(0, 0, pageWidth, 45, 'F');
  
  // White title
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(22);
  doc.setFont('helvetica', 'bold');
  doc.text('Crop Planner Report', pageWidth / 2, 18, { align: 'center' });
  
  doc.setFontSize(11);
  doc.setFont('helvetica', 'normal');
  doc.text(`District: ${data.district}`, pageWidth / 2, 28, { align: 'center' });
  doc.text(`Generated: ${new Date().toLocaleDateString()}`, pageWidth / 2, 36, { align: 'center' });
  
  y = 55;

  // Reset text color
  doc.setTextColor(0, 0, 0);

  // === Location Info Box ===
  doc.setFillColor(240, 248, 255); // Light blue
  doc.roundedRect(margin, y, contentWidth, 16, 3, 3, 'F');
  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.text(`Location: ${data.location.lat.toFixed(4)}, ${data.location.lon.toFixed(4)}`, margin + 5, y + 10);
  y += 22;

  // === Soil & Weather Section ===
  checkPageBreak(42);
  
  const boxWidth = (contentWidth - 4) / 2;
  
  // Soil Box
  doc.setFillColor(255, 250, 205); // Light yellow
  doc.roundedRect(margin, y, boxWidth, 38, 3, 3, 'F');
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text('Soil Analysis', margin + 4, y + 9);
  
  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.text(`Clay: ${data.soil?.clay?.toFixed(1)}%`, margin + 4, y + 17);
  doc.text(`Sand: ${data.soil?.sand?.toFixed(1)}%`, margin + 4, y + 23);
  doc.text(`pH: ${data.soil?.phh2o?.toFixed(2)}`, margin + 4, y + 29);
  if (data.soil?.source) {
    doc.setFontSize(7);
    doc.setTextColor(100, 100, 100);
    doc.text(`Source: ${data.soil.source}`, margin + 4, y + 34);
    doc.setTextColor(0, 0, 0);
  }

  // Weather Box
  doc.setFillColor(173, 216, 230); // Light blue
  doc.roundedRect(margin + boxWidth + 4, y, boxWidth, 38, 3, 3, 'F');
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text('Weather', margin + boxWidth + 8, y + 9);
  
  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.text(`Temp: ${data.weather?.main?.temp}°C`, margin + boxWidth + 8, y + 17);
  doc.text(`Humidity: ${data.weather?.main?.humidity}%`, margin + boxWidth + 8, y + 23);
  doc.text(`Location: ${data.weather?.name || 'N/A'}`, margin + boxWidth + 8, y + 29);
  
  y += 46;

  // === Crop Recommendations Section ===
  checkPageBreak(18);
  doc.setFillColor(34, 139, 34);
  doc.rect(margin, y, contentWidth, 12, 'F');
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(13);
  doc.setFont('helvetica', 'bold');
  doc.text('Recommended Crops', margin + 4, y + 8);
  doc.setTextColor(0, 0, 0);
  y += 17;

  if (data.recommendations && data.recommendations.length > 0) {
    data.recommendations.forEach((rec: any, idx: number) => {
      // Calculate required height based on content
      const reasonLines = doc.splitTextToSize(rec.reason, contentWidth - 12);
      const maxLines = Math.min(reasonLines.length, 4); // Max 4 lines
      const boxHeight = 18 + (maxLines * 4);
      
      checkPageBreak(boxHeight + 4);
      
      // Crop box with dynamic height
      doc.setFillColor(240, 255, 240); // Light green
      doc.roundedRect(margin, y, contentWidth, boxHeight, 3, 3, 'F');
      
      // Crop name
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(34, 139, 34);
      doc.text(`${idx + 1}. ${rec.crop}`, margin + 4, y + 9);
      
      // Score badge
      doc.setFillColor(34, 139, 34);
      doc.roundedRect(pageWidth - margin - 22, y + 4, 18, 7, 2, 2, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(8);
      doc.text(`${rec.score || rec.confidence}%`, pageWidth - margin - 13, y + 9, { align: 'center' });
      
      // Reason with proper wrapping
      doc.setTextColor(60, 60, 60);
      doc.setFontSize(8);
      doc.setFont('helvetica', 'normal');
      const displayLines = reasonLines.slice(0, maxLines);
      displayLines.forEach((line: string, lineIdx: number) => {
        doc.text(line, margin + 4, y + 15 + (lineIdx * 4));
      });
      
      doc.setTextColor(0, 0, 0);
      y += boxHeight + 3;
    });
  }
  
  y += 2;

  // === Fertilizer Section ===
  if (data.fertilizers && data.fertilizers.length > 0) {
    checkPageBreak(18);
    doc.setFillColor(138, 43, 226); // Purple
    doc.rect(margin, y, contentWidth, 12, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.text(`Fertilizer - ${data.topCrop}`, margin + 4, y + 8);
    doc.setTextColor(0, 0, 0);
    y += 17;

    data.fertilizers.forEach((fert: any, idx: number) => {
      const boxHeight = fert.npkRatio ? 22 : 18;
      checkPageBreak(boxHeight + 3);
      
      doc.setFillColor(240, 230, 255); // Light purple
      doc.roundedRect(margin, y, contentWidth, boxHeight, 3, 3, 'F');
      
      doc.setFontSize(10);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(138, 43, 226);
      doc.text(`${idx + 1}. ${fert.type}`, margin + 4, y + 8);
      
      doc.setFontSize(8);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(0, 0, 0);
      doc.text(`Qty: ${fert.quantity} | Timing: ${fert.timing}`, margin + 4, y + 13);
      if (fert.npkRatio) {
        doc.text(`NPK: ${fert.npkRatio}`, margin + 4, y + 18);
      }
      
      y += boxHeight + 3;
    });
    
    y += 2;
  }

  // === Seed Dealers Section ===
  if (data.dealers && data.dealers.length > 0) {
    checkPageBreak(18);
    doc.setFillColor(70, 130, 180); // Steel blue
    doc.rect(margin, y, contentWidth, 12, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.text('Seed Dealers', margin + 4, y + 8);
    doc.setTextColor(0, 0, 0);
    y += 17;

    data.dealers.slice(0, 6).forEach((dealer: any) => {
      const boxHeight = (dealer.availableSeeds && dealer.availableSeeds.length > 0) ? 20 : 16;
      checkPageBreak(boxHeight + 3);
      
      doc.setFillColor(230, 240, 255); // Light blue
      doc.roundedRect(margin, y, contentWidth, boxHeight, 3, 3, 'F');
      
      doc.setFontSize(9);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(70, 130, 180);
      doc.text(dealer.name, margin + 4, y + 8);
      
      doc.setFontSize(8);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(0, 0, 0);
      doc.text(`${dealer.city}, ${dealer.district} | ${dealer.contact}`, margin + 4, y + 13);
      
      if (dealer.availableSeeds && dealer.availableSeeds.length > 0) {
        doc.setFontSize(7);
        doc.setTextColor(100, 100, 100);
        const seeds = dealer.availableSeeds.slice(0, 3).join(', ');
        doc.text(`Seeds: ${seeds}`, margin + 4, y + 17);
      }
      
      doc.setTextColor(0, 0, 0);
      y += boxHeight + 3;
    });
  }

  // === Footer on all pages ===
  const pageCount = doc.getNumberOfPages();
  for (let i = 1; i <= pageCount; i++) {
    doc.setPage(i);
    doc.setFillColor(245, 245, 245);
    doc.rect(0, pageHeight - footerHeight, pageWidth, footerHeight, 'F');
    doc.setFontSize(7);
    doc.setTextColor(120, 120, 120);
    doc.setFont('helvetica', 'normal');
    doc.text(`CropPlanner AI - Page ${i} of ${pageCount}`, pageWidth / 2, pageHeight - 11, { align: 'center' });
    doc.text(new Date().toLocaleString(), pageWidth / 2, pageHeight - 6, { align: 'center' });
  }

  // Save the PDF
  const langSuffix = data.language === 'kn' ? 'KN' : 'EN';
  doc.save(`CropPlanner-${data.district}-${langSuffix}-${Date.now()}.pdf`);
}
