package fr.ensim.interop.introrest.service;

import fr.ensim.interop.introrest.model.Joke;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class JokeService {

    private static final List<Joke> JOKES = Arrays.asList(
            new Joke(1, "Le mathématicien", "Pourquoi les mathématiciens font des barbecues ? Parce qu'ils adorent les pi-grillades !", 7.5),
            new Joke(2, "Le chat", "Qu'est-ce qu'un chat qui tombe dans un pot de peinture le jour de Noël ? Un chat-peint de Noël !", 6.0),
            new Joke(3, "L'informaticien", "Pourquoi les informaticiens confondent-ils Halloween et Noël ? Parce que Oct 31 = Dec 25 !", 9.0),
            new Joke(4, "Le crocodile", "Qu'est-ce qu'un crocodile qui surveille la cour d'école ? Un sac à dents !", 7.0),
            new Joke(5, "Le programmeur", "Un programmeur entre dans un bar, commande 1 bière, puis 0 bières, puis 2147483647 bières.", 8.5)
    );

    private final Random random = new Random();

    public Joke getRandomJoke() {
        return JOKES.get(random.nextInt(JOKES.size()));
    }
}
