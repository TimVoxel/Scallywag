package me.timpixel;

import me.timpixel.listeners.LoginListener;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface Scallywag
{
    /**
     * Checks if the specified player is currently logged in.
     *
     * @param player the Player to check
     * @return true if the player is logged in, false otherwise
     */
    static boolean isLoggedIn(Player player)
    {
        return ScallywagPlugin.registrationManager().isLoggedIn(player);
    }

    /**
     * Checks if the specified player is currently logged in by their UUID.
     *
     * @param uuid the UUID of the player
     * @return true if the player is logged in, false otherwise
     */
    static boolean isLoggedIn(UUID uuid)
    {
        return ScallywagPlugin.registrationManager().isLoggedIn(uuid);
    }

    /**
     * Adds a login listener
     *
     * @param listener the listener to add
     */
    static void addListener(LoginListener listener)
    {
        ScallywagPlugin.registrationManager().addListener(listener);
    }

    /**
     * Removes the login listener
     *
     * @param listener the listener to remove
     */
    static void removeListener(LoginListener listener)
    {
        ScallywagPlugin.registrationManager().removeListener(listener);
    }
}
