package me.timpixel.scallywag;

import me.timpixel.scallywag.exceptions.ScallywagAuthenticationSetException;
import me.timpixel.scallywag.exceptions.ScallywagNativeSourceException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
        return ScallywagPlugin.authenticationManager().isLoggedIn(player.getUniqueId());
    }

    /**
     * Checks if the specified player is currently logged in by their UUID.
     *
     * @param uuid the UUID of the player
     * @return true if the player is logged in, false otherwise
     */
    static boolean isLoggedIn(@NotNull UUID uuid)
    {
        return ScallywagPlugin.authenticationManager().isLoggedIn(uuid);
    }

    /**
     * Sets the password validator (the function that determines whether the password is strong enough)
     *
     * @param validator the validator. The function should take in the password and return true if the password is strong enough, false otherwise
     */
    static void setPasswordValidator(@NotNull PasswordValidator validator)
    {
        ScallywagPlugin.authenticationManager().setPasswordValidator(validator);
    }

    /**
     * Sets the authentication handler (the object responsible for external authentication).
     *
     * @param authenticationHandler the authentication handler.
     * @throws ScallywagNativeSourceException      if native authentication is being used
     * @throws ScallywagAuthenticationSetException if the authentication handler is already set
     */
    static void setAuthenticationHandler(@NotNull AuthenticationHandler authenticationHandler) throws ScallywagNativeSourceException, ScallywagAuthenticationSetException
    {
        ScallywagPlugin.authenticationManager().setAuthenticationHandler(authenticationHandler);
    }

}
