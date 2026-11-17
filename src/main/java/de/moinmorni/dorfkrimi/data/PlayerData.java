package de.moinmorni.dorfkrimi.data;

import de.moinmorni.dorfkrimi.logic.Role;
import de.moinmorni.dorfkrimi.logic.Roles;
import java.util.UUID;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Werte einer Person über alle Runden: gespielt, gewonnen (gesamt, als Wilderer, als Gendarm), wie oft welche Rolle,
 * Festnahmen, Betäubungen, Hinweise, Punkte, beste Runde – und die Lose für eine gerechte Rollenverteilung.
 */
public final class PlayerData {

    /** Was eine Person in einer Runde geschafft hat. */
    public record Round(Role role, boolean won, boolean gendarm, int arrests, int stuns, int clues, int points) {
    }

    private final UUID uuid;
    private String name;
    private int games;
    private int wins;
    private int winsWilderer;
    private int winsGendarm;
    private int timesWilderer;
    private int timesGendarm;
    private int arrests;
    private int stuns;
    private int clues;
    private int points;
    private int bestPoints;
    private Roles.Lots lots = Roles.Lots.NEW;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public void name(String n) {
        if (n != null && !n.isBlank()) {
            name = n;
        }
    }

    public int games() {
        return games;
    }

    public int wins() {
        return wins;
    }

    public int winsWilderer() {
        return winsWilderer;
    }

    public int winsGendarm() {
        return winsGendarm;
    }

    public int timesWilderer() {
        return timesWilderer;
    }

    public int timesGendarm() {
        return timesGendarm;
    }

    public int arrests() {
        return arrests;
    }

    public int stuns() {
        return stuns;
    }

    public int clues() {
        return clues;
    }

    public int points() {
        return points;
    }

    public int bestPoints() {
        return bestPoints;
    }

    /** Wie lange diese Person nicht mehr Wilderer bzw. Gendarm war (für die gerechte Verteilung). */
    public Roles.Lots lots() {
        return lots;
    }

    public void lots(Roles.Lots l) {
        if (l != null) {
            lots = l;
        }
    }

    /**
     * Eine Runde zu Ende: Werte hochzählen; liefert {@code true} bei neuer Bestleistung. Negative Rundenpunkte
     * (Pech-Punkte) zählen als 0.
     */
    public boolean add(Round r) {
        games++;
        if (r.won()) {
            wins++;
            if (r.role() == Role.WILDERER) {
                winsWilderer++;
            }
            if (r.gendarm()) {
                winsGendarm++;
            }
        }
        if (r.role() == Role.WILDERER) {
            timesWilderer++;
        } else if (r.role() == Role.GENDARM) {
            timesGendarm++;
        }
        arrests += Math.max(0, r.arrests());
        stuns += Math.max(0, r.stuns());
        clues += Math.max(0, r.clues());
        int p = Math.max(0, r.points());
        points += p;
        if (p > bestPoints) {
            bestPoints = p;
            return true;
        }
        return false;
    }

    public String toYaml() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("name", name);
        y.set("runden", games);
        y.set("siege", wins);
        y.set("siege-als-wilderer", winsWilderer);
        y.set("siege-als-gendarm", winsGendarm);
        y.set("mal-wilderer", timesWilderer);
        y.set("mal-gendarm", timesGendarm);
        y.set("festnahmen", arrests);
        y.set("betaeubt", stuns);
        y.set("hinweise", clues);
        y.set("punkte", points);
        y.set("beste-runde", bestPoints);
        y.set("lose.wilderer", lots.sinceWilderer());
        y.set("lose.gendarm", lots.sinceGendarm());
        return y.saveToString();
    }

    public static PlayerData fromYaml(UUID uuid, String text) {
        YamlConfiguration y = new YamlConfiguration();
        try {
            y.loadFromString(text);
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        PlayerData d = new PlayerData(uuid, y.getString("name", "?"));
        d.games = y.getInt("runden");
        d.wins = y.getInt("siege");
        d.winsWilderer = y.getInt("siege-als-wilderer");
        d.winsGendarm = y.getInt("siege-als-gendarm");
        d.timesWilderer = y.getInt("mal-wilderer");
        d.timesGendarm = y.getInt("mal-gendarm");
        d.arrests = y.getInt("festnahmen");
        d.stuns = y.getInt("betaeubt");
        d.clues = y.getInt("hinweise");
        d.points = y.getInt("punkte");
        d.bestPoints = y.getInt("beste-runde");
        d.lots = new Roles.Lots(y.getInt("lose.wilderer", Roles.Lots.NEW.sinceWilderer()),
                y.getInt("lose.gendarm", Roles.Lots.NEW.sinceGendarm()));
        return d;
    }
}
