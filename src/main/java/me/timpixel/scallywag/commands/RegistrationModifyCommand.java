package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.CommandLogger;
import me.timpixel.scallywag.results.UpdateResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RegistrationModifyCommand implements SubCommand
{
    private final RegistrationCommand root;

    public RegistrationModifyCommand(RegistrationCommand root)
    {
        this.root = root;
    }

    @Override
    public String getName()
    {
        return "modify";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String @NotNull [] args)
    {
        if (args.length == 0)
        {
            return CommandLogger.error(sender, "Specify the target's uuid or username");
        }
        if (args.length < 3)
        {
            return CommandLogger.error(sender, "Specify the new username and password");
        }

        var stringUUID = args[0];
        var newUsername = args[1];
        var newPassword = args[2];
        UUID uuid;

        try
        {
            uuid = UUID.fromString(stringUUID);
            root.manager().tryUpdateRegistration(uuid, newUsername, newPassword).thenAccept(r -> announceResult(r, sender, stringUUID, newUsername));
        }
        catch (IllegalArgumentException exception)
        {
            var oldUsername = args[0];
            root.manager().tryUpdateRegistration(oldUsername, newUsername, newPassword).thenAccept(r -> announceResult(r, sender, oldUsername, newUsername));
        }
        return true;
    }

    private void announceResult(UpdateResult result, CommandSender sender, String identifier, String newUsername)
    {
        switch (result)
        {
            case SUCCESSFUL -> CommandLogger.info(sender, "Successfully changed " + newUsername + "'s registration");
            case NOT_FOUND -> CommandLogger.error(sender, "Unable to find the registration of " + identifier);
            case IDENTICAL -> CommandLogger.warning(sender, "Nothing changed, the properties already have that value");
            case INTERNAL_ERROR ->
                    CommandLogger.error(sender, "Unable to modify registration due to an internal error");
            case INVALID -> CommandLogger.error(sender, "The values provided are invalid");
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
