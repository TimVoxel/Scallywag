package me.timpixel.scallywag.listeners;

import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import io.papermc.paper.event.player.PlayerSignCommandPreprocessEvent;
import me.timpixel.scallywag.AuthenticationManager;
import me.timpixel.scallywag.ScallywagLogInEvent;
import me.timpixel.scallywag.ScallywagUnauthorisedPlayerJoinEvent;
import me.timpixel.scallywag.ScallywagPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnauthorisedPlayerListener implements Listener
{
    private record UnauthorisedPlayerInfo(Location location, boolean isAllowFlight)
    {
    }

    private final static PotionEffect DARKNESS_EFFECT = new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 1, true, false);

    private final AuthenticationManager authenticationManager;
    private final Map<UUID, UnauthorisedPlayerInfo> unauthorisedInfo = new HashMap<>();

    private final boolean isSetUnauthorisedInvulnerable;
    private final boolean isApplyDarkness;
    private final @Nullable Location limboLocation;

    public UnauthorisedPlayerListener(AuthenticationManager authenticationManager,
                                      boolean isSetUnauthorisedInvulnerable,
                                      boolean isApplyDarkness,
                                      @Nullable Location limboLocation)
    {
        this.authenticationManager = authenticationManager;
        this.isSetUnauthorisedInvulnerable = isSetUnauthorisedInvulnerable;
        this.isApplyDarkness = isApplyDarkness;
        this.limboLocation = limboLocation;
    }

    @EventHandler
    private void onNonLoggedInPlayerJoin(ScallywagUnauthorisedPlayerJoinEvent event)
    {
        var player = event.getPlayer();
        var location = player.getLocation();
        var isAllowFlight = player.getAllowFlight();
        unauthorisedInfo.put(player.getUniqueId(), new UnauthorisedPlayerInfo(location, isAllowFlight));

        player.setAllowFlight(true);
        if (isApplyDarkness)
        {
            player.addPotionEffect(DARKNESS_EFFECT);
        }
        if (limboLocation != null)
        {
            player.teleport(limboLocation);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        if (!authenticationManager.isLoggedIn(event.getPlayer()))
        {
            resetChangedProperties(event.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerLoggedIn(ScallywagLogInEvent event)
    {
        var player = Bukkit.getPlayer(event.getUuid());

        if (player != null)
        {
            resetChangedProperties(player);
        }
    }

    private void resetChangedProperties(Player player)
    {
        var info = unauthorisedInfo.remove(player.getUniqueId());
        if (info != null)
        {
            player.setAllowFlight(info.isAllowFlight);

            if (limboLocation != null)
            {
                player.teleport(info.location);
            }
        }
        if (isApplyDarkness)
        {
            player.removePotionEffect(PotionEffectType.DARKNESS);
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
        if (!isSetUnauthorisedInvulnerable)
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
        if (!authenticationManager.isLoggedIn(player))
        {
            event.setCancelled(true);
        }
    }
}
