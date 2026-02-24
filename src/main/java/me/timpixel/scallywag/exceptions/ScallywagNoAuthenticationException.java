package me.timpixel.scallywag.exceptions;

public class ScallywagNoAuthenticationException extends ScallywagException
{
    public ScallywagNoAuthenticationException()
    {
        super("Attempting to use Scallywag functionality but no authentication manager is set");
    }
}
