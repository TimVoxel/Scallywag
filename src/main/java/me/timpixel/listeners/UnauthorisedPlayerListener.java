package me.timpixel.listeners;

import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import me.timpixel.PlayerRegistration;
import me.timpixel.RegistrationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class UnauthorisedPlayerListener implements Listener
{
    private final RegistrationManager registrationManager;

    public UnauthorisedPlayerListener(RegistrationManager registrationManager)
    {
        this.registrationManager = registrationManager;
    }

    @EventHandler
    public void onPlayerMoved(PlayerMoveEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    public void onPlayerBrokeBlock(BlockBreakEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    public void onPlayerPickedItem(PlayerPickItemEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    public void onPlayerPickedBlock(PlayerPickBlockEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    public void onPlayerPickedEntity(PlayerPickEntityEvent event)
    {
        cancelIfUnauthorised(event.getPlayer(), event);
    }

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event)
    {
        if (event.getPlayer() instanceof Player player)
        {
            cancelIfUnauthorised(player, event);
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
