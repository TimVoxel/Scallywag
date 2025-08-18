package me.timpixel.database;

import java.nio.ByteBuffer;
import java.util.UUID;

public class TypeConversionUtil
{
    public static byte[] uuidToBinary(UUID uuid)
    {
        var byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID binaryToUUID(byte[] bytes)
    {
        if (bytes == null || bytes.length != 16)
        {
            throw new IllegalArgumentException("Byte array must be 16 bytes long.");
        }

        var byteBuffer = ByteBuffer.wrap(bytes);
        var mostSigBits = byteBuffer.getLong();
        var leastSigBits = byteBuffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }
}
