package soporte;

import java.io.*;
import java.util.Map;

class Serializator<E extends Map> {

    public void serialize(E t) {
        try{
            FileOutputStream fos = new FileOutputStream("Diccionario.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(t);
            fos.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public E deserialize() {
        E des;
        try {
            FileInputStream fos = new FileInputStream("Diccionario.dat");
            ObjectInputStream ios = new ObjectInputStream(fos);
            des = (E) ios.readObject();
            fos.close();
            ios.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            des = null;
        }
        return des;
    }
}