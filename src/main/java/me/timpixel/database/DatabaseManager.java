package me.timpixel.database;

import me.timpixel.Scallywag;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MutableCallSite;
import java.sql.SQLException;

public class DatabaseManager
{
    private final ConnectionPool connectionPool;

    private DatabaseManager(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public static @Nullable DatabaseManager tryCreate(DatabaseConnectionInfo connectionInfo)
    {
        try
        {
            var initialSize = 1;
            var pool = ConnectionPool.create(connectionInfo, initialSize);
            return new DatabaseManager(pool);
        }
        catch (SQLException exception)
        {
            Scallywag.logger().severe(exception.getMessage());
            return null;
        }
    }

    public void init()
    {
        try
        {
            var connection = connectionPool.take();

            try (var statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS scallywag_players(uuid BINARY(16) primary key, username VARCHAR(16), password VARCHAR(60))"))
            {
                statement.execute();
            }
        }

        catch (SQLException exception)
        {
            Scallywag.logger().severe(exception.getMessage());
        }
    }
}
