package de.moinmorni.dorfkrimi.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Karte „Oberdorf bei Nacht“ (eigener Entwurf): ein dicht bebautes Bergdorf im Mondlicht. In der Mitte der Dorfplatz
 * mit Brunnen (dort beginnt die Runde und dort tagt die Dorfversammlung), im Osten das Rathaus mit dem Glockenstuhl,
 * im Westen das Gasthaus „Zur Post“ mit der Bank davor, im Norden die Kirche mit begehbarem Turm. Ringsum acht Häuser
 * mit zwei Türen (zum Durchlaufen) und Kammern im Obergeschoss, zwei Scheunen mit Heuboden, ein Backhaus, ein
 * Holzlager, ein Obstgarten, ein Kräutergarten – und im Süden der Wald mit Jagdhütte und Hochsitz.
 */
final class OberdorfPlan {

    static final int HALF_X = 58;
    static final int HALF_Z = 46;
    static final int VOID_Y = -10;
    /** Mitternacht bei Vollmond (Tag 0 der Mondphasen). */
    static final long NIGHT = 18000L;
    static final int[] BELL = {13, 3, -5};
    static final double PLAZA = 11.5;

    private OberdorfPlan() {
    }

    /** Häuser: x1, z1, x2, z2, Tür, Tür bei, Putz, Einrichtung (0 Stube, 1 Küche, 2 Werkstatt, 3 Laden). */
    private static final Object[][] HOUSES = {
        {14, -24, 22, -16, 's', 18, "white_terracotta", 0},
        {-24, -24, -16, -16, 's', -20, "white_terracotta", 1},
        {14, 16, 22, 24, 'n', 18, "white_terracotta", 1},
        {-24, 16, -16, 24, 'n', -20, "light_gray_terracotta", 0},
        {34, -14, 42, -6, 'w', -10, "white_terracotta", 3},
        {34, 6, 42, 14, 'w', 10, "white_terracotta", 0},
        {-44, -14, -36, -6, 'e', -10, "light_gray_terracotta", 2},
        {-44, 6, -36, 14, 'e', 10, "white_terracotta", 1},
    };

    static MapLayout layout() {
        List<double[]> seats = new ArrayList<>();
        benches(new Plan(), seats);
        return new MapLayout(new int[] {0, 0}, 4.5, new int[] {0, 0}, 7.5, BELL.clone(), List.copyOf(seats), HALF_X, HALF_Z,
                VOID_Y, NIGHT, false);
    }

    /** Zwei Bänke vor dem Gasthaus (Lehne im Westen, Blick zum Dorfplatz): 16 Plätze. */
    private static void benches(Plan p, List<double[]> seats) {
        Town.bench(p, -13, -9, -2, "west", seats);
        Town.bench(p, -13, 2, 9, "west", seats);
    }

    static Plan build() {
        Plan p = new Plan();
        Random r = new Random(1789);
        Decor.island(p, 70, 58, 10, r);
        streets(p, r);
        Town.plaza(p, 0, 0, PLAZA, r);
        Town.fountain(p, 0, 0);
        for (int[] l : new int[][] {{7, 7}, {-7, 7}, {7, -7}, {-7, -7}}) {
            Town.lamp(p, l[0], l[1]);
        }
        // Rathaus (Osten) mit Glockenstuhl davor
        Kit.House rathaus = new Kit.House(15, -6, 27, 6, 'w', 0, "white_terracotta", "dark_oak", true, true,
                "dark_oak_planks").through();
        Kit.house(p, rathaus, r);
        Rooms.furnish(p, rathaus, Rooms.Kind.AMTSSTUBE, r);
        p.banner(15, 4, -3, "light_blue_wall_banner[facing=west]", Decor.lozenges());
        p.banner(15, 4, 3, "light_blue_wall_banner[facing=west]", Decor.lozenges());
        Town.bellFrame(p, BELL[0], BELL[2], false);
        // Gasthaus „Zur Post“ (Westen) mit der Bank davor
        Kit.House post = new Kit.House(-27, -6, -15, 6, 'e', 0, "white_terracotta", "spruce", true, true,
                "spruce_planks").through();
        Kit.house(p, post, r);
        Rooms.furnish(p, post, Rooms.Kind.STUBE, r);
        Kit.longTable(p, -24, 1, -3, 4, true, "spruce", true, r, "flower_pot", "candle[candles=1,lit=true,waterlogged=false]");
        Kit.longTable(p, -24, 1, 3, 4, true, "spruce", true, r, "flower_pot");
        benches(p, new ArrayList<>());
        p.banner(-15, 4, -3, "green_wall_banner[facing=east]", Decor.emerald());
        // Kirche (Norden) mit Turm
        Town.church(p, -6, -42, 6, -26, r);
        // Häuser
        for (Object[] h : HOUSES) {
            Kit.House house = new Kit.House((int) h[0], (int) h[1], (int) h[2], (int) h[3], (char) h[4], (int) h[5],
                    (String) h[6], "spruce", true, r.nextBoolean(), "spruce_planks").through();
            Kit.house(p, house, r);
            Rooms.furnish(p, house, Rooms.Kind.values()[(int) h[7]], r);
        }
        // Backhaus (Osten), Schuppen im Holzlager (Westen), Jagdhütte im Wald
        Kit.House back = new Kit.House(46, -4, 52, 4, 'w', 0, "bricks", "spruce", false, false, "bricks").through();
        Kit.house(p, back, r);
        Rooms.furnish(p, back, Rooms.Kind.KUECHE, r);
        Kit.House shed = new Kit.House(-52, -36, -46, -30, 'e', -33, "spruce_planks", "spruce", false, false,
                "spruce_planks").through();
        Kit.house(p, shed, r);
        Rooms.furnish(p, shed, Rooms.Kind.WERKSTATT, r);
        Kit.House hut = new Kit.House(-4, 34, 4, 40, 'n', 0, "stripped_spruce_wood[axis=y]", "spruce", false, false,
                "spruce_planks").through();
        Kit.house(p, hut, r);
        Rooms.furnish(p, hut, Rooms.Kind.LADEN, r);
        // Scheunen
        Town.barn(p, 26, 22, 38, 32, r);
        Town.barn(p, -38, 22, -26, 32, r);
        // Holzlager, Obstgarten, Kräutergarten
        for (int i = 0; i < 4; i++) {
            Town.woodpile(p, -42 + i * 3, -40, 5, false);
        }
        Town.woodpile(p, -44, -26, 6, true);
        Town.cart(p, -34, -30, true);
        orchard(p, r);
        herbGarden(p, 10, -42, 24, -32, r);
        // Wald im Süden mit Hochsitz, Wäldchen im Nordwesten
        Town.hide(p, 13, 33);
        Town.forest(p, -24, 28, 24, 44, 26, r);
        Town.forest(p, -56, -20, -48, 20, 10, r);
        Town.forest(p, 44, 12, 56, 44, 10, r);
        for (int[] b : new int[][] {{-30, 38}, {22, 41}, {-12, 30}, {48, -18}, {-52, 26}}) {
            Town.boulder(p, b[0], b[1], 1, r);
        }
        Town.cart(p, 30, -2, false);
        Town.cart(p, -8, 18, true);
        Decor.greenery(p, -HALF_X + 2, -HALF_Z + 2, HALF_X - 2, HALF_Z - 2, 18, r,
                (x, z) -> Math.hypot(x, z) < PLAZA + 3);
        Decor.flowers(p, -HALF_X, -HALF_Z, HALF_X, HALF_Z, 7, r, (x, z) -> Math.hypot(x, z) < PLAZA + 1);
        Decor.border(p, HALF_X, HALF_Z, 8, "spruce_fence");
        for (int i = 0; i < 14; i++) {
            Decor.cloud(p, -90 + r.nextInt(180), -14 - r.nextInt(6), -70 + r.nextInt(140), 3 + r.nextInt(4), r);
        }
        Decor.mountainRing(p, 120, 105, -12, r);
        p.connect();
        return p;
    }

