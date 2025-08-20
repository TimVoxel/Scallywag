package me.timpixel;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PasswordVerifier
{
    private static final int THREAD_POOL_SIZE = 3;
    private final ExecutorService executorService;

    public PasswordVerifier()
    {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public Future<Boolean> verify(String expected, String actual)
    {
        return executorService.submit(() -> BCrypt.checkpw(expected, actual));
    }

    public void shutdown()
    {
        executorService.shutdown();
    }
}
