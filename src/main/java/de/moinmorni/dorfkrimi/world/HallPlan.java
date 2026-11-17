package de.moinmorni.dorfkrimi.world;

import java.util.Random;

/**
 * Der Wartebereich „An der Dorflinde“: eine Alm-Insel in den Wolken. In der Mitte die alte Dorflinde mit einer runden
 * Bank, ringsum Tische auf Kies, im Norden das Amtshaus mit der Steckbrief-Tafel (Bestenliste) davor, links und rechts
 * die Kartentische, im Westen die Tafel „So geht's“. Hier wartet man auf die nächste Runde und stimmt ab. Eigener
 * Entwurf.
 */
public final class HallPlan {

    public static final double SPAWN_X = 0.5;
    public static final double SPAWN_Y = 1;
    public static final double SPAWN_Z = 12.5;
    public static final float SPAWN_YAW = 180f;
    /** Alles innerhalb dieses Abstands zur Mitte gehört zur Dorflinde (bleibt geladen). */
    public static final int REACH = 32;
    /** Radius der Insel. */
    public static final int RADIUS = 19;
    /** Kartentische (relativ): Oberdorf links, Almwirtschaft rechts. */
    public static final int[][] TABLES = {{-9, 2}, {9, 2}};
    /** Steckbrief-Tafel (Bestenliste) vor dem Amtshaus. */
    public static final int[] BOARD = {0, -10};
    /** Tafel „So geht's“ (Westen). */
    public static final int[] HOWTO = {-13, 9};
    /** Wer tiefer fällt, kommt zurück zum Ankunftsplatz. */
    public static final int FALL_Y = -12;

    private HallPlan() {
    }

    public static Plan build() {
        Plan p = new Plan();
        Random r = new Random(1505);
        Decor.island(p, RADIUS, RADIUS, 14, r);
        garden(p, r);
        linden(p, r);
        for (int[] t : TABLES) {
            table(p, t[0], t[1]);
        }
        inn(p, r);
        howto(p);
        for (int[] t : new int[][] {{-12, -3}, {12, -3}, {-11, 12}, {11, 12}}) {
            Decor.broadTree(p, t[0], 1, t[1], "oak_log", "oak_leaves", 4, 3, r);
        }
        Kit.longTable(p, -6, 1, 7, 3, true, "spruce", true, r, "flower_pot", "lantern[hanging=false,waterlogged=false]");
        Kit.longTable(p, 4, 1, 7, 3, true, "spruce", true, r, "flower_pot", "lantern[hanging=false,waterlogged=false]");
        Kit.longTable(p, -6, 1, -5, 3, true, "spruce", true, r, "flower_pot");
        Kit.longTable(p, 4, 1, -5, 3, true, "spruce", true, r, "flower_pot");
        for (int i = 0; i < 8; i++) {
            double a = Math.PI / 8 + i * Math.PI / 4;
            int x = (int) Math.round(Math.cos(a) * 15);
            int z = (int) Math.round(Math.sin(a) * 15);
            if ((Math.abs(x) <= 2 && z > 0) || z < -10) {
                continue;
            }
            Decor.lanternPost(p, x, 1, z, "spruce_fence");
        }
        Decor.flowers(p, -RADIUS, -RADIUS, RADIUS, RADIUS, 5, r, (x, z) -> Math.hypot(x, z) < 14);
        for (int i = 0; i < 12; i++) {
            double a = 2 * Math.PI * i / 12 + r.nextDouble() * 0.3;
            double d = RADIUS - 3 + r.nextInt(8);
            Decor.cloud(p, (int) Math.round(Math.cos(a) * d), -12 - r.nextInt(4), (int) Math.round(Math.sin(a) * d),
                    3 + r.nextInt(3), r);
        }
        Decor.mountainRing(p, 70, 70, -40, r);
        barrierRing(p);
        p.connect();
        return p;
    }

