package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.CommandLogger;
import me.timpixel.scallywag.results.RegistrationRemovalResult;
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
            root.manager().tryRemoveRegistration(uuid).thenAccept(r -> announceResult(r, sender, stringUUID));
        }
        catch (IllegalArgumentException exception)
        {
            var username = args[0];
            root.manager().tryRemoveRegistration(username).thenAccept(r -> announceResult(r, sender, username));
        }
        return true;
    }

    private void announceResult(RegistrationRemovalResult result, CommandSender sender, String identifier)
    {
        switch (result)
        {
            case SUCCESSFUL ->
                    CommandLogger.info(sender, "Successfully removed registration of player with username " + identifier);
            case INTERNAL_ERROR ->
                    CommandLogger.error(sender, "Unable to remove registration due to an internal error");
            case NOT_FOUND ->
                    CommandLogger.error(sender, "Unable to find the registration of player with username \"" + identifier + "\"");
            case UNSUPPORTED ->
                    CommandLogger.error(sender, "The current authentication handler does not allow editing registrations");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args)
    {
        if (args.length == 1)
        {
            return root.manager().registeredUsernames();
        }
        return Collections.emptyList();
    }
}