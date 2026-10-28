package com.booklibrary.kids;

import java.util.List;
import java.util.Map;

import static com.booklibrary.kids.ActorType.*;
import static com.booklibrary.kids.Action.*;

/**
 * The stories that come with the app. All of them were written for this project, so there
 * are no copyright problems.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the short stories and planned which word makes which character do what.
 * Purpose: each page lists its characters and a map of cue words. When the voice reads a cue
 * word, the matching character acts it out (walks, jumps, swims, sings, falls asleep...).
 */
public final class KidsStories {

    private KidsStories() {
    }

    private static Placed at(ActorType type, double x, double y) {
        return new Placed(type, x, y, 1.0);
    }

    private static Placed at(ActorType type, double x, double y, double scale) {
        return new Placed(type, x, y, scale);
    }

    private static Cue c(ActorType type, Action action) {
        return Cue.of(type, action);
    }

    private static Cue go(ActorType type, Action action, double dx) {
        return Cue.to(type, action, dx);
    }

    private static Cue go(ActorType type, Action action, double dx, double dy) {
        return Cue.to(type, action, dx, dy);
    }

    /**
     * Returns the built-in stories.
     *
     * @return the stories
     */
    public static List<KidsStory> all() {
        return List.of(luna(), bo(), pip(), daisy(), tilly(), rocky());
    }

    private static KidsStory luna() {
        return new KidsStory("Luna and the Sleepy Moon", "A little fox, a big sky and a moon that wants to sleep.", List.of(
                new KidsPage("Luna the little fox woke up in the green meadow. The sun was warm and the birds were singing.",
                        Backdrop.MEADOW,
                        List.of(at(SUN, 0.86, 0.22), at(CLOUD, 0.28, 0.2), at(TREE, 0.1, 0.7), at(TREE, 0.92, 0.66, 0.8),
                                at(BIRD, 0.62, 0.36), at(FOX, 0.4, 0.84)),
                        Map.of("woke", c(FOX, WAVE), "sun", c(SUN, REACT), "birds", go(BIRD, FLY, 0.16, -0.04),
                                "singing", c(BIRD, SING))),
                new KidsPage("A big white cloud floated by. Luna jumped up high to touch it, but the cloud was too far away.",
                        Backdrop.MEADOW,
                        List.of(at(SUN, 0.14, 0.2), at(CLOUD, 0.45, 0.22, 1.3), at(CLOUD, 0.8, 0.12, 0.8), at(TREE, 0.9, 0.7),
                                at(FOX, 0.4, 0.84)),
                        Map.of("floated", go(CLOUD, FLY, 0.16), "jumped", c(FOX, HOP), "touch", c(FOX, SPIN),
                                "away", go(CLOUD, FLY, 0.14))),
                new KidsPage("Luna ran along the little river. Splash! She jumped over a log. A friendly bird told her, \"The moon is feeling sleepy tonight.\"",
                        Backdrop.RIVER,
                        List.of(at(CLOUD, 0.7, 0.16), at(TREE, 0.08, 0.6), at(BIRD, 0.75, 0.4), at(FOX, 0.2, 0.64)),
                        Map.of("ran", go(FOX, WALK, 0.4), "splash", c(FOX, SHAKE), "jumped", c(FOX, HOP),
                                "bird", c(BIRD, WAVE), "sleepy", c(CLOUD, REACT))),
                new KidsPage("When the stars came out, Luna climbed the tallest hill. She sang a soft song for the moon.",
                        Backdrop.NIGHT,
                        List.of(at(MOON, 0.8, 0.22, 1.2), at(STARS, 0.3, 0.2), at(STARS, 0.55, 0.12, 0.8), at(TREE, 0.1, 0.78),
                                at(FOX, 0.3, 0.86)),
                        Map.of("stars", c(STARS, REACT), "climbed", go(FOX, WALK, 0.3, -0.06), "sang", c(FOX, SING),
                                "moon", c(MOON, REACT))),
                new KidsPage("The moon smiled and closed its eyes. The owl whispered, \"Good night, Luna.\" And Luna went home to sleep.",
                        Backdrop.NIGHT,
                        List.of(at(MOON, 0.78, 0.24, 1.2), at(STARS, 0.28, 0.18), at(TREE, 0.88, 0.8, 1.1), at(OWL, 0.2, 0.78),
                                at(FOX, 0.6, 0.86)),
                        Map.of("smiled", c(MOON, REACT), "owl", c(OWL, WAVE), "went", go(FOX, WALK, -0.25),
                                "sleep", c(FOX, SLEEP), "night", c(STARS, REACT)))));
    }

