package me.timpixel.database;

import me.timpixel.PlayerRegistration;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager
{
    private final ConnectionPool connectionPool;

    private DatabaseManager(ConnectionPool connectionPool)
    {
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

    public @Nullable PlayerRegistration getRegistration(UUID uuid) throws SQLException
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
    }

    public @Nullable PlayerRegistration getRegistration(String username) throws SQLException
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

    public void tryRegisterPlayer(UUID uuid, String username, String password) throws SQLException
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
    }

    public void updatePlayerUsername(UUID uuid, String username) throws SQLException
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
    }

    public void updatePlayerPassword(UUID uuid, String password) throws SQLException
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
    }

    public @Nullable PlayerRegistration deleteRegistrationsWithUsername(String username, int limit) throws SQLException
    {
        var connection = connectionPool.take();
        var registration = getRegistration(username);

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
    }

    public @Nullable PlayerRegistration deleteRegistrationWithUUID(UUID uuid) throws SQLException
    {
        var byteUUID = TypeConversionUtil.uuidToBytes(uuid);
        var connection = connectionPool.take();
        var registration = getRegistration(uuid);

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
    }

    public List<String> getRegisteredUsernames() throws SQLException
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
    }

    private static String hashPassword(String password)
    {
        var salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }
}
