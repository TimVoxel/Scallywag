package me.timpixel.scallywag.exceptions;

public class ScallywagNativeSourceException extends ScallywagException
{
    public ScallywagNativeSourceException()
    {
        super("Attempting to set an external authentication manager while Scallywag uses native authentication");
    }
}
