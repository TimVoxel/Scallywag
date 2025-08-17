package me.timpixel.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPoolImpl implements ConnectionPool
{
    private final DatabaseConnectionInfo connectionInfo;
    private final List<ReleasableConnection> pool;

    public ConnectionPoolImpl(List<Connection> connections, DatabaseConnectionInfo connectionInfo)
    {
        this.connectionInfo = connectionInfo;
        this.pool = new ArrayList<>(connections.size());

        for (var connection : connections)
        {
            var releasable = new ReleasableConnection(connection);
            this.pool.add(releasable);
        }
    }

    @Override
    public ReleasableConnection take() throws SQLException
    {
        for (var connection : pool)
        {
            if (connection.isFree())
            {
                return connection.take();
            }
        }

        var connection = ConnectionPool.createConnection(connectionInfo);
        var releasable = new ReleasableConnection(connection);
        pool.add(releasable);
        return releasable.take();
    }

    public int size()
    {
        return pool.size();
    }

    @Override
    public DatabaseConnectionInfo connectionInfo()
    {
        return connectionInfo;
    }
}
