package de.moinmorni.dorfkrimi.world;

import java.util.List;
import java.util.Random;

/**
 * Bauten für die Karten des Dorfkrimis: Kirche mit begehbarem Turm, Scheune zum Durchlaufen mit Heuboden, Glockenstuhl
 * für die Dorfversammlung, Brunnen, Bänke, Hochsitz, Holzstoß, Leiterwagen, Felsbrocken, Wege und Wald. Eigene Entwürfe,
 * nur Minecraft-Blöcke.
 */
final class Town {

    private Town() {
    }

    /** Weg (gerade, in x oder z) aus Trampelpfad und Kies, nur auf Wiese. */
    static void path(Plan p, int x1, int z1, int x2, int z2, int width, Random r) {
        int half = width / 2;
        boolean alongX = Math.abs(x2 - x1) >= Math.abs(z2 - z1);
        for (int x = Math.min(x1, x2) - (alongX ? 0 : half); x <= Math.max(x1, x2) + (alongX ? 0 : half); x++) {
            for (int z = Math.min(z1, z2) - (alongX ? half : 0); z <= Math.max(z1, z2) + (alongX ? half : 0); z++) {
                String g = p.get(x, 0, z);
                if (g != null && g.startsWith("grass_block")) {
                    p.set(x, 0, z, r.nextInt(5) == 0 ? "gravel" : r.nextInt(7) == 0 ? "coarse_dirt" : "dirt_path");
                }
            }
        }
    }

    /** Runder gepflasterter Platz. */
    static void plaza(Plan p, int cx, int cz, double radius, Random r) {
        int rr = (int) Math.ceil(radius);
        for (int x = -rr; x <= rr; x++) {
            for (int z = -rr; z <= rr; z++) {
                double d = Math.hypot(x, z);
                if (d <= radius) {
                    p.set(cx + x, 0, cz + z, d > radius - 1 ? "stone_bricks"
                            : Decor.pick(r, "cobblestone", "stone_bricks", "andesite", "cobblestone", "mossy_cobblestone"));
                    p.clear(cx + x, 1, cz + z);
                }
            }
        }
    }

