package de.moinmorni.dorfkrimi.game;

import de.moinmorni.dorfkrimi.DorfkrimiPlugin;
import de.moinmorni.dorfkrimi.Items;
import de.moinmorni.dorfkrimi.Texts;
import de.moinmorni.dorfkrimi.logic.Clue;
import de.moinmorni.dorfkrimi.logic.Clues;
import de.moinmorni.dorfkrimi.logic.Meeting;
import de.moinmorni.dorfkrimi.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Truhen-Menüs in der Runde (auch für Bedrock): Notizbuch (alle Leute mit Tracht, Zustand und wie gut sie zu den eigenen
 * Hinweisen passen) und Stimmzettel der Dorfversammlung.
 */
public final class Menus {

    /** Bis zu 16 Leute: zwei Reihen à acht, darunter die Hinweise bzw. „abwarten“. */
    public static final int CLUES_SLOT = 22;
    public static final int SKIP_SLOT = 22;

    private Menus() {
    }

    private static int slot(int i) {
        return i < 8 ? i : 9 + (i - 8);
    }

    private static ItemStack head(Player owner, UUID uuid, Component name, List<Component> lore) {
        ItemStack head = Items.named(Material.PLAYER_HEAD, 1, name, lore);
        head.editMeta(m -> {
            if (m instanceof SkullMeta s) {
                s.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            }
        });
        return head;
    }

    /** Notizbuch: wer trägt was, wer ist noch wach, wer passt zu meinen Hinweisen. */
    public static void notes(DorfkrimiPlugin plugin, Game g, Player p) {
        String lang = plugin.lang(p);
        Suspect me = g.suspect(p.getUniqueId());
        List<Clue> mine = me == null ? List.of() : me.clues();
        Menu menu = new Menu(3, Texts.text(lang, "menu.notes.title"));
        int i = 0;
        for (Suspect s : g.people().values()) {
            if (i >= 16) {
                break;
            }
            List<Component> lore = new ArrayList<>();
            lore.add(Texts.text(lang, "menu.notes.outfit", Placeholder.unparsed("outfit", Game.outfitText(lang, s.outfit()))));
            String status = s.gone() ? "gone" : g.round().status(s.uuid()).name().toLowerCase(Locale.ROOT);
            lore.add(Texts.text(lang, "menu.notes.status." + status));
            if (me != null && !s.uuid().equals(me.uuid()) && g.round().awake(s.uuid()) && !mine.isEmpty()) {
                lore.add(Texts.text(lang, "menu.notes.fits", Placeholder.unparsed("n",
                        String.valueOf(Clues.fits(s.outfit(), mine))), Placeholder.unparsed("of", String.valueOf(mine.size()))));
            }
            if (me != null && s.uuid().equals(me.uuid())) {
                lore.add(Texts.text(lang, "menu.notes.you"));
            }
            menu.set(slot(i++), head(p, s.uuid(), Component.text(s.name(), TextColor.color(g.round().awake(s.uuid())
                    ? 0xFFFFFF : 0x888888)), lore), null);
        }
        List<Component> clues = new ArrayList<>();
        if (mine.isEmpty()) {
            clues.addAll(Texts.lines(lang, "menu.notes.none"));
        }
        for (Clue c : mine) {
            clues.add(Component.text("• " + g.clueText(lang, c), TextColor.color(Texts.GOLD)));
        }
        menu.set(CLUES_SLOT, Items.named(Material.WRITABLE_BOOK, 1, Texts.text(lang, "menu.notes.clues",
                Placeholder.unparsed("n", String.valueOf(mine.size()))), clues), null);
        menu.set(26, Items.named(Material.BARRIER, 1, Texts.text(lang, "menu.close"), null), Player::closeInventory);
        menu.open(p);
    }

    /** Stimmzettel: alle Wachen (außer man selbst), dazu „abwarten“. */
    static void vote(DorfkrimiPlugin plugin, Game g, Player p) {
        String lang = plugin.lang(p);
        Meeting m = g.meeting();
        UUID mineVote = m == null ? null : m.voteOf(p.getUniqueId());
        Menu menu = new Menu(3, Texts.text(lang, "menu.vote.title"));
        int i = 0;
        for (Suspect s : g.people().values()) {
            if (s.uuid().equals(p.getUniqueId()) || !g.awake(s.uuid()) || i >= 16) {
                continue;
            }
            List<Component> lore = new ArrayList<>();
            lore.add(Texts.text(lang, "menu.notes.outfit", Placeholder.unparsed("outfit", Game.outfitText(lang, s.outfit()))));
            Suspect me = g.suspect(p.getUniqueId());
            if (me != null && !me.clues().isEmpty()) {
                lore.add(Texts.text(lang, "menu.notes.fits", Placeholder.unparsed("n",
                        String.valueOf(Clues.fits(s.outfit(), me.clues()))),
                        Placeholder.unparsed("of", String.valueOf(me.clues().size()))));
            }
            boolean chosen = s.uuid().equals(mineVote);
            lore.add(Texts.text(lang, chosen ? "menu.vote.yours" : "menu.vote.click"));
            UUID target = s.uuid();
            menu.set(slot(i++), Items.glow(head(p, s.uuid(), Component.text(s.name(), TextColor.color(0xFFFFFF)), lore),
                    chosen), q -> {
                        g.vote(q, target);
                        q.closeInventory();
                    });
        }
        boolean skip = Meeting.SKIP.equals(mineVote);
        menu.set(SKIP_SLOT, Items.glow(Items.named(Material.CLOCK, 1, Texts.text(lang, "menu.vote.skip"),
                Texts.lines(lang, skip ? "menu.vote.yours" : "menu.vote.skip.lore")), skip), q -> {
                    g.vote(q, Meeting.SKIP);
                    q.closeInventory();
                });
        menu.set(26, Items.named(Material.BARRIER, 1, Texts.text(lang, "menu.close"), null), Player::closeInventory);
        menu.open(p);
    }
}
