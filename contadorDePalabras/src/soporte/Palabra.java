package soporte;

import java.io.Serializable;

public class Palabra implements Serializable {
    private String palabra;
    private int frecuencia;

    public Palabra(String palabra, int repeticiones) {
        this.palabra = palabra;
        this.frecuencia = repeticiones;
    }

    public String getPalabra() {
        return palabra;
    }

    public int getRepeticiones() {
        return frecuencia;
    }

    public void contarRepeticion()
    {
        frecuencia += 1;
    }

    @Override
    public String toString() {
        return palabra;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Palabra palabra1 = (Palabra) o;

        return palabra != null ? palabra.equals(palabra1.palabra) : palabra1.palabra == null;
    }
}