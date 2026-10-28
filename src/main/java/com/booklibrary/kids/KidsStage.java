package com.booklibrary.kids;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.List;

/**
 * The picture area of an animated story. It draws the page about 30 times per second,
 * lets the child tap characters, and acts out the story: when the voice reads a cue word,
 * the matching characters walk, swim, fly, jump, dance, sing or fall asleep.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the animation loop, character movement, actions, tapping and calm page changes.
 * Purpose: animations can be switched off with {@link #setAnimated(boolean)}. Then the page
 * is drawn once as a still picture and nothing moves, which is calmer and uses less battery.
 * The loop also stops when the view is not visible ({@link #setActive(boolean)}).
 */
public class KidsStage extends StackPane {

    private static final long FRAME_NANOS = 33_000_000L;
    private static final double NONE = -1000;

    /** What one character is doing right now. */
    private static final class State {
        double offX;
        double offY;
        double fromX;
        double fromY;
        double toX;
        double toY;
        double moveStart = NONE;
        double moveDuration;
        boolean flip;
        Action action;
        double actionStart = NONE;
        double pokeAt = NONE;
        boolean sleeping;

        boolean moving(double now) {
            return moveStart != NONE && now < moveStart + moveDuration;
        }

        void settle(double now) {
            if (moveStart != NONE && now >= moveStart + moveDuration) {
                offX = toX;
                offY = toY;
                moveStart = NONE;
            }
        }

        double x(double now) {
            if (moving(now)) {
                return fromX + (toX - fromX) * smooth((now - moveStart) / moveDuration);
            }
            return moveStart == NONE ? offX : toX;
        }

        double y(double now) {
            if (moving(now)) {
                return fromY + (toY - fromY) * smooth((now - moveStart) / moveDuration);
            }
            return moveStart == NONE ? offY : toY;
        }
    }

    private final Canvas canvas = new Canvas();
    private final AnimationTimer timer;
    private KidsPage page;
    private State[] states = new State[0];
    private boolean animated = true;
    private boolean active;
    private long startNanos;
    private long lastNanos;
    private double now;

    /** Creates an empty stage. Call {@link #showPage(KidsPage)} to show something. */
    public KidsStage() {
        setMinSize(0, 0);
        setStyle("-fx-background-color: #0b0b0f; -fx-background-radius: 14;");
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
        timer = new AnimationTimer() {
            @Override
            public void handle(long nanos) {
                if (startNanos == 0) {
                    startNanos = nanos;
                }
                if (nanos - lastNanos < FRAME_NANOS) {
                    return;
                }
                lastNanos = nanos;
                now = (nanos - startNanos) / 1_000_000_000.0;
                render();
            }
        };
        widthProperty().addListener((obs, oldValue, newValue) -> renderStill());
        heightProperty().addListener((obs, oldValue, newValue) -> renderStill());
        canvas.setOnMouseClicked(event -> tap(event.getX(), event.getY()));
    }

