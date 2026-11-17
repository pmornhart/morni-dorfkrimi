package de.moinmorni.dorfkrimi.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Dorfversammlung: nur eine klare Mehrheit nimmt fest. */
class MeetingTest {

    private static final UUID A = new UUID(2, 1);
    private static final UUID B = new UUID(2, 2);
    private static final UUID C = new UUID(2, 3);
    private static final UUID D = new UUID(2, 4);
    private static final List<UUID> ALL = List.of(A, B, C, D);

    @Test
    void clearMajorityWins() {
        Meeting m = new Meeting();
        m.vote(A, C);
        m.vote(B, C);
        m.vote(D, A);
        assertEquals(Optional.of(C), m.result(ALL));
        assertEquals(List.of(A, B), m.votersFor(C, ALL));
        assertEquals(1, m.tally(ALL).get(Meeting.SKIP), "wer nicht stimmt, wartet ab");
    }

    @Test
    void tieSkipOrTooFewMeansNobody() {
        Meeting tie = new Meeting();
        tie.vote(A, C);
        tie.vote(B, C);
        tie.vote(C, A);
        tie.vote(D, A);
        assertTrue(tie.result(ALL).isEmpty(), "Gleichstand");
        Meeting skip = new Meeting();
        skip.vote(A, C);
        skip.vote(B, C);
        skip.vote(C, Meeting.SKIP);
        skip.vote(D, Meeting.SKIP);
        assertTrue(skip.result(ALL).isEmpty(), "abwarten genauso stark");
        Meeting one = new Meeting();
        one.vote(A, C);
        assertTrue(one.result(List.of(A, C)).isEmpty(), "eine Stimme reicht nicht");
    }

    @Test
    void changingTheVoteAndLeavingVoters() {
        Meeting m = new Meeting();
        m.vote(A, C);
        m.vote(A, D);
        m.vote(B, D);
        m.vote(C, D);
        assertEquals(Optional.of(D), m.result(ALL), "A hat umgestimmt: 3 für D");
        assertTrue(m.result(List.of(C, D)).isEmpty(), "Stimmen von Ausgeschiedenen zählen nicht");
    }
}
