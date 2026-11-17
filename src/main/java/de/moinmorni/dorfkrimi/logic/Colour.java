package de.moinmorni.dorfkrimi.logic;

/** Farben der Tracht (Hut, Joppe, Hose). Lederfarbe als RGB. */
public enum Colour {
    GRUEN("gruen", 0x3E7B3A),
    BRAUN("braun", 0x6B4423),
    GRAU("grau", 0x8C8C8C),
    SCHWARZ("schwarz", 0x26262B),
    ROT("rot", 0xB0302A),
    BLAU("blau", 0x2F5FA8);

    private final String id;
    private final int rgb;

    Colour(String id, int rgb) {
        this.id = id;
        this.rgb = rgb;
    }

    public String id() {
        return id;
    }

    public int rgb() {
        return rgb;
    }
}
