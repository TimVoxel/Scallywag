package me.timpixel.scallywag;

import me.timpixel.scallywag.exceptions.ScallywagAuthenticationSetException;
import me.timpixel.scallywag.exceptions.ScallywagNativeSourceException;
import me.timpixel.scallywag.exceptions.ScallywagNoAuthenticationException;
import me.timpixel.scallywag.exceptions.ScallywagUninitializedException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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
    static boolean isLoggedIn(@NotNull Player player) throws ScallywagNoAuthenticationException
    {
        return ScallywagPlugin.login().loginManager().isLoggedIn(player.getUniqueId());
    }

    /**
     * Checks if the specified player is currently logged in by their UUID.
     *
     * @param uuid the UUID of the player
     * @return true if the player is logged in, false otherwise
     */
    static boolean isLoggedIn(@NotNull UUID uuid) throws ScallywagNoAuthenticationException
    {
        return ScallywagPlugin.login().loginManager().isLoggedIn(uuid);
    }

    /**
     * Sets the password validator (the function that determines whether the password is strong enough)
     *
     * @param validator the validator. The function should take in the password and return true if the password is strong enough, false otherwise
     */
    static void setPasswordValidator(@NotNull JavaPlugin setter, @NotNull Function<String, Boolean> validator) throws ScallywagNoAuthenticationException
    {
        ScallywagPlugin.registration().registrationManager().setPasswordValidator(setter, validator);
    }

    /**
     * Sets the login manager (the object responsible for login behaviour).
     *
     * @param loginManager the login manager.
     * @throws ScallywagUninitializedException if the method is used before Scallywag is fully initialized
     * @throws ScallywagNativeSourceException if native authentication is being used
     * @throws ScallywagAuthenticationSetException if the login manager was already set by a different call
     */
    static void setLoginManager(@NotNull LoginManager loginManager) throws ScallywagUninitializedException,
            ScallywagNativeSourceException, ScallywagAuthenticationSetException
    {
        ScallywagPlugin.login().set(loginManager);
    }

    /**
     * Sets the registration manager (the object responsible for registration creation, update and deletion).
     *
     * @param registrationManager the login manager.
     * @throws ScallywagUninitializedException     if the method is used before Scallywag is fully initialized
     * @throws ScallywagNativeSourceException      if native authentication is being used
     * @throws ScallywagAuthenticationSetException if the registration manager was already set by a different call
     */
    static void setRegistrationManager(@NotNull RegistrationManager registrationManager) throws ScallywagUninitializedException,
            ScallywagNativeSourceException, ScallywagAuthenticationSetException
    {
        ScallywagPlugin.registration().set(registrationManager);
    }

}
