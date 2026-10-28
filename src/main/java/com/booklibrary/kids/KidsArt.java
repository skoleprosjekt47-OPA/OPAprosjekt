package com.booklibrary.kids;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;

import java.util.Random;

/**
 * Draws the places and characters of the animated stories with the JavaFX canvas.
 * Nothing here is a picture file: every frame is calculated from the time, so the
 * characters really move.
 *
 * Author: Farid Rahmani
 * Contribution: Designed and drew all characters, backgrounds and reactions in code.
 * Purpose: an actor is drawn around the point (0, 0), which is its feet, and is about
 * 100 units tall. The caller moves and scales the drawing space before calling.
 * {@code t} is the time in seconds. {@code age} is the seconds since the actor was tapped
 * or its word was read; it is negative when nothing happened.
 */
public final class KidsArt {

    /** How long a reaction lasts, in seconds. */
    public static final double REACTION_SECONDS = 2.2;

    private static final double[] STAR_X = new double[60];
    private static final double[] STAR_Y = new double[60];
    private static final double[] STAR_PHASE = new double[60];

    static {
        Random random = new Random(7);
        for (int i = 0; i < STAR_X.length; i++) {
            STAR_X[i] = random.nextDouble();
            STAR_Y[i] = random.nextDouble() * 0.6;
            STAR_PHASE[i] = random.nextDouble() * 6.28;
        }
    }

