package de.moinmorni.dorfkrimi.game;

import de.moinmorni.dorfkrimi.logic.Clue;
import de.moinmorni.dorfkrimi.logic.Outfit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

/**
 * Eine Person in einer Runde: Tracht, gefundene Hinweise, ob sie gerade verbunden ist, ob sie nicht mehr dabei ist –
 * die Rolle und ob sie wach ist, weiß die {@code logic.Round}.
 */
public final class Suspect {

    private final UUID uuid;
    private final String name;
    private final int index;
    private final Outfit outfit;
    private final List<Clue> clues = new ArrayList<>();
    private boolean online = true;
    private boolean gone;
    private boolean late;
    /** Wo sie zuletzt stand (für die Mütze, falls sie nicht wiederkommt). */
    private Location last;

    Suspect(UUID uuid, String name, int index, Outfit outfit) {
        this.uuid = uuid;
        this.name = name;
        this.index = index;
        this.outfit = outfit;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    /** Nummer (Startplatz). */
    int index() {
        return index;
    }

    public Outfit outfit() {
        return outfit;
    }

    /** Hinweise, die diese Person gefunden hat (nur sie kennt sie). */
    public List<Clue> clues() {
        return clues;
    }

    public boolean online() {
        return online;
    }

    void online(boolean o) {
        online = o;
    }

    /** Nicht mehr dabei (selbst gegangen oder nicht rechtzeitig zurückgekommen). */
    public boolean gone() {
        return gone;
    }

    void gone(boolean g) {
        gone = g;
    }

    public boolean late() {
        return late;
    }

    void late(boolean l) {
        late = l;
    }

    /** Verbunden und nicht gegangen. */
    public boolean active() {
        return online && !gone;
    }

    Location last() {
        return last;
    }

    void last(Location l) {
        last = l;
    }
}
