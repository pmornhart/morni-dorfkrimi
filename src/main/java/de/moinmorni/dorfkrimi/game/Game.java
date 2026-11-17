package de.moinmorni.dorfkrimi.game;

import de.moinmorni.dorfkrimi.DorfkrimiPlugin;
import de.moinmorni.dorfkrimi.Holo;
import de.moinmorni.dorfkrimi.Items;
import de.moinmorni.dorfkrimi.Settings;
import de.moinmorni.dorfkrimi.Texts;
import de.moinmorni.dorfkrimi.Ui;
import de.moinmorni.dorfkrimi.data.PlayerData;
import de.moinmorni.dorfkrimi.logic.Clue;
import de.moinmorni.dorfkrimi.logic.Clues;
import de.moinmorni.dorfkrimi.logic.Comeback;
import de.moinmorni.dorfkrimi.logic.Cooldowns;
import de.moinmorni.dorfkrimi.logic.Meeting;
import de.moinmorni.dorfkrimi.logic.Outcome;
import de.moinmorni.dorfkrimi.logic.Outfit;
import de.moinmorni.dorfkrimi.logic.Outfits;
import de.moinmorni.dorfkrimi.logic.Part;
import de.moinmorni.dorfkrimi.logic.Role;
import de.moinmorni.dorfkrimi.logic.Roles;
import de.moinmorni.dorfkrimi.logic.Round;
import de.moinmorni.dorfkrimi.world.MapLayout;
import de.moinmorni.dorfkrimi.world.MapType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * Eine Runde Dorfkrimi auf einer Karte. Im Dorf treibt ein Wilderer sein Unwesen (ab vielen Leuten zwei): Er betäubt
 * die Leute mit Schlafpulver – wer betäubt ist, setzt sich auf die Bank vor der Wirtschaft und schaut zu. Der Gendarm
 * darf festnehmen; trifft er einen Unschuldigen, ist er blamiert und seine Mütze fällt liegen – jemand aus dem Dorf kann
 * sie aufheben und wird Gendarm. Die Dorfleute finden Spuren, die etwas über die Tracht des Wilderers verraten, und
 * können mit der Glocke eine Dorfversammlung rufen: wer die meisten Stimmen hat, wird festgenommen. Die Regeln stehen in
 * {@link Round}; hier geht es um Anzeige, Orte und Abläufe in Minecraft.
 */
public final class Game {

    public enum Phase {
        START, PLAY, MEETING, OVER
    }

    /** Eine Spur, die irgendwo liegt: gewöhnliche Spur (Hinweis wird beim Finden ausgedacht) oder Feder vom Tatort. */
    private record Trace(Location at, ItemDisplay display, Clue fixed, int spot, long since) {
    }

    /** Eine liegengebliebene Dienstmütze. */
    private record DroppedHat(Location at, ItemDisplay display, Holo label) {
    }

    /** Ein Abdruck auf dem Weg des Wilderers (leuchtet nach einer Betäubung kurz). */
    private record Print(Location at, long until) {
    }

    public static final String PROP_TAG = "dorfkrimi_ding";
    public static final String FIREWORK_TAG = "dorfkrimi_feuerwerk";
    private static final int SECOND = 20;
    private static final int STEP = 5;
    private static final int COUNTDOWN = 3;
    private static final double PICKUP = 1.6;
    private static final double SPECTATOR_RANGE = 90;
    private static final int MEETING_SAFE_SECONDS = 4;
    private static final int MEETING_RESULT_DELAY = 3 * SECOND;
    private static final int SNACK_SECONDS = 3;
    private static final int SNACK_PAUSE = 30;
    private static final int WARN_MINUTE = 60;
    private static final int WARN_END = 10;
    private static final double CLUE_KEEP_AWAY = 8;
    private static final String[] TRACE_ITEMS = {"feather", "string", "leather", "brown_dye", "paper", "bone_meal"};
    private static final String STUN = "stun";
    private static final String SNACK = "snack";
    private static final String HAT_PAUSE = "hat";

    private final DorfkrimiPlugin plugin;
    private final MapType map;
    private final World world;
    private final MapLayout layout;
    private final Settings settings;
    private final Random random = new Random();
    private final Map<UUID, Suspect> people = new LinkedHashMap<>();
    private final Round round;
    private final Map<UUID, Integer> wildererNo = new HashMap<>();
    private final Set<UUID> spectators = new LinkedHashSet<>();
    private final Comeback comeback = new Comeback();
    private final Cooldowns cooldowns = new Cooldowns();
    private final Set<UUID> told = new HashSet<>();
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private final List<Trace> traces = new ArrayList<>();
    private final Map<Integer, DroppedHat> hats = new HashMap<>();
    private final Map<Integer, Long> rearmAt = new HashMap<>();
    private final Map<UUID, Long> trailUntil = new HashMap<>();
    private final Deque<Print> prints = new ArrayDeque<>();
    private final Map<UUID, Location> lastPrint = new HashMap<>();
    private final Map<UUID, ArmorStand> seats = new HashMap<>();
    private final Map<UUID, Long> hinted = new HashMap<>();
    private final List<Entity> props = new ArrayList<>();
    private Phase phase = Phase.START;
    private int phaseTicks;
    private long ticks;
    private int secondsLeft;
    private int clueTimer;
    private boolean paused;
    private Outcome outcome;
    private int meetings;
    private Meeting meeting;
    private UUID caller;
    private int meetingTicks;
    private int resultIn = -1;
    private long meetingStart;
    private final Map<UUID, Location> beforeMeeting = new HashMap<>();
    private int nextSeat;

