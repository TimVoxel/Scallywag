package me.timpixel;

import me.timpixel.database.DatabaseManager;
import me.timpixel.listeners.LoginListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.logging.Level;

public class DatabaseRegistrationManager implements RegistrationManager
{
    private final PasswordVerifier passwordVerifier;
    private final Set<UUID> loggedInPlayers;
    private final List<String> registeredUsernames;

    private final DatabaseManager databaseManager;
    private final boolean automaticallyLogInUponRegistration;
    private final List<LoginListener> listeners;

    public DatabaseRegistrationManager(DatabaseManager databaseManager, boolean automaticallyLogInUponRegistration)
    {
        this.databaseManager = databaseManager;
        this.automaticallyLogInUponRegistration = automaticallyLogInUponRegistration;
        this.loggedInPlayers = new HashSet<>();
        this.listeners = new ArrayList<>();

        registeredUsernames = new ArrayList<>();

        try
        {
            var registeredInDatabase = databaseManager.getRegisteredUsernames();
            registeredUsernames.addAll(registeredInDatabase.get());
        }
        catch (Exception exception)
        {
            logException(exception);
        }

        passwordVerifier = new PasswordVerifier();
    }

    public void shutdown()
    {
        passwordVerifier.shutdown();
    }

    @Override
    public boolean isLoggedIn(UUID uuid)
    {
        return loggedInPlayers.contains(uuid);
    }

    @Override
    public boolean isLoggedIn(Player player)
    {
        return loggedInPlayers.contains(player.getUniqueId());
    }

    @Override
    public RegistrationResult tryRegister(UUID uuid, String username, String password)
    {
        try
        {
            var registration = databaseManager.getRegistration(uuid);

            if (registration != null)
            {
                return RegistrationResult.ALREADY_REGISTERED;
            }
            else
            {
                databaseManager.tryRegisterPlayer(uuid, username, password).get();

                registeredUsernames.add(username);
                Scallywag.logger().info("Added new registration of player \"" + username + "\", uuid: " + uuid);

                if (automaticallyLogInUponRegistration)
                {
                    logIn(uuid, username, username);
                }
                return RegistrationResult.SUCCESSFUL;
            }
        }
        catch (Exception exception)
        {
            logException(exception);
            return RegistrationResult.INTERNAL_ERROR;
        }
    }

    @Override
    public LoginResult tryLogIn(UUID uuid, String username, String actualPassword)
    {
        if (isLoggedIn(uuid))
        {
            return LoginResult.ALREADY_LOGGED_IN;
        }

        try
        {
            var registration = databaseManager.getRegistration(uuid).get();

            if (registration == null)
            {
                return LoginResult.NOT_REGISTERED;
            }

            var expectedPassword = registration.passwordHash();
            var isCorrectPassword = passwordVerifier.verify(actualPassword, expectedPassword).get();

            if (isCorrectPassword)
            {
                var storedUsername = registration.username();
                logIn(uuid, storedUsername, username);
                return LoginResult.SUCCESSFUL;
            }
            else
            {
                return LoginResult.WRONG_PASSWORD;
            }
        }
        catch (Exception exception)
        {
            logException(exception);
            return LoginResult.INTERNAL_ERROR;
        }
    }

    private void logIn(UUID uuid, String storedUsername, String username)
    {
        loggedInPlayers.add(uuid);

        if (!storedUsername.equals(username))
        {
            updatePlayerUsername(uuid, storedUsername, username);
        }

        Scallywag.logger().info("Player \"" + username + "\" successfully logged in (uuid: " + uuid + ")");

        for (var listener : listeners)
        {
            listener.onPlayerLoggedIn(uuid, username);
        }
    }

    @Override
    public void tryLogOut(UUID uuid, String username)
    {
        if (loggedInPlayers.remove(uuid))
        {
            for (var listener : listeners)
            {
                listener.onPlayerLoggedOut(uuid, username);
            }
        }
    }

    @Override
    public RegistrationRemovalResult tryRemoveRegistration(UUID uuid)
    {
        try
        {
            var deletedRegistration = databaseManager.deleteRegistrationWithUUID(uuid).get();
            return processDeletedRegistration(deletedRegistration);
        }
        catch (Exception exception)
        {
            logException(exception);
            return RegistrationRemovalResult.INTERNAL_ERROR;
        }
    }

