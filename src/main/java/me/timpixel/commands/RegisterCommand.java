package me.timpixel.commands;

import me.timpixel.CommandLogger;
import me.timpixel.RegistrationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class RegisterCommand implements TabExecutor
{
    private final RegistrationManager registrationManager;

    public RegisterCommand(RegistrationManager registrationManager)
    {
        this.registrationManager = registrationManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        if (!(sender instanceof Player player))
        {
            return CommandLogger.error(sender, "This command can only be used by players");
        }

        if (args.length == 0)
        {
            return CommandLogger.error(sender, "Specify a password");
        }

        var password = args[0];

        return switch (registrationManager.tryRegister(player.getUniqueId(), player.getName(), password))
        {
            case SUCCESSFUL -> CommandLogger.info(sender, "Registration successful!");
            case INTERNAL_ERROR -> CommandLogger.error(sender, "Unable to register due to an internal error");
            case ALREADY_REGISTERED -> CommandLogger.warning(sender, "You are already registered! Use /login to login instead");
        };
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
