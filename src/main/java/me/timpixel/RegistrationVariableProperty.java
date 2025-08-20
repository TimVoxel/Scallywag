package me.timpixel;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record RegistrationVariableProperty<T>(String name)
{
    private static final RegistrationVariableProperty<?>[] allProperties = new RegistrationVariableProperty[2];
    private static final List<String> propertyNames = new ArrayList<>(allProperties.length);

    public static RegistrationVariableProperty<String> USERNAME = new RegistrationVariableProperty<>("username");
    public static RegistrationVariableProperty<String> PASSWORD = new RegistrationVariableProperty<>("password");

    static
    {
        allProperties[0] = USERNAME;
        allProperties[1] = PASSWORD;

        for (var allProperty : allProperties)
        {
            propertyNames.add(allProperty.name());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable RegistrationVariableProperty<T> valueOf(String value)
    {
        for (var property : allProperties)
        {
            if (property.name.equalsIgnoreCase(value))
            {
                return (RegistrationVariableProperty<T>) property;
            }
        }
        return null;
    }

    public static List<String> propertyNames()
    {
        return propertyNames;
    }
}
