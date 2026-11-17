package de.moinmorni.dorfkrimi.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Die Karten. Jede liegt an einer eigenen Stelle der Welt (Dorflinde bei 0|0), mit eigenem Aufbau ({@link MapLayout})
 * und Bauplan; auf jeder Karte läuft höchstens eine Runde gleichzeitig.
 */
public enum MapType {
    OBERDORF("oberdorf", 1000, "lantern"),
    ALMWIRTSCHAFT("almwirtschaft", 2000, "white_wool");

    /** Höhe der obersten Bodenschicht in der Welt (man steht eins darüber). */
    public static final int FLOOR_Y = 100;
    /** So tief und so hoch reicht die Spielfläche (Wiederherstellung nach jeder Runde). */
    private static final int PLAY_BOTTOM = -14;
    private static final int PLAY_TOP = 34;
    private static final int MARGIN = 6;
    /** Spuren liegen mindestens so weit auseinander … */
    static final double CLUE_GAP = 6;
    /** … und so viele Stellen gibt es höchstens je Karte. */
    static final int CLUE_SPOTS = 32;

    private final String id;
    private final int originX;
    private final String icon;
    private MapLayout layout;
    private Plan plan;
    private Plan playPlan;
    private List<int[]> clueSpots;

    MapType(String id, int originX, String icon) {
        this.id = id;
        this.originX = originX;
        this.icon = icon;
    }

    public String id() {
        return id;
    }

    public int originX() {
        return originX;
    }

    public int originZ() {
        return 0;
    }

    /** Gegenstand fürs Menü. */
    public String icon() {
        return icon;
    }

    public synchronized MapLayout layout() {
        if (layout == null) {
            layout = switch (this) {
                case OBERDORF -> OberdorfPlan.layout();
                case ALMWIRTSCHAFT -> AlmPlan.layout();
            };
        }
        return layout;
    }

    /** Ganzer Bauplan mit Kulisse (Berge, Wolken). */
    public synchronized Plan plan() {
        if (plan == null) {
            plan = switch (this) {
                case OBERDORF -> OberdorfPlan.build();
                case ALMWIRTSCHAFT -> AlmPlan.build();
            };
        }
        return plan;
    }

    /** Nur die Spielfläche (wird nach jeder Runde wiederhergestellt). */
    public synchronized Plan playPlan() {
        if (playPlan == null) {
            int[] b = playBox();
            playPlan = plan().crop(b[0], b[1], b[2], b[3], b[4], b[5]);
        }
        return playPlan;
    }

    /** Ganzen Bauplan vergessen (nach dem Bauen; die Spielfläche bleibt im Speicher). */
    public synchronized void forgetFullPlan() {
        playPlan();
        clueSpots();
        plan = null;
    }

    /** Box der Spielfläche (relativ): minX, minY, minZ, maxX, maxY, maxZ. */
    public int[] playBox() {
        MapLayout l = layout();
        return new int[] {-l.halfX() - MARGIN, PLAY_BOTTOM, -l.halfZ() - MARGIN, l.halfX() + MARGIN, PLAY_TOP,
            l.halfZ() + MARGIN};
    }

    /** Alle Stellen, die man vom Dorfplatz aus zu Fuß erreicht. */
    public Set<Long> reachable() {
        MapLayout l = layout();
        return Walk.from(playPlan(), l, l.square()[0] + (int) Math.round(l.spawnRadius()), 1, l.square()[1]);
    }

    /**
     * Stellen für Spuren (Füße, relativ): erreichbar, nicht bei der Versammlung, nicht zu nah beieinander. Feste
     * Auswahl (gleicher Bauplan = gleiche Stellen) – welche gerade eine Spur hat, entscheidet die Runde.
     */
    public synchronized List<int[]> clueSpots() {
        if (clueSpots == null) {
            MapLayout l = layout();
            List<Long> keys = new ArrayList<>(reachable());
            Collections.sort(keys);
            Collections.shuffle(keys, new Random(4711));
            List<int[]> out = new ArrayList<>();
            for (long k : keys) {
                int x = Plan.keyX(k);
                int y = Plan.keyY(k);
                int z = Plan.keyZ(k);
                if (Math.hypot(x - l.meeting()[0], z - l.meeting()[1]) < l.meetingRadius() + 3
                        || Math.hypot(x - l.square()[0], z - l.square()[1]) < l.spawnRadius() + 3
                        || Math.abs(x) > l.halfX() - 3 || Math.abs(z) > l.halfZ() - 3) {
                    continue;
                }
                boolean far = out.stream().allMatch(o -> Math.sqrt((o[0] - x) * (o[0] - x) + (o[1] - y) * (o[1] - y) * 4.0
                        + (o[2] - z) * (o[2] - z)) >= CLUE_GAP);
                if (far) {
                    out.add(new int[] {x, y, z});
                    if (out.size() >= CLUE_SPOTS) {
                        break;
                    }
                }
            }
            clueSpots = List.copyOf(out);
        }
        return clueSpots;
    }

    public static MapType byId(String s) {
        if (s == null) {
            return null;
        }
        String k = s.toLowerCase(Locale.ROOT);
        for (MapType m : values()) {
            if (m.id.equals(k)) {
                return m;
            }
        }
        return switch (k) {
            case "nacht", "night", "dorf", "village" -> OBERDORF;
            case "alm", "nebel", "fog", "inn" -> ALMWIRTSCHAFT;
            default -> null;
        };
    }
}
