package me.timpixel.scallywag.exceptions;

public class ScallywagUninitializedException extends ScallywagException
{
    public ScallywagUninitializedException()
    {
        super("Attempting to use Scallywag before it is initialized");
    }
}