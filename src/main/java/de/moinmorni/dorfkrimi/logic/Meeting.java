package de.moinmorni.dorfkrimi.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Dorfversammlung: Wer wach ist, stimmt ab, wer festgenommen wird – oder „abwarten“. Festgenommen wird nur, wer mehr
 * Stimmen hat als jede andere Wahl (auch als „abwarten“) und mindestens {@link #MIN_VOTES} Stimmen bekommt. Wer nicht
 * abstimmt, wartet ab.
 */
public final class Meeting {

    /** Stimme für „abwarten“. */
    public static final UUID SKIP = new UUID(0, 0);
    public static final int MIN_VOTES = 2;

    private final Map<UUID, UUID> votes = new LinkedHashMap<>();

    /** Stimme abgeben (oder ändern). Man darf auch für sich selbst stimmen – sinnvoll ist es selten. */
    public void vote(UUID voter, UUID target) {
        votes.put(voter, target);
    }

    public UUID voteOf(UUID voter) {
        return votes.get(voter);
    }

    public void forget(UUID voter) {
        votes.remove(voter);
    }

    /** Stimmen für jemanden. */
    public int count(UUID target) {
        return (int) votes.values().stream().filter(target::equals).count();
    }

    public int voted() {
        return votes.size();
    }

    /** Stimmen zählen nur von Leuten, die noch wach sind. */
    public Map<UUID, Integer> tally(Collection<UUID> voters) {
        Map<UUID, Integer> out = new HashMap<>();
        for (UUID v : voters) {
            UUID t = votes.get(v);
            out.merge(t == null ? SKIP : t, 1, Integer::sum);
        }
        return out;
    }

    /** Ergebnis: wer festgenommen wird (leer: niemand – Gleichstand, zu wenig Stimmen oder „abwarten“ vorn). */
    public Optional<UUID> result(Collection<UUID> voters) {
        Map<UUID, Integer> t = tally(voters);
        UUID best = null;
        int top = -1;
        int second = -1;
        for (var e : t.entrySet()) {
            int n = e.getValue();
            if (n > top) {
                second = top;
                top = n;
                best = e.getKey();
            } else if (n > second) {
                second = n;
            }
        }
        if (best == null || SKIP.equals(best) || top < MIN_VOTES || top == second) {
            return Optional.empty();
        }
        return Optional.of(best);
    }

    /** Wer für diese Person gestimmt hat. */
    public List<UUID> votersFor(UUID target, Collection<UUID> voters) {
        List<UUID> out = new ArrayList<>();
        for (UUID v : voters) {
            if (target.equals(votes.get(v))) {
                out.add(v);
            }
        }
        return out;
    }
}
