package de.moinmorni.dorfkrimi.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Rollen verteilen: richtige Anzahl, Partys bekommen nie zwei Sonderrollen, gerecht über viele Runden. */
class RolesTest {

    private static List<UUID> people(int n) {
        List<UUID> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add(new UUID(7, i));
        }
        return out;
    }

    private static List<List<UUID>> singles(List<UUID> all) {
        return all.stream().map(List::of).toList();
    }

    @Test
    void countsDependOnPlayers() {
        assertEquals(1, Roles.wilderers(4, 10));
        assertEquals(1, Roles.wilderers(9, 10));
        assertEquals(2, Roles.wilderers(10, 10));
        assertEquals(2, Roles.wilderers(16, 10));
        assertEquals(1, Roles.wilderers(16, 17), "17 = nie zwei");
        for (int n = 2; n <= 16; n++) {
            List<UUID> all = people(n);
            int k = Roles.wilderers(n, 10);
            Map<UUID, Role> r = Roles.assign(singles(all), Map.of(), k, k, new Random(n));
            assertEquals(n, r.size());
            assertEquals(k, Roles.with(r, Role.WILDERER).size(), "Wilderer bei " + n);
            assertEquals(n >= 2 * k + 1 ? k : Math.max(0, n - 1 - k), Roles.with(r, Role.GENDARM).size(), "Gendarmen bei " + n);
            assertFalse(Roles.with(r, Role.DORF).isEmpty(), "mindestens einer gehört zum Dorf");
        }
    }

    /** Ab vier Leuten: niemand aus derselben Party hat zwei Sonderrollen (wenn es anders geht). */
    @Test
    void partiesNeverShareSpecialRoles() {
        Random random = new Random(3);
        for (int round = 0; round < 2000; round++) {
            List<UUID> all = people(4 + random.nextInt(13));
            List<List<UUID>> groups = new ArrayList<>();
            int i = 0;
            while (i < all.size()) {
                int size = Math.min(all.size() - i, 1 + random.nextInt(4));
                groups.add(all.subList(i, i + size));
                i += size;
            }
            int k = Roles.wilderers(all.size(), 10);
            Map<UUID, Role> r = Roles.assign(groups, Map.of(), k, k, random);
            if (groups.size() >= 2 * k) {
                assertFalse(Roles.sameParty(r, groups), "Party mit zwei Sonderrollen: " + groups + " " + r);
            }
        }
    }

    /** Über viele Runden ist jeder ungefähr gleich oft Wilderer, und niemand ist es lange am Stück. */
    @Test
    void fairOverManyRounds() {
        List<UUID> all = people(8);
        Map<UUID, Roles.Lots> lots = new HashMap<>();
        Map<UUID, Integer> wilderer = new HashMap<>();
        Map<UUID, Integer> gendarm = new HashMap<>();
        Random random = new Random(42);
        UUID last = null;
        int twice = 0;
        int longestWait = 0;
        Map<UUID, Integer> waiting = new HashMap<>();
        int rounds = 4000;
        for (int r = 0; r < rounds; r++) {
            Map<UUID, Role> roles = Roles.assign(singles(all), lots, 1, 1, random);
            UUID w = Roles.with(roles, Role.WILDERER).get(0);
            wilderer.merge(w, 1, Integer::sum);
            gendarm.merge(Roles.with(roles, Role.GENDARM).get(0), 1, Integer::sum);
            if (w.equals(last)) {
                twice++;
            }
            last = w;
            for (UUID u : all) {
                int wait = u.equals(w) ? 0 : waiting.getOrDefault(u, 0) + 1;
                waiting.put(u, wait);
                longestWait = Math.max(longestWait, wait);
            }
            lots = Roles.update(lots, roles);
        }
        double share = 1.0 / all.size();
        for (UUID u : all) {
            double got = wilderer.getOrDefault(u, 0) / (double) rounds;
            assertTrue(Math.abs(got - share) < 0.02, "Wilderer-Anteil " + got + " statt " + share);
            double g = gendarm.getOrDefault(u, 0) / (double) rounds;
            assertTrue(Math.abs(g - share) < 0.02, "Gendarm-Anteil " + g + " statt " + share);
        }
        // reiner Zufall wäre 1/8 = 12,5 % zweimal hintereinander
        assertTrue(twice / (double) rounds < 0.02, "zweimal hintereinander Wilderer: " + twice);
        assertTrue(longestWait <= 40, "längstes Warten aufs Wildern: " + longestWait);
    }

    @Test
    void lotsCountUpAndReset() {
        Roles.Lots l = new Roles.Lots(3, 5);
        assertEquals(new Roles.Lots(0, 6), l.after(Role.WILDERER));
        assertEquals(new Roles.Lots(4, 0), l.after(Role.GENDARM));
        assertEquals(new Roles.Lots(4, 6), l.after(Role.DORF));
        assertEquals(Roles.CAP, new Roles.Lots(Roles.CAP, 0).after(Role.DORF).sinceWilderer(), "gedeckelt");
        assertTrue(Roles.weight(5) > Roles.weight(0));
    }
}