    public Game(DorfkrimiPlugin plugin, MapType map, List<Player> players, List<List<UUID>> groups,
            Map<UUID, Role> forced) {
        this.plugin = plugin;
        this.map = map;
        this.world = plugin.world();
        this.layout = map.layout();
        this.settings = plugin.settings();
        this.secondsLeft = settings.roundSeconds();
        int n = players.size();
        int count = Roles.wilderers(n, settings.twoFrom());
        Map<UUID, Roles.Lots> lots = new HashMap<>();
        for (Player p : players) {
            lots.put(p.getUniqueId(), plugin.data(p).lots());
        }
        Map<UUID, Role> roles = Roles.assign(groups, lots, count, count, random);
        applyForced(roles, forced, count);
        List<UUID> order = players.stream().map(Player::getUniqueId).toList();
        Map<UUID, Outfit> outfits = Outfits.assign(order, Roles.with(roles, Role.WILDERER), random);
        this.round = new Round(roles);
        int w = 0;
        for (UUID u : Roles.with(roles, Role.WILDERER)) {
            wildererNo.put(u, w++);
        }
        for (int i = 0; i < n; i++) {
            Player p = players.get(i);
            people.put(p.getUniqueId(), new Suspect(p.getUniqueId(), p.getName(), i, outfits.get(p.getUniqueId())));
            plugin.store().pin(p.getUniqueId());
        }
        phaseTicks = settings.startSeconds() * SECOND;
        for (Suspect s : people.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p == null) {
                continue;
            }
            p.teleport(spawnSpot(s.index(), n));
            prepare(p, GameMode.ADVENTURE);
            dress(p, s);
            p.setPlayerTime(layout.time(), false);
            showBar(p);
            tellPlaying(p, true);
            String lang = plugin.lang(p);
            p.sendMessage(Texts.msg(lang, "game.welcome", Placeholder.unparsed("map", Texts.plain(lang, "map." + map.id())),
                    Placeholder.unparsed("n", String.valueOf(n))));
            tellRole(p, true);
        }
        names();
        updateBars();
        plugin.boards().game(this);
    }

    /** Für Tests und Verwaltung: vorgegebene Rollen übernehmen (die Zahl der Wilderer und Gendarmen bleibt). */
    private static void applyForced(Map<UUID, Role> roles, Map<UUID, Role> forced, int count) {
        if (forced == null || forced.isEmpty()) {
            return;
        }
        forced.forEach((u, r) -> {
            if (roles.containsKey(u)) {
                roles.put(u, r);
            }
        });
        for (Role special : new Role[] {Role.WILDERER, Role.GENDARM}) {
            List<UUID> with = Roles.with(roles, special);
            Iterator<UUID> it = with.iterator();
            int extra = with.size() - count;
            while (extra > 0 && it.hasNext()) {
                UUID u = it.next();
                if (forced.get(u) != special) {
                    roles.put(u, Role.DORF);
                    extra--;
                }
            }
        }
    }

    // ---------- Zugriff ----------

    public MapType map() {
        return map;
    }

    public Phase phase() {
        return phase;
    }

    public int secondsLeft() {
        return secondsLeft;
    }

    public Suspect suspect(UUID uuid) {
        return people.get(uuid);
    }

    public Map<UUID, Suspect> people() {
        return people;
    }

    public Round round() {
        return round;
    }

    public boolean spectating(UUID uuid) {
        return spectators.contains(uuid);
    }

    public boolean waitingFor(UUID uuid) {
        return comeback.waitingFor(uuid);
    }

    /** Spielt mit und ist wach (betäubt oder festgenommen zählt nicht). */
    public boolean awake(UUID uuid) {
        Suspect s = people.get(uuid);
        return s != null && s.active() && round.awake(uuid);
    }

    /** Ausgeschieden, aber noch in der Runde (sitzt auf der Bank, schaut zu). */
    public boolean ghost(UUID uuid) {
        Suspect s = people.get(uuid);
        return s != null && !s.gone() && !round.awake(uuid) && phase != Phase.OVER;
    }

    /** Leute, die gerade mitspielen (verbunden, nicht gegangen). */
    public int activeCount() {
        return (int) people.values().stream().filter(Suspect::active).count();
    }

    /** Wach und verbunden. */
    private int awakeOnline() {
        return (int) people.values().stream().filter(s -> s.active() && round.awake(s.uuid())).count();
    }

    Location loc(double x, double y, double z) {
        return new Location(world, map.originX() + x, MapType.FLOOR_Y + y, map.originZ() + z);
    }

    private double relX(Location l) {
        return l.getX() - map.originX();
    }

    private double relZ(Location l) {
        return l.getZ() - map.originZ();
    }

    /** Gehört die Stelle (grob) zu dieser Karte? */
    public boolean onMap(Location l) {
        return l.getWorld() == world && Math.abs(relX(l)) < 300 && Math.abs(relZ(l)) < 300;
    }

    private Location spawnSpot(int i, int n) {
        double[] s = layout.spawn(i, n);
        Location l = loc(s[0], 1, s[1]);
        l.setYaw((float) s[2]);
        return l;
    }

    /** Wo Zuschauer anfangen: hoch über dem Dorfplatz. */
    public Location spectatorSpot() {
        Location l = loc(layout.square()[0] + 0.5, 24, layout.square()[1] + 30.5);
        l.setYaw(180f);
        l.setPitch(35f);
        return l;
    }

    /** Ist das die Glocke für die Dorfversammlung? */
    public boolean bell(Location block) {
        int[] b = layout.bell();
        return block.getWorld() == world && block.getBlockX() == map.originX() + b[0]
                && block.getBlockY() == MapType.FLOOR_Y + b[1] && block.getBlockZ() == map.originZ() + b[2];
    }

    /** Alle, die die Runde sehen: verbundene Mitspieler (auch Ausgeschiedene) und Zuschauer. */
    public List<Player> audience() {
        List<Player> out = new ArrayList<>();
        for (Suspect s : people.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p != null && s.active()) {
                out.add(p);
            }
        }
        for (UUID u : spectators) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                out.add(p);
            }
        }
        return out;
    }

    /** Wer Nachrichten der Ausgeschiedenen lesen darf: Ausgeschiedene und Zuschauer dieser Runde. */
    public List<Player> benchListeners() {
        List<Player> out = new ArrayList<>();
        for (Player p : audience()) {
            if (!awake(p.getUniqueId()) || phase == Phase.OVER) {
                out.add(p);
            }
        }
        return out;
    }

    void broadcast(Function<String, Component> msg) {
        for (Player p : audience()) {
            p.sendMessage(msg.apply(plugin.lang(p)));
        }
    }

    private void soundAll(String key, float pitch) {
        for (Player p : audience()) {
            Ui.sound(p, key, pitch);
        }
    }

    private boolean hintOnce(UUID u) {
        Long last = hinted.get(u);
        if (last != null && ticks - last < SECOND) {
            return false;
        }
        hinted.put(u, ticks);
        return true;
    }

    private static TagResolver name(String tag, String name) {
        return Placeholder.unparsed(tag, name);
    }

    /** Tracht als Text: „grüner Hut, rote Joppe, braune Hose“. */
    public static String outfitText(String lang, Outfit o) {
        return Texts.plain(lang, "outfit.hut." + o.hut().id()) + ", " + Texts.plain(lang, "outfit.joppe." + o.joppe().id())
                + ", " + Texts.plain(lang, "outfit.hose." + o.hose().id());
    }

    /** Hinweis als Text: „Der Wilderer trägt einen grünen Hut.“ */
    public String clueText(String lang, Clue c) {
        boolean two = wildererNo.size() > 1;
        String who = Texts.plain(lang, two ? "clue.who.one" : "clue.who.the");
        String what = Texts.plain(lang, "clue." + c.part().id() + "." + (c.positive() ? "ja" : "nein") + "."
                + c.colour().id());
        return who + " " + what;
    }

    /** Rolle als farbiger Name. */
    public static Component roleName(String lang, Role r) {
        return Component.text(Texts.plain(lang, "role." + r.id()), TextColor.color(r.rgb()));
    }

    // ---------- Spieler ----------

    private void prepare(Player p, GameMode mode) {
        leaveSeat(p.getUniqueId());
        p.setGameMode(mode);
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setInvulnerable(false);
        p.getInventory().clear();
        p.clearActivePotionEffects();
        AttributeInstance max = p.getAttribute(Attribute.MAX_HEALTH);
        p.setHealth(max == null ? 20 : max.getValue());
        p.setFoodLevel(20);
        p.setSaturation(8);
        p.setFireTicks(0);
        p.setFallDistance(0);
        p.setExp(0);
        p.setLevel(0);
        p.setGlowing(false);
    }

    /** Tracht anziehen: Hut, Joppe, Lederhose in den Farben der Runde, schwarze Schuhe. */
    private void dress(Player p, Suspect s) {
        String lang = plugin.lang(p);
        Outfit o = s.outfit();
        PlayerInventory inv = p.getInventory();
        inv.setHelmet(Items.dyed(Material.LEATHER_HELMET, o.hut().rgb(), Texts.text(lang, "outfit.hut." + o.hut().id())));
        inv.setChestplate(Items.dyed(Material.LEATHER_CHESTPLATE, o.joppe().rgb(),
                Texts.text(lang, "outfit.joppe." + o.joppe().id())));
        inv.setLeggings(Items.dyed(Material.LEATHER_LEGGINGS, o.hose().rgb(), Texts.text(lang, "outfit.hose." + o.hose().id())));
        inv.setBoots(Items.dyed(Material.LEATHER_BOOTS, 0x2A2420, Texts.text(lang, "outfit.boots")));
    }

    /** Schnellleiste: Werkzeug der Rolle (Schlafpulver oder Dienstmütze), Brotzeit, Notizbuch, Rollenkarte. */
    private void kit(Player p) {
        String lang = plugin.lang(p);
        UUID u = p.getUniqueId();
        PlayerInventory inv = p.getInventory();
        inv.clear(0);
        if (round.role(u) == Role.WILDERER) {
            inv.setItem(0, tool(lang, Material.SUGAR, STUN, "item.stun"));
        } else if (round.hatOf(u) != null) {
            inv.setItem(0, hatItem(lang));
        }
        inv.setItem(1, tool(lang, Material.BREAD, SNACK, "item.snack"));
        inv.setItem(4, tool(lang, Material.BOOK, "notes", "item.notes"));
        inv.setItem(8, tool(lang, Material.PAPER, "role", "item.role"));
        dress(p, people.get(u));
    }

    private static ItemStack tool(String lang, Material m, String id, String key) {
        return Items.helpTag(Items.named(m, 1, Texts.text(lang, key), Texts.lines(lang, key + ".lore")), id);
    }

    private static ItemStack hatItem(String lang) {
        ItemStack item = Items.dyed(Material.LEATHER_HELMET, 0x1F3A5F, Texts.text(lang, "item.hat"));
        item.editMeta(m -> m.lore(Texts.lines(lang, "item.hat.lore")));
        return Items.helpTag(item, "hat");
    }

    /** Rolle verraten – nur der Person selbst (Titel und Chat). */
    private void tellRole(Player p, boolean start) {
        String lang = plugin.lang(p);
        UUID u = p.getUniqueId();
        Role r = round.role(u);
        boolean hat = round.hatOf(u) != null;
        String key = r == Role.WILDERER ? "wilderer" : hat ? "gendarm" : "dorf";
        Ui.title(p, Texts.text(lang, "role.title." + key), Texts.text(lang, "role.sub." + key), start ? 4500 : 2500);
        p.sendMessage(Texts.msg(lang, "role.chat." + key));
        p.sendMessage(Texts.msg(lang, "role.outfit", name("outfit", outfitText(lang, people.get(u).outfit()))));
        if (r == Role.WILDERER && wildererNo.size() > 1) {
            String partner = wildererNo.keySet().stream().filter(w -> !w.equals(u)).map(w -> people.get(w).name())
                    .collect(Collectors.joining(", "));
            p.sendMessage(Texts.msg(lang, "role.partner", name("name", partner)));
        }
        if (start) {
            Ui.sound(p, r == Role.WILDERER ? "minecraft:entity.fox.screech" : "minecraft:block.bell.use", 0.9f);
        }
    }

    /** Namen über den Köpfen verbergen (man erkennt sich an der Tracht) – wenn eingeschaltet. */
    private void names() {
        List<String> all = people.values().stream().map(Suspect::name).toList();
        for (Player p : audience()) {
            plugin.boards().sidebar().hideNames(p, settings.hideNames() ? all : List.of());
        }
        visibility();
    }

    /** Ausgeschiedene sehen nur Ausgeschiedene und Zuschauer – Wache sehen sie nicht (niemand kann etwas verraten). */
    private void visibility() {
        List<Player> all = audience();
        for (Player viewer : all) {
            boolean viewerAwake = awake(viewer.getUniqueId()) && phase != Phase.OVER;
            for (Player other : all) {
                if (viewer.equals(other)) {
                    continue;
                }
                boolean hide = viewerAwake && (ghost(other.getUniqueId()) || spectators.contains(other.getUniqueId()));
                if (hide) {
                    viewer.hidePlayer(plugin, other);
                } else {
                    viewer.showPlayer(plugin, other);
                }
            }
        }
    }

    /** Am Ende: alle sehen wieder alle. */
    private void showAll() {
        List<Player> all = audience();
        for (Player a : all) {
            for (Player b : all) {
                if (!a.equals(b)) {
                    a.showPlayer(plugin, b);
                }
            }
        }
    }

    // ---------- Bank vor der Wirtschaft ----------

    /** Auf einen freien Platz der Bank setzen (Ausgeschiedene). */
    public void seat(Player p) {
        UUID u = p.getUniqueId();
        leaveSeat(u);
        List<double[]> s = layout.seats();
        if (s.isEmpty()) {
            return;
        }
        double[] seat = s.get(Math.floorMod(nextSeat++, s.size()));
        Location at = loc(seat[0], seat[1], seat[2]);
        at.setYaw((float) seat[3]);
        p.setGameMode(GameMode.ADVENTURE);
        p.teleport(at.clone().add(0, 0.5, 0));
        ArmorStand stand = world.spawn(at.clone().add(0, -0.15, 0), ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setMarker(true);
            a.setGravity(false);
            a.setInvulnerable(true);
            a.setPersistent(false);
            a.setSmall(true);
            a.addScoreboardTag(PROP_TAG);
        });
        seats.put(u, stand);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (stand.isValid() && p.isOnline() && ghost(u)) {
                stand.addPassenger(p);
            }
        }, 2L);
    }

    /** Aufgestanden oder fertig: Sitz weg. */
    public void leaveSeat(UUID u) {
        ArmorStand a = seats.remove(u);
        if (a != null) {
            a.eject();
            a.remove();
        }
    }

    /** Ausgeschiedene: zwischen Bank und Umherschweben wechseln. */
    public void lookAround(Player p) {
        if (!ghost(p.getUniqueId())) {
            return;
        }
        leaveSeat(p.getUniqueId());
        p.setGameMode(GameMode.SPECTATOR);
        p.sendMessage(Texts.msg(plugin.lang(p), "ghost.look"));
    }

    /** Zurück auf die Bank (/bank). */
    public void backToBench(Player p) {
        if (!ghost(p.getUniqueId())) {
            p.sendMessage(Texts.msg(plugin.lang(p), "ghost.not"));
            return;
        }
        ghostKit(p);
        seat(p);
    }

    private void ghostKit(Player p) {
        String lang = plugin.lang(p);
        p.setGameMode(GameMode.ADVENTURE);
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setItem(0, tool(lang, Material.ENDER_EYE, "look", "item.look"));
        inv.setItem(4, tool(lang, Material.BOOK, "notes", "item.notes"));
        dress(p, people.get(p.getUniqueId()));
    }

    /** Raus aus der Runde (betäubt, festgenommen, blamiert, eingesperrt): auf die Bank, zuschauen, nichts verraten. */
    private void knockOut(UUID u, String why) {
        Player p = Bukkit.getPlayer(u);
        trailUntil.remove(u);
        if (p != null) {
            tellPlaying(p, false);
            String lang = plugin.lang(p);
            Ui.title(p, Texts.text(lang, "out." + why + ".title"), Texts.text(lang, "out." + why + ".sub"), 3500);
            p.sendMessage(Texts.msg(lang, "out." + why + ".chat"));
            p.sendMessage(Texts.msg(lang, "ghost.how"));
            prepare(p, GameMode.ADVENTURE);
            ghostKit(p);
            seat(p);
        }
        visibility();
        plugin.games().ghostsChanged();
        plugin.boards().game(this);
    }

    // ---------- Betäuben, festnehmen, Mütze ----------

    /** Treffer oder Rechtsklick auf jemanden: Schlafpulver (Wilderer) oder Dienstmütze (Gendarm) in der Hand? */
    public void touch(Player by, Player target) {
        if (phase != Phase.PLAY || !awake(by.getUniqueId()) || !awake(target.getUniqueId())) {
            return;
        }
        String tag = Items.helpTag(by.getInventory().getItemInMainHand());
        if (STUN.equals(tag)) {
            stun(by, target);
        } else if ("hat".equals(tag)) {
            arrest(by, target);
        }
    }

    private void stun(Player by, Player target) {
        UUID u = by.getUniqueId();
        String lang = plugin.lang(by);
        int left = cooldowns.secondsLeft(u, STUN, ticks);
        if (left > 0) {
            if (hintOnce(u)) {
                by.sendActionBar(Texts.text(lang, "stun.wait", Placeholder.unparsed("secs", String.valueOf(left))));
            }
            return;
        }
        Round.Result r = round.stun(u, target.getUniqueId());
        if (r.why() == Round.Why.PARTNER) {
            if (hintOnce(u)) {
                by.sendActionBar(Texts.text(lang, "stun.partner"));
            }
            return;
        }
        if (!r.ok()) {
            return;
        }
        cooldowns.use(u, STUN, ticks, (long) settings.stunPause() * SECOND);
        Location at = target.getLocation();
        Suspect victim = people.get(target.getUniqueId());
        if (settings.trailSeconds() > 0) {
            trailUntil.put(u, ticks + (long) settings.trailSeconds() * SECOND);
            lastPrint.remove(u);
        }
        if (settings.feathers()) {
            Outfit o = people.get(u).outfit();
            spawnTrace(at, new Clue(Part.HUT, o.hut(), true, wildererNo.getOrDefault(u, 0)), -1);
        }
        world.spawnParticle(Particle.CLOUD, at.clone().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.02);
        world.playSound(at, "minecraft:entity.player.breath", 1f, 0.6f);
        by.sendActionBar(Texts.text(lang, "stun.done", name("name", victim.name())));
        Ui.sound(by, "minecraft:entity.fox.sniff", 1.2f);
        broadcast(l -> Texts.msg(l, "stun.all", name("name", victim.name()),
                Placeholder.unparsed("n", String.valueOf(round.villageAwake()))));
        if (r.droppedHat() != null) {
            dropHat(r.droppedHat(), at);
            broadcast(l -> Texts.msg(l, "hat.dropped.stun"));
        }
        knockOut(target.getUniqueId(), "stunned");
        check();
    }

    private void arrest(Player by, Player target) {
        UUID u = by.getUniqueId();
        String lang = plugin.lang(by);
        Round.Hat hat = round.hatOf(u);
        if (hat == null) {
            return;
        }
        if (!hat.ready()) {
            if (hintOnce(u)) {
                by.sendActionBar(Texts.text(lang, "arrest.wait", Placeholder.unparsed("secs",
                        String.valueOf(cooldowns.secondsLeft(u, HAT_PAUSE, ticks)))));
            }
            return;
        }
        Location at = by.getLocation();
        Suspect t = people.get(target.getUniqueId());
        Suspect g = people.get(u);
        Round.Result r = round.arrest(u, target.getUniqueId());
        if (!r.ok()) {
            return;
        }
        if (r.right()) {
            cooldowns.use(u, HAT_PAUSE, ticks, (long) settings.arrestPause() * SECOND);
            rearmAt.put(hat.id(), ticks + (long) settings.arrestPause() * SECOND);
            world.playSound(at, "minecraft:entity.villager.celebrate", 1f, 1f);
            broadcast(l -> Texts.msg(l, "arrest.right", name("gendarm", g.name()), name("name", t.name())));
            for (Player p : audience()) {
                Ui.title(p, Texts.text(plugin.lang(p), "arrest.right.title"),
                        Texts.text(plugin.lang(p), "arrest.right.sub", name("name", t.name())), 2500);
            }
            knockOut(t.uuid(), "arrested");
        } else {
            world.playSound(at, "minecraft:entity.villager.no", 1f, 0.8f);
            broadcast(l -> Texts.msg(l, "arrest.wrong", name("gendarm", g.name()), name("name", t.name())));
            Player victim = Bukkit.getPlayer(t.uuid());
            if (victim != null) {
                victim.sendMessage(Texts.msg(plugin.lang(victim), "arrest.wrong.victim"));
            }
            if (r.droppedHat() != null) {
                dropHat(r.droppedHat(), at);
            }
            knockOut(u, "blamed");
        }
        check();
    }

    /** Mütze liegt jetzt hier: zu sehen für alle, mit Schrift darüber. */
    private void dropHat(int id, Location where) {
        removeHat(id);
        Location at = ground(where);
        ItemStack item = hatItem("de");
        ItemDisplay d = world.spawn(at.clone().add(0, 0.35, 0), ItemDisplay.class, e -> {
            e.setItemStack(item);
            e.setPersistent(false);
            e.setGlowing(true);
            e.setGlowColorOverride(Color.fromRGB(0x5AA0FF));
            e.addScoreboardTag(PROP_TAG);
            e.setBillboard(Display.Billboard.FIXED);
            e.setBrightness(new Display.Brightness(15, 15));
        });
        Holo label = Holo.spawn(plugin, at.clone().add(0, 1.1, 0), lang -> Texts.text(lang, "hat.label"), 0.7f);
        hats.put(id, new DroppedHat(at, d, label));
        rearmAt.remove(id);
    }

    private void removeHat(int id) {
        DroppedHat h = hats.remove(id);
        if (h != null) {
            h.display().remove();
            h.label().remove();
        }
    }

    /** Stelle auf dem Boden unter {@code l} (für Mütze und Feder – nicht in der Luft). */
    private Location ground(Location l) {
        Location at = l.clone();
        for (int i = 0; i < 6 && at.getBlock().isPassable() && at.getBlockY() > MapType.FLOOR_Y + layout.voidY(); i++) {
            at.add(0, -1, 0);
        }
        if (!at.getBlock().isPassable()) {
            at.add(0, 1, 0);
        }
        return new Location(world, at.getBlockX() + 0.5, at.getBlockY(), at.getBlockZ() + 0.5);
    }

    // ---------- Spuren ----------

    /** Neue Spur: an einer freien Stelle der Karte (fester Hinweis = Feder vom Tatort). */
    private void spawnTrace(Location at, Clue fixed, int spot) {
        Location g = ground(at);
        String itemName = fixed != null ? "feather" : TRACE_ITEMS[random.nextInt(TRACE_ITEMS.length)];
        Material m = Material.matchMaterial(itemName);
        ItemDisplay d = world.spawn(g.clone().add(0, 0.25, 0), ItemDisplay.class, e -> {
            e.setItemStack(new ItemStack(m == null ? Material.PAPER : m));
            e.setPersistent(false);
            e.setGlowing(true);
            e.setGlowColorOverride(Color.fromRGB(fixed != null ? 0xFFFFFF : 0xF2B632));
            e.addScoreboardTag(PROP_TAG);
            e.setBillboard(Display.Billboard.VERTICAL);
            e.setBrightness(new Display.Brightness(15, 15));
            e.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(0.55f, 0.55f, 0.55f),
                    new AxisAngle4f()));
        });
        traces.add(new Trace(g, d, fixed, spot, ticks));
    }

    /** Eine neue Spur an einer zufälligen Stelle, die gerade frei ist und an der niemand steht. */
    public boolean newTrace() {
        List<int[]> spots = map.clueSpots();
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < spots.size(); i++) {
            int idx = i;
            if (traces.stream().anyMatch(t -> t.spot() == idx)) {
                continue;
            }
            int[] s = spots.get(i);
            Location l = loc(s[0] + 0.5, s[1], s[2] + 0.5);
            boolean someone = audience().stream().anyMatch(p -> awake(p.getUniqueId())
                    && p.getLocation().getWorld() == world && p.getLocation().distance(l) < CLUE_KEEP_AWAY);
            if (!someone) {
                free.add(i);
            }
        }
        if (free.isEmpty()) {
            return false;
        }
        int i = free.get(random.nextInt(free.size()));
        int[] s = spots.get(i);
        spawnTrace(loc(s[0] + 0.5, s[1], s[2] + 0.5), null, i);
        return true;
    }

    /** Verwaltung/Tests: eine Spur genau hier. */
    public void traceAt(Location l) {
        spawnTrace(l, null, -1);
    }

    /** Wer an einer Spur steht, hebt sie auf (Dorf: Hinweis) oder verwischt sie (Wilderer). */
    private void pickUps() {
        for (Suspect s : people.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p == null || !awake(s.uuid()) || p.getWorld() != world) {
                continue;
            }
            Location l = p.getLocation();
            boolean wilderer = round.role(s.uuid()) == Role.WILDERER;
            for (Iterator<Trace> it = traces.iterator(); it.hasNext();) {
                Trace t = it.next();
                // Eine Feder vom Tatort kann der Wilderer nicht verwischen (er steht ja meist direkt daneben).
                if (near(l, t.at()) && !(wilderer && t.fixed() != null)) {
                    it.remove();
                    t.display().remove();
                    found(p, s, t);
                    break;
                }
            }
            for (var e : new ArrayList<>(hats.entrySet())) {
                if (near(l, e.getValue().at()) && round.mayPickUp(s.uuid()) && round.pickUp(s.uuid(), e.getKey())) {
                    removeHat(e.getKey());
                    pickedHat(p, s);
                }
            }
        }
    }

    private static boolean near(Location a, Location b) {
        return a.getWorld() == b.getWorld() && Math.abs(a.getY() - b.getY()) < 2.2
                && Math.hypot(a.getX() - b.getX(), a.getZ() - b.getZ()) < PICKUP;
    }

    private void found(Player p, Suspect s, Trace t) {
        String lang = plugin.lang(p);
        UUID u = s.uuid();
        world.spawnParticle(Particle.HAPPY_VILLAGER, t.at().clone().add(0, 0.5, 0), 12, 0.3, 0.3, 0.3, 0);
        if (round.role(u) == Role.WILDERER) {
            Ui.sound(p, "minecraft:block.grass.break", 0.8f);
            p.sendActionBar(Texts.text(lang, "trace.wiped"));
            return;
        }
        Clue c = t.fixed();
        if (c == null) {
            List<UUID> wilderers = round.awakeWith(Role.WILDERER);
            if (wilderers.isEmpty()) {
                wilderers = new ArrayList<>(wildererNo.keySet());
            }
            UUID target = wilderers.get(random.nextInt(wilderers.size()));
            List<UUID> suspects = round.awake().stream().filter(x -> !x.equals(u)).toList();
            Map<UUID, Outfit> outfits = new HashMap<>();
            people.values().forEach(x -> outfits.put(x.uuid(), x.outfit()));
            c = Clues.next(outfits, target, wildererNo.getOrDefault(target, 0), suspects, s.clues(), random);
        }
        round.clueFound(u);
        Ui.sound(p, "minecraft:item.book.page_turn", 1.2f);
        if (c == null || s.clues().contains(c)) {
            p.sendMessage(Texts.msg(lang, "trace.nothing"));
            Ui.title(p, Texts.text(lang, "trace.title"), Texts.text(lang, "trace.nothing.sub"), 1800);
        } else {
            s.clues().add(c);
            String text = clueText(lang, c);
            p.sendMessage(Texts.msg(lang, t.fixed() != null ? "trace.feather" : "trace.found", name("clue", text)));
            Ui.title(p, Texts.text(lang, t.fixed() != null ? "trace.feather.title" : "trace.title"),
                    Component.text(text, TextColor.color(Texts.GOLD)), 3000);
        }
        plugin.boards().game(this);
    }

    private void pickedHat(Player p, Suspect s) {
        String lang = plugin.lang(p);
        p.getInventory().setItem(0, hatItem(lang));
        p.getInventory().setHeldItemSlot(0);
        Ui.title(p, Texts.text(lang, "hat.picked.title"), Texts.text(lang, "hat.picked.sub"), 3000);
        Ui.sound(p, "minecraft:item.armor.equip_leather", 1f);
        p.sendMessage(Texts.msg(lang, "hat.picked"));
        broadcast(l -> Texts.msg(l, "hat.picked.all"));
        plugin.boards().game(this);
    }

    // ---------- Dorfversammlung ----------

    /** Jemand läutet die Glocke. */
    public void ring(Player p) {
        String lang = plugin.lang(p);
        UUID u = p.getUniqueId();
        if (phase == Phase.MEETING) {
            voteMenu(p);
            return;
        }
        if (phase != Phase.PLAY || !awake(u)) {
            return;
        }
        int elapsed = settings.roundSeconds() - secondsLeft;
        if (!settings.meetings() || settings.meetingMax() <= 0) {
            p.sendMessage(Texts.msg(lang, "meeting.off"));
        } else if (meetings >= settings.meetingMax()) {
            p.sendMessage(Texts.msg(lang, "meeting.none-left"));
        } else if (elapsed < settings.meetingFrom()) {
            p.sendMessage(Texts.msg(lang, "meeting.too-early", Placeholder.unparsed("secs",
                    String.valueOf(settings.meetingFrom() - elapsed))));
        } else {
            startMeeting(p);
        }
    }

    /** Verwaltung/Tests: Versammlung sofort. */
    public void forceMeeting(Player by) {
        if (phase == Phase.PLAY) {
            startMeeting(by);
        }
    }

    private void startMeeting(Player by) {
        phase = Phase.MEETING;
        meetings++;
        meeting = new Meeting();
        caller = by == null ? null : by.getUniqueId();
        meetingTicks = settings.meetingSeconds() * SECOND;
        resultIn = -1;
        meetingStart = ticks;
        beforeMeeting.clear();
        List<Suspect> awakeOnes = people.values().stream().filter(s -> s.active() && round.awake(s.uuid())).toList();
        int n = awakeOnes.size();
        int i = 0;
        String callerName = by == null ? "?" : by.getName();
        for (Suspect s : awakeOnes) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p == null) {
                continue;
            }
            beforeMeeting.put(s.uuid(), p.getLocation());
            double[] spot = layout.meetingSpot(i++, n);
            Location at = loc(spot[0], 1, spot[1]);
            at.setYaw((float) spot[2]);
            p.teleport(at);
            p.setInvulnerable(true);
            p.getInventory().setItem(7, tool(plugin.lang(p), Material.NAME_TAG, "vote", "item.ballot"));
            String lang = plugin.lang(p);
            Ui.title(p, Texts.text(lang, "meeting.title"), Texts.text(lang, "meeting.sub", name("name", callerName)), 2500);
            Ui.sound(p, "minecraft:block.bell.use", 0.7f);
        }
        broadcast(l -> Texts.msg(l, "meeting.called", name("name", callerName),
                Placeholder.unparsed("secs", String.valueOf(settings.meetingSeconds()))));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Suspect s : awakeOnes) {
                Player p = Bukkit.getPlayer(s.uuid());
                if (p != null && phase == Phase.MEETING && resultIn < 0) {
                    voteMenu(p);
                }
            }
        }, 15L);
        updateBars();
        plugin.boards().game(this);
    }

    /** Stimmzettel: alle Wachen (außer man selbst) und „abwarten“. */
    public void voteMenu(Player p) {
        if (phase != Phase.MEETING || meeting == null || !awake(p.getUniqueId()) || resultIn >= 0) {
            return;
        }
        Menus.vote(plugin, this, p);
    }

    /** Stimme abgeben (aus dem Menü). */
    public void vote(Player p, UUID target) {
        UUID u = p.getUniqueId();
        if (phase != Phase.MEETING || meeting == null || !awake(u) || resultIn >= 0) {
            return;
        }
        if (!Meeting.SKIP.equals(target) && !awake(target)) {
            return;
        }
        boolean first = meeting.voteOf(u) == null;
        meeting.vote(u, target);
        String lang = plugin.lang(p);
        p.sendMessage(Meeting.SKIP.equals(target) ? Texts.msg(lang, "meeting.voted.skip")
                : Texts.msg(lang, "meeting.voted", name("name", people.get(target).name())));
        Ui.sound(p, "minecraft:item.book.page_turn", 1f);
        if (first) {
            broadcast(l -> Texts.msg(l, "meeting.someone", name("name", p.getName()),
                    Placeholder.unparsed("n", String.valueOf(meeting.voted())),
                    Placeholder.unparsed("of", String.valueOf(voters().size()))));
        }
        if (voters().stream().allMatch(v -> meeting.voteOf(v) != null)) {
            resultIn = MEETING_RESULT_DELAY;
        }
        plugin.boards().game(this);
    }

    public Meeting meeting() {
        return meeting;
    }

    /** Wer abstimmen darf: wach und verbunden. */
    public List<UUID> voters() {
        return people.values().stream().filter(s -> s.active() && round.awake(s.uuid())).map(Suspect::uuid).toList();
    }

    private void tickMeeting() {
        if (resultIn < 0) {
            meetingTicks--;
            if (ticks % STEP == 0) {
                holdInRing();
            }
            if (meetingTicks <= 0) {
                resultIn = MEETING_RESULT_DELAY;
            }
        }
        if (resultIn >= 0 && resultIn-- == MEETING_RESULT_DELAY) {
            for (Player p : audience()) {
                p.closeInventory();
            }
            soundAll("minecraft:block.bell.resonate", 1f);
        }
        if (resultIn == 0) {
            endMeeting();
        }
        if (ticks % SECOND == 0) {
            updateBars();
            plugin.boards().game(this);
        }
    }

    private void holdInRing() {
        for (var e : beforeMeeting.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p == null) {
                continue;
            }
            double dx = relX(p.getLocation()) - (layout.meeting()[0] + 0.5);
            double dz = relZ(p.getLocation()) - (layout.meeting()[1] + 0.5);
            if (Math.hypot(dx, dz) > layout.meetingRadius() + 1.5) {
                double a = Math.atan2(dz, dx);
                Location back = loc(layout.meeting()[0] + 0.5 + Math.cos(a) * layout.meetingRadius(), 1,
                        layout.meeting()[1] + 0.5 + Math.sin(a) * layout.meetingRadius());
                back.setYaw(p.getLocation().getYaw());
                back.setPitch(p.getLocation().getPitch());
                p.teleport(back);
            }
        }
    }

    private void endMeeting() {
        // Während der Versammlung stand die Uhr: Wartezeiten um genau so lange verschieben.
        long took = ticks - meetingStart;
        cooldowns.delay(took);
        rearmAt.replaceAll((k, v) -> v + took);
        trailUntil.replaceAll((k, v) -> v + took);
        List<UUID> voters = voters();
        Map<UUID, Integer> tally = meeting.tally(voters);
        var result = meeting.result(voters);
        String board = tally.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue())
                .map(e -> (Meeting.SKIP.equals(e.getKey()) ? "–" : people.get(e.getKey()).name()) + " " + e.getValue())
                .collect(Collectors.joining(" · "));
        broadcast(l -> Texts.msg(l, "meeting.tally", Placeholder.unparsed("list", board.replace("–",
                Texts.plain(l, "meeting.skip")))));
        Integer dropped = null;
        Location chosenAt = null;
        if (result.isPresent()) {
            UUID chosen = result.get();
            Suspect c = people.get(chosen);
            Player cp = Bukkit.getPlayer(chosen);
            chosenAt = cp != null ? cp.getLocation() : null;
            List<UUID> forHim = meeting.votersFor(chosen, voters);
            Round.Result r = round.vote(chosen, forHim, settings.pech());
            dropped = r.droppedHat();
            if (r.right()) {
                broadcast(l -> Texts.msg(l, "meeting.right", name("name", c.name())));
                for (UUID v : forHim) {
                    Player vp = Bukkit.getPlayer(v);
                    if (vp != null && !v.equals(chosen)) {
                        vp.sendMessage(Texts.msg(plugin.lang(vp), "meeting.right.you"));
                    }
                }
                knockOut(chosen, "voted");
            } else {
                broadcast(l -> Texts.msg(l, "meeting.wrong", name("name", c.name())));
                for (UUID v : forHim) {
                    Player vp = Bukkit.getPlayer(v);
                    if (vp != null && !v.equals(chosen) && settings.pech() > 0) {
                        vp.sendMessage(Texts.msg(plugin.lang(vp), "meeting.wrong.you",
                                Placeholder.unparsed("n", String.valueOf(settings.pech()))));
                    }
                }
                knockOut(chosen, "jailed");
            }
        } else {
            broadcast(l -> Texts.msg(l, "meeting.nobody"));
        }
        // zurück, wo jeder war; kurz Schonzeit
        for (var e : beforeMeeting.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p == null) {
                continue;
            }
            p.setInvulnerable(false);
            p.getInventory().clear(7);
            if (awake(e.getKey())) {
                p.teleport(e.getValue());
            }
            if (round.role(e.getKey()) == Role.WILDERER && cooldowns.left(e.getKey(), STUN, ticks) < MEETING_SAFE_SECONDS
                    * SECOND) {
                cooldowns.use(e.getKey(), STUN, ticks, (long) MEETING_SAFE_SECONDS * SECOND);
            }
        }
        if (dropped != null) {
            dropHat(dropped, chosenAt != null ? chosenAt : loc(layout.meeting()[0] + 0.5, 1, layout.meeting()[1] + 0.5));
            broadcast(l -> Texts.msg(l, "hat.dropped.meeting"));
        }
        beforeMeeting.clear();
        meeting = null;
        phase = Phase.PLAY;
        updateBars();
        plugin.boards().game(this);
        check();
    }

    // ---------- Verbindung weg und wieder da ----------

    /** Wiederkommen nach einem Verbindungsabbruch: rechtzeitig → gleiche Rolle, gleicher Ort; zu spät → raus. */
    public void rejoin(Player p) {
        UUID u = p.getUniqueId();
        Suspect s = people.get(u);
        if (s != null && comeback.waitingFor(u)) {
            if (comeback.back(u, ticks) && phase != Phase.OVER && !s.gone()) {
                comeBack(p, s);
                return;
            }
            tooLate(u);
            plugin.hall().enter(p);
            p.sendMessage(Texts.msg(plugin.lang(p), "game.too-late.self"));
            return;
        }
        if (s != null && !s.gone() && phase != Phase.OVER) {
            s.online(true);
            if (!round.awake(u)) {
                // ausgeschieden: wieder auf die Bank
                showBar(p);
                p.setPlayerTime(layout.time(), false);
                names();
                ghostKit(p);
                seat(p);
                plugin.boards().game(this);
                return;
            }
        }
        plugin.hall().enter(p);
    }

    /** Verbindung weg: Platz bleibt eine Weile frei (wer wach war); Ausgeschiedene sind einfach weg. */
    public void quit(Player p) {
        UUID u = p.getUniqueId();
        Suspect s = people.get(u);
        spectators.remove(u);
        told.remove(u);
        leaveSeat(u);
        hideBar(p);
        if (s == null || s.gone()) {
            return;
        }
        s.last(p.getLocation());
        s.online(false);
        if (phase == Phase.OVER || !round.awake(u)) {
            return;
        }
        int seconds = settings.comebackSeconds();
        if (Comeback.holds(seconds, true)) {
            comeback.away(u, ticks, seconds);
            plugin.net().holdSlot(u, seconds);
            broadcast(lang -> Texts.msg(lang, "game.away", name("name", s.name()),
                    Placeholder.unparsed("secs", String.valueOf(seconds))));
        } else {
            out(u, false);
        }
        plugin.boards().game(this);
    }

    private void comeBack(Player p, Suspect s) {
        UUID u = s.uuid();
        s.online(true);
        plugin.net().holdSlot(u, 0);
        tellPlaying(p, true);
        showBar(p);
        p.setPlayerTime(layout.time(), false);
        String lang = plugin.lang(p);
        p.setGameMode(GameMode.ADVENTURE);
        if (phase == Phase.START) {
            p.teleport(spawnSpot(s.index(), people.size()));
        }
        if (phase != Phase.START) {
            kit(p);
        } else {
            dress(p, s);
        }
        if (phase == Phase.MEETING) {
            beforeMeeting.put(u, p.getLocation());
            Location at = loc(layout.meeting()[0] + 0.5 + layout.meetingRadius(), 1, layout.meeting()[1] + 0.5);
            p.teleport(at);
            p.getInventory().setItem(7, tool(lang, Material.NAME_TAG, "vote", "item.ballot"));
        }
        names();
        Ui.title(p, Texts.text(lang, "game.back.title"), Texts.text(lang, "game.back.sub"), 2500);
        Ui.sound(p, "minecraft:entity.player.levelup", 1.2f);
        p.sendMessage(Texts.msg(lang, "game.back"));
        tellRole(p, false);
        for (Player o : audience()) {
            if (!o.equals(p)) {
                o.sendMessage(Texts.msg(plugin.lang(o), "game.back.all", name("name", s.name())));
            }
        }
        plugin.boards().game(this);
    }

    private void tooLate(UUID u) {
        plugin.net().holdSlot(u, 0);
        Suspect s = people.get(u);
        if (s == null || s.gone()) {
            return;
        }
        s.late(true);
        if (phase != Phase.OVER) {
            broadcast(lang -> Texts.msg(lang, "game.too-late", name("name", s.name())));
        }
        out(u, true);
    }

    /** Endgültig raus (zu spät oder selbst gegangen): Mütze bleibt liegen, wo man zuletzt stand. */
    private void out(UUID u, boolean late) {
        Suspect s = people.get(u);
        s.gone(true);
        s.online(false);
        boolean wasWilderer = round.role(u) == Role.WILDERER && round.awake(u);
        Integer hat = round.leave(u);
        if (hat != null && phase != Phase.OVER) {
            Location at = s.last() != null ? s.last() : loc(layout.square()[0] + 0.5, 1, layout.square()[1] + 0.5);
            dropHat(hat, at);
            broadcast(l -> Texts.msg(l, "hat.dropped.gone"));
        }
        if (wasWilderer && phase != Phase.OVER) {
            broadcast(l -> Texts.msg(l, "game.wilderer-gone"));
        }
        plugin.games().ghostsChanged();
        check();
        plugin.boards().game(this);
    }

    /** Freiwillig gehen (/verlassen). */
    public void leave(Player p) {
        UUID u = p.getUniqueId();
        Suspect s = people.get(u);
        spectators.remove(u);
        leaveSeat(u);
        hideBar(p);
        tellPlaying(p, false);
        p.showPlayer(plugin, p);
        for (Player o : audience()) {
            o.showPlayer(plugin, p);
            p.showPlayer(plugin, o);
        }
        if (s == null || s.gone()) {
            return;
        }
        s.last(p.getLocation());
        if (comeback.waitingFor(u)) {
            comeback.back(u, ticks);
            plugin.net().holdSlot(u, 0);
        }
        if (phase != Phase.OVER) {
            broadcast(lang -> Texts.msg(lang, "game.left", name("name", s.name())));
            out(u, false);
        } else {
            s.gone(true);
            s.online(false);
        }
    }

    /** Zuschauen (aus der Dorflinde). */
    public void spectate(Player p) {
        if (people.get(p.getUniqueId()) == null) {
            spectators.add(p.getUniqueId());
        }
        prepare(p, GameMode.SPECTATOR);
        p.teleport(spectatorSpot());
        p.setPlayerTime(layout.time(), false);
        showBar(p);
        names();
        plugin.games().ghostsChanged();
        plugin.boards().game(this);
        String lang = plugin.lang(p);
        p.sendMessage(Texts.msg(lang, "spectate.start", Placeholder.unparsed("map", Texts.plain(lang, "map." + map.id()))));
    }

    private void tellPlaying(Player p, boolean playing) {
        if (playing ? told.add(p.getUniqueId()) : told.remove(p.getUniqueId())) {
            plugin.net().playing(p, playing);
        }
    }

    private void releaseAll() {
        for (UUID u : comeback.clear()) {
            plugin.net().holdSlot(u, 0);
        }
        for (UUID u : new ArrayList<>(told)) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                tellPlaying(p, false);
            }
        }
        told.clear();
    }

    // ---------- Ablauf ----------

    /** Jeder Tick. false = Runde ist vorbei und kann weg. */
    public boolean tick() {
        ticks++;
        if (ticks % SECOND == 0 && phase != Phase.OVER && comeback.count() > 0) {
            for (UUID u : comeback.expired(ticks)) {
                tooLate(u);
            }
        }
        switch (phase) {
            case START -> tickStart();
            case PLAY -> tickPlay();
            case MEETING -> tickMeeting();
            case OVER -> {
                if (ticks % 10 == 0) {
                    ambience();
                }
                if (--phaseTicks <= 0) {
                    close();
                    return false;
                }
            }
        }
        return true;
    }

    private void tickStart() {
        phaseTicks--;
        if (ticks % STEP == 0) {
            for (Suspect s : people.values()) {
                Player p = Bukkit.getPlayer(s.uuid());
                if (p == null || !s.active()) {
                    continue;
                }
                Location spot = spawnSpot(s.index(), people.size());
                Location l = p.getLocation();
                if (l.getWorld() != world || Math.hypot(l.getX() - spot.getX(), l.getZ() - spot.getZ()) > 0.9) {
                    spot.setYaw(l.getYaw());
                    spot.setPitch(l.getPitch());
                    p.teleport(spot);
                }
            }
        }
        if (phaseTicks % SECOND == 0) {
            int secs = phaseTicks / SECOND;
            if (secs > 0 && secs <= COUNTDOWN) {
                for (Player p : audience()) {
                    String lang = plugin.lang(p);
                    Ui.title(p, Texts.text(lang, "game.count", Placeholder.unparsed("n", String.valueOf(secs))),
                            Texts.text(lang, "game.count.sub"), 900);
                    Ui.sound(p, "minecraft:block.note_block.hat", 1.0f + (COUNTDOWN - secs) * 0.15f);
                }
            }
            updateBars();
            plugin.boards().game(this);
        }
        if (phaseTicks <= 0) {
            begin();
        }
    }

    private void begin() {
        phase = Phase.PLAY;
        clueTimer = Math.min(settings.clueEvery(), 5);
        for (Suspect s : people.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p == null || !s.active()) {
                continue;
            }
            kit(p);
            if (round.role(s.uuid()) == Role.WILDERER) {
                cooldowns.use(s.uuid(), STUN, ticks, (long) settings.graceSeconds() * SECOND);
            }
            String lang = plugin.lang(p);
            Ui.title(p, Texts.text(lang, "game.go"), Texts.text(lang, "game.go.sub"), 1800);
            Ui.sound(p, "minecraft:block.bell.use", 1.0f);
        }
        broadcast(lang -> Texts.msg(lang, "game.started"));
        updateBars();
        plugin.stateChanged();
    }

    private void tickPlay() {
        if (awakeOnline() == 0) {
            paused = true;
            if (ticks % SECOND == 0) {
                updateBars();
            }
            return;
        }
        paused = false;
        if (ticks % STEP == 0) {
            pickUps();
            footprints();
            falls();
        }
        if (phase == Phase.PLAY && ticks % SECOND == 0) {
            second();
        }
    }

    private void second() {
        secondsLeft--;
        rearmAt.entrySet().removeIf(e -> {
            if (ticks >= e.getValue()) {
                round.rearm(e.getKey());
                Round.Hat h = round.hats().get(e.getKey());
                Player p = h.holder() == null ? null : Bukkit.getPlayer(h.holder());
                if (p != null) {
                    p.sendActionBar(Texts.text(plugin.lang(p), "arrest.ready"));
                    Ui.sound(p, "minecraft:block.note_block.pling", 1.5f);
                }
                return true;
            }
            return false;
        });
        if (--clueTimer <= 0) {
            clueTimer = settings.clueEvery();
            if (traces.stream().filter(t -> t.fixed() == null).count() < settings.clueMax()) {
                newTrace();
            }
        }
        if (secondsLeft == WARN_MINUTE) {
            broadcast(lang -> Texts.msg(lang, "time.minute"));
            soundAll("minecraft:block.note_block.bell", 0.8f);
        } else if (secondsLeft > 0 && secondsLeft <= WARN_END) {
            for (Player p : audience()) {
                String lang = plugin.lang(p);
                Ui.title(p, Component.empty(), Texts.text(lang, "time.left", Placeholder.unparsed("n",
                        String.valueOf(secondsLeft))), 900);
                Ui.sound(p, "minecraft:block.note_block.hat", 1.4f);
            }
        }
        actionBars();
        ambience();
        for (Trace t : traces) {
            world.spawnParticle(t.fixed() != null ? Particle.WHITE_ASH : Particle.END_ROD, t.at().clone().add(0, 0.6, 0), 3,
                    0.15, 0.2, 0.15, 0.01);
        }
        for (DroppedHat h : hats.values()) {
            world.spawnParticle(Particle.ENCHANT, h.at().clone().add(0, 0.8, 0), 8, 0.3, 0.3, 0.3, 0.2);
        }
        for (UUID u : spectators) {
            keepNear(Bukkit.getPlayer(u));
        }
        for (Suspect s : people.values()) {
            if (ghost(s.uuid())) {
                keepNear(Bukkit.getPlayer(s.uuid()));
            }
        }
        updateBars();
        plugin.boards().game(this);
        if (secondsLeft <= 0) {
            end(round.check(true));
        }
    }

    /** Was in der Aktionsleiste steht: Schlafpulver bzw. Festnahme bereit, Spuren in der Nähe. */
    private void actionBars() {
        for (Suspect s : people.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p == null || !awake(s.uuid())) {
                continue;
            }
            String lang = plugin.lang(p);
            UUID u = s.uuid();
            String held = Items.helpTag(p.getInventory().getItemInMainHand());
            if (round.role(u) == Role.WILDERER && STUN.equals(held)) {
                int left = cooldowns.secondsLeft(u, STUN, ticks);
                p.sendActionBar(left > 0 ? Texts.text(lang, "stun.wait", Placeholder.unparsed("secs", String.valueOf(left)))
                        : Texts.text(lang, "stun.ready"));
            } else if ("hat".equals(held) && round.hatOf(u) != null) {
                Round.Hat h = round.hatOf(u);
                p.sendActionBar(h.ready() ? Texts.text(lang, "arrest.ready")
                        : Texts.text(lang, "arrest.wait", Placeholder.unparsed("secs",
                                String.valueOf(cooldowns.secondsLeft(u, HAT_PAUSE, ticks)))));
            }
        }
    }

    /** Nebel um die Leute (nur auf Nebelkarten) – nur für den, der drinsteht. */
    private void ambience() {
        if (!layout.fog()) {
            return;
        }
        Particle.DustOptions mist = new Particle.DustOptions(Color.fromRGB(0xE8ECEF), 3.5f);
        for (Player p : audience()) {
            Location l = p.getLocation();
            for (int i = 0; i < 26; i++) {
                double a = random.nextDouble() * Math.PI * 2;
                double d = 3 + random.nextDouble() * 14;
                p.spawnParticle(Particle.DUST, l.getX() + Math.cos(a) * d, l.getY() + random.nextDouble() * 3,
                        l.getZ() + Math.sin(a) * d, 1, 0.6, 0.3, 0.6, 0, mist);
            }
        }
    }

    /** Leuchtende Fußspuren eines Wilderers kurz nach einer Betäubung – alle sehen sie. */
    private void footprints() {
        Particle.DustOptions glow = new Particle.DustOptions(Color.fromRGB(0xC6F25A), 1.1f);
        for (var e : trailUntil.entrySet()) {
            if (ticks > e.getValue()) {
                continue;
            }
            Player p = Bukkit.getPlayer(e.getKey());
            if (p == null || !awake(e.getKey())) {
                continue;
            }
            Location l = p.getLocation();
            Location before = lastPrint.get(e.getKey());
            if (before == null || before.getWorld() != l.getWorld() || before.distance(l) > 0.9) {
                prints.add(new Print(ground(l), ticks + (long) settings.trailSeconds() * SECOND));
                lastPrint.put(e.getKey(), l);
            }
        }
        trailUntil.entrySet().removeIf(e -> ticks > e.getValue());
        while (!prints.isEmpty() && prints.peekFirst().until() < ticks) {
            prints.pollFirst();
        }
        for (Print pr : prints) {
            world.spawnParticle(Particle.DUST, pr.at().clone().add(0, 0.08, 0), 2, 0.12, 0, 0.12, 0, glow);
        }
    }

    /** Wer hinunterfällt, kommt zurück auf den Dorfplatz. */
    private void falls() {
        for (Suspect s : people.values()) {
            Player p = Bukkit.getPlayer(s.uuid());
            if (p != null && s.active() && round.awake(s.uuid()) && p.getWorld() == world
                    && p.getLocation().getY() < MapType.FLOOR_Y + layout.voidY()) {
                p.setFallDistance(0);
                p.teleport(spawnSpot(s.index(), people.size()));
            }
        }
    }

    private void keepNear(Player p) {
        if (p == null || p.getGameMode() != GameMode.SPECTATOR) {
            return;
        }
        Location l = p.getLocation();
        if (l.getWorld() != world || Math.hypot(relX(l), relZ(l)) > SPECTATOR_RANGE
                || l.getY() < MapType.FLOOR_Y + layout.voidY() - 20) {
            p.teleport(spectatorSpot());
        }
    }

    /** Ist die Runde vorbei? */
    private void check() {
        if (phase == Phase.OVER) {
            return;
        }
        Outcome o = round.check(false);
        if (o != null) {
            end(o);
        }
    }

    /** Brotzeit: kurz schneller (alle Rollen gleich – so verrät die Brotzeit niemanden). */
    public void snack(Player p) {
        UUID u = p.getUniqueId();
        if (phase != Phase.PLAY || !awake(u)) {
            return;
        }
        String lang = plugin.lang(p);
        int left = cooldowns.secondsLeft(u, SNACK, ticks);
        if (left > 0) {
            if (hintOnce(u)) {
                p.sendActionBar(Texts.text(lang, "snack.wait", Placeholder.unparsed("secs", String.valueOf(left))));
            }
            return;
        }
        cooldowns.use(u, SNACK, ticks, (long) SNACK_PAUSE * SECOND);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SNACK_SECONDS * SECOND, 1, false, false, true));
        Ui.sound(p, "minecraft:entity.generic.eat", 1f);
        p.sendActionBar(Texts.text(lang, "snack.done"));
    }

    /** Rollenkarte: noch einmal zeigen, wer man ist. */
    public void showRole(Player p) {
        if (people.containsKey(p.getUniqueId()) && phase != Phase.OVER) {
            tellRole(p, false);
        }
    }

    /** Vorspulen (Verwaltung, Tests): {@code -1} = gleich losgehen, sonst die restliche Zeit setzen. */
    public void skipTo(int second) {
        if (phase == Phase.START) {
            phaseTicks = 1;
            return;
        }
        if (phase == Phase.MEETING && second < 0) {
            meetingTicks = 1;
            return;
        }
        if (second >= 0) {
            secondsLeft = Math.max(1, second);
        }
    }

    // ---------- Ende ----------

    private void end(Outcome result) {
        if (phase == Phase.OVER || result == null) {
            return;
        }
        if (phase == Phase.MEETING) {
            for (var e : beforeMeeting.entrySet()) {
                Player p = Bukkit.getPlayer(e.getKey());
                if (p != null) {
                    p.setInvulnerable(false);
                    p.closeInventory();
                }
            }
        }
        phase = Phase.OVER;
        outcome = result;
        phaseTicks = settings.afterSeconds() * SECOND;
        releaseAll();
        round.finish(result);
        traces.forEach(t -> t.display().remove());
        traces.clear();
        for (int id : new ArrayList<>(hats.keySet())) {
            removeHat(id);
        }
        report();
        finale();
        updateBars();
        plugin.boards().game(this);
        plugin.stateChanged();
    }

    /** Auflösung: alle auf den Dorfplatz, Rollen werden verraten, Feuerwerk. */
    private void finale() {
        showAll();
        List<Player> all = audience();
        int n = Math.max(1, all.size());
        String wilderers = wildererNo.keySet().stream().map(u -> people.get(u).name()).collect(Collectors.joining(", "));
        for (int i = 0; i < all.size(); i++) {
            Player p = all.get(i);
            leaveSeat(p.getUniqueId());
            Suspect s = people.get(p.getUniqueId());
            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().clear();
            if (s != null) {
                dress(p, s);
            }
            p.teleport(spawnSpot(i, n));
            p.setInvulnerable(true);
            plugin.boards().sidebar().hideNames(p, List.of());
        }
        for (Player p : all) {
            String lang = plugin.lang(p);
            Suspect s = people.get(p.getUniqueId());
            boolean won = s != null && outcome.winner(round.role(s.uuid()));
            Ui.title(p, Texts.text(lang, outcome.villageWins() ? "end.village.title" : "end.wilderer.title"),
                    Texts.text(lang, "end.sub." + outcome.key()), 4500);
            Ui.sound(p, won ? "minecraft:ui.toast.challenge_complete" : "minecraft:entity.fox.ambient", 1f);
            p.sendMessage(Texts.msg(lang, "end." + outcome.key()));
            p.sendMessage(Texts.msg(lang, "end.reveal", name("names", wilderers)));
            for (UUID w : wildererNo.keySet()) {
                p.sendMessage(Texts.msg(lang, "end.reveal.outfit", name("name", people.get(w).name()),
                        name("outfit", outfitText(lang, people.get(w).outfit()))));
            }
            String gendarms = people.values().stream().filter(x -> round.wasGendarm(x.uuid())).map(Suspect::name)
                    .collect(Collectors.joining(", "));
            if (!gendarms.isEmpty()) {
                p.sendMessage(Texts.msg(lang, "end.gendarms", name("names", gendarms)));
            }
            if (s != null) {
                p.sendMessage(Texts.msg(lang, won ? "end.mine.won" : "end.mine.lost",
                        Placeholder.component("role", roleName(lang, round.role(s.uuid()))),
                        Placeholder.unparsed("points", String.valueOf(round.points(s.uuid()))),
                        Placeholder.unparsed("clues", String.valueOf(round.clues(s.uuid())))));
            }
        }
        Location center = loc(layout.square()[0] + 0.5, 1, layout.square()[1] + 0.5);
        int colour = outcome.villageWins() ? Role.DORF.rgb() : Role.WILDERER.rgb();
        for (int i = 0; i < 3; i++) {
            Location l = center.clone().add(random.nextInt(9) - 4, 1, random.nextInt(9) - 4);
            Bukkit.getScheduler().runTaskLater(plugin, () -> fireworks(l, colour), 10 + i * 14L);
        }
    }

    private void fireworks(Location at, int rgb) {
        if (!plugin.isEnabled()) {
            return;
        }
        world.spawn(at, Firework.class, fw -> {
            fw.addScoreboardTag(FIREWORK_TAG);
            var meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.fromRGB(rgb))
                    .withFade(Color.WHITE).trail(true).build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        });
    }

    /** Statistik: lokal, Lose für die nächste Verteilung, ans Netzwerk. */
    private void report() {
        for (Suspect s : people.values()) {
            UUID u = s.uuid();
            Role role = round.role(u);
            boolean won = outcome.winner(role);
            boolean gendarm = round.wasGendarm(u);
            PlayerData d = plugin.store().get(u, s.name());
            boolean best = d.add(new PlayerData.Round(role, won, gendarm, round.arrests(u), round.stuns(u), round.clues(u),
                    round.points(u)));
            d.lots(d.lots().after(role));
            plugin.store().save(d);
            plugin.board().update(u, d.name(), d.wins(), d.arrests(), d.points(), d.games());
            Map<String, Long> add = new LinkedHashMap<>();
            add.put("games", 1L);
            add.put("wins", won ? 1L : 0L);
            add.put("wins_wilderer", won && role == Role.WILDERER ? 1L : 0L);
            add.put("wins_gendarm", won && gendarm ? 1L : 0L);
            add.put("arrests", (long) round.arrests(u));
            add.put("stuns", (long) round.stuns(u));
            add.put("clues", (long) round.clues(u));
            add.put("points", (long) Math.max(0, round.points(u)));
            plugin.net().stats(u, add, Map.of("best_points", (long) Math.max(0, round.points(u))));
            Player p = Bukkit.getPlayer(u);
            if (p != null && best && round.points(u) > 0 && s.active()) {
                p.sendMessage(Texts.msg(plugin.lang(p), "end.best", Placeholder.unparsed("n",
                        String.valueOf(round.points(u)))));
            }
        }
        plugin.saveBoard();
    }

    /** Runde abbrechen oder fertig: alle zurück zur Dorflinde, Karte wiederherstellen. */
    public void close() {
        phase = Phase.OVER;
        releaseAll();
        showAll();
        for (Player p : audience()) {
            hideBar(p);
            p.setInvulnerable(false);
            plugin.hall().enter(p);
        }
        for (Suspect s : people.values()) {
            plugin.store().unpin(s.uuid(), Bukkit.getPlayer(s.uuid()) != null);
            leaveSeat(s.uuid());
        }
        spectators.clear();
        traces.forEach(t -> t.display().remove());
        traces.clear();
        for (int id : new ArrayList<>(hats.keySet())) {
            removeHat(id);
        }
        seats.values().forEach(Entity::remove);
        seats.clear();
        props.forEach(Entity::remove);
        props.clear();
        bars.clear();
        plugin.games().ghostsChanged();
        if (plugin.isEnabled()) {
            plugin.construction().restore(map, () -> plugin.boards().refreshHall());
        }
    }

    // ---------- Leiste oben ----------

    private void showBar(Player p) {
        BossBar bar = bars.computeIfAbsent(p.getUniqueId(), u -> BossBar.bossBar(Component.empty(), 1f, BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS));
        p.showBossBar(bar);
    }

    private void hideBar(Player p) {
        BossBar bar = bars.remove(p.getUniqueId());
        if (bar != null) {
            p.hideBossBar(bar);
        }
    }

    private void updateBars() {
        for (Player p : audience()) {
            BossBar bar = bars.get(p.getUniqueId());
            if (bar == null) {
                showBar(p);
                bar = bars.get(p.getUniqueId());
            }
            String lang = plugin.lang(p);
            float progress = secondsLeft / (float) Math.max(1, settings.roundSeconds());
            Component name;
            BossBar.Color colour = BossBar.Color.GREEN;
            TagResolver time = Placeholder.unparsed("time", Ui.clock(secondsLeft));
            TagResolver awake = Placeholder.unparsed("n", String.valueOf(round.villageAwake()));
            if (phase == Phase.START) {
                int secs = Math.max(0, (phaseTicks + SECOND - 1) / SECOND);
                name = Texts.text(lang, "bar.start", Placeholder.unparsed("secs", String.valueOf(secs)));
                colour = BossBar.Color.YELLOW;
                progress = 1f;
            } else if (phase == Phase.MEETING) {
                int secs = Math.max(0, (meetingTicks + SECOND - 1) / SECOND);
                name = Texts.text(lang, resultIn >= 0 ? "bar.meeting.count" : "bar.meeting",
                        Placeholder.unparsed("secs", String.valueOf(secs)),
                        Placeholder.unparsed("n", String.valueOf(meeting == null ? 0 : meeting.voted())),
                        Placeholder.unparsed("of", String.valueOf(voters().size())));
                colour = BossBar.Color.PURPLE;
                progress = meetingTicks / (float) Math.max(1, settings.meetingSeconds() * SECOND);
            } else if (phase == Phase.OVER) {
                name = Texts.text(lang, outcome != null && outcome.villageWins() ? "bar.over.village" : "bar.over.wilderer");
                colour = BossBar.Color.PINK;
                progress = 1f;
            } else if (paused) {
                name = Texts.text(lang, "bar.paused");
                colour = BossBar.Color.WHITE;
            } else if (ghost(p.getUniqueId())) {
                name = Texts.text(lang, "bar.ghost", time, awake);
                colour = BossBar.Color.WHITE;
            } else {
                name = Texts.text(lang, "bar.time", time, awake);
                if (secondsLeft <= WARN_MINUTE) {
                    colour = BossBar.Color.RED;
                }
            }
            bar.name(name).progress(Math.max(0f, Math.min(1f, progress))).color(colour);
        }
    }

    /** Für /krimiadmin status (Tests, Verwaltung). */
    public String status() {
        StringBuilder sb = new StringBuilder();
        int[] b = layout.bell();
        sb.append(String.format(Locale.ROOT, "karte=%s phase=%s zeit=%d angehalten=%s versammlungen=%d spuren=%d "
                + "glocke=%d:%d:%d wach=%d", map.id(), phase, secondsLeft, paused, meetings, traces.size(),
                map.originX() + b[0], MapType.FLOOR_Y + b[1], map.originZ() + b[2], round.villageAwake()));
        if (outcome != null) {
            sb.append(" ausgang=").append(outcome.villageWins() ? "dorf" : "wilderer").append(':').append(outcome.key());
        }
        for (int i = 0; i < traces.size(); i++) {
            Location l = traces.get(i).at();
            sb.append(String.format(Locale.ROOT, " spur%d=%.1f:%.1f:%.1f:%s", i, l.getX(), l.getY(), l.getZ(),
                    traces.get(i).fixed() != null ? "feder" : "spur"));
        }
        for (Round.Hat h : round.hats()) {
            DroppedHat d = hats.get(h.id());
            if (d != null) {
                sb.append(String.format(Locale.ROOT, " muetze%d=liegt:%.1f:%.1f:%.1f", h.id(), d.at().getX(), d.at().getY(),
                        d.at().getZ()));
            } else {
                sb.append(String.format(Locale.ROOT, " muetze%d=%s:%s", h.id(),
                        h.holder() == null ? "-" : people.get(h.holder()).name(), h.ready() ? "bereit" : "pause"));
            }
        }
        for (Suspect s : people.values()) {
            UUID u = s.uuid();
            String state = s.gone() ? "weg" : s.online() ? "da" : "fort";
            Outfit o = s.outfit();
            String votedFor = meeting == null || meeting.voteOf(u) == null ? "-"
                    : Meeting.SKIP.equals(meeting.voteOf(u)) ? "abwarten" : people.get(meeting.voteOf(u)).name();
            sb.append(String.format(Locale.ROOT, " %s=%s:%s:%s:p%d:s%d:a%d:c%d:w%d:%s-%s-%s:m%d:k%d:v%s:b%d", s.name(),
                    round.role(u).id(), round.status(u).name().toLowerCase(Locale.ROOT), state, round.points(u),
                    round.stuns(u), round.arrests(u), round.clues(u), comeback.secondsLeft(u, ticks), o.hut().id(),
                    o.joppe().id(), o.hose().id(), round.hatOf(u) != null ? 1 : 0, s.clues().size(), votedFor,
                    seats.containsKey(u) ? 1 : 0));
        }
        for (UUID u : spectators) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                sb.append(' ').append(p.getName()).append("=zuschauer");
            }
        }
        return sb.toString();
    }

    /** Hinweise einer Person für Tests: „hut=gruen#0,joppe!=rot#0“. */
    public String cluesOf(UUID u) {
        Suspect s = people.get(u);
        return s == null ? "" : s.clues().stream().map(Clue::code).collect(Collectors.joining(","));
    }
}
