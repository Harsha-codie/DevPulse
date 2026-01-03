#!/usr/bin/env node

/**
 * i18n Completeness Checker
 * Validates that:
 * 1. All t() calls have corresponding keys in locale files
 * 2. en.json and kn.json have matching keys
 * 3. No missing translations
 */

const fs = require('fs');
const path = require('path');

const COLORS = {
  reset: '\x1b[0m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  cyan: '\x1b[36m'
};

function log(color, message) {
  console.log(`${COLORS[color]}${message}${COLORS.reset}`);
}

function readJSONFile(filePath) {
  try {
    const content = fs.readFileSync(filePath, 'utf-8');
    return JSON.parse(content);
  } catch (error) {
    log('red', `❌ Error reading ${filePath}: ${error.message}`);
    process.exit(1);
  }
}

function findTCalls(dir, extensions = ['.tsx', '.ts', '.jsx', '.js']) {
  const tCalls = new Set();
  const tCallRegex = /t\(['"`]([^'"`]+)['"`]\)/g;
  
  function scanDirectory(directory) {
    const entries = fs.readdirSync(directory, { withFileTypes: true });
    
    for (const entry of entries) {
      const fullPath = path.join(directory, entry.name);
      
      if (entry.isDirectory() && entry.name !== 'node_modules') {
        scanDirectory(fullPath);
      } else if (entry.isFile() && extensions.some(ext => entry.name.endsWith(ext))) {
        const content = fs.readFileSync(fullPath, 'utf-8');
        let match;
        
        while ((match = tCallRegex.exec(content)) !== null) {
          tCalls.add(match[1]);
        }
      }
    }
  }
  
  scanDirectory(dir);
  return tCalls;
}

function validateI18n() {
  log('cyan', '\n🔍 Running i18n Completeness Check...\n');
  
  const projectRoot = path.resolve(__dirname, '..');
  const localesDir = path.join(projectRoot, 'src', 'locales');
  const srcDir = path.join(projectRoot, 'src');
  
  // Load locale files
  const enPath = path.join(localesDir, 'en.json');
  const knPath = path.join(localesDir, 'kn.json');
  
  log('blue', '📖 Loading locale files...');
  const enLocale = readJSONFile(enPath);
  const knLocale = readJSONFile(knPath);
  
  const enKeys = new Set(Object.keys(enLocale));
  const knKeys = new Set(Object.keys(knLocale));
  
  log('green', `✅ en.json: ${enKeys.size} keys`);
  log('green', `✅ kn.json: ${knKeys.size} keys`);
  
  // Find all t() calls in source code
  log('blue', '\n🔎 Scanning source code for t() calls...');
  const tCalls = findTCalls(srcDir);
  log('green', `✅ Found ${tCalls.size} unique translation keys in code`);
  
  // Check 1: Missing keys in en.json
  const missingInEn = [...tCalls].filter(key => !enKeys.has(key));
  
  // Check 2: Missing keys in kn.json
  const missingInKn = [...tCalls].filter(key => !knKeys.has(key));
  
  // Check 3: Keys in en.json but not in kn.json
  const missingTranslationsKn = [...enKeys].filter(key => !knKeys.has(key));
  
  // Check 4: Keys in kn.json but not in en.json
  const missingTranslationsEn = [...knKeys].filter(key => !enKeys.has(key));
  
  // Check 5: Unused keys in locale files
  const unusedInEn = [...enKeys].filter(key => !tCalls.has(key));
  const unusedInKn = [...knKeys].filter(key => !tCalls.has(key));
  
  let hasErrors = false;
  
  // Report Check 1
  if (missingInEn.length > 0) {
    hasErrors = true;
    log('red', `\n❌ Missing keys in en.json (${missingInEn.length}):`);
    missingInEn.forEach(key => log('yellow', `   - ${key}`));
  }
  
  // Report Check 2
  if (missingInKn.length > 0) {
    hasErrors = true;
    log('red', `\n❌ Missing keys in kn.json (${missingInKn.length}):`);
    missingInKn.forEach(key => log('yellow', `   - ${key}`));
  }
  
  // Report Check 3
  if (missingTranslationsKn.length > 0) {
    hasErrors = true;
    log('red', `\n❌ Keys in en.json missing from kn.json (${missingTranslationsKn.length}):`);
    missingTranslationsKn.forEach(key => log('yellow', `   - ${key}`));
  }
  
  // Report Check 4
  if (missingTranslationsEn.length > 0) {
    hasErrors = true;
    log('red', `\n❌ Keys in kn.json missing from en.json (${missingTranslationsEn.length}):`);
    missingTranslationsEn.forEach(key => log('yellow', `   - ${key}`));
  }
  
  // Report Check 5 (warnings only)
  if (unusedInEn.length > 0) {
    log('yellow', `\n⚠️  Unused keys in locale files (${unusedInEn.length}):`);
    log('yellow', '   (These keys exist in locale files but are not used in code)');
    unusedInEn.forEach(key => log('yellow', `   - ${key}`));
  }
  
  // Final report
  console.log('\n' + '='.repeat(60));
  if (hasErrors) {
    log('red', '\n❌ i18n CHECK FAILED');
    log('yellow', '\nPlease fix the issues above before committing.');
    process.exit(1);
  } else {
    log('green', '\n✅ i18n CHECK PASSED');
    log('cyan', '\n📊 Summary:');
    log('green', `   • ${enKeys.size} keys in en.json`);
    log('green', `   • ${knKeys.size} keys in kn.json`);
    log('green', `   • ${tCalls.size} translation calls in code`);
    log('green', `   • All keys are present and synchronized`);
    
    if (unusedInEn.length > 0) {
      log('yellow', `   • ${unusedInEn.length} unused keys (consider cleanup)`);
    }
    
    process.exit(0);
  }
}

// Run the validation
validateI18n();