    /** Brunnen: Steinbecken 5 × 5 mit Wasser, in der Mitte eine Säule mit Laterne. */
    static void fountain(Plan p, int cx, int cz) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                p.set(cx + dx, -1, cz + dz, "stone_bricks");
                p.set(cx + dx, 0, cz + dz, rim ? "stone_bricks" : "water");
                if (rim) {
                    p.set(cx + dx, 1, cz + dz, "stone_brick_slab[type=bottom,waterlogged=false]");
                }
            }
        }
        p.set(cx, 0, cz, "chiseled_stone_bricks");
        p.set(cx, 1, cz, "stone_brick_wall");
        p.set(cx, 2, cz, "stone_brick_wall");
        p.set(cx, 3, cz, "lantern[hanging=false,waterlogged=false]");
    }

    /** Glockenstuhl: zwei Pfosten, Balken, darunter die Glocke für die Dorfversammlung (Glocke bei y = 3). */
    static void bellFrame(Plan p, int x, int z, boolean alongX) {
        int ox = alongX ? 1 : 0;
        int oz = alongX ? 0 : 1;
        for (int s : new int[] {-1, 1}) {
            for (int y = 1; y <= 4; y++) {
                p.set(x + s * ox, y, z + s * oz, "stripped_spruce_log[axis=y]");
            }
            p.set(x + s * ox, 0, z + s * oz, "stone_bricks");
        }
        p.set(x, 4, z, "stripped_spruce_log[axis=" + (alongX ? "x" : "z") + "]");
        p.set(x, 5, z, "spruce_slab[type=bottom]");
        p.set(x + ox, 5, z + oz, "spruce_slab[type=bottom]");
        p.set(x - ox, 5, z - oz, "spruce_slab[type=bottom]");
        p.set(x, 3, z, "bell[attachment=ceiling,facing=" + (alongX ? "north" : "east") + ",powered=false]");
        p.set(x, 0, z, "polished_andesite");
    }

    /** Bank aus Treppen (in z-Richtung), Lehne nach {@code back}. Liefert die Sitzplätze {x, y, z, Blick}. */
    static void bench(Plan p, int x, int z1, int z2, String back, List<double[]> seats) {
        float yaw = switch (back) {
            case "west" -> -90f;
            case "east" -> 90f;
            case "north" -> 0f;
            default -> 180f;
        };
        for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
            p.set(x, 1, z, "spruce_stairs[facing=" + back + ",half=bottom,shape=straight,waterlogged=false]");
            seats.add(new double[] {x + 0.5, 1, z + 0.5, yaw});
        }
    }

    /** Bank in x-Richtung. */
    static void benchX(Plan p, int z, int x1, int x2, String back, List<double[]> seats) {
        float yaw = "north".equals(back) ? 0f : 180f;
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            p.set(x, 1, z, "spruce_stairs[facing=" + back + ",half=bottom,shape=straight,waterlogged=false]");
            seats.add(new double[] {x + 0.5, 1, z + 0.5, yaw});
        }
    }

    /**
     * Kirche (Eingang im Süden, Hintertür im Norden) mit Bänken, Altar, hohen Fenstern und einem Turm vorn links: innen
     * eine Leiter bis zur Glockenstube mit Ausblick.
     */
    static void church(Plan p, int x1, int z1, int x2, int z2, Random r) {
        int top = 7;
        int door = (x1 + x2) / 2;
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                boolean edge = x == x1 || x == x2 || z == z1 || z == z2;
                p.set(x, 0, z, edge ? "stone_bricks" : (Math.abs(x - door) <= 1 ? "red_terracotta" : "polished_andesite"));
                for (int y = 1; y <= top; y++) {
                    if (!edge) {
                        p.clear(x, y, z);
                        continue;
                    }
                    boolean corner = (x == x1 || x == x2) && (z == z1 || z == z2);
                    p.set(x, y, z, corner ? "stone_bricks" : y == 1 ? "stone_bricks" : "white_concrete");
                }
            }
        }
        // hohe Fenster an den Seiten
        for (int z = z1 + 2; z <= z2 - 2; z += 3) {
            for (int y = 2; y <= 5; y++) {
                p.set(x1, y, z, "yellow_stained_glass_pane");
                p.set(x2, y, z, "yellow_stained_glass_pane");
            }
        }
        // Türen vorn und hinten (zwei breit)
        for (int[] d : new int[][] {{z2, 0}, {z1, 1}}) {
            String facing = d[1] == 0 ? "south" : "north";
            for (int dx = 0; dx <= 1; dx++) {
                int x = door - 1 + dx;
                String hinge = dx == 0 ? "left" : "right";
                p.set(x, 1, d[0], "dark_oak_door[facing=" + facing + ",half=lower,hinge=" + hinge + ",open=true,powered=false]");
                p.set(x, 2, d[0], "dark_oak_door[facing=" + facing + ",half=upper,hinge=" + hinge + ",open=true,powered=false]");
            }
        }
        // Bänke mit Mittelgang, Altar im Norden
        for (int z = z1 + 5; z <= z2 - 2; z += 2) {
            for (int x = x1 + 1; x <= x2 - 1; x++) {
                if (Math.abs(x - door) > 1 && Math.abs(x - door + 0.5) > 1) {
                    p.set(x, 1, z, "dark_oak_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]");
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            p.set(door + dx, 1, z1 + 2, "quartz_block");
            p.set(door + dx, 2, z1 + 2, dx == 0 ? "candle[candles=4,lit=true,waterlogged=false]" : "white_carpet");
        }
        p.set(door, 1, z1 + 2, "chiseled_quartz_block");
        p.set(door, 3, z1 + 1, "gold_block");
        for (int z = z1 + 3; z <= z2 - 3; z += 4) {
            p.set(door, top, z, "lantern[hanging=true,waterlogged=false]");
        }
        Kit.roof(p, x1, z1, x2, z2, top + 1, "dark_oak", true);
        tower(p, x1 - 5, z2 - 4, r);
    }

    /** Kirchturm 5 × 5 (Ecke {@code tx}|{@code tz}), Tür im Süden, Leiter innen bis zur Glockenstube. */
    static void tower(Plan p, int tx, int tz, Random r) {
        int belfry = 13;
        for (int x = tx; x <= tx + 4; x++) {
            for (int z = tz; z <= tz + 4; z++) {
                boolean edge = x == tx || x == tx + 4 || z == tz || z == tz + 4;
                boolean corner = (x == tx || x == tx + 4) && (z == tz || z == tz + 4);
                p.set(x, 0, z, "stone_bricks");
                for (int y = 1; y <= belfry + 3; y++) {
                    if (!edge) {
                        p.clear(x, y, z);
                        continue;
                    }
                    boolean opening = !corner && y >= belfry + 1 && y <= belfry + 2;
                    if (opening) {
                        p.set(x, y, z, y == belfry + 1 ? "dark_oak_fence" : "air");
                        if (y == belfry + 2) {
                            p.clear(x, y, z);
                        }
                    } else {
                        p.set(x, y, z, corner || y == 1 ? "stone_bricks" : "white_concrete");
                    }
                }
                if (!edge) {
                    p.set(x, belfry, z, "spruce_planks");
                    p.set(x, belfry + 4, z, "spruce_planks");
                }
            }
        }
        // Tür
        p.set(tx + 2, 1, tz + 4, "dark_oak_door[facing=south,half=lower,hinge=left,open=true,powered=false]");
        p.set(tx + 2, 2, tz + 4, "dark_oak_door[facing=south,half=upper,hinge=left,open=true,powered=false]");
        // Fenster im Turm
        for (int y = 5; y <= 10; y += 5) {
            p.set(tx, y, tz + 2, "glass_pane");
            p.set(tx + 4, y, tz + 2, "glass_pane");
        }
        // Leiter an der Nordwand
        for (int y = 1; y <= belfry; y++) {
            p.set(tx + 2, y, tz + 1, "ladder[facing=south,waterlogged=false]");
        }
        p.set(tx + 2, belfry + 3, tz + 2, "bell[attachment=ceiling,facing=south,powered=false]");
        // Spitzdach
        for (int i = 0; i <= 2; i++) {
            int y = belfry + 4 + i;
            for (int x = tx - 1 + i; x <= tx + 5 - i; x++) {
                for (int z = tz - 1 + i; z <= tz + 5 - i; z++) {
                    boolean edge = x == tx - 1 + i || x == tx + 5 - i || z == tz - 1 + i || z == tz + 5 - i;
                    if (edge) {
                        p.set(x, y, z, "dark_oak_planks");
                    }
                }
            }
        }
        for (int y = belfry + 7; y <= belfry + 9; y++) {
            p.set(tx + 2, y, tz + 2, "dark_oak_fence");
        }
        p.set(tx + 2, belfry + 10, tz + 2, "gold_block");
    }

    /**
     * Scheune (lang in x): offene Tore an beiden Stirnseiten, Heuballen, Heuboden über der östlichen Hälfte mit Leiter,
     * Leiterwagen. Tore drei breit, vier hoch.
     */
    static void barn(Plan p, int x1, int z1, int x2, int z2, Random r) {
        int top = 6;
        int mid = (z1 + z2) / 2;
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                boolean ex = x == x1 || x == x2;
                boolean edge = ex || z == z1 || z == z2;
                p.set(x, 0, z, edge ? "cobblestone" : Decor.pick(r, "packed_mud", "coarse_dirt", "dirt"));
                for (int y = 1; y <= top; y++) {
                    if (!edge) {
                        p.clear(x, y, z);
                        continue;
                    }
                    boolean gate = ex && Math.abs(z - mid) <= 1 && y <= 4;
                    if (gate) {
                        p.clear(x, y, z);
                        continue;
                    }
                    boolean post = (x - x1) % 4 == 0 || z == z1 && x == x2 || ex && (z == z1 || z == z2);
                    p.set(x, y, z, post ? "spruce_log[axis=y]" : y == 1 ? "cobblestone" : "spruce_planks");
                }
            }
        }
        // Heuboden über der Osthälfte (Stufen oben), Leiter hinauf
        int loft = (x1 + x2) / 2 + 1;
        for (int x = loft; x < x2; x++) {
            for (int z = z1 + 1; z < z2; z++) {
                p.set(x, 4, z, "spruce_slab[type=top,waterlogged=false]");
                if (r.nextInt(3) == 0 && Math.abs(z - mid) > 0) {
                    p.set(x, 5, z, "hay_block[axis=y]");
                }
            }
        }
        for (int y = 1; y <= 4; y++) {
            p.set(loft, y, z1 + 1, "ladder[facing=east,waterlogged=false]");
        }
        p.clear(loft, 5, z1 + 1);
        // Heuballen unten an den Längswänden (Lücken zum Verstecken)
        for (int x = x1 + 1; x < loft - 1; x++) {
            for (int z : new int[] {z1 + 1, z2 - 1}) {
                if (r.nextInt(3) > 0) {
                    p.set(x, 1, z, "hay_block[axis=y]");
                    if (r.nextBoolean()) {
                        p.set(x, 2, z, "hay_block[axis=y]");
                    }
                }
            }
        }
        cart(p, (x1 + loft) / 2, mid, true);
        p.set((x1 + x2) / 2, top - 1, mid, "lantern[hanging=true,waterlogged=false]");
        Kit.roof(p, x1, z1, x2, z2, top + 1, "spruce", false);
    }

    /** Leiterwagen mit Heu: 3 lang, Räder aus Falltüren. */
    static void cart(Plan p, int x, int z, boolean alongX) {
        for (int i = -1; i <= 1; i++) {
            int cx = alongX ? x + i : x;
            int cz = alongX ? z : z + i;
            p.set(cx, 1, cz, "spruce_slab[type=bottom,waterlogged=false]");
            if (i != 0) {
                p.set(cx, 2, cz, "hay_block[axis=y]");
            }
        }
        if (alongX) {
            p.set(x - 1, 1, z - 1, "spruce_trapdoor[facing=north,half=bottom,open=true,powered=false,waterlogged=false]");
            p.set(x + 1, 1, z - 1, "spruce_trapdoor[facing=north,half=bottom,open=true,powered=false,waterlogged=false]");
            p.set(x - 1, 1, z + 1, "spruce_trapdoor[facing=south,half=bottom,open=true,powered=false,waterlogged=false]");
            p.set(x + 1, 1, z + 1, "spruce_trapdoor[facing=south,half=bottom,open=true,powered=false,waterlogged=false]");
        }
    }

    /** Hochsitz: Plattform auf vier Stämmen (y = 5), Leiter, Geländer, Dach. */
    static void hide(Plan p, int x, int z) {
        for (int dx : new int[] {0, 2}) {
            for (int dz : new int[] {0, 2}) {
                for (int y = 1; y <= 8; y++) {
                    p.set(x + dx, y, z + dz, y <= 4 || y >= 6 ? "spruce_log[axis=y]" : "spruce_fence");
                }
            }
        }
        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                p.set(x + dx, 5, z + dz, "spruce_planks");
                p.set(x + dx, 9, z + dz, "spruce_slab[type=bottom,waterlogged=false]");
                if ((dx == 1) != (dz == 1) && dz != 2) {
                    p.set(x + dx, 6, z + dz, "spruce_fence");
                }
            }
        }
        p.clear(x + 1, 6, z + 2);
        for (int y = 1; y <= 5; y++) {
            p.set(x + 1, y, z + 3, "ladder[facing=south,waterlogged=false]");
        }
        p.set(x + 1, 5, z + 2, "spruce_planks");
    }

    /** Holzstoß (liegende Stämme), dahinter kann man sich ducken. */
    static void woodpile(Plan p, int x, int z, int length, boolean alongX) {
        Kit.woodpile(p, x, 1, z, length, 2, alongX);
        for (int i = 0; i < length; i++) {
            p.set(alongX ? x + i : x, 3, alongX ? z : z + i, "spruce_slab[type=bottom,waterlogged=false]");
        }
    }

    /** Felsbrocken aus bemoostem Stein. */
    static void boulder(Plan p, int x, int z, int size, Random r) {
        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                for (int y = 1; y <= size + 1; y++) {
                    double d = Math.sqrt(dx * dx + dz * dz + (y - 1) * (y - 1) * 1.6);
                    if (d <= size + 0.4 - r.nextDouble() * 0.5) {
                        p.set(x + dx, y, z + dz, Decor.pick(r, "mossy_cobblestone", "stone", "andesite", "cobblestone",
                                "mossy_stone_bricks"));
                    }
                }
            }
        }
    }

    /** Wald: Fichten und ein paar Laubbäume in einem Rechteck, nur auf freier Wiese, mit Farn dazwischen. */
    static void forest(Plan p, int x1, int z1, int x2, int z2, int count, Random r) {
        int placed = 0;
        for (int i = 0; i < count * 6 && placed < count; i++) {
            int x = x1 + r.nextInt(Math.max(1, x2 - x1 + 1));
            int z = z1 + r.nextInt(Math.max(1, z2 - z1 + 1));
            if (!freeGround(p, x, z, 2)) {
                continue;
            }
            if (r.nextInt(4) == 0) {
                Decor.broadTree(p, x, 1, z, "oak_log", "oak_leaves", 4, 2, r);
            } else {
                Decor.spruce(p, x, 1, z, 5 + r.nextInt(4), r);
            }
            placed++;
        }
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                String g = p.get(x, 0, z);
                if (g != null && g.startsWith("grass_block") && p.get(x, 1, z) == null && r.nextInt(6) == 0) {
                    p.set(x, 1, z, Decor.pick(r, "fern", "short_grass", "fern", "sweet_berry_bush[age=1]"));
                } else if (g != null && g.startsWith("grass_block") && r.nextInt(5) == 0) {
                    p.set(x, 0, z, Decor.pick(r, "podzol[snowy=false]", "coarse_dirt", "moss_block"));
                }
            }
        }
    }

    /** Ist hier freie Wiese mit Platz drumherum (für Bäume, Büsche)? */
    static boolean freeGround(Plan p, int x, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                String g = p.get(x + dx, 0, z + dz);
                if (g == null || !(g.startsWith("grass_block") || g.startsWith("podzol") || g.equals("moss_block"))) {
                    return false;
                }
                for (int y = 1; y <= 3; y++) {
                    String a = p.get(x + dx, y, z + dz);
                    if (a != null && !Blocks.passable(a)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Laternenpfahl und eine unsichtbare Lichtquelle darüber (Nachtkarten bleiben spielbar). */
    static void lamp(Plan p, int x, int z) {
        Decor.lanternPost(p, x, 1, z, "spruce_fence");
        p.set(x, 5, z, "light[level=12,waterlogged=false]");
    }
}
