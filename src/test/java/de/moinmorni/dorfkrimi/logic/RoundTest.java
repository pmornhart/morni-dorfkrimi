package de.moinmorni.dorfkrimi.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Die Regeln einer Runde: betäuben, festnehmen, Mütze, Versammlung, Sieg. */
class RoundTest {

    private static final UUID W = new UUID(1, 1);
    private static final UUID G = new UUID(1, 2);
    private static final UUID A = new UUID(1, 3);
    private static final UUID B = new UUID(1, 4);
    private static final UUID C = new UUID(1, 5);

    private static Round round() {
        Map<UUID, Role> r = new LinkedHashMap<>();
        r.put(W, Role.WILDERER);
        r.put(G, Role.GENDARM);
        r.put(A, Role.DORF);
        r.put(B, Role.DORF);
        r.put(C, Role.DORF);
        return new Round(r);
    }

    @Test
    void onlyTheWildererStunsAndNotTwice() {
        Round r = round();
        assertEquals(Round.Why.NOT_ALLOWED, r.stun(A, B).why());
        assertEquals(Round.Why.SELF, r.stun(W, W).why());
        assertTrue(r.stun(W, A).ok());
        assertEquals(Round.Status.STUNNED, r.status(A));
        assertEquals(Round.Why.NOT_AWAKE, r.stun(W, A).why(), "schon betäubt");
        assertEquals(Score.STUN, r.points(W));
        assertEquals(1, r.stuns(W));
        assertNull(r.check(false));
    }

    @Test
    void wildererPartnersCannotStunEachOther() {
        Map<UUID, Role> m = new LinkedHashMap<>();
        m.put(W, Role.WILDERER);
        m.put(A, Role.WILDERER);
        m.put(B, Role.DORF);
        Round r = new Round(m);
        assertEquals(Round.Why.PARTNER, r.stun(W, A).why());
    }

    @Test
    void stunningTheGendarmDropsTheHat() {
        Round r = round();
        Round.Result res = r.stun(W, G);
        assertTrue(res.ok() && res.right(), "Gendarm betäubt");
        assertNotNull(res.droppedHat());
        assertTrue(r.hats().get(0).dropped());
        assertEquals(Score.STUN + Score.STUN_GENDARM, r.points(W));
        assertFalse(r.mayPickUp(W), "Wilderer hebt keine Mütze auf");
        assertFalse(r.pickUp(W, 0));
        assertTrue(r.pickUp(A, 0), "Dorfbewohner hebt sie auf");
        assertTrue(r.gendarm(A));
        assertTrue(r.wasGendarm(A));
        assertFalse(r.pickUp(B, 0), "nur einer kann sie haben");
    }

    @Test
    void rightArrestCatchesAndPausesWrongArrestBlamesAndDropsTheHat() {
        Round r = round();
        assertEquals(Round.Why.NOT_ALLOWED, r.arrest(A, W).why(), "ohne Mütze kein Festnehmen");
        Round.Result wrong = r.arrest(G, A);
        assertTrue(wrong.ok());
        assertFalse(wrong.right());
        assertEquals(Round.Status.BLAMED, r.status(G), "Gendarm blamiert");
        assertTrue(r.awake(A), "der Unschuldige bleibt im Spiel");
        assertEquals(0, wrong.droppedHat());
        assertEquals(Score.ARREST_WRONG, r.points(G));
        // B hebt die Mütze auf und fasst den Wilderer
        assertTrue(r.pickUp(B, 0));
        Round.Result right = r.arrest(B, W);
        assertTrue(right.ok() && right.right());
        assertEquals(Round.Status.ARRESTED, r.status(W));
        assertEquals(1, r.arrests(B));
        assertFalse(r.hatOf(B).ready(), "nach einer Festnahme erst eine Pause");
        r.rearm(0);
        assertTrue(r.hatOf(B).ready());
        Outcome o = r.check(false);
        assertNotNull(o);
        assertTrue(o.villageWins());
        assertEquals(Outcome.How.CAUGHT, o.how());
    }

    @Test
    void wildererWinsWhenEveryoneSleepsOrTimeIsUp() {
        Round r = round();
        for (UUID u : List.of(G, A, B)) {
            assertTrue(r.stun(W, u).ok());
            assertNull(r.check(false));
        }
        assertTrue(r.stun(W, C).ok());
        Outcome o = r.check(false);
        assertFalse(o.villageWins());
        assertEquals(Outcome.How.STUNNED, o.how());
        Outcome time = round().check(true);
        assertFalse(time.villageWins());
        assertEquals(Outcome.How.TIME, time.how());
    }

    @Test
    void meetingVotesCatchOrJailWithBadLuckPoints() {
        Round r = round();
        Round.Result right = r.vote(W, List.of(A, B), 3);
        assertTrue(right.right());
        assertEquals(Score.VOTE_RIGHT, r.points(A));
        assertEquals(Round.Status.ARRESTED, r.status(W));
        Round r2 = round();
        Round.Result wrong = r2.vote(G, List.of(A, B, W), 3);
        assertFalse(wrong.right());
        assertEquals(Round.Status.JAILED, r2.status(G));
        assertEquals(-3, r2.points(A), "Pech-Punkte");
        assertEquals(-3, r2.points(W), "auch der Wilderer, wenn er mitgestimmt hat");
        assertNotNull(wrong.droppedHat(), "Mütze bleibt liegen");
    }

    @Test
    void leavingWildererMeansTheVillageWinsAndPointsAreGiven() {
        Round r = round();
        r.leave(W);
        Outcome o = r.check(false);
        assertTrue(o.villageWins());
        assertEquals(Outcome.How.FLED, o.how());
        r.finish(o);
        assertEquals(Score.WIN_VILLAGE + Score.AWAKE, r.points(A));
        assertEquals(0, r.points(W));
        assertTrue(o.winner(Role.GENDARM));
        assertFalse(o.winner(Role.WILDERER));
    }

    @Test
    void endPoints() {
        assertEquals(Score.WIN_WILDERER, Score.end(Role.WILDERER, true, true));
        assertEquals(Score.WIN_VILLAGE, Score.end(Role.DORF, true, false));
        assertEquals(0, Score.end(Role.DORF, false, true));
    }
}
