package me.timpixel.commands;

import me.timpixel.CommandLogger;
import me.timpixel.RegistrationManager;
import me.timpixel.ScallywagPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PasswordCommand implements TabExecutor
{
    private final RegistrationManager registrationManager;
    private final boolean allowPlayerPasswordChanging;

    public PasswordCommand(RegistrationManager registrationManager, boolean allowPlayerPasswordChanging)
    {
        this.registrationManager = registrationManager;
        this.allowPlayerPasswordChanging = allowPlayerPasswordChanging;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String @NotNull[] args)
    {
        if (!(sender instanceof Player player))
        {
            return CommandLogger.error(sender, "This command can only be used by players");
        }

        if (!allowPlayerPasswordChanging && !ScallywagPlugin.hasAdminPermission(sender))
        {
            return CommandLogger.error(sender, "Players are not allowed to change their passwords on this server");
        }

        if (args.length < 2)
        {
            return CommandLogger.error(sender, "First specify the current password, then the new one");
        }

        var currentPassword = args[0];
        var newPassword = args[1];

        registrationManager.tryUpdatePassword(player.getUniqueId(), currentPassword, newPassword, passwordUpdateResult ->
        {
            switch (passwordUpdateResult)
            {
                case SUCCESSFUL -> CommandLogger.info(sender, "Successfully logged in!");
                case INTERNAL_ERROR -> CommandLogger.error(sender, "Unable to register due to an internal error");
                case NOT_LOGGED_IN -> CommandLogger.error(sender, "You are not logged in. Please log in and try again");
                case WRONG_PASSWORD -> CommandLogger.error(sender, "Current password does not match");
            }
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args)
    {
        return Collections.emptyList();
    }
}
