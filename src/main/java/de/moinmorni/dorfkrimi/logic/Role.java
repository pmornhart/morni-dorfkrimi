package de.moinmorni.dorfkrimi.logic;

import java.util.Locale;

/** Die geheimen Rollen einer Runde. */
public enum Role {
    /** Treibt sein Unwesen im Dorf und betäubt die Leute (eins, ab vielen Leuten zwei). */
    WILDERER("wilderer", 0x5E7D3A),
    /** Trägt die Dienstmütze und darf festnehmen – trifft er einen Unschuldigen, ist er selbst raus. */
    GENDARM("gendarm", 0x2F5FA8),
    /** Alle anderen: Hinweise suchen, Verdächtige eingrenzen, bei der Dorfversammlung abstimmen. */
    DORF("dorf", 0xD9A441);

    private final String id;
    private final int rgb;

    Role(String id, int rgb) {
        this.id = id;
        this.rgb = rgb;
    }

    public String id() {
        return id;
    }

    /** Farbe für Titel und Leiste. */
    public int rgb() {
        return rgb;
    }

    /** Gehört zu den Guten (Dorfleute und Gendarm)? */
    public boolean village() {
        return this != WILDERER;
    }

    public static Role byId(String s) {
        if (s == null) {
            return null;
        }
        String k = s.toLowerCase(Locale.ROOT);
        for (Role r : values()) {
            if (r.id.equals(k)) {
                return r;
            }
        }
        return switch (k) {
            case "poacher", "w" -> WILDERER;
            case "constable", "g", "polizist" -> GENDARM;
            case "villager", "dorfbewohner", "d" -> DORF;
            default -> null;
        };
    }
}
