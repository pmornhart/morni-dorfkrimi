package de.moinmorni.dorfkrimi.logic;

import java.util.Locale;

/** Ausgang einer Runde: wer gewinnt (Dorf oder Wilderer) und wie. */
public record Outcome(boolean villageWins, How how) {

    public enum How {
        /** Alle Wilderer sind gefasst (Gendarm oder Dorfversammlung). */
        CAUGHT,
        /** Die Wilderer haben sich aus dem Staub gemacht (Verbindung weg, nicht zurück). */
        FLED,
        /** Alle anderen sind betäubt (oder raus). */
        STUNNED,
        /** Die Zeit ist um, ohne dass alle Wilderer gefasst wurden. */
        TIME,
        /** Abgebrochen (Verwaltung) – ohne Wertung. */
        ABORTED
    }

    public String key() {
        return how.name().toLowerCase(Locale.ROOT);
    }

    /** Wer hat gewonnen? Rolle zählt mit (Gendarm gehört zum Dorf). */
    public boolean winner(Role role) {
        return how != How.ABORTED && role.village() == villageWins;
    }

    /**
     * Ist die Runde vorbei?
     *
     * @param wilderersIn Wilderer, die noch frei herumlaufen (auch wenn sie gerade die Verbindung verloren haben)
     * @param villageIn   Dorfleute und Gendarmen, die noch wach sind
     * @param wilderersFled ist ein Wilderer verschwunden (statt gefasst)?
     * @param timeUp      ist die Zeit um?
     * @return null, solange weitergespielt wird
     */
    public static Outcome check(int wilderersIn, int villageIn, boolean wilderersFled, boolean timeUp) {
        if (wilderersIn <= 0) {
            return new Outcome(true, wilderersFled ? How.FLED : How.CAUGHT);
        }
        if (villageIn <= 0) {
            return new Outcome(false, How.STUNNED);
        }
        if (timeUp) {
            return new Outcome(false, How.TIME);
        }
        return null;
    }
}
