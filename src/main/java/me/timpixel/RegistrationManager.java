package me.timpixel;

import me.timpixel.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class RegistrationManager
{
    private final Set<UUID> loggedInPlayers;
    private final List<String> registeredUsernames;

    private final @Nullable DatabaseManager databaseManager;

    public RegistrationManager(@Nullable DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
        this.loggedInPlayers = new HashSet<>();

        registeredUsernames = new ArrayList<>();

        if (databaseManager != null)
        {
            try
            {
                var registeredInDatabase = databaseManager.getRegisteredUsernames();
                registeredUsernames.addAll(registeredInDatabase);
            }
            catch (SQLException exception)
            {
                logException(exception);
            }
        }
    }

    private void logException(Exception exception)
    {
        Scallywag.logger().log(Level.SEVERE, "An exception occurred while performing registration operations", exception);
    }

    public List<String> registeredUsernames()
    {
        return registeredUsernames;
    }

    public RegistrationResult tryRegister(Player player, String password)
    {
        return tryRegister(player.getUniqueId(), player.getName(), password);
    }

    public RegistrationResult tryRegister(UUID uuid, String username, String password)
    {
        if (databaseManager == null)
        {
            return RegistrationResult.INTERNAL_ERROR;
        }

        try
        {
            var registration = databaseManager.getRegistration(uuid);

            if (registration != null)
            {
                return RegistrationResult.ALREADY_REGISTERED;
            }
            else
            {
                databaseManager.tryRegisterPlayer(uuid, username, password);
                registeredUsernames.add(username);
                Scallywag.logger().info("Player \"" + username + "\" successfully registered (uuid: " + uuid + ")");
                return RegistrationResult.SUCCESSFUL;
            }
        }
        catch (SQLException exception)
        {
            logException(exception);
            return RegistrationResult.INTERNAL_ERROR;
        }
    }

    public LoginResult tryLogin(Player player, String actualPassword)
    {
        var uuid = player.getUniqueId();

        if (loggedInPlayers.contains(uuid))
        {
            return LoginResult.ALREADY_LOGGED_IN;
        }

        if (databaseManager == null)
        {
            return LoginResult.INTERNAL_ERROR;
        }

        try
        {
            var registration = databaseManager.getRegistration(uuid);

            if (registration == null)
            {
                return LoginResult.NOT_REGISTERED;
            }

            var expectedPassword = registration.passwordHash();

            if (BCrypt.checkpw(actualPassword, expectedPassword))
            {
                loggedInPlayers.add(uuid);

                var storedName = registration.username();
                var actualName = player.getName();

                if (!storedName.equals(actualName))
                {
                    Scallywag.logger().info("Player with uuid " + uuid + "logged in with a different username, updating username to: \"" + actualName + "\"");
                    databaseManager.updatePlayerName(uuid, actualName);
                }
                else
                {
                    Scallywag.logger().info("Player \"" + actualName + "\" successfully logged in (uuid: " + uuid + ")");
                }

                return LoginResult.SUCCESSFUL;
            }
            else
            {
                return LoginResult.WRONG_PASSWORD;
            }
        }
        catch (SQLException exception)
        {
            logException(exception);
            return LoginResult.INTERNAL_ERROR;
        }
    }

    public RegistrationRemovalResult tryRemoveRegistration(UUID uuid)
    {
        if (databaseManager == null)
        {
            return RegistrationRemovalResult.INTERNAL_ERROR;
        }

        try
        {
            var deletedRegistration = databaseManager.deleteRegistrationWithUUID(uuid);

            if (deletedRegistration != null)
            {
                registeredUsernames.remove(deletedRegistration.username());
                return RegistrationRemovalResult.SUCCESSFUL;
            }
            else
            {
                return RegistrationRemovalResult.NOT_FOUND;
            }
        }
        catch (SQLException exception)
        {
            logException(exception);
            return RegistrationRemovalResult.INTERNAL_ERROR;
        }
    }

    public RegistrationRemovalResult tryRemoveRegistration(String username)
    {
        if (databaseManager == null)
        {
            return RegistrationRemovalResult.INTERNAL_ERROR;
        }

        try
        {
            var deletedRegistration = databaseManager.deleteRegistrationsWithUsername(username, 1);

            if (deletedRegistration != null)
            {
                registeredUsernames.remove(deletedRegistration.username());
                return RegistrationRemovalResult.SUCCESSFUL;
            }
            else
            {
                return RegistrationRemovalResult.NOT_FOUND;
            }
        }
        catch (SQLException exception)
        {
            logException(exception);
            return RegistrationRemovalResult.INTERNAL_ERROR;
        }
    }
}