package de.moinmorni.dorfkrimi.game;

import de.moinmorni.dorfkrimi.DorfkrimiPlugin;
import de.moinmorni.dorfkrimi.logic.Role;
import de.moinmorni.dorfkrimi.world.MapType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.entity.Player;

/** Alle laufenden Runden (höchstens eine je Karte). */
public final class Games {

    private final DorfkrimiPlugin plugin;
    private final Map<MapType, Game> running = new EnumMap<>(MapType.class);
    /** Für Tests und Verwaltung: Rollen für die nächste Runde (Name → Rolle). */
    private final Map<String, Role> forced = new HashMap<>();
    /** Wer gerade nur auf der Bank mitreden darf (Ausgeschiedene, Zuschauer) – auch vom Chat-Thread gelesen. */
    private final Set<UUID> bench = ConcurrentHashMap.newKeySet();

    public Games(DorfkrimiPlugin plugin) {
        this.plugin = plugin;
    }

    public Game start(MapType map, List<Player> players, List<List<UUID>> groups) {
        Map<UUID, Role> f = new HashMap<>();
        for (Player p : players) {
            Role r = forced.remove(p.getName().toLowerCase(Locale.ROOT));
            if (r != null) {
                f.put(p.getUniqueId(), r);
            }
        }
        Game g = new Game(plugin, map, players, groups, f);
        running.put(map, g);
        plugin.stateChanged();
        return g;
    }

    /** Rolle für die nächste Runde vorgeben (nur Verwaltung/Tests). */
    public void force(String name, Role role) {
        forced.put(name.toLowerCase(Locale.ROOT), role);
    }

    public Collection<Game> all() {
        return running.values();
    }

    public Game on(MapType map) {
        return running.get(map);
    }

    public Set<MapType> free() {
        Set<MapType> out = EnumSet.allOf(MapType.class);
        out.removeAll(running.keySet());
        out.removeIf(m -> plugin.construction().restoring(m));
        return out;
    }

    /** Runde, in der jemand mitspielt (auch ausgeschieden oder gerade nicht verbunden) oder zuschaut. */
    public Game of(UUID uuid) {
        for (Game g : running.values()) {
            Suspect s = g.suspect(uuid);
            if ((s != null && !s.gone()) || g.spectating(uuid)) {
                return g;
            }
        }
        return null;
    }

    public boolean late(UUID uuid) {
        for (Game g : running.values()) {
            Suspect s = g.suspect(uuid);
            if (s != null && s.late()) {
                return true;
            }
        }
        return false;
    }

    /** Runde, in der jemand gerade wach mitspielt. */
    public Game playing(UUID uuid) {
        for (Game g : running.values()) {
            if (g.awake(uuid)) {
                return g;
            }
        }
        return null;
    }

    /** Darf diese Person nur auf der Bank mitreden? (vom Chat-Thread) */
    public boolean onBench(UUID uuid) {
        return bench.contains(uuid);
    }

    /** Wer auf der Bank mitliest (vom Chat-Thread aus gerufen: nur Liste der Namen der Runde). */
    public List<Player> benchOf(UUID uuid) {
        for (Game g : List.copyOf(running.values())) {
            if (g.suspect(uuid) != null || g.spectating(uuid)) {
                return g.benchListeners();
            }
        }
        return List.of();
    }

    /** Neu zählen, wer nur auf der Bank mitreden darf. */
    public void ghostsChanged() {
        Set<UUID> now = new java.util.HashSet<>();
        for (Game g : running.values()) {
            if (g.phase() == Game.Phase.OVER) {
                continue;
            }
            for (Suspect s : g.people().values()) {
                if (g.ghost(s.uuid())) {
                    now.add(s.uuid());
                }
            }
            for (Player p : g.audience()) {
                if (g.spectating(p.getUniqueId())) {
                    now.add(p.getUniqueId());
                }
            }
        }
        bench.retainAll(now);
        bench.addAll(now);
    }

    public void tick() {
        for (Game g : new ArrayList<>(running.values())) {
            boolean alive;
            try {
                alive = g.tick();
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.SEVERE, "Fehler in der Runde " + g.map().id() + " – sie wird beendet", e);
                g.close();
                alive = false;
            }
            if (!alive) {
                running.remove(g.map());
                ghostsChanged();
                plugin.stateChanged();
                plugin.boards().refreshHall();
            }
        }
    }

    public void stop(Game g) {
        g.close();
        running.remove(g.map());
        ghostsChanged();
        plugin.stateChanged();
        plugin.boards().refreshHall();
    }

    public void abortAll() {
        for (Game g : new ArrayList<>(running.values())) {
            g.close();
        }
        running.clear();
        bench.clear();
    }

    public int players() {
        return running.values().stream().mapToInt(Game::activeCount).sum();
    }
}
