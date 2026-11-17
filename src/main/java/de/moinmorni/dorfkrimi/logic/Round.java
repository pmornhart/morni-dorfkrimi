package de.moinmorni.dorfkrimi.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Die Regeln einer Runde, ohne Minecraft: wer welche Rolle hat, wer noch wach ist, wer eine Dienstmütze trägt, wer wie
 * viele Punkte hat – und wann die Runde vorbei ist. Die Runde in Minecraft ({@code game.Game}) fragt hier nach und zeigt
 * nur an.
 *
 * <ul>
 * <li>Der <b>Wilderer</b> betäubt Dorfleute und Gendarmen (nie den anderen Wilderer). Trug der Betäubte die
 * Dienstmütze, fällt sie liegen.</li>
 * <li>Der <b>Gendarm</b> darf festnehmen. Ein Wilderer: gefasst, und nach einer Pause darf der Gendarm wieder. Ein
 * Unschuldiger: der Gendarm ist blamiert und raus, seine Mütze fällt liegen (der Unschuldige bleibt im Spiel).</li>
 * <li>Eine liegende <b>Mütze</b> hebt nur auf, wer zum Dorf gehört und noch wach ist – er wird Gendarm.</li>
 * <li><b>Dorfversammlung</b>: Wird ein Wilderer gewählt, ist er gefasst (Punkte für alle, die ihn gewählt haben). Ein
 * Unschuldiger muss in den Arrest (raus), wer ihn gewählt hat, bekommt Pech-Punkte.</li>
 * </ul>
 */
public final class Round {

    public enum Status {
        AWAKE, STUNNED, ARRESTED, BLAMED, JAILED, GONE
    }

    /** Eine Dienstmütze: wer sie trägt (null = liegt irgendwo) und ob er gerade festnehmen darf. */
    public static final class Hat {
        private final int id;
        private UUID holder;
        private boolean ready = true;

        Hat(int id, UUID holder) {
            this.id = id;
            this.holder = holder;
        }

        public int id() {
            return id;
        }

        public UUID holder() {
            return holder;
        }

        public boolean dropped() {
            return holder == null;
        }

        public boolean ready() {
            return ready;
        }
    }

    public enum Why {
        OK, NOT_ALLOWED, NOT_READY, NOT_AWAKE, PARTNER, SELF
    }

    /** Ergebnis von Betäuben, Festnehmen oder Abstimmen: geklappt? richtig? welche Mütze ist dabei liegengeblieben? */
    public record Result(Why why, boolean right, Integer droppedHat) {
        public boolean ok() {
            return why == Why.OK;
        }
    }

    private final Map<UUID, Role> roles;
    private final Map<UUID, Status> status = new LinkedHashMap<>();
    private final Map<UUID, Integer> points = new HashMap<>();
    private final Map<UUID, Integer> stuns = new HashMap<>();
    private final Map<UUID, Integer> arrests = new HashMap<>();
    private final Map<UUID, Integer> clues = new HashMap<>();
    private final List<Hat> hats = new ArrayList<>();
    private final Set<UUID> wasGendarm = new HashSet<>();
    private boolean fled;

    public Round(Map<UUID, Role> roles) {
        this.roles = new LinkedHashMap<>(roles);
        roles.keySet().forEach(u -> status.put(u, Status.AWAKE));
        for (var e : roles.entrySet()) {
            if (e.getValue() == Role.GENDARM) {
                hats.add(new Hat(hats.size(), e.getKey()));
                wasGendarm.add(e.getKey());
            }
        }
    }

    public Role role(UUID u) {
        return roles.get(u);
    }

    public Map<UUID, Role> roles() {
        return roles;
    }

    public Status status(UUID u) {
        return status.get(u);
    }

    public boolean awake(UUID u) {
        return status.get(u) == Status.AWAKE;
    }

    public int points(UUID u) {
        return points.getOrDefault(u, 0);
    }

    public void addPoints(UUID u, int n) {
        points.merge(u, n, Integer::sum);
    }

    public int stuns(UUID u) {
        return stuns.getOrDefault(u, 0);
    }

    public int arrests(UUID u) {
        return arrests.getOrDefault(u, 0);
    }

    public int clues(UUID u) {
        return clues.getOrDefault(u, 0);
    }

    /** Eine Spur gefunden (Punkte und Zähler). */
    public void clueFound(UUID u) {
        clues.merge(u, 1, Integer::sum);
        addPoints(u, Score.CLUE);
    }

    /** Hatte diese Person in dieser Runde eine Dienstmütze (von Anfang an oder aufgehoben)? */
    public boolean wasGendarm(UUID u) {
        return wasGendarm.contains(u);
    }

    public List<Hat> hats() {
        return hats;
    }

    /** Mütze, die diese Person trägt (null = keine). */
    public Hat hatOf(UUID u) {
        for (Hat h : hats) {
            if (u.equals(h.holder)) {
                return h;
            }
        }
        return null;
    }

    /** Wach und mit Mütze: darf festnehmen (wenn bereit). */
    public boolean gendarm(UUID u) {
        return awake(u) && hatOf(u) != null;
    }

