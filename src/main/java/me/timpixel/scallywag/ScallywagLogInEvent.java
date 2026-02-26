package me.timpixel.scallywag;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents the event that is called when a player successfully logs in.
 * This event is synchronous and is called on the game tick after the async log in operation is completed
 */
public class ScallywagLogInEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID uuid;

    public ScallywagLogInEvent(UUID uuid)
    {
        this.uuid = uuid;
    }

    /**
     * Gets the uuid of the player that logged out
     *
     * @return The uuid of player that logged out
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

