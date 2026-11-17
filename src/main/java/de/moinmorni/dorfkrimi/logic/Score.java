package de.moinmorni.dorfkrimi.logic;

/**
 * Punkte einer Runde. Gute Detektivarbeit zählt (Hinweise, richtige Festnahmen, richtig abstimmen); wer bei der
 * Dorfversammlung einen Unschuldigen wählt, bekommt Pech-Punkte (Abzug, einstellbar).
 */
public final class Score {

    /** Eine Spur gefunden. */
    public static final int CLUE = 2;
    /** Die liegengebliebene Dienstmütze aufgehoben (man wird Gendarm). */
    public static final int HAT = 2;
    /** Der Gendarm nimmt einen Wilderer fest. */
    public static final int ARREST = 15;
    /** Der Gendarm nimmt einen Unschuldigen fest (Abzug). */
    public static final int ARREST_WRONG = -5;
    /** Bei der Dorfversammlung für einen Wilderer gestimmt. */
    public static final int VOTE_RIGHT = 4;
    /** Der Wilderer betäubt jemanden … */
    public static final int STUN = 5;
    /** … den Gendarm sogar (zusätzlich). */
    public static final int STUN_GENDARM = 3;
    /** Sieg fürs Dorf: alle Guten. */
    public static final int WIN_VILLAGE = 10;
    /** Sieg fürs Dorf: wer am Ende noch wach ist, zusätzlich. */
    public static final int AWAKE = 5;
    /** Sieg für die Wilderer. */
    public static final int WIN_WILDERER = 20;

    private Score() {
    }

    /** Punkte am Ende der Runde für eine Person. */
    public static int end(Role role, boolean won, boolean awake) {
        if (!won) {
            return 0;
        }
        if (role == Role.WILDERER) {
            return WIN_WILDERER;
        }
        return WIN_VILLAGE + (awake ? AWAKE : 0);
    }
}
