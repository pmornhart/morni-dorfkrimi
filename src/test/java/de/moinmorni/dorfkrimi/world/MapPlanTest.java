package de.moinmorni.dorfkrimi.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

/**
 * Baupläne ohne Server: nur Blöcke von 26.x, Startplätze und Versammlungsplätze frei und erreichbar, die Glocke erreicht
 * man, die Bank hat 16 Plätze, genug Spuren-Stellen überall im Dorf, Kirchturm und Obergeschosse sind begehbar, der Zaun
 * hält alle drin.
 */
class MapPlanTest {

    /** Blocknamen, die es in 26.x nicht mehr gibt (ein Stück des Bauplans würde still fehlen). */
    private static final Set<String> OLD = Set.of("chain", "grass", "grass_path", "sign", "wall_sign");
    private static final int MAX_PLAYERS = 16;
    private static final double REACH = 4.0;

    @Test
    void onlyBlocksThatExist() {
        Set<String> unknown = new TreeSet<>();
        for (Plan p : new Plan[] {HallPlan.build(), MapType.OBERDORF.plan(), MapType.ALMWIRTSCHAFT.plan()}) {
            p.forEach((k, s) -> {
                String base = Blocks.base(s);
                if (OLD.contains(base) || Material.matchMaterial(base) == null) {
                    unknown.add(base);
                }
            });
        }
        assertEquals(Set.of(), unknown);
    }

    @Test
    void startAndMeetingSpotsAreFreeAndReachable() {
        for (MapType m : MapType.values()) {
            MapLayout l = m.layout();
            Plan p = m.playPlan();
            Set<Long> reach = m.reachable();
            for (int n : new int[] {4, 9, MAX_PLAYERS}) {
                for (int i = 0; i < n; i++) {
                    for (double[] s : new double[][] {l.spawn(i, n), l.meetingSpot(i, n)}) {
                        int x = (int) Math.floor(s[0]);
                        int z = (int) Math.floor(s[1]);
                        assertTrue(Walk.stand(p, x, 1, z), m.id() + ": Platz " + i + "/" + n + " bei " + x + " " + z);
                        assertTrue(reach.contains(Plan.key(x, 1, z)), m.id() + ": Platz nicht erreichbar " + x + " " + z);
                    }
                }
            }
        }
    }

    @Test
    void theBellCanBeRung() {
        for (MapType m : MapType.values()) {
            int[] b = m.layout().bell();
            Plan p = m.playPlan();
            assertNotNull(p.get(b[0], b[1], b[2]), m.id());
            assertTrue(p.get(b[0], b[1], b[2]).startsWith("bell["), m.id() + ": keine Glocke");
            boolean reachable = m.reachable().stream().anyMatch(k -> {
                double dx = Plan.keyX(k) + 0.5 - (b[0] + 0.5);
                double dy = Plan.keyY(k) + 1.62 - (b[1] + 0.5);
                double dz = Plan.keyZ(k) + 0.5 - (b[2] + 0.5);
                return Math.sqrt(dx * dx + dy * dy + dz * dz) <= REACH;
            });
            assertTrue(reachable, m.id() + ": an die Glocke kommt man nicht heran");
        }
    }

    @Test
    void theBenchHasSixteenSeats() {
        for (MapType m : MapType.values()) {
            List<double[]> seats = m.layout().seats();
            assertEquals(MAX_PLAYERS, seats.size(), m.id());
            Plan p = m.playPlan();
            for (double[] s : seats) {
                String block = p.get((int) Math.floor(s[0]), (int) s[1], (int) Math.floor(s[2]));
                assertNotNull(block, m.id());
                assertTrue(block.startsWith("spruce_stairs"), m.id() + ": Sitz ist keine Bank: " + block);
            }
        }
    }

    @Test
    void enoughCluePlacesAllOverTheVillage() {
        for (MapType m : MapType.values()) {
            List<int[]> spots = m.clueSpots();
            assertTrue(spots.size() >= 24, m.id() + ": nur " + spots.size() + " Spuren-Stellen");
            Set<Long> reach = m.reachable();
            int inside = 0;
            for (int[] s : spots) {
                assertTrue(reach.contains(Plan.key(s[0], s[1], s[2])), m.id() + ": Spur nicht erreichbar");
                if (m.playPlan().solid(s[0], s[1] + 3, s[2])) {
                    inside++;
                }
            }
            assertTrue(inside >= 3, m.id() + ": keine Spuren in Häusern oder Scheunen (" + inside + ")");
            // über die ganze Karte verteilt: in jedem Viertel welche
            for (int q = 0; q < 4; q++) {
                int qq = q;
                long n = spots.stream().filter(s -> (s[0] >= 0) == (qq % 2 == 0) && (s[2] >= 0) == (qq / 2 == 0)).count();
                assertTrue(n >= 3, m.id() + ": Viertel " + q + " hat nur " + n + " Spuren-Stellen");
            }
        }
    }

    /** Dicht bebaut, aber begehbar: viele Stellen erreichbar, Obergeschosse und (im Oberdorf) der Kirchturm. */
    @Test
    void housesTowersAndLoftsAreReachable() {
        for (MapType m : MapType.values()) {
            Set<Long> reach = m.reachable();
            assertTrue(reach.size() > 4000, m.id() + ": nur " + reach.size() + " erreichbare Stellen");
            long upstairs = reach.stream().filter(k -> Plan.keyY(k) >= Kit.GROUND_TOP + 2).count();
            assertTrue(upstairs > 100, m.id() + ": Obergeschosse nicht erreichbar (" + upstairs + ")");
        }
        long belfry = MapType.OBERDORF.reachable().stream().filter(k -> Plan.keyY(k) >= 14).count();
        assertTrue(belfry > 0, "Glockenstube im Kirchturm nicht erreichbar");
    }

    @Test
    void theBorderKeepsEveryoneIn() {
        for (MapType m : MapType.values()) {
            MapLayout l = m.layout();
            Plan p = m.playPlan();
            for (int x = -l.halfX(); x <= l.halfX(); x += 7) {
                for (int y = 2; y <= 6; y++) {
                    assertEquals("barrier", p.get(x, y, l.halfZ()), m.id() + " Rand bei x = " + x);
                    assertEquals("barrier", p.get(x, y, -l.halfZ()), m.id() + " Rand bei x = " + x);
                }
            }
            assertTrue(m.reachable().stream().allMatch(k -> l.inside(Plan.keyX(k) + 0.5, Plan.keyZ(k) + 0.5)));
        }
    }

    @Test
    void hallHasRoomToStand() {
        Plan p = HallPlan.build();
        assertTrue(p.standable((int) Math.floor(HallPlan.SPAWN_X), 1, (int) Math.floor(HallPlan.SPAWN_Z)));
        for (int[] t : HallPlan.TABLES) {
            assertEquals("cartography_table", p.get(t[0], 2, t[1]));
        }
        assertNotNull(p.get(0, 9, 0), "Dorflinde");
    }
}
