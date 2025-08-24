package me.timpixel.scallywag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents the event that is called when a player successfully logs in.
 * This event is synchronous and is called on the game tick after the async log in operation is completed
 */
public class ScallywagLogInEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Nullable Player player;
    private final UUID uuid;

    public ScallywagLogInEvent(UUID uuid)
    {
        this.uuid = uuid;
    }

    /**
     * Gets the player that logged in
     * @return The player that logged in if they are online, null otherwise
     * @implNote Uses {@link Bukkit#getPlayer(UUID)} upon the first call, then uses the cached result.
     */
    public synchronized @Nullable Player getPlayer()
    {
        if (player != null)
        {
            return player;
        }

        player = Bukkit.getPlayer(uuid);
        return player;
    }

    /**
     * Gets the uuid of the player that logged out
     *
     * @return The uuid of player that logged out
     * @apiNote Use {@link #getPlayer()} if you want simply access the player.
     */
    public @NotNull UUID getUuid()
    {
        return uuid;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }
}

