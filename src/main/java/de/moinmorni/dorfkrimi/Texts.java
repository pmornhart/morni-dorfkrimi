package de.moinmorni.dorfkrimi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Alle Texte für Spieler, auf Deutsch und Englisch, als MiniMessage. Eigene Farben: {@code <sky>} Himmelblau,
 * {@code <au>} Gold, {@code <em>} Smaragd, {@code <bl>} Blau (Gendarm), {@code <rt>} Rot (Warnung). Namen von Spielern
 * kommen immer als {@code Placeholder.unparsed} hinein.
 */
public final class Texts {

    public static final int SKY = 0x7EC8F2;
    public static final int GOLD = 0xF2B632;
    public static final int EMERALD = 0x2BB673;
    public static final int BLUE = 0x3C8DE0;
    public static final int RED = 0xD8413A;

    public static final MiniMessage MM = MiniMessage.builder()
            .editTags(t -> t.resolvers(
                    TagResolver.resolver("sky", Tag.styling(TextColor.color(SKY))),
                    TagResolver.resolver("au", Tag.styling(TextColor.color(GOLD))),
                    TagResolver.resolver("em", Tag.styling(TextColor.color(EMERALD))),
                    TagResolver.resolver("bl", Tag.styling(TextColor.color(BLUE))),
                    TagResolver.resolver("rt", Tag.styling(TextColor.color(RED)))))
            .build();

    private static final Map<String, String[]> TEXTS = new LinkedHashMap<>();

    private Texts() {
    }

    private static void t(String key, String de, String en) {
        if (TEXTS.put(key, new String[] {de, en}) != null) {
            throw new IllegalStateException("Text doppelt: " + key);
        }
    }