    private static KidsStory bo() {
        return new KidsStory("Bo the Bubble Fish", "A small blue fish makes a new friend under the sea.", List.of(
                new KidsPage("Bo was a little blue fish who lived deep in the sea. Every morning he blew bubbles.",
                        Backdrop.SEA,
                        List.of(at(SEAWEED, 0.12, 0.92), at(SEAWEED, 0.88, 0.92, 1.2), at(FISH, 0.5, 0.55)),
                        Map.of("bo", c(FISH, WAVE), "sea", c(SEAWEED, REACT), "blew", c(FISH, REACT))),
                new KidsPage("One day a bubble floated up, up, up, and Bo wanted to follow it.",
                        Backdrop.SEA,
                        List.of(at(SEAWEED, 0.1, 0.92), at(SEAWEED, 0.9, 0.92), at(FISH, 0.4, 0.8)),
                        Map.of("floated", c(FISH, REACT), "up", go(FISH, SWIM, 0.1, -0.3), "follow", go(FISH, SWIM, 0.15, -0.1))),
                new KidsPage("He met a soft pink jellyfish. \"Hello!\" she said, and she glowed like a little lamp.",
                        Backdrop.SEA,
                        List.of(at(SEAWEED, 0.9, 0.92), at(FISH, 0.2, 0.6), at(JELLYFISH, 0.68, 0.5, 1.2)),
                        Map.of("met", go(FISH, SWIM, 0.2), "hello", c(JELLYFISH, WAVE), "glowed", c(JELLYFISH, REACT))),
                new KidsPage("Together they swam through the waving seaweed and sang a bubble song.",
                        Backdrop.SEA,
                        List.of(at(SEAWEED, 0.15, 0.92, 1.3), at(SEAWEED, 0.5, 0.95, 1.1), at(SEAWEED, 0.85, 0.92, 1.3),
                                at(FISH, 0.2, 0.55), at(JELLYFISH, 0.65, 0.4)),
                        Map.of("swam", go(FISH, SWIM, 0.4), "waving", c(SEAWEED, REACT), "sang", c(JELLYFISH, SING),
                                "bubble", c(FISH, SPIN))),
                new KidsPage("When the light grew dim, Bo and his new friend went home. Good night, sea!",
                        Backdrop.SEA,
                        List.of(at(SEAWEED, 0.1, 0.92), at(SEAWEED, 0.9, 0.92), at(FISH, 0.5, 0.62), at(JELLYFISH, 0.7, 0.5)),
                        Map.of("went", go(FISH, SWIM, -0.25, 0.1), "friend", c(JELLYFISH, DANCE), "night", c(FISH, SLEEP)))));
    }

    private static KidsStory pip() {
        return new KidsStory("Pip and the Giant Carrot", "A hungry bunny finds the biggest carrot ever.", List.of(
                new KidsPage("Pip the bunny lived under a big oak tree. One sunny morning, Pip woke up very hungry.",
                        Backdrop.MEADOW,
                        List.of(at(SUN, 0.85, 0.2), at(TREE, 0.12, 0.72, 1.3), at(FLOWER, 0.7, 0.9), at(BUNNY, 0.38, 0.86)),
                        Map.of("pip", c(BUNNY, WAVE), "sunny", c(SUN, REACT), "woke", c(BUNNY, HOP), "hungry", c(BUNNY, SHAKE))),
                new KidsPage("Pip hopped across the meadow, past the red flowers and the tall grass.",
                        Backdrop.MEADOW,
                        List.of(at(SUN, 0.15, 0.2), at(CLOUD, 0.6, 0.18), at(FLOWER, 0.5, 0.9), at(FLOWER, 0.65, 0.92),
                                at(FLOWER, 0.8, 0.9), at(BUNNY, 0.1, 0.86)),
                        Map.of("hopped", go(BUNNY, WALK, 0.55), "flowers", c(FLOWER, REACT), "grass", c(FLOWER, DANCE))),
                new KidsPage("Under the big tree, Pip saw something orange. It was a giant carrot!",
                        Backdrop.MEADOW,
                        List.of(at(SUN, 0.85, 0.2), at(TREE, 0.75, 0.72, 1.3), at(CARROT, 0.55, 0.9, 1.4), at(BUNNY, 0.2, 0.86)),
                        Map.of("saw", c(BUNNY, HOP), "orange", c(CARROT, SHAKE), "giant", c(CARROT, GROW),
                                "carrot", go(BUNNY, WALK, 0.2))),
                new KidsPage("Pip pulled and pulled. Pop! The carrot flew out of the ground, and Pip rolled over and over.",
                        Backdrop.MEADOW,
                        List.of(at(CLOUD, 0.3, 0.16), at(TREE, 0.88, 0.72), at(CARROT, 0.6, 0.9, 1.4), at(BUNNY, 0.42, 0.86)),
                        Map.of("pulled", c(BUNNY, SHAKE), "pop", c(CARROT, HOP), "flew", go(CARROT, FLY, 0.1, -0.1),
                                "rolled", go(BUNNY, WALK, -0.2))),
                new KidsPage("Pip shared the carrot with all his friends, and everyone danced happily in the sun.",
                        Backdrop.MEADOW,
                        List.of(at(SUN, 0.5, 0.2, 1.2), at(FLOWER, 0.12, 0.92), at(FLOWER, 0.9, 0.92), at(CARROT, 0.5, 0.9, 1.2),
                                at(BUNNY, 0.3, 0.86), at(BUNNY, 0.7, 0.86, 0.85)),
                        Map.of("shared", c(CARROT, SHAKE), "friends", c(BUNNY, HOP), "danced", c(BUNNY, DANCE),
                                "sun", c(SUN, REACT)))));
    }

