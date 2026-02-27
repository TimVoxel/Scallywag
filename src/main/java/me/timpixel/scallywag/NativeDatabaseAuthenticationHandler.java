package me.timpixel.scallywag;

import me.timpixel.scallywag.database.NativeDatabaseConnector;
import me.timpixel.scallywag.results.*;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import org.mindrot.jbcrypt.*;

public class NativeDatabaseAuthenticationHandler implements MutableAuthenticationHandler
{
    private final ExecutorService executorService;
    private final NativeDatabaseConnector connector;
    private final List<String> registeredUsernames;

    public NativeDatabaseAuthenticationHandler(ExecutorService executorService, NativeDatabaseConnector connector)
    {
        this.executorService = executorService;
        this.connector = connector;
        registeredUsernames = new ArrayList<>();

        try
        {
            var registeredInDatabase = connector.getRegisteredUsernames();
            registeredUsernames.addAll(registeredInDatabase.get());
        }
        catch (Exception exception)
        {
            ScallywagPlugin.logger().log(Level.SEVERE, "An error occurred while obtaining the list of all registered usernames", exception);
        }
    }

    @Override
    public CompletableFuture<PasswordVerificationResult> verify(UUID uuid, String username, String providedPassword)
    {
        return connector.getRegistration(uuid).thenCompose(r ->
        {
            if (r == null)
            {
                return CompletableFuture.completedFuture(PasswordVerificationResult.NOT_FOUND);
            }

            return CompletableFuture.supplyAsync(() -> BCrypt.checkpw(providedPassword, r.passwordHash())
                    ? PasswordVerificationResult.SUCCESSFUL
                    : PasswordVerificationResult.INCORRECT, executorService);
        });
    }

    @Override
    public CompletableFuture<RegistrationResult> tryRegister(UUID uuid, String username, String password)
    {
        return connector.getRegistration(uuid).thenCompose(registration ->
        {
            if (registration != null)
            {
                return CompletableFuture.completedFuture(RegistrationResult.ALREADY_REGISTERED);
            }

            return connector.register(uuid, username, password).thenApply(result ->
            {
                registeredUsernames.add(username);
                return RegistrationResult.SUCCESSFUL;
            });
        });
    }

    @Override
    public CompletableFuture<RegistrationRemovalResult> tryRemoveRegistration(UUID uuid)
    {
        return connector.deleteRegistrationWithUUID(uuid).thenApply(this::processDeletedRegistration);
    }

    @Override
    public CompletableFuture<RegistrationRemovalResult> tryRemoveRegistration(String username)
    {
        return connector.deleteRegistrationsWithUsername(username, 1).thenApply(this::processDeletedRegistration);
    }

    private RegistrationRemovalResult processDeletedRegistration(@Nullable PlayerRegistration deletedRegistration)
    {
        if (deletedRegistration != null)
        {
            registeredUsernames.remove(deletedRegistration.username());
            return RegistrationRemovalResult.SUCCESSFUL;
        }
        else
        {
            return RegistrationRemovalResult.NOT_FOUND;
        }
    }

    @Override
    public CompletableFuture<UpdateResult> tryUpdateRegistration(String oldUsername, String newUsername, String newPassword)
    {
        return connector.getRegistration(oldUsername).thenCompose(r ->
        {
            if (r == null)
            {
                return CompletableFuture.completedFuture(UpdateResult.NOT_FOUND);
            }

            registeredUsernames.remove(oldUsername);
            registeredUsernames.add(newUsername);

            return connector.tryUpdateRegistration(oldUsername, newUsername, newPassword).thenCompose(a -> CompletableFuture.completedFuture(UpdateResult.SUCCESSFUL));
        });
    }

    @Override
    public CompletableFuture<UpdateResult> tryUpdateRegistration(UUID uuid, String newUsername, String newPassword)
    {
        return connector.getRegistration(uuid).thenCompose(r ->
        {
            if (r == null)
            {
                return CompletableFuture.completedFuture(UpdateResult.NOT_FOUND);
            }

            registeredUsernames.remove(r.username());
            registeredUsernames.add(newUsername);

            return connector.tryUpdateRegistration(uuid, newUsername, newPassword).thenCompose(a -> CompletableFuture.completedFuture(UpdateResult.SUCCESSFUL));
        });
    }

    @Override
    public List<String> getRegisteredUsernames()
    {
        return registeredUsernames;
    }
}
