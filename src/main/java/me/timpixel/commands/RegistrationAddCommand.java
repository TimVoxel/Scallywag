package me.timpixel.commands;

import me.timpixel.CommandLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class RegistrationAddCommand implements SubCommand
{
    private final RegistrationCommand root;

    public RegistrationAddCommand(RegistrationCommand root)
    {
        this.root = root;
    }

    @Override
    public String getName()
    {
        return "add";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String @NotNull [] args)
    {
        if (args.length < 3)
        {
            return CommandLogger.error(sender, "Specify the player uuid, username and password");
        }

        var stringUUID = args[0];
        UUID uuid;
        try
        {
            uuid = UUID.fromString(stringUUID);
        }
        catch (IllegalArgumentException exception)
        {
            return CommandLogger.error(sender, "Unable to parse uuid \"" + stringUUID + "\"");
        }

        var username = args[1];
        var password = args[2];

        root.getRegistrationManager().tryRegister(uuid, username, password, result ->
        {
            switch (result)
            {
                case SUCCESSFUL ->
                        CommandLogger.info(sender, "Successfully registered player \"" + username + "\", uuid: " + stringUUID);
                case INTERNAL_ERROR ->
                        CommandLogger.error(sender, "Unable to add registration due to an internal error");
                case ALREADY_REGISTERED ->
                        CommandLogger.warning(sender, "Player with uuid " + stringUUID + " is already registered. Use /registration modify if you wish to modify their registration");
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
