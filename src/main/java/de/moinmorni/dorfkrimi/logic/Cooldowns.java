package de.moinmorni.dorfkrimi.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Wartezeiten je Person und Sache, in Spielticks (hängt der Server, läuft auch die Wartezeit nicht weiter). */
public final class Cooldowns {

    private final Map<UUID, Map<String, Long>> readyAt = new HashMap<>();

    public boolean ready(UUID player, String what, long now) {
        return left(player, what, now) == 0;
    }

    /** Benutzt: erst nach {@code ticks} wieder bereit. */
    public void use(UUID player, String what, long now, long ticks) {
        readyAt.computeIfAbsent(player, u -> new HashMap<>()).put(what, now + Math.max(0, ticks));
    }

    public long left(UUID player, String what, long now) {
        Long at = readyAt.getOrDefault(player, Map.of()).get(what);
        return at == null ? 0 : Math.max(0, at - now);
    }

    public int secondsLeft(UUID player, String what, long now) {
        return (int) ((left(player, what, now) + Comeback.TICKS_PER_SECOND - 1) / Comeback.TICKS_PER_SECOND);
    }

    /** Wartezeit verschieben (z. B. während der Dorfversammlung steht die Uhr). */
    public void delay(long ticks) {
        readyAt.values().forEach(m -> m.replaceAll((k, v) -> v + ticks));
    }

    public void clear() {
        readyAt.clear();
    }
}