    private static KidsStory daisy() {
        return new KidsStory("Daisy Duck and the Rain", "A duck who loves puddles on a rainy day.", List.of(
                new KidsPage("Daisy the duck loved the river. Every morning she waddled to the water and quacked hello.",
                        Backdrop.RIVER,
                        List.of(at(SUN, 0.85, 0.2), at(TREE, 0.08, 0.62), at(DUCK, 0.2, 0.84)),
                        Map.of("daisy", c(DUCK, WAVE), "waddled", go(DUCK, WALK, 0.25, -0.15), "quacked", c(DUCK, SING))),
                new KidsPage("But today the sky was gray. A big cloud came, and drip, drop, the rain began to fall.",
                        Backdrop.RIVER,
                        List.of(at(CLOUD, 0.2, 0.2, 1.4), at(CLOUD, 0.7, 0.16, 1.1), at(TREE, 0.9, 0.62), at(DUCK, 0.5, 0.68)),
                        Map.of("cloud", go(CLOUD, FLY, 0.12), "drip", c(CLOUD, REACT), "rain", c(CLOUD, REACT),
                                "fall", c(DUCK, SHAKE))),
                new KidsPage("Daisy was not sad. She jumped into a puddle and splashed all around.",
                        Backdrop.RIVER,
                        List.of(at(CLOUD, 0.4, 0.18, 1.3), at(TREE, 0.08, 0.62), at(DUCK, 0.4, 0.82)),
                        Map.of("sad", c(CLOUD, REACT), "jumped", c(DUCK, HOP), "puddle", c(DUCK, SHAKE),
                                "splashed", c(DUCK, SPIN))),
                new KidsPage("Then she swam up the river, quack, quack, with a big smile.",
                        Backdrop.RIVER,
                        List.of(at(CLOUD, 0.75, 0.16), at(TREE, 0.9, 0.6), at(DUCK, 0.1, 0.78)),
                        Map.of("swam", go(DUCK, SWIM, 0.55, -0.02), "quack", c(DUCK, SING), "smile", c(DUCK, HOP))),
                new KidsPage("The rain stopped and the sun came out. Daisy shook her feathers and went to sleep under a tree.",
                        Backdrop.RIVER,
                        List.of(at(SUN, 0.82, 0.2, 1.2), at(TREE, 0.2, 0.62, 1.2), at(DUCK, 0.7, 0.8)),
                        Map.of("sun", c(SUN, REACT), "shook", c(DUCK, SHAKE), "went", go(DUCK, WALK, -0.3, -0.04),
                                "sleep", c(DUCK, SLEEP)))));
    }

