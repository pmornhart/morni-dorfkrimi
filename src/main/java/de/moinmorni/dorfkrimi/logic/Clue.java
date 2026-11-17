package de.moinmorni.dorfkrimi.logic;

/**
 * Ein Hinweis auf einen Wilderer: „trägt einen grünen Hut“ ({@code positive}) oder „trägt keine rote Joppe“.
 * {@code wilderer} ist die Nummer des Wilderers, um den es geht (0 oder 1 – bei zweien weiß man, dass es um einen von
 * ihnen geht; zwei Hinweise mit gleicher Nummer betreffen denselben).
 */
public record Clue(Part part, Colour colour, boolean positive, int wilderer) {

    /** Passt die Tracht zu diesem Hinweis? */
    public boolean matches(Outfit o) {
        return (o.get(part) == colour) == positive;
    }

    /** Für die Speicherung und Tests: „hut=gruen“ bzw. „joppe!=rot“, mit Nummer „#1“. */
    public String code() {
        return part.id() + (positive ? "=" : "!=") + colour.id() + "#" + wilderer;
    }
}
