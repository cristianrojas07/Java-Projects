package soporte;

import java.io.*;
import java.util.*;

public class Archivo implements Serializable{

    private File file;
    private TSB_OAHashtable<String, Palabra> list;
    private ArrayList<String> palabrasLimpias = new ArrayList<>();
    private Integer cantidadArchivosCargados = 0;
    private Serializator serializador;

    public Archivo() {
        list = new TSB_OAHashtable<>();
    }

    public Integer getCantidadPalabrasDistintas() {return list.size();}

    public Integer getCantidadArchivosCargados() {
        return cantidadArchivosCargados;
    }

    public boolean setFile(File file){
        this.file = file;
        if (!estaVacio()){
            cantidadArchivosCargados++;
            leerArchivo();
            return true;
        }else{
            return false;
        }
    }

    public void cargarDiccionario(){
        serializador = new Serializator();
        TSB_OAHashtable contenido = (TSB_OAHashtable) serializador.deserialize();
        if(contenido != null) { // si el objeto está presente
            list = contenido;
        }
    }

    private void leerArchivo(){
        palabrasSeparadas();
        for (String palabra: palabrasLimpias) {
            Palabra c = list.get(palabra);
            if(c == null){
                list.put(palabra,new Palabra(palabra,1));
            }else c.contarRepeticion();
        }
        serializador = new Serializator();
        serializador.serialize(list);
        palabrasLimpias.clear();
    }

    private void palabrasSeparadas(){
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            Scanner sc = new Scanner(br);
            while (sc.hasNext()) {
                String linea = sc.nextLine();
                separarPalabras(linea);
            }
            br.close();
            fr.close();
        }catch (FileNotFoundException ex){
            System.out.println("Error al leer el archivo: " + ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void separarPalabras(String palabra){
        String vector[] = palabra.split("[^a-zA-ZñÑáéíóúÁÉÍÓÚ]");
        for (String valor : vector) {
            if(!valor.equals("")){
                if(valor.length()>1){
                    palabrasLimpias.add(valor.toLowerCase());
                }else if(valor.length()==1 && valor.matches("[aeyouAEYOUáéíóúÁÉÍÓÚ]")){
                    // aeyouAEYOUáéíóúÁÉÍÓÚ son consideradas palabras
                    palabrasLimpias.add(valor.toLowerCase());
                }
            }
        }
    }

    private boolean estaVacio(){
        return this.file.length() == 0;
    }

    public boolean buscarPalabra(Palabra p)
    {
        return list.contains(p);
    }

    public Collection<Palabra> getList() {
        return list.values();
    }

    public String mostrarFrecuencia(String p){
        Palabra palabra = list.get(p);
        return String.valueOf(palabra.getRepeticiones());
    }
}