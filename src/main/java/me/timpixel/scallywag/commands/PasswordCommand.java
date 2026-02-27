package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.*;
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
    private final AuthenticationManager manager;
    private final boolean allowPlayerPasswordChanging;

    public PasswordCommand(AuthenticationManager manager, boolean allowPlayerPasswordChanging)
    {
        this.manager = manager;
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

        if (args.length < 1)
        {
            return CommandLogger.error(sender, "Specify the new password");
        }

        var newPassword = args[0];

        manager.tryUpdatePassword(player.getUniqueId(), player.getName(), newPassword).thenAccept(r ->
        {
            switch (r)
            {
                case SUCCESSFUL: CommandLogger.info(sender, "Password changed successfully!"); break;
                case INTERNAL_ERROR: CommandLogger.error(sender, "Unable to change password due to an internal error"); break;
                case IDENTICAL:
                case INVALID: CommandLogger.error(sender, "The password you provided is invalid"); break;
                case NOT_FOUND: CommandLogger.error(sender, "You are not registered, register with a password first"); break;
                case UNSUPPORTED: CommandLogger.error(sender, "The current authentication handler does not allow editing registrations"); break;
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
