package de.moinmorni.dorfkrimi;

import org.bukkit.configuration.ConfigurationSection;

/** Werte aus der config.yml (Erklärungen stehen dort). Alles wird auf sinnvolle Grenzen gestutzt. */
public record Settings(
        String world,
        int hallCountdown,
        int hallMinPlayers,
        int maxPlayers,
        int startSeconds,
        int roundSeconds,
        int afterSeconds,
        int comebackSeconds,
        int twoFrom,
        boolean hideNames,
        int stunPause,
        int graceSeconds,
        int trailSeconds,
        int arrestPause,
        int clueEvery,
        int clueMax,
        boolean feathers,
        boolean meetings,
        int meetingMax,
        int meetingFrom,
        int meetingSeconds,
        int pech) {

    /** Höchstens so lange wartet eine Runde auf jemanden, der die Verbindung verloren hat. */
    static final int MAX_COMEBACK_SECONDS = 600;
    /** „zwei-wilderer-ab: 17“ heißt: nie zwei. */
    public static final int NEVER_TWO = 17;

    public static Settings load(ConfigurationSection c) {
        return new Settings(
                c.getString("welt", "dorfkrimi"),
                clamp(c.getInt("dorflinde.countdown-sekunden", 30), 3, 300),
                clamp(c.getInt("dorflinde.mindestens-spieler", 4), 4, 16),
                clamp(c.getInt("runde.hoechstens-spieler", 16), 4, 16),
                clamp(c.getInt("runde.start-sekunden", 10), 5, 30),
                clamp(c.getInt("runde.minuten", 6), 2, 20) * 60,
                clamp(c.getInt("runde.nachspiel-sekunden", 10), 5, 60),
                clamp(c.getInt("rueckkehr-sekunden", 60), 0, MAX_COMEBACK_SECONDS),
                clamp(c.getInt("runde.zwei-wilderer-ab", 10), 4, NEVER_TWO),
                c.getBoolean("runde.namen-verstecken", true),
                clamp(c.getInt("wilderer.betaeuben-pause", 25), 5, 120),
                clamp(c.getInt("wilderer.schonzeit", 20), 0, 120),
                clamp(c.getInt("wilderer.spuren-sekunden", 8), 0, 30),
                clamp(c.getInt("gendarm.festnahme-pause", 15), 3, 120),
                clamp(c.getInt("hinweise.alle-sekunden", 20), 5, 120),
                clamp(c.getInt("hinweise.hoechstens-gleichzeitig", 4), 1, 10),
                c.getBoolean("hinweise.federn", true),
                c.getBoolean("versammlung.an", true),
                clamp(c.getInt("versammlung.hoechstens", 2), 0, 5),
                clamp(c.getInt("versammlung.frueheste-sekunde", 60), 0, 600),
                clamp(c.getInt("versammlung.abstimm-sekunden", 30), 10, 120),
                clamp(c.getInt("versammlung.pech-punkte", 3), 0, 20));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
