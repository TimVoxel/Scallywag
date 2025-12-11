package me.timpixel.scallywag.listeners;

import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import io.papermc.paper.event.player.PlayerSignCommandPreprocessEvent;
import me.timpixel.scallywag.RegistrationManager;
import me.timpixel.scallywag.ScallywagLogInEvent;
import me.timpixel.scallywag.ScallywagPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnauthorisedPlayerListener implements Listener
{
    private final RegistrationManager registrationManager;
    private final Map<UUID, Boolean> originalAllowFlightValue = new HashMap<>();

    private final boolean doSetUnauthorisedInvulnerable;

    public UnauthorisedPlayerListener(RegistrationManager registrationManager, boolean doSetUnauthorisedInvulnerable)
    {
        this.registrationManager = registrationManager;
        this.doSetUnauthorisedInvulnerable = doSetUnauthorisedInvulnerable;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        var player = event.getPlayer();

        if (!registrationManager.isLoggedIn(player))
        {
            originalAllowFlightValue.put(player.getUniqueId(), player.getAllowFlight());
            player.setAllowFlight(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        if (!registrationManager.isLoggedIn(event.getPlayer()))
        {
            resetChangedProperties(event.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerLoggedIn(ScallywagLogInEvent event)
    {
        var player = event.getPlayer();

        if (player != null)
        {
            resetChangedProperties(player);
        }
    }

    private void resetChangedProperties(Player player)
    {
        var allowFlight = originalAllowFlightValue.remove(player.getUniqueId());

        if (allowFlight != null)
        {
            player.setAllowFlight(allowFlight);
        }
    }

    @EventHandler
    private void onPlayerMoved(PlayerMoveEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    private void onPlayerToggleFlightEvent(PlayerToggleFlightEvent event) { cancelIfUnauthorised(event.getPlayer(), event); }

    @EventHandler
    private void onPlayerBrokeBlock(BlockBreakEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    private void onPlayerPlaceBlock(BlockPlaceEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    private void onPlayerPickedItem(PlayerPickItemEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    private void onPlayerPickedBlock(PlayerPickBlockEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    private void onPlayerPickedEntity(PlayerPickEntityEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event)
    {
        if (!doSetUnauthorisedInvulnerable)
        {
            return;
        }

        if (event.getEntity() instanceof Player player)
        {
            cancelIfUnauthorised(player, event);
        }
    }

    @EventHandler
    private void onPlayerOpenInventory(InventoryOpenEvent event)
    {
        if (event.getPlayer() instanceof Player player)
        {
            cancelIfUnauthorised(player, event);
        }
    }

    @EventHandler
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (!ScallywagPlugin.isAllowedUnauthorizedCommand(event.getMessage()))
        {
            cancelIfUnauthorised(event.getPlayer(), event);
        }
    }

    @EventHandler
    private void onCommandPreprocess(PlayerSignCommandPreprocessEvent event)
    {
        if (!ScallywagPlugin.isAllowedUnauthorizedCommand(event.getMessage()))
        {
            cancelIfUnauthorised(event.getPlayer(), event);
        }
    }

    private void cancelIfUnauthorised(Player player, Cancellable event)
    {
        if (!registrationManager.isLoggedIn(player))
        {
            event.setCancelled(true);
        }
    }
}
