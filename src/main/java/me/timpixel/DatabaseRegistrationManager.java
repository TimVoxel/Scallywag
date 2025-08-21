package me.timpixel;

import me.timpixel.database.DatabaseManager;
import me.timpixel.listeners.LoginListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class DatabaseRegistrationManager implements RegistrationManager
{
    private final PasswordVerifier passwordVerifier;
    private final Set<UUID> loggedInPlayers;
    private final List<String> registeredUsernames;

    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final boolean automaticallyLogInUponRegistration;
    private final List<LoginListener> listeners;

    public DatabaseRegistrationManager(JavaPlugin plugin, DatabaseManager databaseManager, boolean automaticallyLogInUponRegistration)
    {
        this.plugin = plugin;
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
    public void tryRegister(UUID uuid, String username, String password, Consumer<RegistrationResult> callback)
    {
        databaseManager.getRegistration(uuid).thenAccept(registration ->
        {
            if (registration != null)
            {
                callback.accept(RegistrationResult.ALREADY_REGISTERED);
                return;
            }

            databaseManager.tryRegisterPlayer(uuid, username, password).thenAccept(result ->
            {
                registeredUsernames.add(username);
                Scallywag.logger().info("Added new registration of player \"" + username + "\", uuid: " + uuid);

                if (automaticallyLogInUponRegistration)
                {
                    logIn(uuid, username, username);
                }
                callback.accept(RegistrationResult.SUCCESSFUL);

            }).exceptionally(exception ->
            {
                logException(exception);
                callback.accept(RegistrationResult.INTERNAL_ERROR);
                return null;
            });

        }).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(RegistrationResult.INTERNAL_ERROR);
            return null;
        });
    }

    @Override
    public void tryLogIn(UUID uuid, String username, String actualPassword, Consumer<LoginResult> callback)
    {
        if (isLoggedIn(uuid))
        {
            callback.accept(LoginResult.ALREADY_LOGGED_IN);
            return;
        }

        databaseManager.getRegistration(uuid).thenAccept(registration ->
        {
            if (registration == null)
            {
                callback.accept(LoginResult.NOT_REGISTERED);
                return;
            }

            var expectedPassword = registration.passwordHash();

            passwordVerifier.checkAsync(actualPassword, expectedPassword, isCorrectPassword ->
            {
                if (isCorrectPassword)
                {
                    var storedUsername = registration.username();
                    logIn(uuid, storedUsername, username);
                    callback.accept(LoginResult.SUCCESSFUL);
                }
                else
                {
                    callback.accept(LoginResult.WRONG_PASSWORD);
                }
            });

        }).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(LoginResult.INTERNAL_ERROR);
            return null;
        });
    }

    private synchronized void logIn(UUID uuid, String storedUsername, String username)
    {
        loggedInPlayers.add(uuid);

        if (!storedUsername.equals(username))
        {
            updatePlayerUsername(uuid, storedUsername, username, null);
        }

        Scallywag.logger().info("Player \"" + username + "\" successfully logged in (uuid: " + uuid + ")");

        Bukkit.getScheduler().runTask(plugin, () ->
        {
            for (var listener : listeners)
            {
                listener.onPlayerLoggedIn(uuid, username);
            }
        });
    }

    @Override
    public void tryLogOut(UUID uuid, String username)
    {
        if (loggedInPlayers.remove(uuid))
        {
            Bukkit.getScheduler().runTask(plugin, () ->
            {
                for (var listener : listeners)
                {
                    listener.onPlayerLoggedOut(uuid, username);
                }
            });
        }
    }

    @Override
    public void tryRemoveRegistration(UUID uuid, Consumer<RegistrationRemovalResult> callback)
    {
        databaseManager.deleteRegistrationWithUUID(uuid).thenAccept(registration ->
                callback.accept(processDeletedRegistration(registration))).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(RegistrationRemovalResult.INTERNAL_ERROR);
            return null;
        });
    }

    @Override
    public void tryRemoveRegistration(String username, Consumer<RegistrationRemovalResult> callback)
    {
        databaseManager.deleteRegistrationsWithUsername(username, 1).thenAccept(registration ->
                callback.accept(processDeletedRegistration(registration))).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(RegistrationRemovalResult.INTERNAL_ERROR);
            return null;
        });
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
    public <T> void updateRegistrationProperty(UUID uuid,
                                               RegistrationVariableProperty<T> property,
                                               T value,
                                               Consumer<UpdateResult> callback)
    {
        databaseManager.getRegistration(uuid).thenAccept(registration ->
            updateRegistrationProperty(registration, property, value, callback)).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(UpdateResult.INTERNAL_ERROR);
            return null;
        });
    }

    @Override
    public <T> void updateRegistrationProperty(String username,
                                               RegistrationVariableProperty<T> property,
                                               T value,
                                               Consumer<UpdateResult> callback)
    {
        databaseManager.getRegistration(username).thenAccept(registration ->
                updateRegistrationProperty(registration, property, value, callback)).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(UpdateResult.INTERNAL_ERROR);
            return null;
        });
    }

    private <T> void updateRegistrationProperty(@Nullable PlayerRegistration registration,
                                                        RegistrationVariableProperty<T> property,
                                                        T value,
                                                        Consumer<UpdateResult> callback)
    {
        if (registration == null)
        {
            callback.accept(UpdateResult.REGISTRATION_NOT_FOUND);
            return;
        }

        var uuid = registration.uuid();

        if (property == RegistrationVariableProperty.USERNAME)
        {
            var newUsername = (String) value;
            updatePlayerUsername(uuid, registration.username(), newUsername, callback);
        }
        else
        {
            if (property != RegistrationVariableProperty.PASSWORD)
            {
                Scallywag.logger().severe("Unexpected registration variable property: " + property.name());
                callback.accept(UpdateResult.INTERNAL_ERROR);
                return;
            }

            var newPassword = (String) value;
            updatePlayerPassword(uuid, newPassword, callback);
        }
    }

    private void updatePlayerUsername(UUID uuid, String storedName, String newUsername, @Nullable Consumer<UpdateResult> callback)
    {
        if (callback != null && storedName.equals(newUsername))
        {
            callback.accept(UpdateResult.VALUE_MATCHES);
            return;
        }

        registeredUsernames.remove(storedName);
        registeredUsernames.add(newUsername);

        databaseManager.updatePlayerUsername(uuid, newUsername).thenAccept(voidResult ->
        {
            Scallywag.logger().info("Updated username of player " + uuid + " to: " + newUsername);
            if (callback != null)
            {
                callback.accept(UpdateResult.SUCCESSFUL);
            }
        }).exceptionally(exception ->
        {
            logException(exception);
            if (callback != null)
            {
                callback.accept(UpdateResult.INTERNAL_ERROR);
            }
            return null;
        });
    }

    @Override
    public void tryUpdatePassword(UUID uuid, String currentPassword, String newPassword, Consumer<PasswordUpdateResult> callback)
    {
        if (!isLoggedIn(uuid))
        {
            callback.accept(PasswordUpdateResult.NOT_LOGGED_IN);
            return;
        }

        databaseManager.getRegistration(uuid).thenAccept(registration ->
        {
            if (registration == null)
            {
                Scallywag.logger().severe(uuid + "'s registration was somehow not found even though they are logged in");
                callback.accept(PasswordUpdateResult.INTERNAL_ERROR);
                return;
            }

            var expectedPassword = registration.passwordHash();
            passwordVerifier.checkAsync(currentPassword, expectedPassword, isCorrectPassword ->
            {
                if (isCorrectPassword)
                {
                    callback.accept(PasswordUpdateResult.SUCCESSFUL);

                    updatePlayerPassword(uuid, newPassword, updateResult ->
                    {
                        switch (updateResult)
                        {
                            case SUCCESSFUL -> callback.accept(PasswordUpdateResult.SUCCESSFUL);
                            case INTERNAL_ERROR -> callback.accept(PasswordUpdateResult.INTERNAL_ERROR);
                        }
                    });
                }
                else
                {
                    callback.accept(PasswordUpdateResult.WRONG_PASSWORD);
                }
            });
        }).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(PasswordUpdateResult.INTERNAL_ERROR);
            return null;
        });
    }

    private void updatePlayerPassword(UUID uuid, String newPassword, Consumer<UpdateResult> callback)
    {
        //Do not account for matching values because that would expose the password

        databaseManager.updatePlayerPassword(uuid, newPassword).thenAccept(voidResult ->
        {
            Scallywag.logger().info("Updated password of player " + uuid);
            callback.accept(UpdateResult.SUCCESSFUL);

        }).exceptionally(exception ->
        {
            logException(exception);
            callback.accept(UpdateResult.INTERNAL_ERROR);
            return null;
        });
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

    private void logException(Throwable exception)
    {
        Scallywag.logger().log(Level.SEVERE, "An exception occurred while performing registration operations", exception);
    }
}
