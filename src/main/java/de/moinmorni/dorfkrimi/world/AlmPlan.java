package de.moinmorni.dorfkrimi.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Karte „Almwirtschaft im Nebel“ (eigener Entwurf): eine Hochalm im Morgengrauen, Nebelschwaden ziehen über die Wiese.
 * Im Norden die große Almwirtschaft mit Terrasse und der Bank davor, in der Mitte die Almwiese mit dem Marterl (dort
 * beginnt die Runde und dort tagt die Versammlung, gerufen mit der Almglocke), im Osten die Kapelle mit Türmchen und die
 * Sennerei, im Westen der Kuhstall und der Weiher. Dazu drei Heustadel, drei Holzhütten, ein Hochsitz und Bergwald an
 * den Rändern.
 */
final class AlmPlan {

    static final int HALF_X = 54;
    static final int HALF_Z = 46;
    static final int VOID_Y = -10;
    /** Morgengrauen. */
    static final long DAWN = 23300L;
    static final int[] MEADOW = {0, 4};
    static final int[] BELL = {10, 3, 4};

    private AlmPlan() {
    }

    static MapLayout layout() {
        List<double[]> seats = new ArrayList<>();
        benches(new Plan(), seats);
        return new MapLayout(MEADOW.clone(), 4.5, MEADOW.clone(), 7.5, BELL.clone(), List.copyOf(seats), HALF_X, HALF_Z,
                VOID_Y, DAWN, true);
    }

    /** Bänke vorn auf der Terrasse der Almwirtschaft (Lehne im Norden, Blick zur Wiese): 16 Plätze. */
    private static void benches(Plan p, List<double[]> seats) {
        Town.benchX(p, -12, -10, -3, "north", seats);
        Town.benchX(p, -12, 3, 10, "north", seats);
    }

    static Plan build() {
        Plan p = new Plan();
        Random r = new Random(2207);
        Decor.island(p, 66, 58, 10, r);
        meadowGround(p, r);
        paths(p, r);
        // Almwirtschaft mit Terrasse
        Kit.House inn = new Kit.House(-12, -32, 12, -20, 's', 0, "stripped_spruce_wood[axis=y]", "spruce", true, true,
                "spruce_planks").through();
        Kit.house(p, inn, r);
        Rooms.furnish(p, inn, Rooms.Kind.STUBE, r);
        Kit.longTable(p, -9, 1, -26, 5, true, "spruce", true, r, "flower_pot", "candle[candles=2,lit=true,waterlogged=false]");
        Kit.longTable(p, 5, 1, -26, 5, true, "spruce", true, r, "cake[bites=0]", "flower_pot");
        for (int x = -11; x <= 11; x++) {
            for (int z = -19; z <= -13; z++) {
                p.set(x, 0, z, "spruce_planks");
            }
        }
        Kit.roundTable(p, -7, 1, -16, "spruce", "flower_pot");
        Kit.roundTable(p, 7, 1, -16, "spruce", "candle[candles=3,lit=true,waterlogged=false]");
        benches(p, new ArrayList<>());
        p.banner(-3, 4, -19, "green_wall_banner[facing=south]", Decor.emerald());
        p.banner(3, 4, -19, "light_blue_wall_banner[facing=south]", Decor.lozenges());
        // Wiese mit Marterl und Almglocke
        p.set(MEADOW[0], 1, MEADOW[1], "spruce_fence");
        p.set(MEADOW[0], 2, MEADOW[1], "spruce_fence");
        p.set(MEADOW[0], 3, MEADOW[1], "spruce_planks");
        p.set(MEADOW[0], 4, MEADOW[1], "spruce_slab[type=bottom,waterlogged=false]");
        Town.bellFrame(p, BELL[0], BELL[2], false);
        // Kapelle mit Türmchen, Sennerei
        Town.church(p, 24, 6, 32, 18, r);
        Kit.House dairy = new Kit.House(32, -28, 44, -18, 'w', -23, "white_terracotta", "spruce", false, false,
                "smooth_stone").through();
        Kit.house(p, dairy, r);
        Rooms.furnish(p, dairy, Rooms.Kind.KUECHE, r);
        // Kuhstall, Heustadel
        Town.barn(p, -44, -26, -30, -16, r);
        Town.barn(p, -42, 14, -32, 22, r);
        Town.barn(p, 34, 26, 44, 34, r);
        Town.barn(p, -8, 30, 2, 38, r);
        // Holzhütten
        int[][] huts = {{16, -44, 22, -38, 's', 19}, {-30, 30, -24, 36, 'n', -27}, {40, -6, 46, 0, 'w', -3}};
        for (int[] h : huts) {
            Kit.House hut = new Kit.House(h[0], h[1], h[2], h[3], (char) h[4], h[5], "stripped_spruce_wood[axis=y]", "spruce",
                    false, false, "spruce_planks").through();
            Kit.house(p, hut, r);
            Rooms.furnish(p, hut, Rooms.Kind.LADEN, r);
        }
        pond(p, -26, -8, -14, 2, r);
        trough(p, 16, -14);
        Town.hide(p, -40, -40);
        Town.hide(p, 44, 40);
        // Bergwald an den Rändern
        Town.forest(p, -54, -46, -16, -36, 22, r);
        Town.forest(p, 24, -46, 54, -36, 12, r);
        Town.forest(p, -54, -34, -48, 44, 12, r);
        Town.forest(p, 48, -34, 54, 24, 8, r);
        Town.forest(p, 6, 38, 30, 46, 10, r);
        for (int i = 0; i < 18; i++) {
            int x = -HALF_X + 4 + r.nextInt(2 * HALF_X - 8);
            int z = -HALF_Z + 4 + r.nextInt(2 * HALF_Z - 8);
            if (Math.hypot(x - MEADOW[0], z - MEADOW[1]) > 12 && Town.freeGround(p, x, z, 2)) {
                Town.boulder(p, x, z, 1 + r.nextInt(2), r);
            }
        }
        Town.woodpile(p, 23, -40, 5, false);
        Town.woodpile(p, -36, -12, 6, true);
        Town.cart(p, -20, 12, true);
        Town.cart(p, 20, -10, false);
        Decor.greenery(p, -HALF_X + 2, -HALF_Z + 2, HALF_X - 2, HALF_Z - 2, 22, r,
                (x, z) -> Math.hypot(x - MEADOW[0], z - MEADOW[1]) < 11);
        Decor.flowers(p, -HALF_X, -HALF_Z, HALF_X, HALF_Z, 4, r,
                (x, z) -> Math.hypot(x - MEADOW[0], z - MEADOW[1]) < 9);
        for (int[] l : new int[][] {{-3, -11}, {3, -11}, {12, -2}, {-12, 8}, {20, 4}, {-20, -12}}) {
            Town.lamp(p, l[0], l[1]);
        }
        Decor.border(p, HALF_X, HALF_Z, 8, "spruce_fence");
        for (int i = 0; i < 22; i++) {
            Decor.cloud(p, -90 + r.nextInt(180), -12 - r.nextInt(8), -70 + r.nextInt(140), 3 + r.nextInt(5), r);
        }
        Decor.mountainRing(p, 112, 100, -12, r);
        p.connect();
        return p;
    }

