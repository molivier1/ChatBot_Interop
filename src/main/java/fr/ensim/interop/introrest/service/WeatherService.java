package fr.ensim.interop.introrest.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.ensim.interop.introrest.model.ForecastDay;
import fr.ensim.interop.introrest.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WeatherService {

    @Value("${open.weather.api.url}")
    private String weatherApiUrl;

    @Value("${open.weather.api.token}")
    private String weatherApiToken;

    @Autowired
    private RestTemplate restTemplate;

    public WeatherResponse getWeather(String city, boolean forecast) {
        String currentUrl = weatherApiUrl + "?q=" + city + "&appid=" + weatherApiToken + "&units=metric&lang=fr";
        JsonNode current = restTemplate.getForObject(currentUrl, JsonNode.class);

        if (current == null) return null;

        String cityName = current.get("name").asText();
        double temperature = current.get("main").get("temp").asDouble();
        String condition = current.get("weather").get(0).get("description").asText();

        List<ForecastDay> forecastDays = null;

        if (forecast) {
            String forecastUrl = weatherApiUrl.replace("/weather", "/forecast")
                    + "?q=" + city + "&appid=" + weatherApiToken + "&units=metric&lang=fr";
            JsonNode forecastRoot = restTemplate.getForObject(forecastUrl, JsonNode.class);

            if (forecastRoot != null) {
                forecastDays = new ArrayList<>();
                Set<String> seenDates = new HashSet<>();
                String today = LocalDate.now().toString();

                for (JsonNode item : forecastRoot.get("list")) {
                    String dtTxt = item.get("dt_txt").asText();
                    String date = dtTxt.substring(0, 10);

                    if (date.equals(today) || seenDates.contains(date)) continue;
                    if (forecastDays.size() >= 2) break;

                    seenDates.add(date);
                    double forecastTemp = item.get("main").get("temp").asDouble();
                    String forecastCondition = item.get("weather").get(0).get("description").asText();
                    forecastDays.add(new ForecastDay(date, forecastTemp, forecastCondition));
                }
            }
        }

        return new WeatherResponse(cityName, temperature, condition, forecastDays);
    }
}
