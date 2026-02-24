package me.timpixel.scallywag.commands;

import me.timpixel.scallywag.CommandLogger;
import me.timpixel.scallywag.RegistrationManager;
import me.timpixel.scallywag.RegistrationVariableProperty;
import me.timpixel.scallywag.exceptions.ScallywagNoAuthenticationException;
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
        if (args.length == 1)
        {
            return CommandLogger.error(sender, "Specify the property to modify");
        }
        if (args.length == 2)
        {
            return CommandLogger.error(sender, "Specify the new value");
        }

        var property = RegistrationVariableProperty.valueOf(args[1]);

        if (property == null)
        {
            return CommandLogger.error(sender, "Registrations have no \"" + args[1] + "\" property");
        }

        var stringUUID = args[0];
        var value = args[2];
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

            manager.updateRegistrationProperty(uuid, property, value, updateResult ->
            {
                switch (updateResult)
                {
                    case SUCCESSFUL ->
                            CommandLogger.info(sender, "Successfully changed " + uuid + " registration " + property.name() + " to " + value);
                    case REGISTRATION_NOT_FOUND ->
                            CommandLogger.error(sender, "Unable to find the registration with uuid " + stringUUID);
                    case VALUE_MATCHES ->
                            CommandLogger.warning(sender, "Nothing changed, the property " + property.name() + " already has that value");
                    case INTERNAL_ERROR ->
                            CommandLogger.error(sender, "Unable to modify registration due to an internal error");
                    case INVALID_PASSWORD -> CommandLogger.error(sender, "The password is too weak");
                }
            });
        }
        catch (IllegalArgumentException exception)
        {
            var username = args[0];

            manager.updateRegistrationProperty(username, property, value, updateResult ->
            {
                switch (updateResult)
                {
                    case SUCCESSFUL ->
                            CommandLogger.info(sender, "Successfully changed " + username + " registration " + property.name() + " to " + value);
                    case REGISTRATION_NOT_FOUND ->
                            CommandLogger.error(sender, "Unable to find the registration with username " + username);
                    case VALUE_MATCHES ->
                            CommandLogger.warning(sender, "Nothing changed, the property " + property.name() + " already has that value");
                    case INTERNAL_ERROR ->
                            CommandLogger.error(sender, "Unable to modify registration due to an internal error");
                    case INVALID_PASSWORD -> CommandLogger.error(sender, "The password is too weak");
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
            catch (ScallywagNoAuthenticationException exception)
            {
                return Collections.emptyList();
            }
        }
        else if (args.length == 2)
        {
            return RegistrationVariableProperty.propertyNames();
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