    /** Kiesplatz rund um die Linde, Weg zum Ankunftsplatz. */
    private static void garden(Plan p, Random r) {
        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                double d = Math.hypot(x, z);
                boolean path = Math.abs(x) <= 1 && z > 0 && z <= 15;
                if (d <= 12.5 || path) {
                    p.set(x, 0, z, d <= 2.5 ? "smooth_stone" : path && d > 12.5 ? "dirt_path"
                            : Decor.pick(r, "gravel", "gravel", "gravel", "coarse_dirt", "light_gray_concrete_powder"));
                }
            }
        }
    }

    private static void table(Plan p, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            p.set(x + dx, 1, z, "spruce_fence");
            p.set(x + dx, 2, z, "spruce_pressure_plate");
        }
        p.set(x, 2, z, "cartography_table");
        p.set(x, 0, z, "stone_bricks");
    }

    /** Amtshaus im Norden (Fassade zum Platz), Steckbrief-Tafel davor. */
    private static void inn(Plan p, Random r) {
        Kit.house(p, new Kit.House(-8, -19, 8, -13, 's', 0, "white_terracotta", "spruce", true, true, "spruce_planks"), r);
        int bx = BOARD[0];
        int bz = BOARD[1] - 1;
        for (int dx = -4; dx <= 4; dx++) {
            for (int y = 1; y <= 4; y++) {
                p.set(bx + dx, y, bz, Math.abs(dx) == 4 ? "stripped_spruce_log[axis=y]" : "dark_oak_planks");
            }
            p.set(bx + dx, 5, bz, "spruce_slab[type=bottom]");
        }
        p.banner(bx - 4, 3, bz + 1, "light_blue_wall_banner[facing=south]", Decor.lozenges());
        p.banner(bx + 4, 3, bz + 1, "green_wall_banner[facing=south]", Decor.emerald());
    }

    /** Tafel „So geht's“ im Westen: Holzgestell mit Dach. */
    private static void howto(Plan p) {
        int x = HOWTO[0] - 1;
        int z = HOWTO[1];
        for (int dz = -2; dz <= 2; dz++) {
            for (int y = 1; y <= 3; y++) {
                p.set(x, y, z + dz, Math.abs(dz) == 2 ? "stripped_spruce_log[axis=y]" : "spruce_planks");
            }
            p.set(x, 4, z + dz, "spruce_slab[type=bottom]");
        }
    }

    /** Höhe der Linde (Stamm, Krone) – für Tests. */
    public static final int LINDEN_TOP = 14;

    /** Die Dorflinde: dicker Stamm, große runde Krone, eine Bank rundherum. */
    private static void linden(Plan p, Random r) {
        for (int y = 1; y <= 7; y++) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    p.set(dx, y, dz, "oak_wood[axis=y]");
                }
            }
        }
        for (int dx = -6; dx <= 7; dx++) {
            for (int dy = 0; dy <= 7; dy++) {
                for (int dz = -6; dz <= 7; dz++) {
                    double d = Math.sqrt((dx - 0.5) * (dx - 0.5) + (dy - 3) * (dy - 3) * 1.7 + (dz - 0.5) * (dz - 0.5));
                    if (d <= 6.3 - r.nextDouble() * 0.7) {
                        p.setIfEmpty(dx, 7 + dy, dz, r.nextInt(9) == 0 ? "flowering_azalea_leaves[persistent=true]"
                                : "oak_leaves[persistent=true]");
                    }
                }
            }
        }
        for (int y = 8; y <= 10; y++) {
            p.set(0, y, 0, "oak_log[axis=y]");
        }
        // runde Bank aus Treppen um den Stamm (Lehne zum Stamm)
        for (int i = -1; i <= 2; i++) {
            p.set(i, 1, -2, "spruce_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]");
            p.set(i, 1, 3, "spruce_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]");
            p.set(-2, 1, i, "spruce_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]");
            p.set(3, 1, i, "spruce_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]");
        }
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + Math.PI / 4;
            Decor.hangingLantern(p, (int) Math.round(0.5 + Math.cos(a) * 4), 7, (int) Math.round(0.5 + Math.sin(a) * 4), 1);
        }
    }

    /** Unsichtbarer Zaun um die Insel: niemand fällt hinunter. */
    private static void barrierRing(Plan p) {
        for (int x = -RADIUS - 4; x <= RADIUS + 4; x++) {
            for (int z = -RADIUS - 4; z <= RADIUS + 4; z++) {
                double d = Math.hypot(x, z);
                if (d > RADIUS - 2 && p.get(x, 0, z) == null && near(p, x, z)) {
                    for (int y = 0; y <= 3; y++) {
                        p.setIfEmpty(x, y, z, "barrier");
                    }
                }
            }
        }
    }

    private static boolean near(Plan p, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (p.get(x + dx, 0, z + dz) != null && !"barrier".equals(p.get(x + dx, 0, z + dz))) {
                    return true;
                }
            }
        }
        return false;
    }
}
