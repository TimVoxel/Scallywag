package me.timpixel.scallywag;

import java.util.UUID;

public record PlayerRegistration(UUID uuid, String username, String passwordHash)
{
}
