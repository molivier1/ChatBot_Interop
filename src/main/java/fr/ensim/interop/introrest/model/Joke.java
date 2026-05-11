package fr.ensim.interop.introrest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Joke {
    private Integer id;
    private String title;
    private String text;
    private Double rating;
}
