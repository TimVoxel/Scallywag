package me.timpixel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public interface ConnectionPool
{
    ReleasableConnection take() throws SQLException;
    DatabaseConnectionInfo connectionInfo();
    int size();

    static ConnectionPoolImpl create(DatabaseConnectionInfo connectionInfo, int initialSize) throws SQLException
    {
        var pool = new ArrayList<Connection>(initialSize);

        for (var i = 0; i < initialSize; i++)
        {
            var connection = createConnection(connectionInfo);
            pool.add(connection);
        }
        return new ConnectionPoolImpl(pool, connectionInfo);
    }

    static Connection createConnection(DatabaseConnectionInfo connectionInfo) throws SQLException
    {
        return DriverManager.getConnection(
                connectionInfo.url(),
                connectionInfo.user(),
                connectionInfo.password());
    }
}
