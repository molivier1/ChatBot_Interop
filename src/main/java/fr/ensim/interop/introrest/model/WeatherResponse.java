package fr.ensim.interop.introrest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private String city;
    private Double temperature;
    private String condition;
    private List<ForecastDay> forecast;
}
