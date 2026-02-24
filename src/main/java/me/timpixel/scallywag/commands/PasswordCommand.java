package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.*;
import me.timpixel.scallywag.exceptions.ScallywagNoAuthenticationException;
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
    private final RegistrationHolder holder;
    private final boolean allowPlayerPasswordChanging;

    public PasswordCommand(RegistrationHolder holder, boolean allowPlayerPasswordChanging)
    {
        this.holder = holder;
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

        RegistrationManager manager;
        try
        {
            manager = holder.registrationManager();
        }
        catch (ScallywagNoAuthenticationException exception)
        {
            return CommandLogger.error(sender, "Unable to register, no registration manager is set");
        }
        manager.tryUpdatePassword(player.getUniqueId(), currentPassword, newPassword, passwordUpdateResult ->
        {
            switch (passwordUpdateResult)
            {
                case SUCCESSFUL -> CommandLogger.info(sender, "Password changed successfully!");
                case INTERNAL_ERROR -> CommandLogger.error(sender, "Unable to register due to an internal error");
                case NOT_LOGGED_IN -> CommandLogger.error(sender, "You are not logged in. Please log in and try again");
                case WRONG_PASSWORD -> CommandLogger.error(sender, "Current password does not match");
                case INVALID_PASSWORD -> CommandLogger.error(sender, "The password is too weak");
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
