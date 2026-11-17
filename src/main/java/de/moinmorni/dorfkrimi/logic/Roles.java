package de.moinmorni.dorfkrimi.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Geheime Rollen verteilen – gerecht über viele Runden. Jede Person hat zwei „Lose“-Zähler: wie viele Runden sie nicht
 * mehr Wilderer bzw. Gendarm war. Je länger, desto größer ihre Chance (Gewicht {@code (1 + Runden)²}, höchstens
 * {@link #CAP} Runden): niemand ist dauernd Wilderer, und wer lange warten musste, kommt bald dran. Ein Zufall bleibt –
 * sonst könnte man ausrechnen, wer es ist.
 *
 * <p>Partys: Zwei Sonderrollen (zwei Wilderer, oder Wilderer und Gendarm) kommen nie in dieselbe Party, solange es
 * irgendwie geht – so können Freunde, die nebenbei miteinander reden, nicht gemeinsame Sache machen. Wer welche Rolle
 * hat, erfährt sonst niemand (auch die Party nicht).
 */
public final class Roles {

    /** Mehr als so viele Runden Warten zählen nicht mehr. */
    public static final int CAP = 12;

    /** Wie lange jemand keine Sonderrolle hatte (Runden). */
    public record Lots(int sinceWilderer, int sinceGendarm) {
        public static final Lots NEW = new Lots(CAP / 2, CAP / 2);

        /** Nach einer Runde mit dieser Rolle. */
        public Lots after(Role got) {
            return new Lots(got == Role.WILDERER ? 0 : Math.min(CAP, sinceWilderer + 1),
                    got == Role.GENDARM ? 0 : Math.min(CAP, sinceGendarm + 1));
        }
    }

    private Roles() {
    }

    /** Wie viele Wilderer bei so vielen Leuten ({@code twoFrom}: ab so vielen zwei). Gendarmen: genauso viele. */
    public static int wilderers(int players, int twoFrom) {
        return players >= twoFrom ? 2 : 1;
    }

    public static double weight(int since) {
        int s = Math.max(0, Math.min(CAP, since));
        return (1.0 + s) * (1.0 + s);
    }

    /**
     * Rollen verteilen.
     *
     * @param groups    Partys und Einzelne (jede Person genau einmal)
     * @param lots      Lose-Zähler (fehlt jemand: {@link Lots#NEW})
     * @param wilderers so viele Wilderer …
     * @param gendarms  … und so viele Gendarmen (zusammen weniger als Leute)
     */
    public static Map<UUID, Role> assign(List<List<UUID>> groups, Map<UUID, Lots> lots, int wilderers, int gendarms,
            Random random) {
        Map<UUID, Integer> groupOf = new HashMap<>();
        List<UUID> all = new ArrayList<>();
        for (int g = 0; g < groups.size(); g++) {
            for (UUID u : groups.get(g)) {
                if (!groupOf.containsKey(u)) {
                    groupOf.put(u, g);
                    all.add(u);
                }
            }
        }
        Map<UUID, Role> out = new LinkedHashMap<>();
        all.forEach(u -> out.put(u, Role.DORF));
        int specials = Math.max(0, Math.min(all.size() - 1, wilderers + gendarms));
        int w = Math.min(wilderers, specials);
        int gd = Math.min(gendarms, specials - w);
        Set<Integer> usedGroups = new HashSet<>();
        for (int i = 0; i < w; i++) {
            UUID u = pick(all, out, groupOf, usedGroups, lots, random, true);
            out.put(u, Role.WILDERER);
            usedGroups.add(groupOf.get(u));
        }
        for (int i = 0; i < gd; i++) {
            UUID u = pick(all, out, groupOf, usedGroups, lots, random, false);
            out.put(u, Role.GENDARM);
            usedGroups.add(groupOf.get(u));
        }
        return out;
    }

    private static UUID pick(List<UUID> all, Map<UUID, Role> roles, Map<UUID, Integer> groupOf, Set<Integer> usedGroups,
            Map<UUID, Lots> lots, Random random, boolean wilderer) {
        List<UUID> free = all.stream().filter(u -> roles.get(u) == Role.DORF && !usedGroups.contains(groupOf.get(u)))
                .toList();
        if (free.isEmpty()) {
            free = all.stream().filter(u -> roles.get(u) == Role.DORF).toList();
        }
        double total = 0;
        double[] weights = new double[free.size()];
        for (int i = 0; i < free.size(); i++) {
            Lots l = lots.getOrDefault(free.get(i), Lots.NEW);
            weights[i] = weight(wilderer ? l.sinceWilderer() : l.sinceGendarm());
            total += weights[i];
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < free.size(); i++) {
            roll -= weights[i];
            if (roll < 0) {
                return free.get(i);
            }
        }
        return free.get(free.size() - 1);
    }

    /** Nach der Runde: neue Lose-Zähler für alle, die mitgespielt haben. */
    public static Map<UUID, Lots> update(Map<UUID, Lots> before, Map<UUID, Role> roles) {
        Map<UUID, Lots> out = new HashMap<>(before);
        roles.forEach((u, r) -> out.put(u, before.getOrDefault(u, Lots.NEW).after(r)));
        return out;
    }

    /** Alle mit dieser Rolle. */
    public static List<UUID> with(Map<UUID, Role> roles, Role role) {
        List<UUID> out = new ArrayList<>();
        roles.forEach((u, r) -> {
            if (r == role) {
                out.add(u);
            }
        });
        return out;
    }

    /** Haben zwei Sonderrollen dieselbe Party, obwohl es anders gegangen wäre? (für Tests) */
    public static boolean sameParty(Map<UUID, Role> roles, Collection<List<UUID>> groups) {
        for (List<UUID> g : groups) {
            long specials = g.stream().filter(u -> roles.get(u) != Role.DORF).count();
            if (specials > 1) {
                return true;
            }
        }
        return false;
    }
}
