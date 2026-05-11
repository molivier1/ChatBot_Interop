package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.Joke;
import fr.ensim.interop.introrest.service.JokeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jokes")
public class JokeRestController {

    @Autowired
    private JokeService jokeService;

    @GetMapping("/random")
    public ResponseEntity<Joke> getRandomJoke() {
        return ResponseEntity.ok(jokeService.getRandomJoke());
    }
}