    /** Almboden: Wiese mit Moos, Steinen und Kuhtritten. */
    private static void meadowGround(Plan p, Random r) {
        for (int x = -HALF_X; x <= HALF_X; x++) {
            for (int z = -HALF_Z; z <= HALF_Z; z++) {
                String g = p.get(x, 0, z);
                if (g != null && g.startsWith("grass_block") && r.nextInt(14) == 0) {
                    p.set(x, 0, z, Decor.pick(r, "moss_block", "coarse_dirt", "podzol[snowy=false]", "mossy_cobblestone"));
                }
            }
        }
    }

    private static void paths(Plan p, Random r) {
        Town.path(p, 0, -20, 0, 0, 3, r);
        Town.path(p, 0, 8, 0, 30, 3, r);
        Town.path(p, 4, 4, 23, 4, 3, r);
        Town.path(p, 25, -23, 25, 4, 3, r);
        Town.path(p, 25, -23, 32, -23, 3, r);
        Town.path(p, -30, 4, -4, 4, 3, r);
        Town.path(p, -30, -15, -30, 13, 3, r);
        Town.path(p, 19, -37, 19, -19, 3, r);
        Town.path(p, 25, -3, 40, -3, 3, r);
        Town.path(p, -27, 18, -27, 30, 3, r);
    }

    /** Weiher: ein Block tief, Schilf und Seerosen am Rand. */
    private static void pond(Plan p, int x1, int z1, int x2, int z2, Random r) {
        double cx = (x1 + x2) / 2.0;
        double cz = (z1 + z2) / 2.0;
        double rx = (x2 - x1) / 2.0;
        double rz = (z2 - z1) / 2.0;
        for (int x = x1 - 1; x <= x2 + 1; x++) {
            for (int z = z1 - 1; z <= z2 + 1; z++) {
                double d = Math.hypot((x - cx) / rx, (z - cz) / rz);
                if (d <= 1) {
                    p.set(x, 0, z, "water");
                    p.set(x, -1, z, Decor.pick(r, "clay", "sand", "gravel"));
                    p.clear(x, 1, z);
                    if (d < 0.8 && r.nextInt(12) == 0) {
                        p.set(x, 1, z, "lily_pad");
                    }
                } else if (d <= 1.25 && r.nextInt(3) == 0 && p.get(x, 1, z) == null) {
                    p.set(x, 1, z, "sugar_cane");
                }
            }
        }
    }

    /** Brunnentrog aus Holz mit Wasser. */
    private static void trough(Plan p, int x, int z) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                boolean rim = dx == -1 || dx == 2;
                p.set(x + dx, 1, z + dz, rim ? "spruce_planks" : "water_cauldron[level=3]");
            }
        }
        p.set(x - 2, 1, z, "spruce_log[axis=y]");
        p.set(x - 2, 2, z, "spruce_log[axis=y]");
        p.set(x - 2, 3, z, "spruce_trapdoor[facing=east,half=top,open=false,powered=false,waterlogged=false]");
    }
}
