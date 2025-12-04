package me.timpixel.scallywag.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReleasableConnection
{
    private Connection connection;
    private boolean isFree;

    public ReleasableConnection(Connection connection)
    {
        this.isFree = true;
        this.connection = connection;
    }

    public boolean isFree() { return isFree; }

    public ReleasableConnection take()
    {
        isFree = false;
        return this;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return connection.prepareStatement(sql);
    }

    public void free()
    {
        isFree = true;
    }

    public void rebind(Connection newConnection) throws SQLException
    {
        close();
        connection = newConnection;
        free();
    }

    public void close() throws SQLException
    {
        connection.close();
    }

    public boolean isValid() throws SQLException
    {
        return connection.isValid(1);
    }
}