    /** Gassen: Kirchweg, Wald-Weg, obere und untere Gasse, Ostgasse und Westgasse – mit Laternen. */
    private static void streets(Plan p, Random r) {
        Town.path(p, 0, -26, 0, -11, 3, r);
        Town.path(p, 0, 11, 0, 34, 3, r);
        Town.path(p, -31, -14, 31, -14, 3, r);
        Town.path(p, -31, 14, 31, 14, 3, r);
        Town.path(p, 31, -20, 31, 20, 3, r);
        Town.path(p, -31, -20, -31, 20, 3, r);
        Town.path(p, 11, 0, 15, 0, 3, r);
        Town.path(p, -15, 0, -11, 0, 3, r);
        Town.path(p, 31, 0, 46, 0, 3, r);
        Town.path(p, -46, -33, -31, -33, 3, r);
        Town.path(p, -31, -33, -31, -20, 3, r);
        for (int x = -26; x <= 26; x += 13) {
            if (x != 0) {
                Town.lamp(p, x, -16);
                Town.lamp(p, x, 16);
            }
        }
        for (int z = -8; z <= 8; z += 16) {
            Town.lamp(p, 33, z);
            Town.lamp(p, -33, z);
        }
        Town.lamp(p, 2, 24);
        Town.lamp(p, -2, -20);
        Town.lamp(p, 44, 2);
        Town.lamp(p, -44, -31);
    }

    /** Obstgarten im Nordosten: Obstbäume in Reihen, Bienenstöcke, Zaun. */
    private static void orchard(Plan p, Random r) {
        for (int x = 30; x <= 52; x += 6) {
            for (int z = -42; z <= -26; z += 6) {
                int tx = x + r.nextInt(2);
                int tz = z + r.nextInt(2);
                if (Town.freeGround(p, tx, tz, 2)) {
                    Decor.fruitTree(p, tx, 1, tz, r);
                }
            }
        }
        p.set(28, 1, -34, "beehive[facing=west,honey_level=3]");
        p.set(28, 1, -36, "beehive[facing=west,honey_level=1]");
    }

    /** Kräutergarten: Zaun mit Gattertür, Beete mit Feldfrüchten (man kann hindurchlaufen). */
    private static void herbGarden(Plan p, int x1, int z1, int x2, int z2, Random r) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                boolean edge = x == x1 || x == x2 || z == z1 || z == z2;
                if (edge) {
                    p.set(x, 1, z, "oak_fence");
                } else if ((x - x1) % 3 == 0) {
                    p.set(x, 0, z, "dirt_path");
                } else {
                    p.set(x, 0, z, "farmland[moisture=7]");
                    p.set(x, 1, z, Decor.pick(r, "wheat[age=7]", "carrots[age=7]", "potatoes[age=7]", "beetroots[age=3]"));
                }
            }
        }
        int gx = (x1 + x2) / 2;
        p.set(gx, 1, z2, "oak_fence_gate[facing=south,in_wall=false,open=true,powered=false]");
        p.set(x1, 1, (z1 + z2) / 2, "oak_fence_gate[facing=west,in_wall=false,open=true,powered=false]");
        // Vogelscheuche
        p.set(gx + 1, 1, (z1 + z2) / 2, "oak_fence");
        p.set(gx + 1, 2, (z1 + z2) / 2, "hay_block[axis=y]");
        p.set(gx + 1, 3, (z1 + z2) / 2, "carved_pumpkin[facing=south]");
    }
}
