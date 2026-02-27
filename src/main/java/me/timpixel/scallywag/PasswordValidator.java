package me.timpixel.scallywag;

@FunctionalInterface
public interface PasswordValidator
{
    boolean isValid(String password);
}
