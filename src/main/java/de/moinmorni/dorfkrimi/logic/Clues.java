package de.moinmorni.dorfkrimi.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Welchen Hinweis bekommt jemand, der eine Spur findet? Immer etwas Wahres über einen Wilderer, das die Person noch nicht
 * weiß, und das – aus ihrer Sicht – einige Verdächtige ausschließt, aber nicht gleich alle: Ein Hinweis allein soll
 * den Wilderer nur selten verraten; erst mehrere zusammen (und Reden im Dorf) führen ans Ziel.
 */
public final class Clues {

    private Clues() {
    }

    /**
     * Verdächtige, die zu allen bekannten Hinweisen über diesen Wilderer passen.
     *
     * @param suspects wen die Person noch verdächtigt (alle Wachen außer ihr selbst)
     */
    public static List<UUID> remaining(Map<UUID, Outfit> outfits, Collection<UUID> suspects, List<Clue> known, int wilderer) {
        List<UUID> out = new ArrayList<>();
        for (UUID u : suspects) {
            Outfit o = outfits.get(u);
            if (o != null && known.stream().filter(c -> c.wilderer() == wilderer).allMatch(c -> c.matches(o))) {
                out.add(u);
            }
        }
        return out;
    }

    /** Wie viele Hinweise passen zu dieser Tracht? (Notizbuch: ✔ je passendem Hinweis) */
    public static int fits(Outfit o, List<Clue> known) {
        return (int) known.stream().filter(c -> c.matches(o)).count();
    }

    /**
     * Ein neuer Hinweis über den Wilderer {@code target} (Nummer {@code number}), oder null, wenn es nichts Neues mehr
     * gibt (die Person weiß schon alles über seine Tracht).
     */
    public static Clue next(Map<UUID, Outfit> outfits, UUID target, int number, Collection<UUID> suspects, List<Clue> known,
            Random random) {
        Outfit mine = outfits.get(target);
        if (mine == null) {
            return null;
        }
        List<UUID> left = remaining(outfits, suspects, known, number);
        List<Clue> options = new ArrayList<>();
        for (Part part : Part.values()) {
            options.add(new Clue(part, mine.get(part), true, number));
            for (Colour c : part.colours()) {
                if (c != mine.get(part)) {
                    options.add(new Clue(part, c, false, number));
                }
            }
        }
        options.removeIf(c -> known.stream().anyMatch(k -> k.wilderer() == number && k.part() == c.part()
                && (k.positive() || k.equals(c))));
        if (options.isEmpty()) {
            return null;
        }
        int others = (int) left.stream().filter(u -> !u.equals(target)).count();
        // Gut: schließt mindestens einen aus und lässt (wenn es geht) mindestens einen weiteren Verdächtigen übrig.
        List<Clue> good = new ArrayList<>();
        List<Clue> useful = new ArrayList<>();
        for (Clue c : options) {
            int out = (int) left.stream().filter(u -> !u.equals(target) && !c.matches(outfits.get(u))).count();
            if (out > 0) {
                useful.add(c);
                if (out < others && out <= Math.max(1, (others + 1) / 2)) {
                    good.add(c);
                }
            }
        }
        List<Clue> from = !good.isEmpty() ? good : !useful.isEmpty() ? useful : options;
        return from.get(random.nextInt(from.size()));
    }
}
