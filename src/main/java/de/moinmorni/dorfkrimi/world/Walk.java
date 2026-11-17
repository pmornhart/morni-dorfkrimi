package de.moinmorni.dorfkrimi.world;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Wohin kommt man zu Fuß? Ohne Server, nur aus dem Bauplan: einen Block hochspringen, bis zu drei hinunter, Türen und
 * Gattertüren gehen auf, Leitern klettert man hinauf. So prüfen Tests, dass jede Spur, jeder Startplatz und die Glocke
 * erreichbar sind – und so sucht die Karte die Stellen aus, an denen Spuren liegen dürfen.
 */
public final class Walk {

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int MAX_DROP = 3;

    private Walk() {
    }

    /** Kann man hier hindurch (auch Türen, Tore, Leitern)? */
    static boolean open(Plan p, int x, int y, int z) {
        String s = p.get(x, y, z);
        if (s == null || Blocks.passable(s)) {
            return true;
        }
        String b = Blocks.base(s);
        return b.endsWith("_door") || b.endsWith("_fence_gate") || b.equals("ladder") || b.equals("light");
    }

    static boolean ladder(Plan p, int x, int y, int z) {
        String s = p.get(x, y, z);
        return s != null && Blocks.base(s).equals("ladder");
    }

    /** Hier stehen: fester Boden darunter (oder Leiter), zwei Blöcke frei. */
    static boolean stand(Plan p, int x, int y, int z) {
        boolean ground = !open(p, x, y - 1, z) || ladder(p, x, y - 1, z) || ladder(p, x, y, z);
        return ground && open(p, x, y, z) && open(p, x, y + 1, z);
    }

    /** Alle erreichbaren Stellen (Füße) ab (x, y, z), innerhalb des Zauns. */
    public static Set<Long> from(Plan p, MapLayout l, int x0, int y0, int z0) {
        Set<Long> seen = new LinkedHashSet<>();
        Deque<int[]> todo = new ArrayDeque<>();
        if (!stand(p, x0, y0, z0)) {
            return seen;
        }
        todo.add(new int[] {x0, y0, z0});
        seen.add(Plan.key(x0, y0, z0));
        while (!todo.isEmpty()) {
            int[] c = todo.poll();
            // Leiter hinauf und hinunter
            if (ladder(p, c[0], c[1], c[2]) || ladder(p, c[0], c[1] + 1, c[2])) {
                visit(p, seen, todo, c[0], c[1] + 1, c[2]);
            }
            if (ladder(p, c[0], c[1] - 1, c[2])) {
                visit(p, seen, todo, c[0], c[1] - 1, c[2]);
            }
            for (int[] d : DIRS) {
                int x = c[0] + d[0];
                int z = c[2] + d[1];
                if (!l.inside(x + 0.5, z + 0.5)) {
                    continue;
                }
                for (int dy = 1; dy >= -MAX_DROP; dy--) {
                    int y = c[1] + dy;
                    if (y < l.voidY()) {
                        break;
                    }
                    if (dy == 1 && !open(p, c[0], c[1] + 2, c[2])) {
                        continue;
                    }
                    if (stand(p, x, y, z)) {
                        visit(p, seen, todo, x, y, z);
                        break;
                    }
                    if (!open(p, x, y + 1, z) && dy <= 0) {
                        break;
                    }
                }
            }
        }
        return seen;
    }

    private static void visit(Plan p, Set<Long> seen, Deque<int[]> todo, int x, int y, int z) {
        if (stand(p, x, y, z) && seen.add(Plan.key(x, y, z))) {
            todo.add(new int[] {x, y, z});
        }
    }
}
