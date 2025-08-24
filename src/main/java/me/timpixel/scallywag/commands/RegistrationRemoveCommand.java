package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.CommandLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RegistrationRemoveCommand implements SubCommand
{
    private final RegistrationCommand root;

    public RegistrationRemoveCommand(RegistrationCommand root)
    {
        this.root = root;
    }

    @Override
    public String getName()
    {
        return "remove";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String @NotNull [] args)
    {
        if (args.length == 0)
        {
            return CommandLogger.error(sender, "Specify the player uuid or username");
        }

        var stringUUID = args[0];
        UUID uuid;
        try
        {
            uuid = UUID.fromString(stringUUID);

            root.getRegistrationManager().tryRemoveRegistration(uuid, registrationRemovalResult ->
            {
                switch (registrationRemovalResult)
                {
                    case SUCCESSFUL ->
                            CommandLogger.info(sender, "Successfully removed registration of player with uuid " + stringUUID);
                    case INTERNAL_ERROR ->
                            CommandLogger.error(sender, "Unable to remove registration due to an internal error");
                    case NOT_FOUND ->
                            CommandLogger.error(sender, "Unable to find the registration with uuid " + stringUUID);
                }
            });
        }
        catch (IllegalArgumentException exception)
        {
            var username = args[0];

            root.getRegistrationManager().tryRemoveRegistration(username, registrationRemovalResult ->
            {
                switch (registrationRemovalResult)
                {
                    case SUCCESSFUL ->
                            CommandLogger.info(sender, "Successfully removed registration of player with username " + username);
                    case INTERNAL_ERROR ->
                            CommandLogger.error(sender, "Unable to remove registration due to an internal error");
                    case NOT_FOUND ->
                            CommandLogger.error(sender, "Unable to find the registration of player with username \"" + username + "\"");
                }
            });
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args)
    {
        return args.length == 1
                ? root.getRegistrationManager().registeredUsernames()
                : Collections.emptyList();
    }
}
