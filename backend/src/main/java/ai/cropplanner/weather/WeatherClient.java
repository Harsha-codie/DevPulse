package ai.cropplanner.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WeatherClient {
    private static final Logger logger = LoggerFactory.getLogger(WeatherClient.class);
    
    @Value("${openweather.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getWeather(double lat, double lon) {
        // If no API key configured, return mock data
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("No OpenWeather API key configured - using mock weather data");
            return getMockWeatherData(lat, lon);
        }
        
        String url = UriComponentsBuilder.fromHttpUrl("https://api.openweathermap.org/data/2.5/weather")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully fetched weather data from OpenWeather API");
                return response.getBody();
            } else {
                logger.warn("Weather API returned status {}, falling back to mock data", response.getStatusCode());
                return getMockWeatherData(lat, lon);
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("OpenWeather API authentication failed (401 Unauthorized) - API key may be invalid or expired. Falling back to mock weather data.");
            return getMockWeatherData(lat, lon);
        } catch (HttpClientErrorException e) {
            logger.warn("Weather API client error: {} - falling back to mock data", e.getMessage());
            return getMockWeatherData(lat, lon);
        } catch (Exception e) {
            logger.warn("Failed to fetch weather data: {} - falling back to mock data", e.getMessage());
            return getMockWeatherData(lat, lon);
        }
    }
    
    /**
     * Mock weather data for testing when OpenWeather API key is not configured
     */
    private String getMockWeatherData(double lat, double lon) {
        return String.format("""
            {
              "coord": {"lon": %.4f, "lat": %.4f},
              "weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}],
              "base": "stations",
              "main": {
                "temp": 25.5,
                "feels_like": 25.8,
                "temp_min": 24.0,
                "temp_max": 27.0,
                "pressure": 1013,
                "humidity": 65
              },
              "visibility": 10000,
              "wind": {"speed": 3.5, "deg": 120},
              "clouds": {"all": 0},
              "dt": 1699430400,
              "sys": {
                "type": 1,
                "id": 9052,
                "country": "IN",
                "sunrise": 1699403400,
                "sunset": 1699444800
              },
              "timezone": 19800,
              "id": 1277333,
              "name": "Bangalore",
              "cod": 200
            }
            """, lon, lat);
    }
}
