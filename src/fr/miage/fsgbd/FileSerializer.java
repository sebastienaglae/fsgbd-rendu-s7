package fr.miage.fsgbd;

import java.io.*;

public class FileSerializer<T extends Serializable> {
    private String filename;

    public FileSerializer(String filename) {
        this.filename = filename;
    }

    public void serialize(T object) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public T deserialize() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}