package me.timpixel.scallywag.exceptions;

public class ScallywagAuthenticationSetException extends ScallywagException
{
    public ScallywagAuthenticationSetException()
    {
        super("Attempting to set a new authentication manager while Scallywag is already initialized");
    }
}
