package de.moinmorni.dorfkrimi.logic;

import java.util.List;

/** Teile der Tracht, an denen man jemanden erkennt – und welche Farben es dafür gibt. */
public enum Part {
    HUT("hut", List.of(Colour.GRUEN, Colour.BRAUN, Colour.GRAU, Colour.SCHWARZ)),
    JOPPE("joppe", List.of(Colour.ROT, Colour.BLAU, Colour.GRUEN, Colour.BRAUN)),
    HOSE("hose", List.of(Colour.BRAUN, Colour.GRAU, Colour.SCHWARZ));

    private final String id;
    private final List<Colour> colours;

    Part(String id, List<Colour> colours) {
        this.id = id;
        this.colours = colours;
    }

    public String id() {
        return id;
    }

    public List<Colour> colours() {
        return colours;
    }
}
