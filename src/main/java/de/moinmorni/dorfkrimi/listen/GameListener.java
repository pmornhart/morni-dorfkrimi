package de.moinmorni.dorfkrimi.listen;

import de.moinmorni.dorfkrimi.DorfkrimiPlugin;
import de.moinmorni.dorfkrimi.Texts;
import de.moinmorni.dorfkrimi.game.Game;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Kein Schaden in der ganzen Welt: Ein Schlag (oder Rechtsklick, Antippen auf Bedrock) mit Schlafpulver betäubt, mit der
 * Dienstmütze nimmt man fest – niemand wird verletzt. Ausgeschiedene und Zuschauer reden nur untereinander.
 */
public final class GameListener implements Listener {

    private final DorfkrimiPlugin plugin;

    public GameListener(DorfkrimiPlugin plugin) {
        this.plugin = plugin;
    }

    private static Entity source(Entity damager) {
        if (damager instanceof Projectile pr) {
            ProjectileSource s = pr.getShooter();
            return s instanceof Entity e ? e : null;
        }
        return damager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getEntity().getWorld() != plugin.world()) {
            return;
        }
        e.setCancelled(true);
        if (e.getEntity() instanceof Player victim && source(e.getDamager()) instanceof Player attacker) {
            touch(attacker, victim);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || e.getPlayer().getWorld() != plugin.world()) {
            return;
        }
        if (e.getRightClicked() instanceof Player victim) {
            e.setCancelled(true);
            touch(e.getPlayer(), victim);
        }
    }

    private void touch(Player by, Player target) {
        Game g = plugin.games().playing(by.getUniqueId());
        if (g != null && plugin.games().playing(target.getUniqueId()) == g) {
            g.touch(by, target);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity().getWorld() == plugin.world() && (e.getEntity() instanceof Player || e.getEntity() instanceof ArmorStand)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getEntity().getWorld() != plugin.world()) {
            return;
        }
        e.deathMessage(null);
        e.setKeepInventory(true);
        e.getDrops().clear();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Game g = plugin.games().of(p.getUniqueId());
        if (g == null) {
            e.setRespawnLocation(plugin.hall().spawn());
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.hall().enter(p));
        } else {
            e.setRespawnLocation(g.spectatorSpot());
        }
    }

    /** Aufstehen von der Bank: Sitz weg (zurück mit /bank). */
    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p && e.getDismounted().getScoreboardTags().contains(Game.PROP_TAG)) {
            Game g = plugin.games().of(p.getUniqueId());
            if (g != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> g.leaveSeat(p.getUniqueId()));
            }
        }
    }

    /**
     * Wer ausgeschieden ist (oder zuschaut), redet nur mit den anderen auf der Bank: Die Nachricht geht nicht an die
     * Wachen und nicht ins Netzwerk. Zugestellt wird auf dem Hauptthread.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        if (!plugin.games().onBench(u)) {
            return;
        }
        e.setCancelled(true);
        Component text = e.message();
        String name = e.getPlayer().getName();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : plugin.games().benchOf(u)) {
                p.sendMessage(Texts.MM.deserialize(Texts.raw(plugin.lang(p), "ghost.chat"), Placeholder.unparsed("name", name))
                        .append(text.colorIfAbsent(NamedTextColor.GRAY)));
            }
        });
    }
}
