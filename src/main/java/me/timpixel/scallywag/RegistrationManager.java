package me.timpixel.scallywag;

import me.timpixel.scallywag.database.DatabaseManager;
import me.timpixel.scallywag.results.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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

     <T> void updateRegistrationProperty(UUID uuid, RegistrationVariableProperty<T> property, T value, Consumer<UpdateResult> callback);
     <T> void updateRegistrationProperty(String username, RegistrationVariableProperty<T> property, T value, Consumer<UpdateResult> callback);

     void tryUpdatePassword(UUID uuid, String currentPassword, String newPassword, Consumer<PasswordUpdateResult> callback);

     List<String> registeredUsernames();

     void setPasswordValidator(@NotNull JavaPlugin setter, @NotNull Function<String, Boolean> validator);
}

