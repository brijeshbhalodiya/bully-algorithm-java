import java.io.*;

public class Util {

    public static byte[] serialize(Serializable toFlatten) {
        byte[] serialized = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(toFlatten);
            serialized = byteArrayOutputStream.toByteArray();
        } catch (Exception problem) {
            Logger.logError("Unable to serialize the provided object to a byte array");
            problem.printStackTrace();
        } finally {
            try {
                objectOutputStream.close();
            } catch (Exception problem) {
                Logger.logError("Unable to serialize the provided object to a byte array");
                problem.printStackTrace();
            }
        }
        return serialized;
    }

    public static Object deserialize(final byte[] flattened) {
        Object deserialized = null;
        if (flattened == null || flattened.length == 0) {
            Logger.logError("Cannot deserialize an empty or null byte array");
            return deserialized;
        }
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(new ByteArrayInputStream(flattened));
            deserialized = objectInputStream.readObject();
        } catch (Exception problem) {
            Logger.logError("Unable to deserialize the provided byte array to an object");
            problem.printStackTrace();
        } finally {
            if (objectInputStream != null)
                try {
                    objectInputStream.close();
                } catch (Exception problem) {
                    Logger.logError("Unable to deserialize the provided byte array to an object");
                    problem.printStackTrace();
                }
        }
        return deserialized;
    }

}
