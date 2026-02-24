package me.timpixel.scallywag.exceptions;

public class ScallywagException extends Exception
{
    public ScallywagException(String message, Throwable parent)
    {
        super(message, parent);
    }

    public ScallywagException(String message)
    {
        this(message, null);
    }
}
