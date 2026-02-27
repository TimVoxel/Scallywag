package me.timpixel.scallywag;

import me.timpixel.scallywag.exceptions.ScallywagAuthenticationSetException;
import me.timpixel.scallywag.exceptions.ScallywagNativeSourceException;
import me.timpixel.scallywag.results.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AuthenticationManager
{
    private final AuthenticationSource authenticationSource;
    private final JavaPlugin plugin;
    private final boolean isAutomaticallyLogInUponRegistration;

    private final Set<UUID> loggedInPlayers;

    private PasswordValidator passwordValidator;
    private AuthenticationHandler authenticationHandler;

    public AuthenticationManager(JavaPlugin plugin, boolean isAutomaticallyLogInUponRegistration, AuthenticationSource authenticationSource)
    {
        this.plugin = plugin;
        this.authenticationSource = authenticationSource;
        this.isAutomaticallyLogInUponRegistration = isAutomaticallyLogInUponRegistration;
        this.loggedInPlayers = ConcurrentHashMap.newKeySet();
    }

    public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) throws ScallywagAuthenticationSetException, ScallywagNativeSourceException
    {
        if (authenticationSource != AuthenticationSource.EXTERNAL)
        {
            throw new ScallywagNativeSourceException();
        }
        if (this.authenticationHandler != null)
        {
            throw new ScallywagAuthenticationSetException();
        }
        this.authenticationHandler = authenticationHandler;
    }

    public void setPasswordValidator(PasswordValidator passwordValidator)
    {
        this.passwordValidator = passwordValidator;
    }

    public CompletableFuture<LoginResult> tryLogIn(UUID uuid, String username, String providedPassword)
    {
        if (isLoggedIn(uuid))
        {
            return CompletableFuture.completedFuture(LoginResult.ALREADY_LOGGED_IN);
        }

        return authenticationHandler.verify(uuid, username, providedPassword).thenApply(result ->
        {
            var loginResult = switch (result)
            {
                case PasswordVerificationResult.SUCCESSFUL -> LoginResult.SUCCESSFUL;
                case PasswordVerificationResult.NOT_FOUND -> LoginResult.NOT_REGISTERED;
                case PasswordVerificationResult.INCORRECT -> LoginResult.WRONG_PASSWORD;
            };

            if (loginResult == LoginResult.SUCCESSFUL)
            {
                logIn(uuid, username);
            }
            return loginResult;
        }).exceptionally(exception ->
        {
            logException(exception);
            return LoginResult.INTERNAL_ERROR;
        });
    }

    public synchronized void logIn(UUID uuid, String username)
    {
        loggedInPlayers.add(uuid);
        ScallywagPlugin.logger().info("Player \"" + username + "\" successfully logged in (uuid: " + uuid + ")");
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new ScallywagLogInEvent(uuid)));
    }

    public void tryLogOut(UUID uuid)
    {
        if (loggedInPlayers.remove(uuid))
        {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new ScallywagLogOutEvent(uuid)));
        }
    }

    public boolean isLoggedIn(UUID uuid)
    {
        return loggedInPlayers.contains(uuid);
    }

    public boolean isLoggedIn(Player player)
    {
        return loggedInPlayers.contains(player.getUniqueId());
    }

    public CompletableFuture<RegistrationResult> tryRegister(UUID uuid, String username, String password)
    {
        if (!passwordValidator.isValid(password))
        {
            return CompletableFuture.completedFuture(RegistrationResult.INVALID_PASSWORD);
        }

        if (!(authenticationHandler instanceof MutableAuthenticationHandler mutableAuthenticationHandler))
        {
            return CompletableFuture.completedFuture(RegistrationResult.UNSUPPORTED);
        }

        return mutableAuthenticationHandler.tryRegister(uuid, username, password).thenApply(result ->
        {
            ScallywagPlugin.logger().info("Added new registration of player \"" + username + "\", uuid: " + uuid);
            if (isAutomaticallyLogInUponRegistration)
            {
                logIn(uuid, username);
            }
            return result;
        }).exceptionally(exception ->
        {
            logException(exception);
            return RegistrationResult.INTERNAL_ERROR;
        });
    }

    public CompletableFuture<RegistrationRemovalResult> tryRemoveRegistration(UUID uuid)
    {
        if (!(authenticationHandler instanceof MutableAuthenticationHandler mutableAuthenticationHandler))
        {
            return CompletableFuture.completedFuture(RegistrationRemovalResult.UNSUPPORTED);
        }

        return mutableAuthenticationHandler.tryRemoveRegistration(uuid).thenApply(r -> {
            if (r == RegistrationRemovalResult.SUCCESSFUL)
            {
                tryLogOut(uuid);
                ScallywagPlugin.logger().info("Deleted registration of " + uuid);
            }
            return r;
        }).exceptionally(exception ->
        {
            logException(exception);
            return RegistrationRemovalResult.INTERNAL_ERROR;
        });
    }

    public CompletableFuture<RegistrationRemovalResult> tryRemoveRegistration(String username)
    {
        if (!(authenticationHandler instanceof MutableAuthenticationHandler mutableAuthenticationHandler))
        {
            return CompletableFuture.completedFuture(RegistrationRemovalResult.UNSUPPORTED);
        }

        return mutableAuthenticationHandler.tryRemoveRegistration(username).thenApply(r ->
        {
            if (r == RegistrationRemovalResult.SUCCESSFUL)
            {
                ScallywagPlugin.logger().info("Deleted registration of " + username);
            }
            return r;
        }).exceptionally(exception ->
        {
            logException(exception);
            return RegistrationRemovalResult.INTERNAL_ERROR;
        });
    }

    public CompletableFuture<UpdateResult> tryUpdateRegistration(String oldUsername, String newUsername, String newPassword)
    {
        if (!(authenticationHandler instanceof MutableAuthenticationHandler mutableAuthenticationHandler))
        {
            return CompletableFuture.completedFuture(UpdateResult.UNSUPPORTED);
        }

        return mutableAuthenticationHandler.tryUpdateRegistration(oldUsername, newUsername, newPassword).exceptionally(exception ->
        {
            logException(exception);
            return UpdateResult.INTERNAL_ERROR;
        });
    }

    public CompletableFuture<UpdateResult> tryUpdateRegistration(UUID uuid, String newUsername, String newPassword)
    {
        if (!(authenticationHandler instanceof MutableAuthenticationHandler mutableAuthenticationHandler))
        {
            return CompletableFuture.completedFuture(UpdateResult.UNSUPPORTED);
        }

        return mutableAuthenticationHandler.tryUpdateRegistration(uuid, newUsername, newPassword).exceptionally(exception ->
        {
            logException(exception);
            return UpdateResult.INTERNAL_ERROR;
        });
    }

    public CompletableFuture<UpdateResult> tryUpdatePassword(UUID uuid, String username, String newPassword)
    {
        if (!passwordValidator.isValid(newPassword))
        {
            return CompletableFuture.completedFuture(UpdateResult.INVALID);
        }
        return tryUpdateRegistration(uuid, username, newPassword).exceptionally(exception ->
        {
            logException(exception);
            return UpdateResult.INTERNAL_ERROR;
        });
    }

    public List<String> registeredUsernames()
    {
        if (authenticationHandler instanceof MutableAuthenticationHandler mutableAuthenticationHandler)
        {
            return mutableAuthenticationHandler.getRegisteredUsernames();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private void logException(Throwable exception)
    {
        ScallywagPlugin.logger().log(Level.SEVERE, "An exception occurred while performing registration operations", exception);
    }
}
