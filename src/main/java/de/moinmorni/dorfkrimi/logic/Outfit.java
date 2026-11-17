package de.moinmorni.dorfkrimi.logic;

/** Die Tracht einer Person in einer Runde: Hut, Joppe, Lederhose. Jeder sieht sie – daran erkennt man Verdächtige. */
public record Outfit(Colour hut, Colour joppe, Colour hose) {

    public Colour get(Part part) {
        return switch (part) {
            case HUT -> hut;
            case JOPPE -> joppe;
            case HOSE -> hose;
        };
    }

    /** Alle möglichen Trachten (4 × 4 × 3 = 48). */
    public static java.util.List<Outfit> all() {
        java.util.List<Outfit> out = new java.util.ArrayList<>();
        for (Colour h : Part.HUT.colours()) {
            for (Colour j : Part.JOPPE.colours()) {
                for (Colour o : Part.HOSE.colours()) {
                    out.add(new Outfit(h, j, o));
                }
            }
        }
        return out;
    }
}
