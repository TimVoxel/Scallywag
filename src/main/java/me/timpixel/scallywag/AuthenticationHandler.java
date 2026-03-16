package me.timpixel.scallywag;

import me.timpixel.scallywag.results.PasswordVerificationResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AuthenticationHandler
{
    CompletableFuture<PasswordVerificationResult> verify(UUID uuid, String username, String providedPassword);
}