    private KidsArt() {
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Returns how strong a reaction is.
     *
     * @param age seconds since the reaction started, or negative for none
     * @return 1 at the start, falling to 0
     */
    public static double strength(double age) {
        if (age < 0 || age > REACTION_SECONDS) {
            return 0;
        }
        return 1 - age / REACTION_SECONDS;
    }

    private static void oval(GraphicsContext g, double cx, double cy, double rx, double ry) {
        g.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
    }

    private static void star(GraphicsContext g, double cx, double cy, double radius) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? radius : radius * 0.45;
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            xs[i] = cx + Math.cos(angle) * r;
            ys[i] = cy + Math.sin(angle) * r;
        }
        g.fillPolygon(xs, ys, 10);
    }

    private static Color alpha(Color color, double opacity) {
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(1, opacity)));
    }

    // ------------------------------------------------------------------ backdrops

    /**
     * Draws the background of a page.
     *
     * @param g        the canvas
     * @param backdrop the place
     * @param w        width
     * @param h        height
     * @param t        time in seconds
     */
    public static void backdrop(GraphicsContext g, Backdrop backdrop, double w, double h, double t) {
        switch (backdrop) {
            case MEADOW -> meadow(g, w, h, t, false);
            case RIVER -> meadow(g, w, h, t, true);
            case NIGHT -> night(g, w, h, t);
            case SEA -> sea(g, w, h, t);
            case BEACH -> beach(g, w, h, t);
        }
    }

    private static void meadow(GraphicsContext g, double w, double h, double t, boolean river) {
        g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#7cc8f5")), new Stop(1, Color.web("#e6f6ff"))));
        g.fillRect(0, 0, w, h);
        g.setFill(Color.web("#8fd36b"));
        oval(g, w * 0.2, h * 0.78, w * 0.55, h * 0.3);
        g.setFill(Color.web("#74c257"));
        oval(g, w * 0.85, h * 0.85, w * 0.6, h * 0.3);
        g.setFill(Color.web("#5fb04a"));
        g.fillRect(0, h * 0.82, w, h * 0.18);
        if (river) {
            g.setFill(Color.web("#3f9bd8"));
            g.fillRect(0, h * 0.74, w, h * 0.1);
            g.setStroke(Color.web("#bfe6ff"));
            g.setLineWidth(2);
            for (int i = 0; i < 9; i++) {
                double x = ((i * 0.13 + t * 0.03) % 1.1) * w;
                double y = h * (0.77 + 0.03 * (i % 3) / 2.0);
                g.strokeLine(x, y + Math.sin(t * 2 + i) * 2, x + w * 0.05, y + Math.sin(t * 2 + i + 1) * 2);
            }
        }
        for (int i = 0; i < 14; i++) {
            double x = (i * 0.083 + 0.02) * w;
            double y = h * (0.9 + 0.08 * ((i * 7) % 5) / 5.0);
            double sway = Math.sin(t * 1.5 + i) * 3;
            g.setStroke(Color.web("#3f8f3a"));
            g.setLineWidth(2);
            g.strokeLine(x, y + 10, x + sway, y);
            g.setFill(i % 3 == 0 ? Color.web("#ff8fb1") : i % 3 == 1 ? Color.web("#fff27a") : Color.WHITE);
            oval(g, x + sway, y - 2, 4, 4);
        }
    }

    private static void night(GraphicsContext g, double w, double h, double t) {
        g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a1440")), new Stop(1, Color.web("#3a3a7a"))));
        g.fillRect(0, 0, w, h);
        for (int i = 0; i < STAR_X.length; i++) {
            double twinkle = 0.5 + 0.5 * Math.sin(t * 1.6 + STAR_PHASE[i]);
            g.setFill(alpha(Color.WHITE, 0.35 + 0.65 * twinkle));
            oval(g, STAR_X[i] * w, STAR_Y[i] * h, 1.2 + twinkle, 1.2 + twinkle);
        }
        g.setFill(Color.web("#142a3d"));
        oval(g, w * 0.3, h * 1.02, w * 0.6, h * 0.28);
        g.setFill(Color.web("#0f2233"));
        oval(g, w * 0.9, h * 1.05, w * 0.5, h * 0.25);
        for (int i = 0; i < 12; i++) {
            double x = (0.1 + 0.08 * i + 0.04 * Math.sin(t * 0.4 + i)) * w;
            double y = h * (0.62 + 0.25 * ((i * 5) % 7) / 7.0) + Math.sin(t * 0.9 + i * 2) * 10;
            double glow = 0.4 + 0.6 * Math.max(0, Math.sin(t * 2 + i));
            g.setFill(alpha(Color.web("#f4ff8a"), 0.25 * glow));
            oval(g, x, y, 8, 8);
            g.setFill(alpha(Color.web("#fbffc0"), glow));
            oval(g, x, y, 2.2, 2.2);
        }
    }

    private static void sea(GraphicsContext g, double w, double h, double t) {
        g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1d9bd6")), new Stop(1, Color.web("#042f55"))));
        g.fillRect(0, 0, w, h);
        for (int i = 0; i < 4; i++) {
            double sway = Math.sin(t * 0.5 + i) * w * 0.03;
            double x = w * (0.15 + i * 0.25) + sway;
            g.setFill(alpha(Color.WHITE, 0.06));
            g.fillPolygon(new double[]{x, x + w * 0.1, x + w * 0.2 + sway, x - w * 0.05 + sway},
                    new double[]{0, 0, h, h}, 4);
        }
        g.setFill(Color.web("#d9c389"));
        oval(g, w * 0.3, h * 1.04, w * 0.7, h * 0.14);
        oval(g, w * 0.9, h * 1.06, w * 0.5, h * 0.12);
        for (int i = 0; i < 14; i++) {
            double cycle = (t * 0.06 + i * 0.137) % 1.0;
            double x = w * ((i * 0.071 + 0.03) % 1.0) + Math.sin(t + i) * 6;
            double y = h * (1.0 - cycle);
            g.setStroke(alpha(Color.WHITE, 0.55));
            g.setLineWidth(1.4);
            double r = 2 + (i % 4) * 1.5;
            g.strokeOval(x - r, y - r, r * 2, r * 2);
        }
    }

    // ------------------------------------------------------------------ actors

    /**
     * Draws one actor.
     *
     * @param g    the canvas, already moved so that (0, 0) is the actor's feet
     * @param type the actor
     * @param t    time in seconds
     * @param age  seconds since the last reaction, or negative
     * @param walking {@code true} while the actor is travelling to another place
     * @param sleeping {@code true} when the actor is asleep
     */
    public static void actor(GraphicsContext g, ActorType type, double t, double age, boolean walking, boolean sleeping) {
        double k = strength(age);
        double move = walking ? 1 : 0;
        switch (type) {
            case FOX -> fox(g, t, age, k, move, sleeping);
            case OWL -> owl(g, t, age, k, sleeping);
            case FISH -> fish(g, t, age, k, move, sleeping);
            case BUNNY -> bunny(g, t, age, k, move, sleeping);
            case CARROT -> carrot(g, t, age, k);
            case FLOWER -> flower(g, t, age, k);
            case DUCK -> duck(g, t, age, k, move, sleeping);
            case TURTLE -> turtle(g, t, age, k, move, sleeping);
            case CRAB -> crab(g, t, age, k, move);
            case STARFISH -> starfish(g, t, age, k);
            case ROCKET -> rocket(g, t, age, k, move, sleeping);
            case JELLYFISH -> jellyfish(g, t, age, k);
            case BIRD -> bird(g, t, age, k);
            case CLOUD -> cloud(g, t, age, k);
            case SUN -> sun(g, t, age, k);
            case MOON -> moon(g, t, age, k);
            case STARS -> stars(g, t, age, k);
            case TREE -> tree(g, t, age, k);
            case SEAWEED -> seaweed(g, t, age, k);
        }
    }

    private static void fox(GraphicsContext g, double t, double age, double k, double move, boolean sleeping) {
        double hop = age >= 0 && age < 1.0 ? -Math.abs(Math.sin(age * Math.PI)) * 55 : 0;
        double squash = 1 + 0.03 * Math.sin(t * 2.2);
        g.save();
        g.translate(0, hop);
        // shadow
        g.setFill(alpha(Color.BLACK, 0.18 - hop / 700.0));
        oval(g, 0, -hop, 36, 6);
        // tail
        g.save();
        g.translate(-30, -34);
        g.rotate(-25 + Math.sin(t * 3) * 10 + k * Math.sin(t * 22) * 22);
        g.setFill(Color.web("#f28c28"));
        oval(g, -22, 0, 26, 11);
        g.setFill(Color.WHITE);
        oval(g, -42, 0, 9, 8);
        g.restore();
        // back legs and body
        double stride = Math.sin(t * 14) * 7 * move;
        g.setFill(Color.web("#e07a1f"));
        oval(g, -12 + stride, -6 - Math.max(0, -stride) * 0.6, 8, 6);
        oval(g, 14 - stride, -6 - Math.max(0, stride) * 0.6, 8, 6);
        g.setFill(Color.web("#f28c28"));
        g.save();
        g.scale(1, squash);
        oval(g, 0, -34, 34, 22);
        g.restore();
        g.setFill(Color.web("#fff3df"));
        oval(g, 8, -28, 18, 12);
        // head
        g.setFill(Color.web("#f28c28"));
        oval(g, 30, -58, 22, 19);
        g.fillPolygon(new double[]{16, 22, 30}, new double[]{-70, -96, -74}, 3);
        g.fillPolygon(new double[]{34, 44, 46}, new double[]{-74, -97, -68}, 3);
        g.setFill(Color.web("#3a2418"));
        g.fillPolygon(new double[]{19, 22, 26}, new double[]{-74, -90, -76}, 3);
        g.fillPolygon(new double[]{38, 43, 44}, new double[]{-76, -90, -72}, 3);
        g.setFill(Color.web("#fff3df"));
        oval(g, 38, -50, 14, 9);
        // eyes (they blink now and then)
        boolean blink = (t % 4.0) < 0.14 || (k > 0.5) || sleeping;
        g.setFill(Color.web("#2a1a10"));
        if (blink) {
            g.fillRect(26, -61, 7, 2);
            g.fillRect(40, -61, 7, 2);
        } else {
            oval(g, 30, -61, 3.2, 4);
            oval(g, 43, -61, 3.2, 4);
        }
        oval(g, 50, -54, 3.4, 2.6);
        g.setStroke(Color.web("#2a1a10"));
        g.setLineWidth(1.4);
        g.strokeArc(38, -53, 12, 8, 200, 140, ArcType.OPEN);
        // front legs
        g.setFill(Color.web("#3a2418"));
        oval(g, -4 - stride, -4 - Math.max(0, stride) * 0.6, 7, 5);
        oval(g, 22 + stride, -4 - Math.max(0, -stride) * 0.6, 7, 5);
        g.restore();
    }

    private static void owl(GraphicsContext g, double t, double age, double k, boolean sleeping) {
        double hop = age >= 0 && age < 0.6 ? -Math.abs(Math.sin(age / 0.6 * Math.PI)) * 14 : 0;
        g.save();
        g.translate(0, hop);
        g.setFill(Color.web("#8a5a36"));
        oval(g, 0, -34, 26, 34);
        g.setFill(Color.web("#d8b48a"));
        oval(g, 0, -26, 16, 22);
        // wings
        double flap = k * Math.sin(t * 25) * 12;
        g.setFill(Color.web("#6e4527"));
        g.save();
        g.translate(-24, -38);
        g.rotate(-flap);
        oval(g, -3, 6, 8, 22);
        g.restore();
        g.save();
        g.translate(24, -38);
        g.rotate(flap);
        oval(g, 3, 6, 8, 22);
        g.restore();
        // ears
        g.setFill(Color.web("#8a5a36"));
        g.fillPolygon(new double[]{-22, -14, -20}, new double[]{-62, -58, -76}, 3);
        g.fillPolygon(new double[]{22, 14, 20}, new double[]{-62, -58, -76}, 3);
        // eyes
        double big = 1 + 0.35 * k;
        double look = Math.sin(t * 0.8) * 2.5;
        boolean blink = (t % 5.0) < 0.15 || sleeping;
        g.setFill(Color.WHITE);
        oval(g, -10, -52, 10 * big, blink ? 1.2 : 10 * big);
        oval(g, 10, -52, 10 * big, blink ? 1.2 : 10 * big);
        if (!blink) {
            g.setFill(Color.web("#201208"));
            oval(g, -10 + look, -52, 4.5, 4.5);
            oval(g, 10 + look, -52, 4.5, 4.5);
        }
        g.setFill(Color.web("#f0a020"));
        g.fillPolygon(new double[]{-4, 4, 0}, new double[]{-45, -45, -36}, 3);
        g.setFill(Color.web("#f0a020"));
        oval(g, -8, 0, 6, 3);
        oval(g, 8, 0, 6, 3);
        g.restore();
    }

    private static void fish(GraphicsContext g, double t, double age, double k, double move, boolean sleeping) {
        double driftX = Math.sin(t * 0.8) * 26;
        double driftY = Math.sin(t * 1.3) * 7;
        g.save();
        g.translate(driftX, driftY - 25);
        if (age >= 0 && age < 1.2) {
            g.rotate(Math.sin(age / 1.2 * Math.PI) * 360 * (age / 1.2));
        }
        double tail = Math.sin(t * (7 + 8 * move)) * (sleeping ? 4 : 14);
        g.setFill(Color.web("#2d7fe0"));
        g.fillPolygon(new double[]{-30, -52, -52}, new double[]{0, -16 + tail, 16 + tail}, 3);
        oval(g, 0, 0, 34, 22);
        g.setFill(Color.web("#7cc0ff"));
        oval(g, 4, 8, 22, 9);
        g.setFill(Color.web("#2d7fe0"));
        g.fillPolygon(new double[]{-6, 8, 0}, new double[]{-18, -18, -34 + Math.sin(t * 5) * 4}, 3);
        if (sleeping) {
            g.setStroke(Color.web("#10233a"));
            g.setLineWidth(2);
            g.strokeLine(14, -5, 23, -5);
        } else {
            g.setFill(Color.WHITE);
            oval(g, 18, -5, 7, 7);
            g.setFill(Color.web("#10233a"));
            oval(g, 20, -5, 3.4, 3.4);
        }
        g.setStroke(Color.web("#10233a"));
        g.setLineWidth(1.5);
        g.strokeArc(18, 2, 12, 8, 200, 140, ArcType.OPEN);
        g.restore();
        // bubbles: one now and then, many after a reaction
        g.setStroke(alpha(Color.WHITE, 0.85));
        g.setLineWidth(1.5);
        int count = age >= 0 && age < REACTION_SECONDS ? 9 : 2;
        for (int i = 0; i < count; i++) {
            double life = ((t * 0.5 + i * (1.0 / count)) % 1.0);
            double bx = driftX + 34 + Math.sin(t * 2 + i) * 6 + i * 2;
            double by = driftY - 30 - life * 110;
            double r = 2.5 + (i % 3) * 2;
            g.strokeOval(bx - r, by - r, r * 2, r * 2);
        }
    }

    private static void jellyfish(GraphicsContext g, double t, double age, double k) {
        double pulse = 1 + 0.08 * Math.sin(t * 2.4);
        double bob = Math.sin(t * 1.1) * 8;
        g.save();
        g.translate(0, -40 + bob);
        double glow = 0.25 + 0.5 * k;
        g.setFill(new RadialGradient(0, 0, 0, -10, 70, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.web("#ffd0f0"), glow)), new Stop(1, alpha(Color.web("#ffd0f0"), 0))));
        oval(g, 0, -10, 70, 70);
        g.setStroke(alpha(Color.web("#ff9ad8"), 0.85));
        g.setLineWidth(3);
        for (int i = -3; i <= 3; i++) {
            double[] xs = new double[8];
            double[] ys = new double[8];
            for (int s = 0; s < 8; s++) {
                xs[s] = i * 8 + Math.sin(t * 2.5 + s * 0.8 + i) * 4;
                ys[s] = 8 + s * 8;
            }
            g.strokePolyline(xs, ys, 8);
        }
        g.setFill(alpha(Color.web("#ff9ad8"), 0.9));
        g.save();
        g.scale(pulse, 1 / pulse);
        g.fillArc(-30, -30, 60, 56, 0, 180, ArcType.ROUND);
        g.restore();
        g.setFill(Color.web("#4a1d44"));
        oval(g, -9, -8, 2.6, 3);
        oval(g, 9, -8, 2.6, 3);
        g.setStroke(Color.web("#4a1d44"));
        g.setLineWidth(1.4);
        g.strokeArc(-5, -9, 10, 7, 200, 140, ArcType.OPEN);
        g.restore();
    }

    private static void bird(GraphicsContext g, double t, double age, double k) {
        double flapSpeed = 5 + 14 * k;
        double flap = Math.sin(t * flapSpeed) * 26;
        double fly = age >= 0 && age < 1.6 ? -Math.sin(age / 1.6 * Math.PI) * 55 : 0;
        double x = Math.sin(t * 0.6) * 24 + (age >= 0 && age < 1.6 ? age * 20 : 0);
        g.save();
        g.translate(x, fly - 34 + Math.sin(t * 1.7) * 5);
        g.setFill(Color.web("#4aa3df"));
        oval(g, 0, 0, 16, 11);
        g.setFill(Color.web("#2f7fb5"));
        g.save();
        g.translate(-2, -4);
        g.rotate(-flap);
        g.fillPolygon(new double[]{0, 18, -14}, new double[]{0, -22, -20}, 3);
        g.restore();
        g.setFill(Color.web("#4aa3df"));
        oval(g, 14, -6, 8, 8);
        g.setFill(Color.web("#ffb347"));
        g.fillPolygon(new double[]{21, 31, 21}, new double[]{-8, -5, -3}, 3);
        g.setFill(Color.BLACK);
        oval(g, 16, -8, 1.6, 1.6);
        g.setFill(Color.web("#2f7fb5"));
        g.fillPolygon(new double[]{-14, -30, -26}, new double[]{0, -4, 6}, 3);
        if (k > 0) {
            g.setFill(alpha(Color.web("#ffffff"), k));
            oval(g, 36 + age * 20, -22 - age * 12, 3, 3);
            oval(g, 48 + age * 20, -30 - age * 12, 2.5, 2.5);
        }
        g.restore();
    }

    private static void cloud(GraphicsContext g, double t, double age, double k) {
        double drift = Math.sin(t * 0.25) * 30;
        g.save();
        g.translate(drift, -60);
        Color body = k > 0 ? Color.color(1 - 0.25 * k, 1 - 0.25 * k, 1 - 0.18 * k) : Color.WHITE;
        g.setFill(alpha(body, 0.97));
        oval(g, -30, 4, 28, 20);
        oval(g, 0, -8, 34, 28);
        oval(g, 32, 4, 28, 20);
        g.fillRect(-30, 4, 62, 20);
        if (k > 0) {
            g.setStroke(alpha(Color.web("#5aa8e0"), 0.9));
            g.setLineWidth(2);
            for (int i = 0; i < 10; i++) {
                double fall = ((age * 1.1 + i * 0.13) % 1.0);
                double rx = -38 + i * 8.5;
                double ry = 24 + fall * 90;
                g.strokeLine(rx, ry, rx - 2, ry + 8);
            }
        }
        g.restore();
    }

    private static void sun(GraphicsContext g, double t, double age, double k) {
        g.save();
        g.translate(0, -40);
        g.setFill(new RadialGradient(0, 0, 0, 0, 90, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.web("#fff5a0"), 0.6 + 0.3 * k)), new Stop(1, alpha(Color.web("#fff5a0"), 0))));
        oval(g, 0, 0, 90, 90);
        g.setStroke(Color.web("#ffd23f"));
        g.setLineWidth(5);
        double rot = t * 0.3;
        double len = 18 + 14 * k;
        for (int i = 0; i < 12; i++) {
            double a = rot + i * Math.PI / 6;
            g.strokeLine(Math.cos(a) * 36, Math.sin(a) * 36, Math.cos(a) * (36 + len), Math.sin(a) * (36 + len));
        }
        g.setFill(Color.web("#ffd23f"));
        oval(g, 0, 0, 32, 32);
        g.setFill(Color.web("#7a4a00"));
        oval(g, -10, -5, 2.8, 3.4);
        oval(g, 10, -5, 2.8, 3.4);
        g.setStroke(Color.web("#7a4a00"));
        g.setLineWidth(2);
        g.strokeArc(-11, -4, 22, 6 + 10 * k, 200, 140, ArcType.OPEN);
        g.restore();
    }

    private static void moon(GraphicsContext g, double t, double age, double k) {
        g.save();
        g.translate(0, -40);
        g.setFill(new RadialGradient(0, 0, 0, 0, 80, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.web("#fffbe0"), 0.5)), new Stop(1, alpha(Color.web("#fffbe0"), 0))));
        oval(g, 0, 0, 80, 80);
        g.setFill(Color.web("#fff6c8"));
        oval(g, 0, 0, 34, 34);
        g.setFill(alpha(Color.web("#e6d88a"), 0.6));
        oval(g, -14, 12, 6, 5);
        oval(g, 14, -14, 5, 4);
        oval(g, 12, 16, 4, 3);
        g.setStroke(Color.web("#6b5a1a"));
        g.setLineWidth(2);
        boolean sleepy = k > 0 || Math.sin(t * 0.7) > 0.8;
        if (sleepy) {
            g.strokeArc(-16, -7, 11, 7, 0, 180, ArcType.OPEN);
            g.strokeArc(5, -7, 11, 7, 0, 180, ArcType.OPEN);
        } else {
            g.setFill(Color.web("#6b5a1a"));
            oval(g, -10, -5, 2.6, 3.2);
            oval(g, 10, -5, 2.6, 3.2);
        }
        g.strokeArc(-9, 2, 18, 10 + 6 * k, 200, 140, ArcType.OPEN);
        if (k > 0) {
            g.setFill(alpha(Color.WHITE, k));
            g.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 16));
            g.fillText("z", 30, -30 - age * 8);
            g.fillText("Z", 42, -44 - age * 8);
        }
        g.restore();
    }

    private static void stars(GraphicsContext g, double t, double age, double k) {
        double[][] places = {{0, -30}, {45, -10}, {-48, -2}, {24, -60}, {-26, -52}};
        for (int i = 0; i < places.length; i++) {
            double twinkle = 0.75 + 0.25 * Math.sin(t * 2.2 + i * 1.7);
            double size = 10 * twinkle * (1 + 0.8 * k);
            g.setFill(alpha(Color.web("#fff3a0"), 0.35));
            oval(g, places[i][0], places[i][1], size * 1.6, size * 1.6);
            g.setFill(Color.web("#ffe96a"));
            star(g, places[i][0], places[i][1], size);
            if (k > 0.2) {
                g.setStroke(alpha(Color.WHITE, k));
                g.setLineWidth(1.6);
                double r = size * 2;
                g.strokeLine(places[i][0] - r, places[i][1], places[i][0] + r, places[i][1]);
                g.strokeLine(places[i][0], places[i][1] - r, places[i][0], places[i][1] + r);
            }
        }
    }

    private static void tree(GraphicsContext g, double t, double age, double k) {
        double sway = Math.sin(t * 1.1) * 1.8 + k * Math.sin(t * 18) * 3.5;
        g.setFill(Color.web("#7a4b26"));
        g.fillRect(-8, -70, 16, 70);
        g.save();
        g.translate(0, -70);
        g.rotate(sway);
        g.setFill(Color.web("#2f8f3a"));
        oval(g, 0, -30, 44, 38);
        g.setFill(Color.web("#3da84a"));
        oval(g, -22, -14, 30, 24);
        oval(g, 24, -16, 30, 24);
        g.setFill(Color.web("#56c15f"));
        oval(g, -6, -46, 18, 14);
        g.restore();
        if (k > 0) {
            g.setFill(Color.web("#4db35a"));
            for (int i = 0; i < 7; i++) {
                double fall = (age * 0.7 + i * 0.09) % 1.0;
                double lx = -40 + i * 13 + Math.sin(age * 5 + i) * 8;
                double ly = -90 + fall * 90;
                g.save();
                g.translate(lx, ly);
                g.rotate(age * 200 + i * 40);
                oval(g, 0, 0, 5, 2.5);
                g.restore();
            }
        }
    }

    private static void seaweed(GraphicsContext g, double t, double age, double k) {
        double strong = 1 + 1.6 * k;
        for (int blade = -1; blade <= 1; blade++) {
            double[] xs = new double[10];
            double[] ys = new double[10];
            for (int i = 0; i < 10; i++) {
                xs[i] = blade * 12 + Math.sin(t * 1.4 + i * 0.5 + blade) * i * 1.6 * strong;
                ys[i] = -i * 9.5;
            }
            g.setStroke(Color.web(blade == 0 ? "#2f9e5b" : "#3fb36d"));
            g.setLineWidth(8 - Math.abs(blade) * 2);
            g.strokePolyline(xs, ys, 10);
        }
    }

    // ------------------------------------------------------------------ effects used by the stage

    /**
     * Returns how high a walking character bobs up and down, so that a bunny hops and a turtle plods.
     *
     * @param type the character
     * @param t    time in seconds
     * @return the lift in drawing units (positive is up)
     */
    public static double walkBob(ActorType type, double t) {
        return switch (type) {
            case BUNNY -> Math.abs(Math.sin(t * 8)) * 22;
            case FOX, DUCK, CRAB -> Math.abs(Math.sin(t * 12)) * 5;
            case TURTLE -> Math.abs(Math.sin(t * 4)) * 2;
            case FISH, JELLYFISH, BIRD -> Math.sin(t * 5) * 4;
            default -> 0;
        };
    }

    /**
     * Returns how much a walking character leans from side to side, so that a duck waddles.
     *
     * @param type the character
     * @param t    time in seconds
     * @return the angle in degrees
     */
    public static double walkTilt(ActorType type, double t) {
        return switch (type) {
            case DUCK -> Math.sin(t * 9) * 8;
            case TURTLE -> Math.sin(t * 4) * 3;
            default -> 0;
        };
    }

    /**
     * Draws music notes that float up from a singing character.
     *
     * @param g   the canvas
     * @param age seconds since the song started
     */
    public static void notes(GraphicsContext g, double age) {
        for (int i = 0; i < 3; i++) {
            double life = (age * 0.55 + i * 0.33) % 1.0;
            double x = 18 + i * 16 + Math.sin(age * 3 + i) * 5;
            double y = -92 - life * 60;
            g.setFill(alpha(Color.web("#fff3a0"), 1 - life));
            oval(g, x, y, 5, 3.6);
            g.setStroke(alpha(Color.web("#fff3a0"), 1 - life));
            g.setLineWidth(2);
            g.strokeLine(x + 4.5, y, x + 4.5, y - 16);
            g.strokeLine(x + 4.5, y - 16, x + 11, y - 12);
        }
    }

    /**
     * Draws sleeping "Z" letters that float up.
     *
     * @param g the canvas
     * @param t time in seconds
     */
    public static void zzz(GraphicsContext g, double t) {
        for (int i = 0; i < 3; i++) {
            double life = (t * 0.35 + i * 0.33) % 1.0;
            double size = 6 + life * 8;
            double x = 24 + i * 12 + life * 10;
            double y = -96 - life * 50;
            g.setStroke(alpha(Color.WHITE, 0.9 - life * 0.8));
            g.setLineWidth(2.2);
            g.strokePolyline(new double[]{x, x + size, x, x + size}, new double[]{y, y, y + size, y + size}, 4);
        }
    }

    // ------------------------------------------------------------------ more characters

    private static void bunny(GraphicsContext g, double t, double age, double k, double move, boolean sleeping) {
        double stride = Math.sin(t * 8) * 6 * move;
        double ear = Math.sin(t * 2.5) * 4 + k * Math.sin(t * 20) * 6;
        g.setFill(alpha(Color.BLACK, 0.15));
        oval(g, 0, 0, 30, 5);
        g.setFill(Color.web("#f4efe9"));
        oval(g, -30, -30, 9, 9);
        oval(g, 0, -28, 28, 22);
        oval(g, -14 + stride, -5, 12, 6);
        oval(g, 14 - stride, -5, 12, 6);
        oval(g, 22, -50, 17, 16);
        g.save();
        g.translate(20, -60);
        g.rotate(-8 + ear);
        oval(g, -4, -20, 6, 22);
        g.setFill(Color.web("#ffb6c8"));
        oval(g, -4, -20, 3, 16);
        g.restore();
        g.save();
        g.translate(30, -60);
        g.rotate(10 - ear);
        g.setFill(Color.web("#f4efe9"));
        oval(g, 2, -18, 6, 22);
        g.setFill(Color.web("#ffb6c8"));
        oval(g, 2, -18, 3, 16);
        g.restore();
        g.setFill(Color.web("#2a1a10"));
        if (sleeping || (t % 4.5) < 0.12) {
            g.fillRect(24, -54, 6, 1.6);
        } else {
            oval(g, 28, -54, 2.8, 3.4);
        }
        g.setFill(Color.web("#ff8fa8"));
        oval(g, 37, -49, 3.2, 2.4);
        g.setFill(Color.web("#f4efe9"));
        oval(g, 4, -6, 6, 5);
    }

    private static void carrot(GraphicsContext g, double t, double age, double k) {
        double sway = Math.sin(t * 1.6) * 3 + k * Math.sin(t * 22) * 4;
        g.setFill(Color.web("#ff8a1f"));
        g.fillPolygon(new double[]{-14, 14, 0}, new double[]{-62, -62, -2}, 3);
        g.setStroke(Color.web("#d96a00"));
        g.setLineWidth(2);
        g.strokeLine(-8, -50, 6, -48);
        g.strokeLine(-5, -34, 7, -32);
        g.strokeLine(-2, -20, 4, -19);
        g.setFill(Color.web("#3fae4a"));
        for (int i = -1; i <= 1; i++) {
            g.save();
            g.translate(i * 6, -62);
            g.rotate(i * 18 + sway);
            oval(g, 0, -12, 4.5, 14);
            g.restore();
        }
    }

    private static void flower(GraphicsContext g, double t, double age, double k) {
        double sway = Math.sin(t * 1.4) * 4 + k * Math.sin(t * 16) * 5;
        g.setStroke(Color.web("#3f9a3a"));
        g.setLineWidth(4);
        g.strokeLine(0, 0, sway, -44);
        g.save();
        g.translate(sway, -48);
        g.rotate(k * age * 300);
        double big = 1 + 0.4 * k;
        g.setFill(Color.web("#ff7aa8"));
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3;
            oval(g, Math.cos(a) * 10 * big, Math.sin(a) * 10 * big, 7 * big, 7 * big);
        }
        g.setFill(Color.web("#ffe14a"));
        oval(g, 0, 0, 6 * big, 6 * big);
        g.restore();
    }

    private static void duck(GraphicsContext g, double t, double age, double k, double move, boolean sleeping) {
        double step = Math.sin(t * 12) * 4 * move;
        g.setFill(alpha(Color.BLACK, 0.15));
        oval(g, 0, 0, 28, 5);
        g.setFill(Color.web("#ff9f2f"));
        oval(g, -6 + step, -3, 7, 3.4);
        oval(g, 8 - step, -3, 7, 3.4);
        g.setFill(Color.web("#ffd93a"));
        g.fillPolygon(new double[]{-22, -38, -20}, new double[]{-30, -40, -22}, 3);
        oval(g, 0, -24, 28, 18);
        double wing = Math.sin(t * 6) * 3 + k * Math.sin(t * 25) * 14;
        g.setFill(Color.web("#f0b820"));
        g.save();
        g.translate(-4, -26);
        g.rotate(-wing);
        oval(g, -2, 0, 14, 8);
        g.restore();
        g.setFill(Color.web("#ffd93a"));
        oval(g, 20, -46, 14, 14);
        g.setFill(Color.web("#ff9f2f"));
        g.fillPolygon(new double[]{32, 46, 32}, new double[]{-50, -45, -41}, 3);
        g.setFill(Color.web("#2a1a10"));
        if (sleeping) {
            g.fillRect(20, -49, 6, 1.6);
        } else {
            oval(g, 25, -49, 2.4, 2.8);
        }
    }

    private static void turtle(GraphicsContext g, double t, double age, double k, double move, boolean sleeping) {
        double step = Math.sin(t * 5) * 4 * move;
        g.setFill(alpha(Color.BLACK, 0.15));
        oval(g, 0, 0, 36, 5);
        g.setFill(Color.web("#7bbf5a"));
        oval(g, -16 + step, -4, 8, 6);
        oval(g, 18 - step, -4, 8, 6);
        g.fillPolygon(new double[]{-32, -46, -30}, new double[]{-10, -6, -3}, 3);
        // head, pulled in when asleep
        double head = sleeping ? 6 : 0;
        oval(g, 38 - head, -16 - Math.sin(t * 2) * 1.5, 11, 9);
        g.setFill(Color.web("#2a1a10"));
        if (sleeping) {
            g.fillRect(39 - head, -18, 5, 1.6);
        } else {
            oval(g, 43, -18, 2.2, 2.6);
        }
        g.setFill(Color.web("#4a9a3f"));
        g.fillArc(-34, -50, 68, 56, 0, 180, ArcType.ROUND);
        g.setStroke(Color.web("#2f7a2f"));
        g.setLineWidth(2.5);
        g.strokeLine(-14, -6, -14, -40);
        g.strokeLine(0, -6, 0, -48);
        g.strokeLine(14, -6, 14, -40);
        g.strokeLine(-30, -22, 30, -22);
    }

    private static void crab(GraphicsContext g, double t, double age, double k, double move) {
        double scuttle = Math.sin(t * 16) * 3.5 * move;
        double claw = Math.sin(t * 3) * 6 + k * Math.sin(t * 18) * 10;
        g.setStroke(Color.web("#d8432f"));
        g.setLineWidth(3);
        for (int i = 0; i < 3; i++) {
            double off = (i - 1) * 9;
            g.strokeLine(-14 + off * 0.4, -12, -34 + off * 0.2, -4 + (i % 2 == 0 ? scuttle : -scuttle));
            g.strokeLine(14 + off * 0.4, -12, 34 + off * 0.2, -4 + (i % 2 == 0 ? -scuttle : scuttle));
        }
        g.setFill(Color.web("#ef5a43"));
        oval(g, 0, -18, 24, 15);
        g.setStroke(Color.web("#d8432f"));
        g.setLineWidth(4);
        g.strokeLine(-20, -24, -32, -42 - claw);
        g.strokeLine(20, -24, 32, -42 + claw);
        g.setFill(Color.web("#ef5a43"));
        oval(g, -32, -46 - claw, 9, 7);
        oval(g, 32, -46 + claw, 9, 7);
        g.setStroke(Color.web("#d8432f"));
        g.setLineWidth(2);
        g.strokeLine(-8, -30, -8, -40);
        g.strokeLine(8, -30, 8, -40);
        g.setFill(Color.WHITE);
        oval(g, -8, -42, 4, 4);
        oval(g, 8, -42, 4, 4);
        g.setFill(Color.BLACK);
        oval(g, -8, -42, 1.8, 1.8);
        oval(g, 8, -42, 1.8, 1.8);
        g.setStroke(Color.web("#7a1f12"));
        g.setLineWidth(1.5);
        g.strokeArc(-6, -22, 12, 8, 200, 140, ArcType.OPEN);
    }

    private static void starfish(GraphicsContext g, double t, double age, double k) {
        g.save();
        g.translate(0, -16);
        g.rotate(Math.sin(t * 0.8) * 6 + k * age * 360);
        g.setFill(Color.web("#ff8a3d"));
        star(g, 0, 0, 20);
        g.setFill(Color.web("#ffd08a"));
        for (int i = 0; i < 5; i++) {
            double a = -Math.PI / 2 + i * 2 * Math.PI / 5;
            oval(g, Math.cos(a) * 9, Math.sin(a) * 9, 1.6, 1.6);
        }
        g.restore();
    }

    private static void rocket(GraphicsContext g, double t, double age, double k, double move, boolean sleeping) {
        double flame = (sleeping ? 0 : 8 + 22 * move) + Math.sin(t * 30) * 3 * (sleeping ? 0 : 1);
        if (flame > 0) {
            g.setFill(Color.web("#ff8a1f"));
            g.fillPolygon(new double[]{-9, 9, 0}, new double[]{0, 0, flame + 8}, 3);
            g.setFill(Color.web("#ffe14a"));
            g.fillPolygon(new double[]{-5, 5, 0}, new double[]{0, 0, flame}, 3);
        }
        g.setFill(Color.web("#e8412f"));
        g.fillPolygon(new double[]{-14, -30, -14}, new double[]{-4, 8, -34}, 3);
        g.fillPolygon(new double[]{14, 30, 14}, new double[]{-4, 8, -34}, 3);
        g.setFill(Color.web("#f5f5fa"));
        oval(g, 0, -46, 17, 44);
        g.setFill(Color.web("#e8412f"));
        g.fillPolygon(new double[]{-16, 16, 0}, new double[]{-72, -72, -112}, 3);
        g.setFill(Color.web("#7fb8e8"));
        oval(g, 0, -56, 8, 8);
        g.setFill(Color.web("#2a4a6a"));
        if (sleeping) {
            g.fillRect(-5, -57, 10, 2);
        } else {
            oval(g, 2, -56, 2.6, 2.6);
        }
        g.setStroke(Color.web("#e8412f"));
        g.setLineWidth(4);
        g.strokeLine(-16, -30, 16, -30);
    }

    private static void beach(GraphicsContext g, double w, double h, double t) {
        g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6cc3f2")), new Stop(1, Color.web("#ffe9c4"))));
        g.fillRect(0, 0, w, h);
        g.setFill(Color.web("#3f9bd8"));
        g.fillRect(0, h * 0.42, w, h * 0.3);
        g.setStroke(alpha(Color.WHITE, 0.7));
        g.setLineWidth(2);
        for (int i = 0; i < 12; i++) {
            double x = ((i * 0.09 + t * 0.04) % 1.1) * w;
            double y = h * (0.46 + 0.05 * (i % 4));
            g.strokeLine(x, y, x + w * 0.05, y + Math.sin(t * 2 + i) * 2);
        }
        g.setFill(Color.web("#f2d9a0"));
        g.fillRect(0, h * 0.68, w, h * 0.32);
        double foam = Math.sin(t * 0.9) * h * 0.025;
        g.setFill(alpha(Color.WHITE, 0.8));
        for (int i = 0; i < 10; i++) {
            oval(g, i * w * 0.11, h * 0.69 + foam, w * 0.07, h * 0.014);
        }
        g.setFill(alpha(Color.web("#d9bd80"), 0.7));
        for (int i = 0; i < 8; i++) {
            oval(g, w * (0.07 + i * 0.13), h * (0.82 + 0.1 * ((i * 3) % 4) / 4.0), 5, 2.5);
        }
    }
}
