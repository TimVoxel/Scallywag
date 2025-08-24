package me.timpixel.scallywag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class PasswordManager
{
    private static final int THREAD_POOL_SIZE = 3;
    private final ExecutorService executorService;

    private @Nullable Function<String, Boolean> validator;

    public PasswordManager()
    {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void checkAsync(String expected, String actual, Consumer<Boolean> callback)
    {
        CompletableFuture.supplyAsync(() -> BCrypt.checkpw(expected, actual)).thenAccept(callback);
    }

    public boolean isValid(String password)
    {
        if (validator == null)
        {
            return true;
        }
        return validator.apply(password);
    }

    public void shutdown()
    {
        executorService.shutdown();
    }

    public void setValidator(@NotNull Function<String, Boolean> validator)
    {
        this.validator = validator;
    }
}