    public List<UUID> awakeWith(Role role) {
        List<UUID> out = new ArrayList<>();
        status.forEach((u, s) -> {
            if (s == Status.AWAKE && roles.get(u) == role) {
                out.add(u);
            }
        });
        return out;
    }

    /** Alle Wachen. */
    public List<UUID> awake() {
        List<UUID> out = new ArrayList<>();
        status.forEach((u, s) -> {
            if (s == Status.AWAKE) {
                out.add(u);
            }
        });
        return out;
    }

    /** Wache vom Dorf (Dorfleute und Gendarmen). */
    public int villageAwake() {
        return (int) status.entrySet().stream().filter(e -> e.getValue() == Status.AWAKE && roles.get(e.getKey()).village())
                .count();
    }

    private Integer drop(UUID u) {
        Hat h = hatOf(u);
        if (h == null) {
            return null;
        }
        h.holder = null;
        h.ready = true;
        return h.id;
    }

    /** Der Wilderer {@code by} betäubt {@code victim}. */
    public Result stun(UUID by, UUID victim) {
        if (roles.get(by) != Role.WILDERER) {
            return new Result(Why.NOT_ALLOWED, false, null);
        }
        if (by.equals(victim)) {
            return new Result(Why.SELF, false, null);
        }
        if (!awake(by) || !awake(victim)) {
            return new Result(Why.NOT_AWAKE, false, null);
        }
        if (roles.get(victim) == Role.WILDERER) {
            return new Result(Why.PARTNER, false, null);
        }
        boolean gendarm = hatOf(victim) != null;
        status.put(victim, Status.STUNNED);
        stuns.merge(by, 1, Integer::sum);
        addPoints(by, Score.STUN + (gendarm ? Score.STUN_GENDARM : 0));
        return new Result(Why.OK, gendarm, drop(victim));
    }

    /** Der Gendarm {@code by} nimmt {@code target} fest. {@code right} = es war ein Wilderer. */
    public Result arrest(UUID by, UUID target) {
        Hat h = hatOf(by);
        if (h == null || !awake(by)) {
            return new Result(Why.NOT_ALLOWED, false, null);
        }
        if (by.equals(target)) {
            return new Result(Why.SELF, false, null);
        }
        if (!h.ready) {
            return new Result(Why.NOT_READY, false, null);
        }
        if (!awake(target)) {
            return new Result(Why.NOT_AWAKE, false, null);
        }
        if (roles.get(target) == Role.WILDERER) {
            status.put(target, Status.ARRESTED);
            arrests.merge(by, 1, Integer::sum);
            addPoints(by, Score.ARREST);
            h.ready = false;
            return new Result(Why.OK, true, drop(target));
        }
        status.put(by, Status.BLAMED);
        addPoints(by, Score.ARREST_WRONG);
        return new Result(Why.OK, false, drop(by));
    }

    /** Nach der Pause darf der Gendarm wieder festnehmen. */
    public void rearm(int hat) {
        hats.get(hat).ready = true;
    }

    /** Darf diese Person die liegende Mütze aufheben? Nur wer zum Dorf gehört, wach ist und noch keine trägt. */
    public boolean mayPickUp(UUID u) {
        return awake(u) && roles.get(u) == Role.DORF && hatOf(u) == null;
    }

    public boolean pickUp(UUID u, int hat) {
        Hat h = hats.get(hat);
        if (!h.dropped() || !mayPickUp(u)) {
            return false;
        }
        h.holder = u;
        h.ready = true;
        wasGendarm.add(u);
        addPoints(u, Score.HAT);
        return true;
    }

    /**
     * Ergebnis der Dorfversammlung anwenden.
     *
     * @param chosen gewählt (wach)
     * @param voters wer für ihn gestimmt hat
     * @param pech   Pech-Punkte je Stimme für einen Unschuldigen
     */
    public Result vote(UUID chosen, List<UUID> voters, int pech) {
        if (!awake(chosen)) {
            return new Result(Why.NOT_AWAKE, false, null);
        }
        boolean wilderer = roles.get(chosen) == Role.WILDERER;
        status.put(chosen, wilderer ? Status.ARRESTED : Status.JAILED);
        for (UUID v : voters) {
            if (!v.equals(chosen)) {
                addPoints(v, wilderer ? Score.VOTE_RIGHT : -Math.max(0, pech));
            }
        }
        return new Result(Why.OK, wilderer, drop(chosen));
    }

    /** Raus (nicht rechtzeitig zurück oder selbst gegangen). Liefert die liegengebliebene Mütze. */
    public Integer leave(UUID u) {
        Status s = status.get(u);
        if (s == null || s == Status.GONE) {
            return null;
        }
        if (s == Status.AWAKE && roles.get(u) == Role.WILDERER) {
            fled = true;
        }
        status.put(u, Status.GONE);
        return drop(u);
    }

    /** Vorbei? (null = weiter) */
    public Outcome check(boolean timeUp) {
        return Outcome.check(awakeWith(Role.WILDERER).size(), villageAwake(), fled, timeUp);
    }

    /** Endpunkte verteilen (einmal am Ende). */
    public void finish(Outcome o) {
        for (UUID u : roles.keySet()) {
            Role r = roles.get(u);
            addPoints(u, Score.end(r, o.winner(r), awake(u)));
        }
    }
}
