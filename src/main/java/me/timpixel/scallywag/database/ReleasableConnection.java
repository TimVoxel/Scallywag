package me.timpixel.scallywag.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReleasableConnection
{
    private final Connection connection;
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

    public void close() throws SQLException
    {
        connection.close();
    }
}
