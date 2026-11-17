package de.moinmorni.dorfkrimi;

import de.moinmorni.dorfkrimi.data.Leaderboard;
import de.moinmorni.dorfkrimi.game.Game;
import de.moinmorni.dorfkrimi.game.Suspect;
import de.moinmorni.dorfkrimi.logic.Role;
import de.moinmorni.dorfkrimi.hall.HallMenus;
import de.moinmorni.dorfkrimi.world.HallPlan;
import de.moinmorni.dorfkrimi.world.MapType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** Leiste rechts (Runde und Dorflinde) und die schwebenden Tafeln an der Dorflinde. */
public final class Boards {

    private static final int TOP_ROWS = 8;

    private final DorfkrimiPlugin plugin;
    private final Sidebar sidebar = new Sidebar();
    private final List<Holo> hallHolos = new ArrayList<>();

    Boards(DorfkrimiPlugin plugin) {
        this.plugin = plugin;
    }

    public Sidebar sidebar() {
        return sidebar;
    }

    /** Leiste für alle, die eine Runde sehen. Die eigene Rolle sieht nur man selbst. */
    public void game(Game g) {
        for (Player p : g.audience()) {
            String lang = plugin.lang(p);
            List<Component> lines = new ArrayList<>();
            lines.add(Component.empty());
            lines.add(Texts.text(lang, "side.map", Placeholder.unparsed("map", Texts.plain(lang, "map." + g.map().id()))));
            lines.add(switch (g.phase()) {
                case START -> Texts.text(lang, "side.start");
                case MEETING -> Texts.text(lang, "side.meeting");
                default -> Texts.text(lang, "side.time", Placeholder.unparsed("time", Ui.clock(g.secondsLeft())));
            });
            lines.add(Texts.text(lang, "side.awake", Placeholder.unparsed("n", String.valueOf(g.round().villageAwake()))));
            lines.add(Component.text(" "));
            Suspect s = g.suspect(p.getUniqueId());
            if (s != null && g.phase() != Game.Phase.OVER) {
                Role r = g.round().role(s.uuid());
                boolean hat = g.round().hatOf(s.uuid()) != null;
                lines.add(Texts.text(lang, "side.role", Placeholder.component("role",
                        Game.roleName(lang, hat && r == Role.DORF ? Role.GENDARM : r))));
                if (g.ghost(s.uuid())) {
                    lines.add(Texts.text(lang, "side.bench"));
                }
                lines.add(Texts.text(lang, "side.clues", Placeholder.unparsed("n", String.valueOf(s.clues().size()))));
                lines.add(Texts.text(lang, "side.points", Placeholder.unparsed("n", String.valueOf(g.round().points(s.uuid())))));
            } else if (s != null) {
                lines.add(Texts.text(lang, "side.points", Placeholder.unparsed("n", String.valueOf(g.round().points(s.uuid())))));
            } else {
                lines.add(Texts.text(lang, "side.spectating"));
            }
            lines.add(Component.text("  "));
            lines.add(Texts.text(lang, "side.site"));
            sidebar.show(p, Texts.text(lang, "side.title"), lines);
        }
    }

    /** Leiste an der Dorflinde. */
    public void hall(Player p) {
        String lang = plugin.lang(p);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());
        lines.add(Texts.text(lang, "side.hall.players", Placeholder.unparsed("n", String.valueOf(plugin.hall().waiting().size()))));
        int cd = plugin.hall().countdown();
        lines.add(cd >= 0 ? Texts.text(lang, "side.hall.start", Placeholder.unparsed("secs", String.valueOf(cd)))
                : Texts.text(lang, "side.hall.wait"));
        lines.add(Component.text(" "));
        MapType next = plugin.hall().nextMap();
        lines.add(Texts.text(lang, "side.map", Placeholder.unparsed("map",
                next == null ? "–" : Texts.plain(lang, "map." + next.id()))));
        lines.add(Component.text("  "));
        var d = plugin.store().loaded(p.getUniqueId());
        lines.add(Texts.text(lang, "side.hall.wins", Placeholder.unparsed("n", String.valueOf(d.map(x -> x.wins()).orElse(0)))));
        lines.add(Texts.text(lang, "side.hall.arrests",
                Placeholder.unparsed("n", String.valueOf(d.map(x -> x.arrests()).orElse(0)))));
        lines.add(Component.text("   "));
        lines.add(Texts.text(lang, "side.site"));
        sidebar.show(p, Texts.text(lang, "side.title"), lines);
    }

    public void hallAll() {
        for (UUID u : plugin.hall().waiting()) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                hall(p);
            }
        }
    }

    // ---------- Tafeln an der Dorflinde ----------

    public void spawnHall() {
        removeHall();
        MapType[] maps = MapType.values();
        for (int i = 0; i < maps.length && i < HallPlan.TABLES.length; i++) {
            MapType m = maps[i];
            int[] t = HallPlan.TABLES[i];
            hallHolos.add(Holo.spawn(plugin, at(t[0] + 0.5, 3.7, t[1] + 0.5), lang -> table(lang, m), 1f));
        }
        hallHolos.add(Holo.spawn(plugin, at(HallPlan.BOARD[0] + 0.5, 2.9, HallPlan.BOARD[1] + 0.3), this::top, 0.9f));
        hallHolos.add(Holo.spawn(plugin, at(HallPlan.HOWTO[0] + 0.3, 2.4, HallPlan.HOWTO[1] + 0.5),
                lang -> Texts.text(lang, "board.howto"), 0.8f));
        hallHolos.add(Holo.spawn(plugin, at(0.5, 4.4, 8.5), lang -> Texts.text(lang, "board.welcome"), 1.1f));
    }

    private Location at(double x, double y, double z) {
        return new Location(plugin.world(), x, MapType.FLOOR_Y + y, z);
    }

    private Component table(String lang, MapType m) {
        Game g = plugin.games().on(m);
        String state = g != null ? Texts.raw(lang, "board.table.running")
                : Texts.raw(lang, "board.table.votes").replace("<n>", String.valueOf(plugin.hall().mapVotes().count(m)));
        return Texts.text(lang, "board.table", Placeholder.unparsed("map", Texts.plain(lang, "map." + m.id())),
                Placeholder.unparsed("desc", Texts.plain(lang, "map." + m.id() + ".short")),
                Placeholder.parsed("state", state));
    }

    private Component top(String lang) {
        Component c = Texts.text(lang, "board.title");
        List<Leaderboard.Entry> top = plugin.board().top(TOP_ROWS);
        if (top.isEmpty()) {
            c = c.append(Component.newline()).append(Texts.text(lang, "top.empty"));
        }
        for (int i = 0; i < top.size(); i++) {
            c = c.append(Component.newline()).append(HallMenus.row(lang, i + 1, top.get(i)));
        }
        return c;
    }

    public void refreshHall() {
        if (hallHolos.isEmpty()) {
            return;
        }
        if (!hallHolos.stream().allMatch(Holo::valid)) {
            spawnHall();
            return;
        }
        MapType[] maps = MapType.values();
        for (int i = 0; i < maps.length; i++) {
            MapType m = maps[i];
            hallHolos.get(i).update(lang -> table(lang, m));
        }
        hallHolos.get(maps.length).update(this::top);
    }

    public void removeHall() {
        hallHolos.forEach(Holo::remove);
        hallHolos.clear();
    }
}
