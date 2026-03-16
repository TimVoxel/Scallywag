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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    static boolean isLoggedIn(@NotNull UUID uuid)
    {
        return ScallywagPlugin.authenticationManager().isLoggedIn(uuid);
    }

    /**
     * Forcefully logs in the player with the specified UUID.
     *
     * @param uuid the UUID of the player to forcefully log in
     */
    @SuppressWarnings("unused")
    static void logIn(@NotNull UUID uuid)
    {
        ScallywagPlugin.authenticationManager().logIn(uuid);
    }

    /**
     * Forcefully logs in the specified player.
     *
     * @param player the player to forcefully log in
     */
    @SuppressWarnings("unused")
    static void logIn(@NotNull Player player)
    {
        logIn(player.getUniqueId());
    }

    /**
     * Sets the password validator (the function that determines whether the password is strong enough)
     *
     * @param validator the validator. The function should take in the password and return true if the password is strong enough, false otherwise
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    static void setAuthenticationHandler(@NotNull AuthenticationHandler authenticationHandler) throws ScallywagNativeSourceException, ScallywagAuthenticationSetException
    {
        ScallywagPlugin.authenticationManager().setAuthenticationHandler(authenticationHandler);
    }
}
