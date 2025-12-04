package me.timpixel.scallywag.database;

import me.timpixel.scallywag.DatabaseConnectionInfo;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPoolImpl implements ConnectionPool
{
    private final ReentrantLock accessLock = new ReentrantLock();
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
        accessLock.lock();
        try
        {
            for (var connection : pool)
            {
                if (connection.isFree())
                {
                    if (!connection.isValid())
                    {
                        connection.rebind(ConnectionPool.createConnection(connectionInfo));
                    }

                    return connection.take();
                }
            }

            var connection = ConnectionPool.createConnection(connectionInfo);
            var releasable = new ReleasableConnection(connection);
            pool.add(releasable);
            return releasable.take();
        }
        finally
        {
            accessLock.unlock();
        }
    }

    public int size()
    {
        int poolSize;
        accessLock.lock();
        try
        {
            poolSize = pool.size();
        }
        finally
        {
            accessLock.unlock();
        }
        return poolSize;
    }

    @Override
    public DatabaseConnectionInfo connectionInfo()
    {
        return connectionInfo;
    }

    @Override
    public @NotNull Iterator<ReleasableConnection> iterator()
    {
        return pool.iterator();
    }
}
