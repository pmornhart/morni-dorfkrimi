package de.moinmorni.dorfkrimi.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Trachten verteilen: jede Person bekommt eine eigene (keine zwei gleich). Damit ein einzelner Hinweis den Wilderer nicht
 * sofort verrät, trägt bei jedem Teil mindestens eine weitere Person dieselbe Farbe wie er (wenn genug Leute da sind).
 */
public final class Outfits {

    private static final int TRIES = 400;
    /** Ab so vielen Leuten teilt jeder Wilderer jede Farbe mit jemandem. */
    public static final int SHARE_FROM = 3;

    private Outfits() {
    }

    public static Map<UUID, Outfit> assign(List<UUID> players, Collection<UUID> wilderers, Random random) {
        List<Outfit> all = Outfit.all();
        Map<UUID, Outfit> best = null;
        for (int t = 0; t < TRIES; t++) {
            List<Outfit> pool = new ArrayList<>(all);
            Collections.shuffle(pool, random);
            Map<UUID, Outfit> out = new LinkedHashMap<>();
            for (int i = 0; i < players.size(); i++) {
                out.put(players.get(i), pool.get(i % pool.size()));
            }
            best = out;
            if (players.size() < SHARE_FROM || shared(out, wilderers)) {
                return out;
            }
        }
        return best == null ? new HashMap<>() : best;
    }

    /** Teilt jeder Wilderer jedes Teil seiner Tracht mit mindestens einer anderen Person? */
    public static boolean shared(Map<UUID, Outfit> outfits, Collection<UUID> wilderers) {
        for (UUID w : wilderers) {
            Outfit mine = outfits.get(w);
            if (mine == null) {
                continue;
            }
            for (Part part : Part.values()) {
                boolean other = outfits.entrySet().stream()
                        .anyMatch(e -> !e.getKey().equals(w) && e.getValue().get(part) == mine.get(part));
                if (!other) {
                    return false;
                }
            }
        }
        return true;
    }
}