    private static KidsStory tilly() {
        return new KidsStory("Tilly the Turtle at the Beach", "A slow turtle, a dancing crab and a beautiful sunset.", List.of(
                new KidsPage("Tilly the turtle woke up on the warm sand. She stretched her little legs and yawned.",
                        Backdrop.BEACH,
                        List.of(at(SUN, 0.85, 0.2), at(STARFISH, 0.15, 0.95, 0.8), at(TURTLE, 0.4, 0.88, 1.3)),
                        Map.of("tilly", c(TURTLE, WAVE), "warm", c(SUN, REACT), "stretched", c(TURTLE, GROW),
                                "yawned", c(TURTLE, SHAKE))),
                new KidsPage("A funny crab waved at her. \"Come and play!\" he said, and he danced sideways.",
                        Backdrop.BEACH,
                        List.of(at(SUN, 0.15, 0.2), at(TURTLE, 0.3, 0.88, 1.3), at(CRAB, 0.7, 0.9)),
                        Map.of("crab", c(CRAB, WAVE), "play", c(TURTLE, HOP), "danced", c(CRAB, DANCE),
                                "sideways", go(CRAB, WALK, -0.2))),
                new KidsPage("Tilly walked slowly to the sea. Step, step, step. The waves sang a soft song.",
                        Backdrop.BEACH,
                        List.of(at(SUN, 0.85, 0.18), at(CLOUD, 0.4, 0.15), at(TURTLE, 0.15, 0.9, 1.3), at(CRAB, 0.85, 0.92, 0.8)),
                        Map.of("walked", go(TURTLE, WALK, 0.45, -0.22), "step", c(CRAB, HOP), "waves", c(CLOUD, REACT),
                                "sang", c(CRAB, SING))),
                new KidsPage("Splash! She swam out into the blue water, and a little starfish spun around and around.",
                        Backdrop.BEACH,
                        List.of(at(SUN, 0.82, 0.2), at(STARFISH, 0.75, 0.9, 0.9), at(TURTLE, 0.3, 0.7, 1.2)),
                        Map.of("splash", c(TURTLE, HOP), "swam", go(TURTLE, SWIM, 0.3, 0.02), "starfish", c(STARFISH, HOP),
                                "spun", c(STARFISH, SPIN))),
                new KidsPage("At sunset, Tilly and her friends watched the sky turn pink. What a beautiful day!",
                        Backdrop.BEACH,
                        List.of(at(SUN, 0.5, 0.3, 1.4), at(STARFISH, 0.15, 0.95), at(CRAB, 0.82, 0.92, 0.8), at(TURTLE, 0.5, 0.88, 1.3)),
                        Map.of("sunset", c(SUN, GROW), "friends", c(CRAB, HOP), "watched", c(TURTLE, WAVE),
                                "beautiful", c(STARFISH, DANCE)))));
    }

    private static KidsStory rocky() {
        return new KidsStory("Rocky the Little Rocket", "A small rocket flies up to say hello to the moon.", List.of(
                new KidsPage("Rocky was a little rocket who dreamed of the stars. Every night he looked up and wished.",
                        Backdrop.NIGHT,
                        List.of(at(STARS, 0.3, 0.2), at(STARS, 0.7, 0.28), at(TREE, 0.9, 0.9), at(ROCKET, 0.45, 0.9, 1.1)),
                        Map.of("rocky", c(ROCKET, WAVE), "stars", c(STARS, REACT), "looked", c(ROCKET, SHAKE),
                                "wished", c(ROCKET, GROW))),
                new KidsPage("Three, two, one... blast off! Rocky flew up into the dark sky, higher and higher.",
                        Backdrop.NIGHT,
                        List.of(at(STARS, 0.25, 0.2), at(STARS, 0.7, 0.18, 0.8), at(MOON, 0.85, 0.3), at(ROCKET, 0.4, 0.9, 1.1)),
                        Map.of("blast", c(ROCKET, SHAKE), "flew", go(ROCKET, FLY, 0.1, -0.55), "higher", c(STARS, REACT))),
                new KidsPage("He zoomed past the sleepy moon and waved hello. The moon yawned and smiled.",
                        Backdrop.NIGHT,
                        List.of(at(STARS, 0.25, 0.25), at(MOON, 0.7, 0.35, 1.3), at(ROCKET, 0.1, 0.8, 1.0)),
                        Map.of("zoomed", go(ROCKET, FLY, 0.55, -0.05), "waved", c(ROCKET, WAVE), "yawned", c(MOON, REACT),
                                "smiled", c(MOON, REACT))),
                new KidsPage("The stars twinkled and danced around him like tiny lights in a big, big party.",
                        Backdrop.NIGHT,
                        List.of(at(STARS, 0.2, 0.25), at(STARS, 0.5, 0.15, 1.1), at(STARS, 0.8, 0.3), at(ROCKET, 0.5, 0.6, 1.0)),
                        Map.of("twinkled", c(STARS, REACT), "danced", c(STARS, DANCE), "party", c(ROCKET, SPIN))),
                new KidsPage("Soon Rocky felt sleepy. He floated home and fell asleep, dreaming of all the stars.",
                        Backdrop.NIGHT,
                        List.of(at(STARS, 0.25, 0.2), at(STARS, 0.75, 0.2), at(TREE, 0.9, 0.9), at(ROCKET, 0.3, 0.45, 1.1)),
                        Map.of("floated", go(ROCKET, FLY, 0.3, 0.4), "asleep", c(ROCKET, SLEEP), "stars", c(STARS, REACT)))));
    }
}
