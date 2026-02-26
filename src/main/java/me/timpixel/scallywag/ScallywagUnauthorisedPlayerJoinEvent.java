package me.timpixel.scallywag;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the event that is called when an unauthorised player joins the server.
 * This event is synchronous and is called on the game tick the player joins the server.
 */
public class ScallywagUnauthorisedPlayerJoinEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public final Player player;

    public ScallywagUnauthorisedPlayerJoinEvent(Player player)
    {
        this.player = player;
    }

    /**
     * Returns the player that joined
     * @return The joined unauthorised player
     */
    public Player getPlayer() { return player; }

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