    static {
        t("prefix", "<em>✦</em> <au>Dorfkrimi</au> <dark_gray>»</dark_gray> ",
                "<em>✦</em> <au>Village Mystery</au> <dark_gray>»</dark_gray> ");
        t("mode", "Dorfkrimi",
                "Village Mystery");

        // ---------- Allgemein ----------
        t("only-players", "Das geht nur im Spiel.",
                "Only possible in-game.");
        t("unknown-player", "<gray>Niemanden namens <white><name></white> gefunden.</gray>",
                "<gray>Nobody called <white><name></white> found.</gray>");
        t("lobby.kick", "<au>MoinMorni</au>\n<gray>Zurück in die Lobby: Bitte verbinde dich neu mit dem Netzwerk.</gray>",
                "<au>MoinMorni</au>\n<gray>Back to the lobby: please reconnect to the network.</gray>");
        t("lobby.going", "<gray>Ab in die Lobby – bis bald!</gray>",
                "<gray>Off to the lobby – see you soon!</gray>");

        // ---------- Karten ----------
        t("map.oberdorf", "Oberdorf bei Nacht",
                "Upper Village by Night");
        t("map.oberdorf.short", "Ein Bergdorf im Mondlicht – Kirchturm, Scheunen, Wald",
                "A mountain village by moonlight – church tower, barns, forest");
        t("map.oberdorf.desc", "<gray>Ein dicht bebautes Bergdorf bei Vollmond:</gray>\n<gray>Häuser zum Durchlaufen, Scheunen mit Heuboden,</gray>\n<gray>ein Kirchturm und ein dunkler Wald.</gray>",
                "<gray>A tightly packed mountain village at full moon:</gray>\n<gray>houses to run through, barns with hay lofts,</gray>\n<gray>a church tower and a dark forest.</gray>");
        t("map.almwirtschaft", "Almwirtschaft im Nebel",
                "Alpine Inn in the Fog");
        t("map.almwirtschaft.short", "Eine Hochalm im Morgengrauen – Nebel, Heustadel, Kapelle",
                "A high pasture at dawn – fog, hay huts, chapel");
        t("map.almwirtschaft.desc", "<gray>Eine Hochalm im Morgengrauen, Nebel zieht auf:</gray>\n<gray>Almwirtschaft, Sennerei, Kapelle, Kuhstall,</gray>\n<gray>Heustadel, Hütten und Bergwald.</gray>",
                "<gray>A high pasture at dawn, fog rolling in:</gray>\n<gray>alpine inn, dairy, chapel, cowshed,</gray>\n<gray>hay huts, cabins and mountain forest.</gray>");

        // ---------- Rollen ----------
        t("role.wilderer", "Wilderer",
                "Poacher");
        t("role.gendarm", "Gendarm",
                "Constable");
        t("role.dorf", "Dorfbewohner",
                "Villager");
        t("role.title.wilderer", "<#8FBF4A><b>Du bist der Wilderer!</b></#8FBF4A>",
                "<#8FBF4A><b>You are the poacher!</b></#8FBF4A>");
        t("role.sub.wilderer", "<gray>Betäube alle – aber lass dich nicht erwischen.</gray>",
                "<gray>Put everyone to sleep – but don't get caught.</gray>");
        t("role.chat.wilderer", "<#8FBF4A>Du bist der Wilderer.</#8FBF4A> <gray>Halte das <white>Schlafpulver</white> in der Hand und schlag (oder klick) jemanden an – er schläft ein. Danach musst du kurz warten. Wenn alle schlafen oder die Zeit um ist, gewinnst du. Spuren verwischst du, indem du darübergehst.</gray>",
                "<#8FBF4A>You are the poacher.</#8FBF4A> <gray>Hold the <white>sleeping powder</white> and hit (or click) someone – they fall asleep. Then you have to wait a little. If everyone is asleep or time runs out, you win. Walk over clues to wipe them.</gray>");
        t("role.title.gendarm", "<bl><b>Du bist der Gendarm!</b></bl>",
                "<bl><b>You are the constable!</b></bl>");
        t("role.sub.gendarm", "<gray>Finde den Wilderer und nimm ihn fest.</gray>",
                "<gray>Find the poacher and arrest him.</gray>");
        t("role.chat.gendarm", "<bl>Du bist der Gendarm.</bl> <gray>Halte die <white>Dienstmütze</white> in der Hand und schlag (oder klick) jemanden an, um ihn festzunehmen. Ist es der Wilderer: gut gemacht! Ist er unschuldig, bist du blamiert und raus – und deine Mütze bleibt liegen.</gray>",
                "<bl>You are the constable.</bl> <gray>Hold the <white>constable's cap</white> and hit (or click) someone to arrest them. If it's the poacher: well done! If they're innocent, you're disgraced and out – and your cap stays on the ground.</gray>");
        t("role.title.dorf", "<au><b>Du gehörst zum Dorf!</b></au>",
                "<au><b>You're a villager!</b></au>");
        t("role.sub.dorf", "<gray>Such Spuren und find heraus, wer der Wilderer ist.</gray>",
                "<gray>Look for clues and find out who the poacher is.</gray>");
        t("role.chat.dorf", "<au>Du gehörst zum Dorf.</au> <gray>Im Dorf tauchen leuchtende <white>Spuren</white> auf – geh hin, dann verraten sie dir etwas über die Tracht des Wilderers. Erzähl es den anderen, ruf mit der <white>Glocke</white> die Dorfversammlung – und heb die Dienstmütze auf, wenn sie irgendwo liegt.</gray>",
                "<au>You're a villager.</au> <gray>Glowing <white>clues</white> appear around the village – walk over them and they tell you something about the poacher's outfit. Tell the others, ring the <white>bell</white> to call a village meeting – and pick up the constable's cap if it's lying around.</gray>");
        t("role.outfit", "<gray>Deine Tracht heute: <white><outfit></white></gray>",
                "<gray>Your outfit today: <white><outfit></white></gray>");
        t("role.partner", "<#8FBF4A>Dein Jagdgeselle:</#8FBF4A> <white><name></white> <gray>(auch Wilderer – ihr könnt euch nicht betäuben)</gray>",
                "<#8FBF4A>Your fellow poacher:</#8FBF4A> <white><name></white> <gray>(also a poacher – you can't put each other to sleep)</gray>");

        // ---------- Tracht und Hinweise ----------
        t("outfit.hut.gruen", "grüner Hut",
                "green hat");
        t("clue.hut.ja.gruen", "trägt einen grünen Hut.",
                "wears a green hat.");
        t("clue.hut.nein.gruen", "trägt keinen grünen Hut.",
                "does not wear a green hat.");
        t("outfit.hut.braun", "brauner Hut",
                "brown hat");
        t("clue.hut.ja.braun", "trägt einen braunen Hut.",
                "wears a brown hat.");
        t("clue.hut.nein.braun", "trägt keinen braunen Hut.",
                "does not wear a brown hat.");
        t("outfit.hut.grau", "grauer Hut",
                "grey hat");
        t("clue.hut.ja.grau", "trägt einen grauen Hut.",
                "wears a grey hat.");
        t("clue.hut.nein.grau", "trägt keinen grauen Hut.",
                "does not wear a grey hat.");
        t("outfit.hut.schwarz", "schwarzer Hut",
                "black hat");
        t("clue.hut.ja.schwarz", "trägt einen schwarzen Hut.",
                "wears a black hat.");
        t("clue.hut.nein.schwarz", "trägt keinen schwarzen Hut.",
                "does not wear a black hat.");
        t("outfit.joppe.rot", "rote Joppe",
                "red jacket");
        t("clue.joppe.ja.rot", "trägt eine rote Joppe.",
                "wears a red jacket.");
        t("clue.joppe.nein.rot", "trägt keine rote Joppe.",
                "does not wear a red jacket.");
        t("outfit.joppe.blau", "blaue Joppe",
                "blue jacket");
        t("clue.joppe.ja.blau", "trägt eine blaue Joppe.",
                "wears a blue jacket.");
        t("clue.joppe.nein.blau", "trägt keine blaue Joppe.",
                "does not wear a blue jacket.");
        t("outfit.joppe.gruen", "grüne Joppe",
                "green jacket");
        t("clue.joppe.ja.gruen", "trägt eine grüne Joppe.",
                "wears a green jacket.");
        t("clue.joppe.nein.gruen", "trägt keine grüne Joppe.",
                "does not wear a green jacket.");
        t("outfit.joppe.braun", "braune Joppe",
                "brown jacket");
        t("clue.joppe.ja.braun", "trägt eine braune Joppe.",
                "wears a brown jacket.");
        t("clue.joppe.nein.braun", "trägt keine braune Joppe.",
                "does not wear a brown jacket.");
        t("outfit.hose.braun", "braune Lederhose",
                "brown lederhosen");
        t("clue.hose.ja.braun", "trägt eine braune Lederhose.",
                "wears brown lederhosen.");
        t("clue.hose.nein.braun", "trägt keine braune Lederhose.",
                "does not wear brown lederhosen.");
        t("outfit.hose.grau", "graue Lederhose",
                "grey lederhosen");
        t("clue.hose.ja.grau", "trägt eine graue Lederhose.",
                "wears grey lederhosen.");
        t("clue.hose.nein.grau", "trägt keine graue Lederhose.",
                "does not wear grey lederhosen.");
        t("outfit.hose.schwarz", "schwarze Lederhose",
                "black lederhosen");
        t("clue.hose.ja.schwarz", "trägt eine schwarze Lederhose.",
                "wears black lederhosen.");
        t("clue.hose.nein.schwarz", "trägt keine schwarze Lederhose.",
                "does not wear black lederhosen.");
        t("outfit.boots", "Haferlschuhe",
                "Buckled shoes");
        t("clue.who.the", "Der Wilderer",
                "The poacher");
        t("clue.who.one", "Einer der Wilderer",
                "One of the poachers");

        // ---------- Dorflinde ----------
        t("hall.welcome", "<gray>Servus an der <au>Dorflinde</au>! Im Dorf treibt ein Wilderer sein Unwesen – findet ihr heraus, wer es ist?</gray>",
                "<gray>Welcome to the <au>Village Linden</au>! A poacher is up to no good in the village – can you find out who it is?</gray>");
        t("hall.how", "<gray>Kompass: Karte wählen · Farbstoff: bereit · Buch: So geht's · Bett: zur Lobby</gray>",
                "<gray>Compass: choose a map · Dye: ready · Book: how to play · Bed: to the lobby</gray>");
        t("hall.title", "<au><b>Dorfkrimi</b></au>",
                "<au><b>Village Mystery</b></au>");
        t("hall.subtitle", "<gray>Ein Wilderer im Dorf – wer ist es?</gray>",
                "<gray>A poacher in the village – who is it?</gray>");
        t("hall.party-playing", "<gray>Deine Party spielt gerade – mit <white>/zuschauen</white> kannst du zusehen.</gray>",
                "<gray>Your party is playing right now – watch with <white>/spectate</white>.</gray>");
        t("hall.only", "<gray>Das geht nur an der Dorflinde.</gray>",
                "<gray>Only possible at the Village Linden.</gray>");
        t("hall.not-ready", "<gray>Du bist nicht mehr bereit.</gray>",
                "<gray>You're no longer ready.</gray>");
        t("hall.ready", "<em>Du bist bereit!</em> <gray>Sobald alle bereit sind, geht es los.</gray>",
                "<em>You're ready!</em> <gray>As soon as everyone is ready, we start.</gray>");
        t("hall.ready.alone", "<em>Du bist bereit!</em> <gray>Für eine Runde braucht es mindestens vier Leute.</gray>",
                "<em>You're ready!</em> <gray>A round needs at least four people.</gray>");
        t("hall.voted", "<gray>Deine Stimme: <sky><choice></sky></gray>",
                "<gray>Your vote: <sky><choice></sky></gray>");
        t("hall.no-map", "<gray>Alle Karten sind gerade belegt – gleich wird eine frei.</gray>",
                "<gray>All maps are busy – one will be free soon.</gray>");
        t("hall.waiting", "<gray>Warte auf Mitspieler: <white><n></white> da, ab <white><min></white> geht's los</gray>",
                "<gray>Waiting for players: <white><n></white> here, starting from <white><min></white></gray>");
        t("hall.countdown", "<au>Nächste Runde in <secs> s</au> <gray>· <map></gray>",
                "<au>Next round in <secs> s</au> <gray>· <map></gray>");
        t("hall.starting", "<em>Auf geht's!</em> <gray>Karte: <sky><map></sky> · <white><n></white> Leute</gray>",
                "<em>Here we go!</em> <gray>Map: <sky><map></sky> · <white><n></white> people</gray>");
        t("hall.full", "<gray>Die Runde ist voll – du bist bei der nächsten dabei.</gray>",
                "<gray>The round is full – you'll be in the next one.</gray>");

        // ---------- Gegenstände ----------
        t("item.vote", "<sky>Karte wählen</sky> <gray>(Rechtsklick)</gray>",
                "<sky>Choose map</sky> <gray>(right-click)</gray>");
        t("item.vote.lore", "<gray>Stimm ab, wo gespielt wird,</gray>\n<gray>oder schau einer Runde zu.</gray>",
                "<gray>Vote where to play,</gray>\n<gray>or watch a round.</gray>");
        t("item.ready", "<em>Bereit</em> <gray>(Rechtsklick)</gray>",
                "<em>Ready</em> <gray>(right-click)</gray>");
        t("item.ready.lore", "<gray>Sind alle bereit, geht es in</gray>\n<gray>5 Sekunden los.</gray>",
                "<gray>When everyone is ready,</gray>\n<gray>we start in 5 seconds.</gray>");
        t("item.howto", "<au>So geht's</au> <gray>(Rechtsklick)</gray>",
                "<au>How to play</au> <gray>(right-click)</gray>");
        t("item.howto.lore", "<gray>Rollen, Spuren, Mütze, Versammlung.</gray>",
                "<gray>Roles, clues, cap, meeting.</gray>");
        t("item.stats", "<au>Statistik</au> <gray>(Rechtsklick)</gray>",
                "<au>Stats</au> <gray>(right-click)</gray>");
        t("item.stats.lore", "<gray>Deine Werte und die besten Dorfdetektive.</gray>",
                "<gray>Your stats and the best village detectives.</gray>");
        t("item.lobby", "<rt>Zur Lobby</rt> <gray>(Rechtsklick)</gray>",
                "<rt>To the lobby</rt> <gray>(right-click)</gray>");
        t("item.lobby.lore", "<gray>Zurück in die Lobby von MoinMorni.</gray>",
                "<gray>Back to the MoinMorni lobby.</gray>");
        t("item.stun", "<#8FBF4A>Schlafpulver</#8FBF4A> <gray>(anschlagen)</gray>",
                "<#8FBF4A>Sleeping powder</#8FBF4A> <gray>(hit someone)</gray>");
        t("item.stun.lore", "<gray>In der Hand halten und jemanden</gray>\n<gray>anschlagen oder anklicken: er schläft ein.</gray>\n<gray>Danach musst du kurz warten.</gray>",
                "<gray>Hold it and hit or click</gray>\n<gray>someone: they fall asleep.</gray>\n<gray>Then you have to wait a little.</gray>");
        t("item.hat", "<bl>Dienstmütze</bl> <gray>(anschlagen: festnehmen)</gray>",
                "<bl>Constable's cap</bl> <gray>(hit: arrest)</gray>");
        t("item.hat.lore", "<gray>In der Hand halten und jemanden anschlagen</gray>\n<gray>oder anklicken: festnehmen. Ein Unschuldiger?</gray>\n<gray>Dann bist du blamiert und raus.</gray>",
                "<gray>Hold it and hit or click someone</gray>\n<gray>to arrest them. Innocent?</gray>\n<gray>Then you're disgraced and out.</gray>");
        t("item.snack", "<au>Brotzeit</au> <gray>(Rechtsklick)</gray>",
                "<au>Snack</au> <gray>(right-click)</gray>");
        t("item.snack.lore", "<gray>Kurz schneller laufen – dann eine Pause.</gray>",
                "<gray>Run faster for a moment – then a pause.</gray>");
        t("item.notes", "<sky>Notizbuch</sky> <gray>(Rechtsklick)</gray>",
                "<sky>Notebook</sky> <gray>(right-click)</gray>");
        t("item.notes.lore", "<gray>Wer trägt was, wer ist noch wach</gray>\n<gray>und wer passt zu deinen Hinweisen?</gray>",
                "<gray>Who wears what, who's still awake</gray>\n<gray>and who matches your clues?</gray>");
        t("item.role", "<gray>Deine Rolle</gray> <gray>(Rechtsklick)</gray>",
                "<gray>Your role</gray> <gray>(right-click)</gray>");
        t("item.role.lore", "<gray>Zeigt dir noch einmal, wer du bist.</gray>\n<gray>Nur du siehst es.</gray>",
                "<gray>Shows you again who you are.</gray>\n<gray>Only you see it.</gray>");
        t("item.ballot", "<au>Stimmzettel</au> <gray>(Rechtsklick)</gray>",
                "<au>Ballot</au> <gray>(right-click)</gray>");
        t("item.ballot.lore", "<gray>Wer soll festgenommen werden?</gray>",
                "<gray>Who should be arrested?</gray>");
        t("item.look", "<sky>Umschauen</sky> <gray>(Rechtsklick)</gray>",
                "<sky>Look around</sky> <gray>(right-click)</gray>");
        t("item.look.lore", "<gray>Frei durchs Dorf schweben und zusehen.</gray>\n<gray>Zurück auf die Bank: /bank</gray>",
                "<gray>Float freely through the village and watch.</gray>\n<gray>Back to the bench: /bench</gray>");

        // ---------- Menüs ----------
        t("menu.close", "<rt>Schließen</rt>",
                "<rt>Close</rt>");
        t("menu.map.title", "Welche Karte?",
                "Which map?");
        t("menu.map.votes", "<gray>Stimmen: <white><n></white></gray>",
                "<gray>Votes: <white><n></white></gray>");
        t("menu.map.busy", "<au>Runde läuft (<n> Leute) – Klick: zuschauen</au>",
                "<au>Round running (<n> people) – click: watch</au>");
        t("menu.map.cleaning", "<gray>Wird gerade aufgeräumt …</gray>",
                "<gray>Being tidied up …</gray>");
        t("menu.map.click", "<em>Klick: dafür stimmen</em>",
                "<em>Click: vote for it</em>");
        t("menu.map.mine", "<em>✔ Deine Stimme</em>",
                "<em>✔ Your vote</em>");
        t("menu.howto.title", "So geht Dorfkrimi",
                "How Village Mystery works");
        t("menu.stats.title", "Statistik",
                "Stats");
        t("menu.stats.me", "<au>Deine Werte</au>",
                "<au>Your stats</au>");
        t("menu.stats.top", "<au>Die besten Dorfdetektive</au>",
                "<au>The best village detectives</au>");
        t("menu.notes.title", "Notizbuch",
                "Notebook");
        t("menu.notes.outfit", "<gray>Tracht: <white><outfit></white></gray>",
                "<gray>Outfit: <white><outfit></white></gray>");
        t("menu.notes.status.awake", "<em>wach</em>",
                "<em>awake</em>");
        t("menu.notes.status.stunned", "<gray>schläft (betäubt)</gray>",
                "<gray>asleep (knocked out)</gray>");
        t("menu.notes.status.arrested", "<rt>festgenommen – war ein Wilderer</rt>",
                "<rt>arrested – was a poacher</rt>");
        t("menu.notes.status.blamed", "<gray>blamiert (falsche Festnahme)</gray>",
                "<gray>disgraced (wrong arrest)</gray>");
        t("menu.notes.status.jailed", "<gray>zu Unrecht eingesperrt</gray>",
                "<gray>wrongly locked up</gray>");
        t("menu.notes.status.gone", "<gray>nicht mehr da</gray>",
                "<gray>gone</gray>");
        t("menu.notes.fits", "<au>Passt zu <n> von <of> deiner Hinweise</au>",
                "<au>Matches <n> of <of> of your clues</au>");
        t("menu.notes.you", "<sky>Das bist du.</sky>",
                "<sky>That's you.</sky>");
        t("menu.notes.clues", "<au>Deine Hinweise (<n>)</au>",
                "<au>Your clues (<n>)</au>");
        t("menu.notes.none", "<gray>Noch keine. Leuchtende Spuren im Dorf</gray>\n<gray>verraten etwas über die Tracht des Wilderers.</gray>",
                "<gray>None yet. Glowing clues in the village</gray>\n<gray>reveal something about the poacher's outfit.</gray>");
        t("menu.vote.title", "Dorfversammlung: Wer war's?",
                "Village meeting: who did it?");
        t("menu.vote.click", "<em>Klick: dafür stimmen</em>",
                "<em>Click: vote for them</em>");
        t("menu.vote.yours", "<em>✔ Deine Stimme</em>",
                "<em>✔ Your vote</em>");
        t("menu.vote.skip", "<au>Abwarten</au>",
                "<au>Wait and see</au>");
        t("menu.vote.skip.lore", "<gray>Lieber niemanden festnehmen.</gray>",
                "<gray>Better not arrest anyone.</gray>");

        // ---------- So geht's ----------
        t("howto.goal", "Worum geht's?",
                "What's it about?");
        t("howto.goal.text", "<gray>Ein Wilderer treibt im Dorf sein Unwesen</gray>\n<gray>und betäubt die Leute. Fasst ihn, bevor</gray>\n<gray>alle schlafen oder die Zeit um ist!</gray>",
                "<gray>A poacher is up to no good in the village</gray>\n<gray>and knocks people out. Catch him before</gray>\n<gray>everyone is asleep or time runs out!</gray>");
        t("howto.goal.chat", "<au>Worum geht's:</au> <gray>Ein Wilderer betäubt die Leute im Dorf. Das Dorf gewinnt, wenn alle Wilderer gefasst sind – der Wilderer, wenn alle schlafen oder die Zeit um ist.</gray>",
                "<au>What it's about:</au> <gray>A poacher knocks out people in the village. The village wins when all poachers are caught – the poacher when everyone is asleep or time runs out.</gray>");
        t("howto.roles", "Die Rollen",
                "The roles");
        t("howto.roles.text", "<gray>Wilderer (Schlafpulver), Gendarm</gray>\n<gray>(Dienstmütze) und Dorfleute. Wer was ist,</gray>\n<gray>bleibt geheim – nur du kennst deine Rolle.</gray>",
                "<gray>Poacher (sleeping powder), constable</gray>\n<gray>(cap) and villagers. Who is who stays</gray>\n<gray>secret – only you know your role.</gray>");
        t("howto.roles.chat", "<au>Die Rollen:</au> <gray>Einer ist Wilderer (ab zehn Leuten zwei), einer Gendarm, alle anderen gehören zum Dorf. Wer was ist, sieht nur jeder selbst – mit der Rollenkarte ganz rechts.</gray>",
                "<au>The roles:</au> <gray>One is the poacher (two from ten people), one the constable, everyone else is a villager. Only you see your own role – with the role card on the far right.</gray>");
        t("howto.clues", "Spuren und Tracht",
                "Clues and outfits");
        t("howto.clues.text", "<gray>Jeder trägt eine Tracht (Hut, Joppe, Hose).</gray>\n<gray>Leuchtende Spuren verraten etwas über die</gray>\n<gray>Tracht des Wilderers – ins Notizbuch!</gray>",
                "<gray>Everyone wears an outfit (hat, jacket, trousers).</gray>\n<gray>Glowing clues reveal something about the</gray>\n<gray>poacher's outfit – into the notebook!</gray>");
        t("howto.clues.chat", "<au>Spuren:</au> <gray>Geh zu einer leuchtenden Spur – sie verrät dir etwas über die Tracht des Wilderers. Am Tatort liegt eine Feder (Farbe des Huts), und nach dem Betäuben leuchten seine Fußspuren kurz. Namen sind verborgen: man erkennt sich an der Tracht.</gray>",
                "<au>Clues:</au> <gray>Walk to a glowing clue – it tells you something about the poacher's outfit. A feather lies where someone was knocked out (hat colour), and his footprints glow for a moment afterwards. Names are hidden: you recognise each other by outfit.</gray>");
        t("howto.hat", "Die Dienstmütze",
                "The constable's cap");
        t("howto.hat.text", "<gray>Der Gendarm nimmt einmal fest. Trifft er</gray>\n<gray>einen Unschuldigen, fällt die Mütze liegen –</gray>\n<gray>wer sie aufhebt, ist der neue Gendarm.</gray>",
                "<gray>The constable can arrest. If he picks an</gray>\n<gray>innocent, the cap drops – whoever picks</gray>\n<gray>it up is the new constable.</gray>");
        t("howto.hat.chat", "<au>Die Mütze:</au> <gray>Der Gendarm nimmt fest, indem er jemanden mit der Mütze anschlägt. Ein Wilderer: gefasst! Ein Unschuldiger: der Gendarm ist blamiert und raus, die Mütze bleibt liegen. Wird der Gendarm betäubt, fällt sie auch. Dorfleute heben sie auf, indem sie hingehen.</gray>",
                "<au>The cap:</au> <gray>The constable arrests by hitting someone with the cap. A poacher: caught! An innocent: the constable is disgraced and out, the cap stays on the ground. If the constable is knocked out, it drops too. Villagers pick it up by walking over it.</gray>");
        t("howto.meeting", "Die Dorfversammlung",
                "The village meeting");
        t("howto.meeting.text", "<gray>Läute die Glocke: alle treffen sich und</gray>\n<gray>stimmen ab. Wer die meisten Stimmen hat,</gray>\n<gray>wird festgenommen – hoffentlich der Richtige.</gray>",
                "<gray>Ring the bell: everyone meets and votes.</gray>\n<gray>Whoever gets the most votes is arrested</gray>\n<gray>– hopefully the right one.</gray>");
        t("howto.meeting.chat", "<au>Versammlung:</au> <gray>Wer die Glocke läutet, ruft alle zusammen. Auf dem Stimmzettel wählt jeder, wer festgenommen wird – oder „abwarten“. Wer die meisten Stimmen hat (mindestens zwei), wird festgenommen. Ein Unschuldiger? Pech-Punkte für alle, die ihn gewählt haben.</gray>",
                "<au>Meeting:</au> <gray>Ringing the bell calls everyone together. On the ballot everyone picks who gets arrested – or “wait and see”. Whoever gets the most votes (at least two) is arrested. Innocent? Bad-luck points for everyone who voted for them.</gray>");
        t("howto.bench", "Auf der Bank",
                "On the bench");
        t("howto.bench.text", "<gray>Wer schläft oder raus ist, sitzt auf der</gray>\n<gray>Bank vor der Wirtschaft und schaut zu –</gray>\n<gray>und redet nur mit den anderen auf der Bank.</gray>",
                "<gray>Whoever is asleep or out sits on the</gray>\n<gray>bench in front of the inn and watches –</gray>\n<gray>talking only to the others on the bench.</gray>");
        t("howto.bench.chat", "<au>Auf der Bank:</au> <gray>Ausgeschiedene sitzen vor der Wirtschaft, sind für die Wachen unsichtbar und reden nur untereinander – so kann niemand etwas verraten. Mit dem Auge schwebt man herum, mit /bank geht's zurück.</gray>",
                "<au>On the bench:</au> <gray>Those who are out sit in front of the inn, are invisible to the players still awake and only talk among themselves – so nobody can give anything away. The eye lets you float around, /bench takes you back.</gray>");

        // ---------- Runde ----------
        t("game.welcome", "<gray>Willkommen auf <sky><map></sky>! <white><n></white> Leute – einer von euch ist ein Wilderer …</gray>",
                "<gray>Welcome to <sky><map></sky>! <white><n></white> people – one of you is a poacher …</gray>");
        t("game.count", "<au><b><n></b></au>",
                "<au><b><n></b></au>");
        t("game.count.sub", "<gray>Gleich geht's los …</gray>",
                "<gray>Starting soon …</gray>");
        t("game.go", "<em><b>Los!</b></em>",
                "<em><b>Go!</b></em>");
        t("game.go.sub", "<gray>Wer ist der Wilderer?</gray>",
                "<gray>Who is the poacher?</gray>");
        t("game.started", "<gray>Die Runde läuft! Spuren leuchten im Dorf, die Glocke ruft die Versammlung.</gray>",
                "<gray>The round is on! Clues glow around the village, the bell calls the meeting.</gray>");
        t("game.away", "<gray><white><name></white> hat die Verbindung verloren – der Platz bleibt <white><secs> s</white> frei.</gray>",
                "<gray><white><name></white> lost the connection – their place is kept for <white><secs> s</white>.</gray>");
        t("game.back", "<em>Willkommen zurück!</em> <gray>Du spielst mit derselben Rolle weiter.</gray>",
                "<em>Welcome back!</em> <gray>You carry on with the same role.</gray>");
        t("game.back.title", "<em>Zurück!</em>",
                "<em>Back!</em>");
        t("game.back.sub", "<gray>Gleiche Rolle, gleicher Ort.</gray>",
                "<gray>Same role, same spot.</gray>");
        t("game.back.all", "<gray><white><name></white> ist wieder da.</gray>",
                "<gray><white><name></white> is back.</gray>");
        t("game.too-late", "<gray><white><name></white> ist nicht rechtzeitig zurückgekommen.</gray>",
                "<gray><white><name></white> didn't come back in time.</gray>");
        t("game.too-late.self", "<gray>Leider zu spät – die Runde ging ohne dich weiter.</gray>",
                "<gray>Sorry, too late – the round went on without you.</gray>");
        t("game.left", "<gray><white><name></white> hat die Runde verlassen.</gray>",
                "<gray><white><name></white> left the round.</gray>");
        t("game.wilderer-gone", "<au>Ein Wilderer hat sich aus dem Staub gemacht!</au>",
                "<au>A poacher has made off!</au>");

        // ---------- Raus und auf der Bank ----------
        t("out.stunned.title", "<gray><b>Betäubt!</b></gray>",
                "<gray><b>Knocked out!</b></gray>");
        t("out.stunned.sub", "<gray>Du schläfst bis zum Ende der Runde.</gray>",
                "<gray>You sleep until the end of the round.</gray>");
        t("out.stunned.chat", "<gray>Der Wilderer hat dich betäubt. Du sitzt jetzt auf der Bank vor der Wirtschaft und schaust zu.</gray>",
                "<gray>The poacher knocked you out. You're now sitting on the bench in front of the inn, watching.</gray>");
        t("out.arrested.title", "<rt><b>Festgenommen!</b></rt>",
                "<rt><b>Arrested!</b></rt>");
        t("out.arrested.sub", "<gray>Der Gendarm hat dich erwischt.</gray>",
                "<gray>The constable got you.</gray>");
        t("out.arrested.chat", "<gray>Erwischt! Du sitzt jetzt auf der Bank und schaust zu.</gray>",
                "<gray>Caught! You're now sitting on the bench, watching.</gray>");
        t("out.blamed.title", "<rt><b>Blamiert!</b></rt>",
                "<rt><b>Disgraced!</b></rt>");
        t("out.blamed.sub", "<gray>Das war ein Unschuldiger.</gray>",
                "<gray>That was an innocent.</gray>");
        t("out.blamed.chat", "<gray>Falsche Festnahme – du bist raus, deine Mütze liegt am Boden.</gray>",
                "<gray>Wrong arrest – you're out, your cap is lying on the ground.</gray>");
        t("out.voted.title", "<rt><b>Erwischt!</b></rt>",
                "<rt><b>Caught!</b></rt>");
        t("out.voted.sub", "<gray>Das Dorf hat dich durchschaut.</gray>",
                "<gray>The village saw through you.</gray>");
        t("out.voted.chat", "<gray>Die Dorfversammlung hat dich erwischt. Ab auf die Bank!</gray>",
                "<gray>The village meeting caught you. Off to the bench!</gray>");
        t("out.jailed.title", "<gray><b>Eingesperrt!</b></gray>",
                "<gray><b>Locked up!</b></gray>");
        t("out.jailed.sub", "<gray>Zu Unrecht – du warst es nicht.</gray>",
                "<gray>Wrongly – it wasn't you.</gray>");
        t("out.jailed.chat", "<gray>Die Versammlung hat dich zu Unrecht gewählt. Du sitzt jetzt auf der Bank.</gray>",
                "<gray>The meeting wrongly picked you. You're now sitting on the bench.</gray>");
        t("ghost.how", "<gray>Auf der Bank redest du nur mit den anderen, die raus sind – bitte nichts verraten! <white>Auge</white>: herumschweben · <white>/bank</white>: zurück auf die Bank.</gray>",
                "<gray>On the bench you only talk to the others who are out – please don't give anything away! <white>Eye</white>: float around · <white>/bench</white>: back to the bench.</gray>");
        t("ghost.look", "<gray>Du schwebst jetzt frei herum. Zurück auf die Bank: <white>/bank</white></gray>",
                "<gray>You're floating freely now. Back to the bench: <white>/bench</white></gray>");
        t("ghost.not", "<gray>Das geht nur, wenn du in einer Runde ausgeschieden bist.</gray>",
                "<gray>Only possible when you're out of a round.</gray>");
        t("ghost.chat", "<dark_gray>[</dark_gray><gray>Bank</gray><dark_gray>]</dark_gray> <white><name></white> <dark_gray>»</dark_gray> ",
                "<dark_gray>[</dark_gray><gray>Bench</gray><dark_gray>]</dark_gray> <white><name></white> <dark_gray>»</dark_gray> ");

        // ---------- Betäuben und Festnehmen ----------
        t("stun.ready", "<#8FBF4A>Schlafpulver bereit</#8FBF4A>",
                "<#8FBF4A>Sleeping powder ready</#8FBF4A>");
        t("stun.wait", "<gray>Schlafpulver wirkt wieder in <white><secs> s</white></gray>",
                "<gray>Sleeping powder ready again in <white><secs> s</white></gray>");
        t("stun.partner", "<gray>Das ist dein Jagdgeselle.</gray>",
                "<gray>That's your fellow poacher.</gray>");
        t("stun.done", "<#8FBF4A><name> schläft.</#8FBF4A> <gray>Schnell weg hier!</gray>",
                "<#8FBF4A><name> is asleep.</#8FBF4A> <gray>Get away quickly!</gray>");
        t("stun.all", "<rt>☾</rt> <white><name></white> <gray>wurde betäubt! Noch wach im Dorf: <white><n></white></gray>",
                "<rt>☾</rt> <white><name></white> <gray>was knocked out! Still awake in the village: <white><n></white></gray>");
        t("arrest.ready", "<bl>Festnahme bereit</bl>",
                "<bl>Arrest ready</bl>");
        t("arrest.wait", "<gray>Festnehmen wieder in <white><secs> s</white></gray>",
                "<gray>Arrest again in <white><secs> s</white></gray>");
        t("arrest.right", "<bl>Gendarm <white><gendarm></white> hat den Wilderer</bl> <white><name></white> <bl>festgenommen!</bl>",
                "<bl>Constable <white><gendarm></white> arrested the poacher</bl> <white><name></white><bl>!</bl>");
        t("arrest.right.title", "<bl><b>Festgenommen!</b></bl>",
                "<bl><b>Arrested!</b></bl>");
        t("arrest.right.sub", "<gray><name> war ein Wilderer.</gray>",
                "<gray><name> was a poacher.</gray>");
        t("arrest.wrong", "<rt>Falsche Festnahme!</rt> <white><gendarm></white> <gray>hat</gray> <white><name></white> <gray>verhaftet – unschuldig. Der Gendarm ist blamiert und raus, seine Mütze liegt am Boden.</gray>",
                "<rt>Wrong arrest!</rt> <white><gendarm></white> <gray>arrested</gray> <white><name></white> <gray>– innocent. The constable is disgraced and out, the cap is lying on the ground.</gray>");
        t("arrest.wrong.victim", "<gray>Puh – du bist unschuldig und bleibst im Spiel.</gray>",
                "<gray>Phew – you're innocent and stay in the game.</gray>");

        // ---------- Mütze ----------
        t("hat.label", "<bl><b>Dienstmütze</b></bl>\n<gray>Hingehen: aufheben</gray>",
                "<bl><b>Constable's cap</b></bl>\n<gray>Walk over it: pick up</gray>");
        t("hat.dropped.stun", "<bl>Der Gendarm ist betäubt – seine Dienstmütze liegt jetzt am Boden!</bl>",
                "<bl>The constable is knocked out – the cap is now lying on the ground!</bl>");
        t("hat.dropped.meeting", "<bl>Die Dienstmütze liegt jetzt am Versammlungsplatz.</bl>",
                "<bl>The constable's cap is now lying at the meeting place.</bl>");
        t("hat.dropped.gone", "<bl>Der Gendarm ist weg – seine Dienstmütze liegt am Boden.</bl>",
                "<bl>The constable is gone – the cap is lying on the ground.</bl>");
        t("hat.picked", "<bl>Du hast die Dienstmütze aufgehoben – du bist jetzt der Gendarm!</bl> <gray>In der Hand halten und anschlagen: festnehmen.</gray>",
                "<bl>You picked up the constable's cap – you're the constable now!</bl> <gray>Hold it and hit someone to arrest them.</gray>");
        t("hat.picked.title", "<bl><b>Du bist jetzt Gendarm!</b></bl>",
                "<bl><b>You're the constable now!</b></bl>");
        t("hat.picked.sub", "<gray>Nimm den Wilderer fest.</gray>",
                "<gray>Arrest the poacher.</gray>");
        t("hat.picked.all", "<bl>Jemand hat die Dienstmütze aufgehoben.</bl>",
                "<bl>Someone picked up the constable's cap.</bl>");

        // ---------- Spuren ----------
        t("trace.title", "<au><b>Hinweis!</b></au>",
                "<au><b>Clue!</b></au>");
        t("trace.feather.title", "<au><b>Eine Feder!</b></au>",
                "<au><b>A feather!</b></au>");
        t("trace.found", "<au>Hinweis:</au> <white><clue></white> <gray>(steht jetzt im Notizbuch)</gray>",
                "<au>Clue:</au> <white><clue></white> <gray>(now in your notebook)</gray>");
        t("trace.feather", "<au>Eine Feder vom Tatort:</au> <white><clue></white> <gray>(steht jetzt im Notizbuch)</gray>",
                "<au>A feather from the scene:</au> <white><clue></white> <gray>(now in your notebook)</gray>");
        t("trace.nothing", "<gray>Die Spur verrät dir nichts Neues – du weißt schon alles darüber.</gray>",
                "<gray>The clue tells you nothing new – you already know all about it.</gray>");
        t("trace.nothing.sub", "<gray>Nichts Neues.</gray>",
                "<gray>Nothing new.</gray>");
        t("trace.wiped", "<#8FBF4A>Spur verwischt.</#8FBF4A>",
                "<#8FBF4A>Clue wiped away.</#8FBF4A>");

        // ---------- Dorfversammlung ----------
        t("meeting.off", "<gray>Versammlungen sind auf diesem Server ausgeschaltet.</gray>",
                "<gray>Meetings are switched off on this server.</gray>");
        t("meeting.none-left", "<gray>In dieser Runde gibt es keine Versammlung mehr.</gray>",
                "<gray>No more meetings in this round.</gray>");
        t("meeting.too-early", "<gray>Noch zu früh – die Glocke läutet erst in <white><secs> s</white>.</gray>",
                "<gray>Too early – the bell can be rung in <white><secs> s</white>.</gray>");
        t("meeting.not-now", "<gray>Gerade ist keine Dorfversammlung.</gray>",
                "<gray>There's no village meeting right now.</gray>");
        t("meeting.title", "<au><b>Dorfversammlung!</b></au>",
                "<au><b>Village meeting!</b></au>");
        t("meeting.sub", "<gray><name> hat die Glocke geläutet.</gray>",
                "<gray><name> rang the bell.</gray>");
        t("meeting.called", "<au>Dorfversammlung!</au> <white><name></white> <gray>hat geläutet. Ihr habt <white><secs> s</white>: Wer war's? (Stimmzettel in der Schnellleiste)</gray>",
                "<au>Village meeting!</au> <white><name></white> <gray>rang the bell. You have <white><secs> s</white>: who did it? (Ballot in your hotbar)</gray>");
        t("meeting.voted", "<gray>Deine Stimme: <white><name></white></gray>",
                "<gray>Your vote: <white><name></white></gray>");
        t("meeting.voted.skip", "<gray>Deine Stimme: abwarten</gray>",
                "<gray>Your vote: wait and see</gray>");
        t("meeting.someone", "<gray><white><name></white> hat abgestimmt (<white><n></white>/<white><of></white>).</gray>",
                "<gray><white><name></white> has voted (<white><n></white>/<white><of></white>).</gray>");
        t("meeting.skip", "abwarten",
                "wait");
        t("meeting.tally", "<au>Ergebnis:</au> <gray><list></gray>",
                "<au>Result:</au> <gray><list></gray>");
        t("meeting.right", "<em>Richtig!</em> <white><name></white> <gray>war ein Wilderer und ist gefasst.</gray>",
                "<em>Right!</em> <white><name></white> <gray>was a poacher and is caught.</gray>");
        t("meeting.right.you", "<em>Gut kombiniert!</em> <gray>Punkte für deine Stimme.</gray>",
                "<em>Well deduced!</em> <gray>Points for your vote.</gray>");
        t("meeting.wrong", "<rt>Oje!</rt> <white><name></white> <gray>war unschuldig – und muss trotzdem bis zum Ende in den Arrest.</gray>",
                "<rt>Oh dear!</rt> <white><name></white> <gray>was innocent – and still has to stay locked up until the end.</gray>");
        t("meeting.wrong.you", "<rt>Pech-Punkte:</rt> <gray>minus <white><n></white>, weil du einen Unschuldigen gewählt hast.</gray>",
                "<rt>Bad-luck points:</rt> <gray>minus <white><n></white> for voting for an innocent.</gray>");
        t("meeting.nobody", "<gray>Keine klare Mehrheit – niemand wird festgenommen. Weiter geht's!</gray>",
                "<gray>No clear majority – nobody is arrested. On we go!</gray>");

        // ---------- Sonstiges in der Runde ----------
        t("snack.done", "<au>Mahlzeit!</au> <gray>Kurz schneller.</gray>",
                "<au>Enjoy!</au> <gray>Faster for a moment.</gray>");
        t("snack.wait", "<gray>Brotzeit wieder in <white><secs> s</white></gray>",
                "<gray>Snack again in <white><secs> s</white></gray>");
        t("time.minute", "<au>Noch eine Minute!</au> <gray>Ist der Wilderer bis dahin nicht gefasst, gewinnt er.</gray>",
                "<au>One minute left!</au> <gray>If the poacher isn't caught by then, he wins.</gray>");
        t("time.left", "<au><n></au>",
                "<au><n></au>");

        // ---------- Ende ----------
        t("end.village.title", "<em><b>Das Dorf gewinnt!</b></em>",
                "<em><b>The village wins!</b></em>");
        t("end.wilderer.title", "<#8FBF4A><b>Der Wilderer gewinnt!</b></#8FBF4A>",
                "<#8FBF4A><b>The poacher wins!</b></#8FBF4A>");
        t("end.sub.caught", "<gray>Alle Wilderer sind gefasst.</gray>",
                "<gray>All poachers are caught.</gray>");
        t("end.caught", "<em>Das Dorf gewinnt:</em> <gray>Alle Wilderer sind gefasst!</gray>",
                "<em>The village wins:</em> <gray>all poachers are caught!</gray>");
        t("end.sub.fled", "<gray>Der Wilderer hat sich aus dem Staub gemacht.</gray>",
                "<gray>The poacher made off.</gray>");
        t("end.fled", "<em>Das Dorf gewinnt:</em> <gray>Der Wilderer hat sich aus dem Staub gemacht.</gray>",
                "<em>The village wins:</em> <gray>the poacher made off.</gray>");
        t("end.sub.stunned", "<gray>Das ganze Dorf schläft.</gray>",
                "<gray>The whole village is asleep.</gray>");
        t("end.stunned", "<#8FBF4A>Der Wilderer gewinnt:</#8FBF4A> <gray>Das ganze Dorf schläft.</gray>",
                "<#8FBF4A>The poacher wins:</#8FBF4A> <gray>the whole village is asleep.</gray>");
        t("end.sub.time", "<gray>Die Zeit ist um.</gray>",
                "<gray>Time's up.</gray>");
        t("end.time", "<#8FBF4A>Der Wilderer gewinnt:</#8FBF4A> <gray>Die Zeit ist um, und er ist noch frei.</gray>",
                "<#8FBF4A>The poacher wins:</#8FBF4A> <gray>time's up and he's still free.</gray>");
        t("end.sub.aborted", "<gray>Abgebrochen.</gray>",
                "<gray>Aborted.</gray>");
        t("end.aborted", "<gray>Die Runde wurde abgebrochen.</gray>",
                "<gray>The round was aborted.</gray>");
        t("end.reveal", "<gray>Der Wilderer war:</gray> <#8FBF4A><names></#8FBF4A>",
                "<gray>The poacher was:</gray> <#8FBF4A><names></#8FBF4A>");
        t("end.reveal.outfit", "<gray>  <white><name></white> trug: <outfit></gray>",
                "<gray>  <white><name></white> wore: <outfit></gray>");
        t("end.gendarms", "<gray>Mit Dienstmütze unterwegs:</gray> <bl><names></bl>",
                "<gray>Wearing the cap:</gray> <bl><names></bl>");
        t("end.mine.won", "<em>Gewonnen</em> <gray>als <role> · <au><points></au> Punkte · <white><clues></white> Hinweise</gray>",
                "<em>Won</em> <gray>as <role> · <au><points></au> points · <white><clues></white> clues</gray>");
        t("end.mine.lost", "<gray>Verloren als <role> · <au><points></au> Punkte · <white><clues></white> Hinweise</gray>",
                "<gray>Lost as <role> · <au><points></au> points · <white><clues></white> clues</gray>");
        t("end.best", "<au>Neue Bestleistung:</au> <gray><white><n></white> Punkte in einer Runde!</gray>",
                "<au>New personal best:</au> <gray><white><n></white> points in one round!</gray>");

        // ---------- Leisten ----------
        t("bar.start", "<au>Gleich geht's los – <secs> s</au> <gray>· merk dir deine Rolle!</gray>",
                "<au>Starting in <secs> s</au> <gray>· remember your role!</gray>");
        t("bar.time", "<gray>Zeit: <white><time></white> · noch wach: <white><n></white></gray>",
                "<gray>Time: <white><time></white> · still awake: <white><n></white></gray>");
        t("bar.ghost", "<gray>Auf der Bank · Zeit: <white><time></white> · noch wach: <white><n></white></gray>",
                "<gray>On the bench · time: <white><time></white> · still awake: <white><n></white></gray>");
        t("bar.meeting", "<au>Dorfversammlung – noch <secs> s</au> <gray>· abgestimmt: <white><n></white>/<white><of></white></gray>",
                "<au>Village meeting – <secs> s left</au> <gray>· voted: <white><n></white>/<white><of></white></gray>");
        t("bar.meeting.count", "<au>Die Stimmen werden gezählt …</au>",
                "<au>Counting the votes …</au>");
        t("bar.paused", "<gray>Angehalten – alle sind gerade weg.</gray>",
                "<gray>Paused – everyone is away right now.</gray>");
        t("bar.over.village", "<em>Das Dorf gewinnt!</em>",
                "<em>The village wins!</em>");
        t("bar.over.wilderer", "<#8FBF4A>Der Wilderer gewinnt!</#8FBF4A>",
                "<#8FBF4A>The poacher wins!</#8FBF4A>");
        t("side.title", "<au><b>Dorfkrimi</b></au>",
                "<au><b>Village Mystery</b></au>");
        t("side.map", "<gray>Karte: <sky><map></sky></gray>",
                "<gray>Map: <sky><map></sky></gray>");
        t("side.start", "<au>Gleich geht's los</au>",
                "<au>Starting soon</au>");
        t("side.meeting", "<au>Dorfversammlung</au>",
                "<au>Village meeting</au>");
        t("side.time", "<gray>Zeit: <white><time></white></gray>",
                "<gray>Time: <white><time></white></gray>");
        t("side.awake", "<gray>Noch wach: <white><n></white></gray>",
                "<gray>Still awake: <white><n></white></gray>");
        t("side.role", "<gray>Du bist: <role></gray>",
                "<gray>You are: <role></gray>");
        t("side.bench", "<gray>Du sitzt auf der Bank.</gray>",
                "<gray>You're on the bench.</gray>");
        t("side.clues", "<gray>Hinweise: <au><n></au></gray>",
                "<gray>Clues: <au><n></au></gray>");
        t("side.points", "<gray>Punkte: <au><n></au></gray>",
                "<gray>Points: <au><n></au></gray>");
        t("side.spectating", "<gray>Du schaust zu.</gray>",
                "<gray>You're watching.</gray>");
        t("side.site", "<au>moinmorni.net</au>",
                "<au>moinmorni.net</au>");
        t("side.hall.players", "<gray>An der Dorflinde: <white><n></white></gray>",
                "<gray>At the linden: <white><n></white></gray>");
        t("side.hall.start", "<gray>Start in <au><secs> s</au></gray>",
                "<gray>Start in <au><secs> s</au></gray>");
        t("side.hall.wait", "<gray>Warte auf Mitspieler</gray>",
                "<gray>Waiting for players</gray>");
        t("side.hall.wins", "<gray>Deine Siege: <au><n></au></gray>",
                "<gray>Your wins: <au><n></au></gray>");
        t("side.hall.arrests", "<gray>Festnahmen: <au><n></au></gray>",
                "<gray>Arrests: <au><n></au></gray>");

        // ---------- Zuschauen, Verlassen, Statistik, Tafeln ----------
        t("spectate.start", "<gray>Du schaust der Runde auf <sky><map></sky> zu. Zurück mit <white>/verlassen</white>. Bitte nichts verraten!</gray>",
                "<gray>You're watching the round on <sky><map></sky>. Back with <white>/leave</white>. Please don't give anything away!</gray>");
        t("spectate.none", "<gray>Gerade läuft keine Runde.</gray>",
                "<gray>No round is running right now.</gray>");
        t("spectate.playing", "<gray>Du spielst gerade selbst mit.</gray>",
                "<gray>You're playing yourself right now.</gray>");
        t("leave.none", "<gray>Du bist in keiner Runde.</gray>",
                "<gray>You're not in a round.</gray>");
        t("leave.done", "<gray>Zurück an der Dorflinde.</gray>",
                "<gray>Back at the Village Linden.</gray>");
        t("stats.head", "<au>Statistik von <white><name></white></au>",
                "<au>Stats of <white><name></white></au>");
        t("stats.none", "<gray>Von <white><name></white> gibt es noch keine Werte.</gray>",
                "<gray>No stats for <white><name></white> yet.</gray>");
        t("stats.lines", "<gray>Runden: <white><games></white> · Siege: <au><wins></au> (als Wilderer <white><ww></white>, als Gendarm <white><wg></white>)</gray>\n<gray>Wilderer: <white><tw></white>× · Gendarm: <white><tg></white>×</gray>\n<gray>Festnahmen: <au><arrests></au> · Betäubt: <white><stuns></white> · Hinweise: <white><clues></white></gray>\n<gray>Punkte: <white><points></white> · Beste Runde: <white><best></white></gray>",
                "<gray>Rounds: <white><games></white> · Wins: <au><wins></au> (as poacher <white><ww></white>, as constable <white><wg></white>)</gray>\n<gray>Poacher: <white><tw></white>× · Constable: <white><tg></white>×</gray>\n<gray>Arrests: <au><arrests></au> · Knocked out: <white><stuns></white> · Clues: <white><clues></white></gray>\n<gray>Points: <white><points></white> · Best round: <white><best></white></gray>");
        t("top.head", "<au>Die besten Dorfdetektive</au>",
                "<au>The best village detectives</au>");
        t("top.empty", "<gray>Noch niemand – sei der Erste!</gray>",
                "<gray>Nobody yet – be the first!</gray>");
        t("top.row", "<au><pos>.</au> <white><name></white> <gray>– <au><wins></au> Siege, <white><arrests></white> Festnahmen, <white><points></white> Punkte</gray>",
                "<au><pos>.</au> <white><name></white> <gray>– <au><wins></au> wins, <white><arrests></white> arrests, <white><points></white> points</gray>");
        t("board.title", "<au><b>Steckbrief-Tafel: die besten Dorfdetektive</b></au>",
                "<au><b>Wanted board: the best village detectives</b></au>");
        t("board.table", "<sky><b><map></b></sky>\n<gray><desc></gray>\n<state>",
                "<sky><b><map></b></sky>\n<gray><desc></gray>\n<state>");
        t("board.table.running", "<au>Runde läuft – Kompass: zuschauen</au>",
                "<au>Round running – compass: watch</au>");
        t("board.table.votes", "<gray>Stimmen: <white><n></white></gray>",
                "<gray>Votes: <white><n></white></gray>");
        t("board.welcome", "<em><b>✦ Dorfkrimi ✦</b></em>\n<gray>Ein Wilderer im Dorf – wer ist es?</gray>",
                "<em><b>✦ Village Mystery ✦</b></em>\n<gray>A poacher in the village – who is it?</gray>");
        t("board.howto", "<au><b>So geht's</b></au>\n<gray>1. Rolle merken – sie ist geheim</gray>\n<gray>2. Spuren finden, Tracht vergleichen</gray>\n<gray>3. Glocke: Dorfversammlung</gray>\n<gray>4. Gendarm nimmt fest – nur den Richtigen!</gray>",
                "<au><b>How to play</b></au>\n<gray>1. Remember your role – it's secret</gray>\n<gray>2. Find clues, compare outfits</gray>\n<gray>3. Bell: village meeting</gray>\n<gray>4. Constable arrests – only the right one!</gray>");
    }

