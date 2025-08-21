package me.timpixel;

import me.timpixel.database.DatabaseManager;
import me.timpixel.listeners.LoginListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public interface RegistrationManager
{
    static RegistrationManager database(JavaPlugin plugin,
                                        DatabaseManager databaseManager,
                                        boolean automaticallyLogInUponRegistration)
    {
        return new DatabaseRegistrationManager(plugin, databaseManager, automaticallyLogInUponRegistration);
    }

     void tryLogIn(UUID uuid, String username, String password, Consumer<LoginResult> callback);
     void tryLogOut(UUID uuid, String username);

     void tryRegister(UUID uuid, String username, String password, Consumer<RegistrationResult> callback);
     void tryRemoveRegistration(UUID uuid, Consumer<RegistrationRemovalResult> callback);
     void tryRemoveRegistration(String username, Consumer<RegistrationRemovalResult> callback);

     boolean isLoggedIn(UUID uuid);
     boolean isLoggedIn(Player player);

     void addListener(LoginListener listener);
     void removeListener(LoginListener listener);

     <T> void updateRegistrationProperty(UUID uuid, RegistrationVariableProperty<T> property, T value, Consumer<UpdateResult> callback);
     <T> void updateRegistrationProperty(String username, RegistrationVariableProperty<T> property, T value, Consumer<UpdateResult> callback);

     void tryUpdatePassword(UUID uuid, String currentPassword, String newPassword, Consumer<PasswordUpdateResult> callback);

     List<String> registeredUsernames();
}

