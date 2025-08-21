package me.timpixel;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class PasswordVerifier
{
    private static final int THREAD_POOL_SIZE = 3;
    private final ExecutorService executorService;

    public PasswordVerifier()
    {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void checkAsync(String expected, String actual, Consumer<Boolean> callback)
    {
        CompletableFuture.supplyAsync(() -> BCrypt.checkpw(expected, actual)).thenAccept(callback);
    }

    public void shutdown()
    {
        executorService.shutdown();
    }
}
