package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.CommandLogger;
import me.timpixel.scallywag.RegistrationManager;
import me.timpixel.scallywag.exceptions.ScallywagNoAuthenticationException;
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
        RegistrationManager manager;
        try
        {
            manager = root.holder().registrationManager();
        }
        catch (ScallywagNoAuthenticationException exception)
        {
            return CommandLogger.error(sender, "Unable to register, no registration manager is set");
        }
        try
        {
            uuid = UUID.fromString(stringUUID);

            manager.tryRemoveRegistration(uuid, registrationRemovalResult ->
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

            manager.tryRemoveRegistration(username, registrationRemovalResult ->
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
        if (args.length == 1)
        {
            try
            {
                return root.holder().registrationManager().registeredUsernames();
            }
            catch (Exception exception)
            {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}