    /** Alle Schlüssel (für Tests). */
    public static Set<String> keys() {
        return Collections.unmodifiableSet(TEXTS.keySet());
    }

    /** Roher MiniMessage-Text. Unbekannte Sprache → Deutsch. */
    public static String raw(String lang, String key) {
        String[] t = TEXTS.get(key);
        if (t == null) {
            return key;
        }
        return "en".equals(lang) ? t[1] : t[0];
    }

    /** Text ohne Vorsilbe; für Gegenstände ohne kursiv. */
    public static Component text(String lang, String key, TagResolver... resolvers) {
        return MM.deserialize(raw(lang, key), resolvers).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /** Nachricht für den Chat, mit „✦ Dorfkrimi »“ davor. */
    public static Component msg(String lang, String key, TagResolver... resolvers) {
        return MM.deserialize(raw(lang, "prefix")).append(MM.deserialize(raw(lang, key), resolvers));
    }

    /** Mehrzeiliger Text als einzelne Zeilen (für Beschreibungen von Gegenständen). */
    public static List<Component> lines(String lang, String key, TagResolver... resolvers) {
        List<Component> out = new ArrayList<>();
        for (String line : raw(lang, key).split("\n")) {
            out.add(MM.deserialize(line, resolvers).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
        return out;
    }

    /** Reiner Text (ohne Formatierung), z. B. für Namen von Karten. */
    public static String plain(String lang, String key) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(MM.deserialize(raw(lang, key)));
    }
}
