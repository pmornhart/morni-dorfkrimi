package de.moinmorni.dorfkrimi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.moinmorni.dorfkrimi.logic.Role;
import de.moinmorni.dorfkrimi.logic.Roles;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Spielerdaten und Bestenliste: zählen, speichern, wieder lesen, Reihenfolge. */
class DataTest {

    private static final UUID ANNA = new UUID(0, 1);
    private static final UUID BERT = new UUID(0, 2);
    private static final UUID CARL = new UUID(0, 3);

    @Test
    void roundsAddUpAndSurviveSaving() {
        PlayerData d = new PlayerData(ANNA, "Anna");
        assertTrue(d.add(new PlayerData.Round(Role.WILDERER, true, false, 0, 3, 0, 35)), "erste Runde ist Bestleistung");
        assertFalse(d.add(new PlayerData.Round(Role.DORF, true, true, 1, 0, 2, 31)));
        assertFalse(d.add(new PlayerData.Round(Role.DORF, false, false, 0, 0, 1, -4)), "Pech-Punkte zählen als 0");
        d.lots(new Roles.Lots(0, 4));
        PlayerData back = PlayerData.fromYaml(ANNA, d.toYaml());
        assertEquals(3, back.games());
        assertEquals(2, back.wins());
        assertEquals(1, back.winsWilderer());
        assertEquals(1, back.winsGendarm());
        assertEquals(1, back.timesWilderer());
        assertEquals(0, back.timesGendarm(), "Gendarm durch aufgehobene Mütze zählt nicht als Gendarm-Rolle");
        assertEquals(1, back.arrests());
        assertEquals(3, back.stuns());
        assertEquals(3, back.clues());
        assertEquals(66, back.points());
        assertEquals(35, back.bestPoints());
        assertEquals(new Roles.Lots(0, 4), back.lots());
        assertEquals("Anna", back.name());
    }

    @Test
    void newPlayersGetMiddleLots() {
        PlayerData fresh = PlayerData.fromYaml(BERT, "name: Bert\n");
        assertEquals(Roles.Lots.NEW, fresh.lots());
    }

    @Test
    void leaderboardOrdersByWinsThenArrestsThenPoints() {
        Leaderboard b = new Leaderboard();
        b.update(ANNA, "Anna", 2, 1, 50, 4);
        b.update(BERT, "Bert", 2, 2, 10, 5);
        b.update(CARL, "Carl", 3, 0, 5, 9);
        assertEquals("Carl", b.top(3).get(0).name());
        assertEquals("Bert", b.top(3).get(1).name(), "gleiche Siege: mehr Festnahmen vorn");
        assertEquals(3, b.rank(ANNA));
        Leaderboard back = Leaderboard.fromYaml(b.toYaml());
        assertEquals(b.top(3), back.top(3));
    }

    @Test
    void leaderboardKeepsOnlyTheBest() {
        Leaderboard b = new Leaderboard();
        for (int i = 0; i < Leaderboard.KEEP + 5; i++) {
            b.update(new UUID(1, i), "P" + i, i, 0, 0, 1);
        }
        assertEquals(Leaderboard.KEEP, b.top(1000).size());
        assertEquals(0, b.rank(new UUID(1, 0)), "der Schwächste ist raus");
    }
}
