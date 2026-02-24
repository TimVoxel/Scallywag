package me.timpixel.scallywag;

import me.timpixel.scallywag.results.PasswordUpdateResult;
import me.timpixel.scallywag.results.RegistrationRemovalResult;
import me.timpixel.scallywag.results.RegistrationResult;
import me.timpixel.scallywag.results.UpdateResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RegistrationManager
{
    void tryRegister(UUID uuid, String username, String password, Consumer<RegistrationResult> callback);
    void tryRemoveRegistration(UUID uuid, Consumer<RegistrationRemovalResult> callback);
    void tryRemoveRegistration(String username, Consumer<RegistrationRemovalResult> callback);
    void setPasswordValidator(@NotNull JavaPlugin setter, @NotNull Function<String, Boolean> validator);
    <T> void updateRegistrationProperty(UUID uuid, RegistrationVariableProperty<T> property, T value, Consumer<UpdateResult> callback);
    <T> void updateRegistrationProperty(String username, RegistrationVariableProperty<T> property, T value, Consumer<UpdateResult> callback);
    void tryUpdatePassword(UUID uuid, String currentPassword, String newPassword, Consumer<PasswordUpdateResult> callback);
    List<String> registeredUsernames();
}
