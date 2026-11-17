package de.moinmorni.dorfkrimi.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Trachten und Hinweise: immer wahr, nie doppelt, grenzen ein – verraten den Wilderer aber nicht mit einem Schlag. */
class CluesTest {

    private static List<UUID> people(int n) {
        List<UUID> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add(new UUID(9, i));
        }
        return out;
    }

    @Test
    void outfitsAreDistinctAndShared() {
        Random r = new Random(1);
        for (int n = 3; n <= 16; n++) {
            for (int t = 0; t < 50; t++) {
                List<UUID> all = people(n);
                List<UUID> w = n >= 10 ? all.subList(0, 2) : all.subList(0, 1);
                Map<UUID, Outfit> o = Outfits.assign(all, w, r);
                assertEquals(n, new HashSet<>(o.values()).size(), "keine zwei gleichen Trachten");
                assertTrue(Outfits.shared(o, w), "jede Farbe des Wilderers trägt noch jemand (n = " + n + ")");
            }
        }
        assertEquals(48, Outfit.all().size());
    }

    @Test
    void cluesAreTrueNewAndNarrowDown() {
        Random r = new Random(7);
        for (int t = 0; t < 500; t++) {
            int n = 4 + r.nextInt(13);
            List<UUID> all = people(n);
            UUID wilderer = all.get(r.nextInt(n));
            UUID finder = all.stream().filter(u -> !u.equals(wilderer)).findFirst().orElseThrow();
            Map<UUID, Outfit> o = Outfits.assign(all, List.of(wilderer), r);
            List<UUID> suspects = all.stream().filter(u -> !u.equals(finder)).toList();
            List<Clue> known = new ArrayList<>();
            int before = suspects.size();
            for (int k = 0; k < 20; k++) {
                Clue c = Clues.next(o, wilderer, 0, suspects, known, r);
                if (c == null) {
                    break;
                }
                assertTrue(c.matches(o.get(wilderer)), "Hinweis stimmt nicht: " + c);
                assertFalse(known.contains(c), "Hinweis doppelt");
                int left = Clues.remaining(o, suspects, List.of(c), 0).size();
                if (k == 0 && before >= 3) {
                    assertTrue(left >= 2, "Der erste Hinweis verrät den Wilderer schon (" + left + " von " + before + ")");
                    assertTrue(left < before, "Der erste Hinweis schließt niemanden aus");
                }
                known.add(c);
            }
            assertTrue(Clues.remaining(o, suspects, known, 0).contains(wilderer), "Wilderer fällt nie heraus");
            assertEquals(1, Clues.remaining(o, suspects, known, 0).size(), "Am Ende bleibt nur der Wilderer übrig");
            assertNull(Clues.next(o, wilderer, 0, suspects, known, r), "Wenn alles bekannt ist, gibt es nichts Neues mehr");
        }
    }

    @Test
    void cluesMatchAndFit() {
        Outfit o = new Outfit(Colour.GRUEN, Colour.ROT, Colour.BRAUN);
        assertTrue(new Clue(Part.HUT, Colour.GRUEN, true, 0).matches(o));
        assertFalse(new Clue(Part.HUT, Colour.GRUEN, false, 0).matches(o));
        assertTrue(new Clue(Part.JOPPE, Colour.BLAU, false, 0).matches(o));
        assertEquals(2, Clues.fits(o, List.of(new Clue(Part.HUT, Colour.GRUEN, true, 0),
                new Clue(Part.HOSE, Colour.BRAUN, true, 0), new Clue(Part.JOPPE, Colour.ROT, false, 0))));
        assertEquals("joppe!=blau#1", new Clue(Part.JOPPE, Colour.BLAU, false, 1).code());
        assertNotNull(Clues.next(Map.of(new UUID(0, 1), o), new UUID(0, 1), 0, List.of(new UUID(0, 1)), List.of(),
                new Random(1)), "auch allein gibt es etwas Wahres");
    }
}
