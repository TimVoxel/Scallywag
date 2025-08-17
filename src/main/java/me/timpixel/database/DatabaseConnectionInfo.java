package me.timpixel.database;

import me.timpixel.Scallywag;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("DatabaseConnectionInfo")
public record DatabaseConnectionInfo(String url, String user, String password) implements ConfigurationSerializable
{
    @Override
    public @NotNull Map<String, Object> serialize()
    {
        return Map.of(
                "url", url,
                "user", user,
                "password", password
        );
    }

    @SuppressWarnings("unused")
    public static DatabaseConnectionInfo deserialize(Map<String, Object> map)
    {
        String url = "";
        String user = "";
        String password = "";

        if (map.containsKey("url"))
        {
            url = (String) map.get("url");
        }
        else
        {
            Scallywag.logger().warning("Did not find \"url\" while deserializing database connection info");
        }

        if (map.containsKey("user"))
        {
            user = (String) map.get("user");
        }
        else
        {
            Scallywag.logger().warning("Did not find \"user\" while deserializing database connection info");
        }

        if (map.containsKey("password"))
        {
            password = (String) map.get("password");
        }
        else
        {
            Scallywag.logger().warning("Did not find \"password\" while deserializing database connection info");
        }

        return new DatabaseConnectionInfo(url, user, password);
    }
}
