package fr.ensim.interop.introrest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForecastDay {
    private String date;
    private Double temperature;
    private String condition;
}
