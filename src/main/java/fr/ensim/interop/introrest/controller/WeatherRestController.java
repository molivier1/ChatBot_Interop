package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.WeatherResponse;
import fr.ensim.interop.introrest.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherRestController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam String city,
            @RequestParam(defaultValue = "false") boolean forecast) {

        WeatherResponse response = weatherService.getWeather(city, forecast);
        if (response == null) return ResponseEntity.status(500).build();
        return ResponseEntity.ok(response);
    }
}
