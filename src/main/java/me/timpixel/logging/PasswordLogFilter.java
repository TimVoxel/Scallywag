package me.timpixel.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.core.Logger;

public class PasswordLogFilter extends AbstractFilter
{
    @Override
    public Result filter(LogEvent event)
    {
        return event == null
                ? Result.NEUTRAL
                : shouldLog(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t)
    {
        return shouldLog(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params)
    {
        return shouldLog(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t)
    {
        return msg == null
                ? Result.NEUTRAL
                : shouldLog(msg.toString());
    }

    private Result shouldLog(String msg)
    {
        return (msg != null && msg.contains("issued server command:") &&
                (msg.contains("/login") || msg.contains("/register") || msg.contains("/registration add")))
                ? Result.DENY
                : Result.NEUTRAL;
    }

}