    @Override
    public RegistrationRemovalResult tryRemoveRegistration(String username)
    {
        try
        {
            var deletedRegistration = databaseManager.deleteRegistrationsWithUsername(username, 1).get();
            return processDeletedRegistration(deletedRegistration);
        }
        catch (Exception exception)
        {
            logException(exception);
            return RegistrationRemovalResult.INTERNAL_ERROR;
        }
    }

    private RegistrationRemovalResult processDeletedRegistration(@Nullable PlayerRegistration deletedRegistration)
    {
        if (deletedRegistration != null)
        {
            var uuid = deletedRegistration.uuid();
            var username = deletedRegistration.username();

            registeredUsernames.remove(deletedRegistration.username());

            if (isLoggedIn(uuid))
            {
                tryLogOut(uuid, deletedRegistration.username());
            }

            Scallywag.logger().info("Deleted registration of " + username + ", uuid: " + uuid);
            return RegistrationRemovalResult.SUCCESSFUL;
        }
        else
        {
            return RegistrationRemovalResult.NOT_FOUND;
        }
    }

    @Override
    public <T> UpdateResult updateRegistrationProperty(UUID uuid, RegistrationVariableProperty<T> property, T value)
    {
        try
        {
            var registration = databaseManager.getRegistration(uuid).get();
            return updateRegistrationProperty(registration, property, value);
        }
        catch (Exception exception)
        {
            logException(exception);
            return UpdateResult.INTERNAL_ERROR;
        }
    }

    @Override
    public <T> UpdateResult updateRegistrationProperty(String username, RegistrationVariableProperty<T> property, T value)
    {
        try
        {
            var registration = databaseManager.getRegistration(username).get();
            return updateRegistrationProperty(registration, property, value);
        }
        catch (Exception exception)
        {
            logException(exception);
            return UpdateResult.INTERNAL_ERROR;
        }
    }

    private <T> UpdateResult updateRegistrationProperty(@Nullable PlayerRegistration registration,
                                                        RegistrationVariableProperty<T> property,
                                                        T value)
    {
        if (registration == null)
        {
            return UpdateResult.REGISTRATION_NOT_FOUND;
        }

        var uuid = registration.uuid();

        if (property == RegistrationVariableProperty.USERNAME)
        {
            var newUsername = (String) value;
            return updatePlayerUsername(uuid, registration.username(), newUsername);
        }
        else
        {
            if (property != RegistrationVariableProperty.PASSWORD)
            {
                Scallywag.logger().severe("Unexpected registration variable property: " + property.name());
                return UpdateResult.INTERNAL_ERROR;
            }

            var newPassword = (String) value;
            return updatePlayerPassword(uuid, newPassword);
        }
    }

    private UpdateResult updatePlayerUsername(UUID uuid, String storedName, String newUsername)
    {
        if (storedName.equals(newUsername))
        {
            return UpdateResult.VALUE_MATCHES;
        }

        registeredUsernames.remove(storedName);
        registeredUsernames.add(newUsername);

        try
        {
            databaseManager.updatePlayerUsername(uuid, newUsername).get();
            Scallywag.logger().info("Updated username of player " + uuid + " to: " + newUsername);
            return UpdateResult.SUCCESSFUL;
        }
        catch (Exception exception)
        {
            logException(exception);
            return UpdateResult.INTERNAL_ERROR;
        }
    }

    private UpdateResult updatePlayerPassword(UUID uuid, String newPassword)
    {
        //Do not account for matching values because that would expose the password
        try
        {
            databaseManager.updatePlayerPassword(uuid, newPassword).get();
            Scallywag.logger().info("Updated password of player " + uuid);
            return UpdateResult.SUCCESSFUL;
        }
        catch (Exception exception)
        {
            logException(exception);
            return UpdateResult.INTERNAL_ERROR;
        }
    }

    @Override
    public List<String> registeredUsernames()
    {
        return registeredUsernames;
    }

    @Override
    public void addListener(LoginListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeListener(LoginListener listener)
    {
        listeners.remove(listener);
    }

    private void logException(Exception exception)
    {
        Scallywag.logger().log(Level.SEVERE, "An exception occurred while performing registration operations", exception);
    }
}
