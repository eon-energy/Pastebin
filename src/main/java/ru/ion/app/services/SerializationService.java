package ru.ion.app.services;

import org.springframework.stereotype.Service;
import ru.ion.app.DTO.PasteData;
import ru.ion.app.entitys.Paste;

import java.io.*;

@Service
public class SerializationService {
    public void serialize(PasteData pasteData, File tempFile) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(pasteData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PasteData deserialize(File tempFile) {
        PasteData deserializedPerson = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
            deserializedPerson = (PasteData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deserializedPerson;
    }
}
