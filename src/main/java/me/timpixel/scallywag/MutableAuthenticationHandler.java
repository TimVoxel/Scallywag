package me.timpixel.scallywag;

import me.timpixel.scallywag.results.RegistrationRemovalResult;
import me.timpixel.scallywag.results.RegistrationResult;
import me.timpixel.scallywag.results.UpdateResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MutableAuthenticationHandler extends AuthenticationHandler
{
    CompletableFuture<RegistrationResult> tryRegister(UUID uuid, String username, String password);

    CompletableFuture<UpdateResult> tryUpdateRegistration(String oldUsername, String newUsername, String newPassword);

    CompletableFuture<UpdateResult> tryUpdateRegistration(UUID uuid, String newUsername, String newPassword);

    CompletableFuture<RegistrationRemovalResult> tryRemoveRegistration(String username);

    CompletableFuture<RegistrationRemovalResult> tryRemoveRegistration(UUID uuid);

    List<String> getRegisteredUsernames();
}

