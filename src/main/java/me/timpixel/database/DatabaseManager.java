package me.timpixel.database;

import me.timpixel.PlayerRegistration;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager
{
    private final ExecutorService executorService;
    private final ConnectionPool connectionPool;

    private DatabaseManager(ConnectionPool connectionPool)
    {
        this.executorService = Executors.newFixedThreadPool(10);
        this.connectionPool = connectionPool;
    }

    public static DatabaseManager tryCreate(DatabaseConnectionInfo connectionInfo) throws SQLException
    {
        var initialSize = 1;
        var pool = ConnectionPool.create(connectionInfo, initialSize);
        return new DatabaseManager(pool);
    }

    public void init() throws SQLException
    {
        var connection = connectionPool.take();

        try (var statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS scallywag_players(uuid BINARY(16) primary key, username VARCHAR(16), password VARCHAR(60))"))
        {
            statement.execute();
        }
        connection.free();
    }

    public Future<@Nullable PlayerRegistration> getRegistration(UUID uuid)
    {
        return executorService.submit(() ->
        {
            var connection = connectionPool.take();

            try (var query = connection.prepareStatement("SELECT * FROM scallywag_players WHERE uuid = ? LIMIT 1"))
            {
                var binary = TypeConversionUtil.uuidToBytes(uuid);
                query.setBytes(1, binary);

                var resultSet = query.executeQuery();
                var registration = mapToPlayerRegistration(uuid, resultSet);
                connection.free();
                return registration;
            }
        });
    }

    public Future<@Nullable PlayerRegistration> getRegistration(String username)
    {
        return executorService.submit(() ->
        {
            var connection = connectionPool.take();

            try (var query = connection.prepareStatement("SELECT * FROM scallywag_players WHERE username = ? LIMIT 1"))
            {
                query.setString(1, username);

                var resultSet = query.executeQuery();
                var registration = mapToPlayerRegistration(resultSet);
                connection.free();
                return registration;
            }
        });
    }

    public Future<Void> tryRegisterPlayer(UUID uuid, String username, String password)
    {
        return executorService.submit(() ->
        {
            var hashedPassword = hashPassword(password);
            var byteUUID = TypeConversionUtil.uuidToBytes(uuid);

            var connection = connectionPool.take();

            try (var statement = connection.prepareStatement("INSERT INTO scallywag_players VALUES (?, ?, ?)"))
            {
                statement.setBytes(1, byteUUID);
                statement.setString(2, username);
                statement.setString(3, hashedPassword);

                statement.executeUpdate();
            }
            connection.free();
            return null;
        });
    }

    public Future<Void> updatePlayerUsername(UUID uuid, String username)
    {
        return executorService.submit(() ->
        {
            var byteUUID = TypeConversionUtil.uuidToBytes(uuid);
            var connection = connectionPool.take();

            try (var statement = connection.prepareStatement("UPDATE scallywag_players SET username = ? WHERE uuid = ?"))
            {
                statement.setString(1, username);
                statement.setBytes(2, byteUUID);
                statement.executeUpdate();
            }
            connection.free();
            return null;
        });
    }

    public Future<Void> updatePlayerPassword(UUID uuid, String password)
    {
        return executorService.submit(() ->
        {
            var byteUUID = TypeConversionUtil.uuidToBytes(uuid);
            var hashedPassword = hashPassword(password);
            var connection = connectionPool.take();

            try (var statement = connection.prepareStatement("UPDATE scallywag_players SET password = ? WHERE uuid = ?"))
            {
                statement.setString(1, hashedPassword);
                statement.setBytes(2, byteUUID);
                statement.executeUpdate();
            }

            connection.free();
            return null;
        });
    }

    public Future<@Nullable PlayerRegistration> deleteRegistrationsWithUsername(String username, int limit)
    {
        return executorService.submit(() ->
        {
            var connection = connectionPool.take();
            var registration = getRegistration(username).get();

            if (registration == null)
            {
                connection.free();
                return null;
            }
            else
            {
                var uuid = TypeConversionUtil.uuidToBytes(registration.uuid());

                try (var statement = connection.prepareStatement("DELETE FROM scallywag_players WHERE uuid = ? LIMIT ?"))
                {
                    statement.setBytes(1, uuid);
                    statement.setInt(2, limit);
                    statement.executeUpdate();
                }

                connection.free();
                return registration;
            }
        });
    }

    public Future<@Nullable PlayerRegistration> deleteRegistrationWithUUID(UUID uuid)
    {
        return executorService.submit(() ->
        {
            var byteUUID = TypeConversionUtil.uuidToBytes(uuid);
            var connection = connectionPool.take();
            var registration = getRegistration(uuid).get();

            if (registration == null)
            {
                connection.free();
                return null;
            }
            else
            {
                try (var statement = connection.prepareStatement("DELETE FROM scallywag_players WHERE uuid = ? LIMIT 1"))
                {
                    statement.setBytes(1, byteUUID);
                    statement.executeUpdate();
                }

                connection.free();
                return registration;
            }
        });
    }

    public Future<List<String>> getRegisteredUsernames()
    {
        return executorService.submit(() ->
        {
            var usernames = new ArrayList<String>();
            var connection = connectionPool.take();

            try (var query = connection.prepareStatement("SELECT username FROM scallywag_players"))
            {
                var resultSet = query.executeQuery();

                while (resultSet.next())
                {
                    var username = resultSet.getString(1);
                    usernames.add(username);
                }
            }
            connection.free();
            return usernames;
        });
    }

    private @Nullable PlayerRegistration mapToPlayerRegistration(UUID uuid, ResultSet resultSet) throws SQLException
    {
        if (resultSet.next())
        {
            var username = resultSet.getString(2);
            var password = resultSet.getString(3);
            return new PlayerRegistration(uuid, username, password);
        }
        return null;
    }

    private @Nullable PlayerRegistration mapToPlayerRegistration(ResultSet resultSet) throws SQLException
    {
        if (resultSet.next())
        {
            var uuid = TypeConversionUtil.bytesToUUID(resultSet.getBytes(1));
            var username = resultSet.getString(2);
            var password = resultSet.getString(3);
            return new PlayerRegistration(uuid, username, password);
        }
        return null;
    }

    private static String hashPassword(String password)
    {
        var salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    public void shutdown() throws SQLException
    {
        executorService.shutdown();

        for (var connection : connectionPool)
        {
            connection.close();
        }
    }
}
