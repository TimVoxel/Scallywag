package me.timpixel.scallywag;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;

public interface Scallywag
{
    /**
     * Checks if the specified player is currently logged in.
     *
     * @param player the Player to check
     * @return true if the player is logged in, false otherwise
     */
    static boolean isLoggedIn(@NotNull Player player)
    {
        return ScallywagPlugin.registrationManager().isLoggedIn(player);
    }

    /**
     * Checks if the specified player is currently logged in by their UUID.
     *
     * @param uuid the UUID of the player
     * @return true if the player is logged in, false otherwise
     */
    static boolean isLoggedIn(@NotNull UUID uuid)
    {
        return ScallywagPlugin.registrationManager().isLoggedIn(uuid);
    }

    /**
     * Sets the password validator (the function that determines whether the password is strong enough)
     *
     * @param validator the validator. The function should take in the password and return true if the password is strong enough, false otherwise
     */
    static void setPasswordValidator(@NotNull Function<String, Boolean> validator)
    {
        ScallywagPlugin.registrationManager().setPasswordValidator(validator);
    }
}
