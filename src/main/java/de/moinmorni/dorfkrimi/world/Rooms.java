package de.moinmorni.dorfkrimi.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Einrichtung der Häuser: Stube mit Tisch und Kachelofen, Küche, Werkstatt, Laden, Kammer oben. Möbel stehen nur an den
 * Wänden; zwischen den beiden Türen bleibt ein Gang frei (man rennt hindurch), neben Schränken bleiben Nischen zum
 * Verstecken. Eigene Entwürfe, nur Minecraft-Blöcke.
 */
final class Rooms {

    enum Kind {
        STUBE, KUECHE, WERKSTATT, LADEN, AMTSSTUBE
    }

    private Rooms() {
    }

    /** Stellen an den Innenwänden (y = 1), ohne Gang, Türen und Leiter. */
    private static List<int[]> wallCells(Kit.House h) {
        List<int[]> out = new ArrayList<>();
        int[] ladder = Kit.ladderAt(h);
        boolean ns = h.door() == 'n' || h.door() == 's';
        for (int x = h.x1() + 1; x <= h.x2() - 1; x++) {
            for (int z = h.z1() + 1; z <= h.z2() - 1; z++) {
                boolean wall = x == h.x1() + 1 || x == h.x2() - 1 || z == h.z1() + 1 || z == h.z2() - 1;
                int along = ns ? x : z;
                boolean corridor = Math.abs(along - h.doorAt()) <= 1;
                boolean nearLadder = ladder != null && Math.abs(x - ladder[0]) + Math.abs(z - ladder[1]) <= 1;
                if (wall && !corridor && !nearLadder) {
                    out.add(new int[] {x, z});
                }
            }
        }
        return out;
    }

    static void furnish(Plan p, Kit.House h, Kind kind, Random r) {
        List<int[]> cells = wallCells(h);
        String w = h.wood();
        int i = 0;
        for (int[] c : cells) {
            i++;
            // Jede dritte Stelle bleibt frei – Nischen zum Verstecken und Platz zum Laufen.
            if (i % 3 == 0) {
                continue;
            }
            String item = switch (kind) {
                case STUBE -> Decor.pick(r, "bookshelf", w + "_planks", "barrel[facing=up,open=false]",
                        "potted_red_tulip", "chiseled_bookshelf[facing=north]", "green_glazed_terracotta");
                case KUECHE -> Decor.pick(r, "smoker[facing=north,lit=true]", "barrel[facing=up,open=false]",
                        "crafting_table", "cauldron", "barrel[facing=up,open=false]", "furnace[facing=north,lit=false]");
                case WERKSTATT -> Decor.pick(r, "smithing_table", "anvil[facing=north]", "grindstone[face=floor,facing=north]",
                        "barrel[facing=up,open=false]", "stonecutter[facing=north]", "crafting_table", "blast_furnace[facing=north,lit=true]");
                case LADEN -> Decor.pick(r, "barrel[facing=up,open=false]", "composter[level=0]", "hay_block[axis=y]",
                        w + "_planks", "barrel[facing=up,open=false]", "pumpkin");
                case AMTSSTUBE -> Decor.pick(r, "lectern[facing=north,has_book=true,powered=false]", "bookshelf",
                        "cartography_table", "bookshelf", "loom[facing=north]");
            };
            p.set(c[0], 1, c[1], item);
            if (Blocks.full(item) && r.nextInt(3) == 0) {
                p.set(c[0], 2, c[1], Decor.pick(r, "flower_pot", "candle[candles=2,lit=true,waterlogged=false]",
                        "lantern[hanging=false,waterlogged=false]", "potted_fern"));
            }
        }
        // Tisch mit Stühlen in der Mitte einer Haushälfte, Lampe an der Decke
        boolean ns = h.door() == 'n' || h.door() == 's';
        int midZ = (h.z1() + h.z2()) / 2;
        int midX = (h.x1() + h.x2()) / 2;
        int tx = ns ? (h.doorAt() - h.x1() > h.x2() - h.doorAt() ? (h.x1() + h.doorAt()) / 2 : (h.doorAt() + h.x2()) / 2) : midX;
        int tz = ns ? midZ : (h.doorAt() - h.z1() > h.z2() - h.doorAt() ? (h.z1() + h.doorAt()) / 2 : (h.doorAt() + h.z2()) / 2);
        if (kind != Kind.WERKSTATT && tx > h.x1() + 1 && tx < h.x2() - 1 && tz > h.z1() + 1 && tz < h.z2() - 1) {
            p.set(tx, 1, tz, w + "_fence");
            p.set(tx, 2, tz, kind == Kind.KUECHE ? "cake[bites=2]" : "candle[candles=3,lit=true,waterlogged=false]");
        }
        p.set(midX, Kit.GROUND_TOP, midZ, "lantern[hanging=true,waterlogged=false]");
        if (h.upper()) {
            upstairs(p, h, r);
        }
    }

    /** Kammer oben: Betten, Truhen (Fässer), Teppich, Laterne. */
    private static void upstairs(Plan p, Kit.House h, Random r) {
        int y = Kit.GROUND_TOP + 2;
        int[] ladder = Kit.ladderAt(h);
        String[] carpets = {"red_carpet", "green_carpet", "brown_carpet", "light_gray_carpet"};
        String carpet = carpets[r.nextInt(carpets.length)];
        int bedX = h.x2() - 1;
        int bedZ = h.z2() - 2;
        for (int x = h.x1() + 1; x <= h.x2() - 1; x++) {
            for (int z = h.z1() + 1; z <= h.z2() - 1; z++) {
                if (ladder != null && x == ladder[0] && z == ladder[1]) {
                    continue;
                }
                boolean inner = x > h.x1() + 1 && x < h.x2() - 1 && z > h.z1() + 1 && z < h.z2() - 1;
                if (inner && r.nextInt(4) > 0) {
                    p.set(x, y, z, carpet);
                }
            }
        }
        if (!(ladder != null && Math.abs(ladder[0] - bedX) <= 1 && Math.abs(ladder[1] - bedZ) <= 2)) {
            p.set(bedX, y, bedZ, "red_bed[facing=south,occupied=false,part=foot]");
            p.set(bedX, y, bedZ + 1, "red_bed[facing=south,occupied=false,part=head]");
        }
        for (int[] c : new int[][] {{h.x2() - 1, h.z1() + 1, 0}, {h.x2() - 2, h.z1() + 1, 1}}) {
            if (ladder == null || Math.abs(ladder[0] - c[0]) + Math.abs(ladder[1] - c[1]) > 1) {
                p.set(c[0], y, c[1], c[2] == 0 ? "barrel[facing=up,open=false]" : "bookshelf");
            }
        }
        p.set((h.x1() + h.x2()) / 2, Kit.UPPER_TOP, (h.z1() + h.z2()) / 2, "lantern[hanging=true,waterlogged=false]");
    }
}
