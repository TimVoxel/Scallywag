package me.timpixel.commands;

import me.timpixel.CommandLogger;
import me.timpixel.RegistrationManager;
import me.timpixel.Scallywag;
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
    private final boolean allowPlayerRegistration;

    public RegisterCommand(RegistrationManager registrationManager, boolean allowPlayerRegistration)
    {
        this.registrationManager = registrationManager;
        this.allowPlayerRegistration = allowPlayerRegistration;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        if (!(sender instanceof Player player))
        {
            return CommandLogger.error(sender, "This command can only be used by players");
        }

        if (!allowPlayerRegistration && !Scallywag.hasAdminPermission(sender))
        {
            return CommandLogger.error(sender, "Players are not allowed to register on this server. If you are wanted on the server, you were most likely already registered by the administration");
        }

        if (args.length == 0)
        {
            return CommandLogger.error(sender, "Specify a password");
        }

        var password = args[0];
        registrationManager.tryRegister(player.getUniqueId(), player.getName(), password, result ->
        {
            switch (result)
            {
                case SUCCESSFUL -> CommandLogger.info(sender, "Registration successful!");
                case INTERNAL_ERROR -> CommandLogger.error(sender, "Unable to register due to an internal error");
                case ALREADY_REGISTERED -> CommandLogger.warning(sender, "You are already registered! Use /login to login instead");
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