    private static double smooth(double p) {
        double x = Math.max(0, Math.min(1, p));
        return x * x * (3 - 2 * x);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean isCreature(ActorType type) {
        return switch (type) {
            case FOX, OWL, FISH, JELLYFISH, BIRD, BUNNY, DUCK, TURTLE, CRAB, STARFISH, ROCKET -> true;
            default -> false;
        };
    }

    /**
     * Shows a page.
     *
     * @param newPage the page
     */
    public void showPage(KidsPage newPage) {
        this.page = newPage;
        this.states = new State[newPage.actors().size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = new State();
        }
        renderStill();
    }

    /**
     * Shows a page with a calm fade between the old and the new picture.
     *
     * @param newPage the page
     */
    public void fadeToPage(KidsPage newPage) {
        if (!animated || !active) {
            showPage(newPage);
            return;
        }
        FadeTransition out = new FadeTransition(Duration.millis(260), canvas);
        out.setToValue(0);
        out.setOnFinished(event -> {
            showPage(newPage);
            FadeTransition in = new FadeTransition(Duration.millis(360), canvas);
            in.setToValue(1);
            in.play();
        });
        out.play();
    }

    /**
     * Turns the animation on or off.
     *
     * @param value {@code true} for moving pictures, {@code false} for a still picture
     */
    public void setAnimated(boolean value) {
        this.animated = value;
        updateTimer();
        renderStill();
    }

    /**
     * Tells the stage if it is on screen. Nothing is calculated while it is not.
     *
     * @param value {@code true} when the tab is visible
     */
    public void setActive(boolean value) {
        this.active = value;
        updateTimer();
    }

    private void updateTimer() {
        if (active && animated) {
            timer.start();
        } else {
            timer.stop();
            for (State state : states) {
                state.settle(Double.MAX_VALUE);
                state.action = null;
                state.actionStart = NONE;
                state.pokeAt = NONE;
            }
            startNanos = 0;
            now = 0;
        }
    }

    /**
     * Acts out a cue: every character of the cue's type does the action.
     *
     * @param cue what should happen
     */
    public void perform(Cue cue) {
        if (!animated || page == null || cue == null) {
            return;
        }
        List<Placed> actors = page.actors();
        for (int i = 0; i < actors.size(); i++) {
            if (actors.get(i).type() == cue.type()) {
                apply(i, actors.get(i), states[i], cue);
            }
        }
    }

    private void apply(int index, Placed placed, State state, Cue cue) {
        Action action = cue.action();
        if (action == Action.REACT) {
            state.pokeAt = now;
            return;
        }
        if (action == Action.SLEEP) {
            state.sleeping = true;
            return;
        }
        state.sleeping = false;
        if (action == Action.WALK || action == Action.SWIM || action == Action.FLY) {
            double curX = state.x(now);
            double curY = state.y(now);
            state.fromX = curX;
            state.fromY = curY;
            state.toX = clamp(placed.x() + curX + cue.moveX(), 0.05, 0.95) - placed.x();
            state.toY = clamp(placed.y() + curY + cue.moveY(), 0.1, 0.98) - placed.y();
            state.moveStart = now;
            state.moveDuration = action.seconds();
            if (cue.moveX() != 0) {
                state.flip = cue.moveX() < 0;
            }
            return;
        }
        state.action = action;
        state.actionStart = now;
    }

    private void tap(double x, double y) {
        if (!animated || page == null) {
            return;
        }
        double unit = Math.max(1, getHeight()) / 420.0;
        List<Placed> actors = page.actors();
        for (int i = actors.size() - 1; i >= 0; i--) {
            Placed placed = actors.get(i);
            State state = states[i];
            double scale = unit * placed.scale();
            double cx = (placed.x() + state.x(now)) * getWidth();
            double cy = (placed.y() + state.y(now)) * getHeight() - 45 * scale;
            if (Math.hypot(x - cx, y - cy) <= 55 * scale) {
                state.pokeAt = now;
                if (isCreature(placed.type())) {
                    state.sleeping = false;
                    state.action = Action.HOP;
                    state.actionStart = now;
                }
                return;
            }
        }
    }

    private void renderStill() {
        if (!animated || !active) {
            now = 0;
            render();
        }
    }

    private void render() {
        double w = getWidth();
        double h = getHeight();
        if (w < 2 || h < 2 || page == null) {
            return;
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        double time = animated ? now : 0;
        KidsArt.backdrop(g, page.backdrop(), w, h, time);
        double unit = h / 420.0;
        List<Placed> actors = page.actors();
        for (int i = 0; i < actors.size(); i++) {
            drawActor(g, actors.get(i), states[i], w, h, unit, time);
        }
    }

    private void drawActor(GraphicsContext g, Placed placed, State state, double w, double h, double unit, double time) {
        if (!animated) {
            g.save();
            g.translate(placed.x() * w, placed.y() * h);
            g.scale(unit * placed.scale(), unit * placed.scale());
            KidsArt.actor(g, placed.type(), 0, -1, false, false);
            g.restore();
            return;
        }
        state.settle(now);
        boolean moving = state.moving(now);
        g.save();
        g.translate((placed.x() + state.x(now)) * w, (placed.y() + state.y(now)) * h);
        g.scale(unit * placed.scale(), unit * placed.scale());
        if (state.flip) {
            g.scale(-1, 1);
        }
        if (moving) {
            g.translate(0, -KidsArt.walkBob(placed.type(), now));
            g.rotate(KidsArt.walkTilt(placed.type(), now));
        }
        double age = -1;
        double progress = 0;
        Action action = state.action;
        if (action != null && state.actionStart != NONE) {
            age = now - state.actionStart;
            progress = age / action.seconds();
            if (progress >= 1) {
                state.action = null;
                state.actionStart = NONE;
                action = null;
            }
        }
        if (action != null) {
            applyAction(g, action, age, progress);
        }
        double pokeAge = state.pokeAt == NONE ? -1 : now - state.pokeAt;
        KidsArt.actor(g, placed.type(), time, pokeAge, moving, state.sleeping);
        if (action == Action.SING) {
            KidsArt.notes(g, age);
        }
        if (state.sleeping) {
            KidsArt.zzz(g, now);
        }
        g.restore();
    }

    /** Moves or turns the drawing space for the action, before the character is drawn. */
    private static void applyAction(GraphicsContext g, Action action, double age, double p) {
        switch (action) {
            case HOP -> g.translate(0, -Math.abs(Math.sin(p * Math.PI)) * 60);
            case SPIN -> {
                g.translate(0, -45);
                g.rotate(360 * smooth(p));
                g.translate(0, 45);
            }
            case DANCE -> {
                g.translate(0, -Math.abs(Math.sin(age * 7)) * 14);
                g.rotate(Math.sin(age * 9) * 9);
            }
            case WAVE -> g.rotate(Math.sin(age * 16) * 7 * (1 - p));
            case SHAKE -> g.translate(Math.sin(age * 45) * 6 * (1 - p), 0);
            case GROW -> {
                double scale = 1 + 0.4 * Math.sin(p * Math.PI);
                g.scale(scale, scale);
            }
            default -> {
            }
        }
    }
}
