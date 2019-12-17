package interfaz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import soporte.Archivo;
import soporte.Palabra;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class InterfazPrincipalController implements Initializable{
    public Text lblArchivo;
    public TextField txtBusqueda;
    public Label lblResultado;
    public ListView lvwTexto;
    public ProgressBar progressCarga;
    public Label lblDistintas;
    public Text progressPorcentaje;
    public Label lblArchivosCargados;
    public ListView lvwNuevasPalabras;
    public Text lblFrecuencia;
    public Label lblDiccionarioCargado;
    private Archivo archivo;
    public Alert alerta;
    final File cargarDiccionario = new File("Diccionario.dat");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cargarDiccionario.exists()){
            archivo.cargarDiccionario();
            lvwTexto.setItems(mostrarListView(archivo));
            palabrasDistintas();
            progressCargado();
            archivosCargados();
            lblDiccionarioCargado.setText("Diccionario Cargado");
        }
    }

    private ObservableList mostrarListView(Archivo archivo){
        ObservableList<Palabra> ol= FXCollections.observableArrayList(archivo.getList());
        return ol;
    }

    public InterfazPrincipalController() {
        archivo = new Archivo();
    }

    private void limpiarBusqueda() {
        txtBusqueda.setText("");
        lblResultado.setText("");
        lblFrecuencia.setText("");
    }

    private void archivosCargados() {
        lblArchivosCargados.setText("Archivos cargados: " + String.valueOf(archivo.getCantidadArchivosCargados()));
    }

    private void progressCargado() {
        progressCarga.setProgress(100);
        progressPorcentaje.setText("100%");
        archivosCargados();
    }

    private void progressVacio() {
        progressCarga.setProgress(0);
        progressPorcentaje.setText("0%");
    }

    private void palabrasDistintas() {
        lblDistintas.setText("Palabras distintas en  el diccionario: " + String.valueOf(archivo.getCantidadPalabrasDistintas()));
    }

    private void mostrarAlertaSatisfactoria() {
        alerta = new Alert(Alert.AlertType.INFORMATION, "Archivo cargado con éxito.");
        alerta.setHeaderText(null);
        alerta.show();
    }

    private void mostrarAlertaError() {
        alerta = new Alert(Alert.AlertType.ERROR, "Archivo vacío, no se cargo.");
        alerta.setHeaderText(null);
        alerta.show();
    }

    public void cargar(ActionEvent actionEvent){
        progressVacio();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Abrir archivo de texto");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt"));
        File file = chooser.showOpenDialog(null);
        if (file!=null) {
            if (archivo.setFile(file)){
                limpiarBusqueda();
                lblArchivo.setText("Archivo: " + file.getName());
                lvwNuevasPalabras.setItems(mostrarListView(archivo));
                lvwNuevasPalabras.refresh();
                palabrasDistintas();
                progressCargado();
                archivosCargados();
                mostrarAlertaSatisfactoria();
                lblDiccionarioCargado.setText("Diccionario guardado..");
            }else {
                mostrarAlertaError();
            }
        }
    }

    public void buscar(ActionEvent actionEvent) {
        if (txtBusqueda.getText().isEmpty()) {
            lblResultado.setText("No se ingreso ninguna palabra");
            borrarMarcado();
        } else {
            Palabra p = new Palabra(txtBusqueda.getText(),0);
            if (archivo.buscarPalabra(p)) {
                lblResultado.setText("Palabra encontrada");
                lblFrecuencia.setText("Frecuencia: " + archivo.mostrarFrecuencia(txtBusqueda.getText()));
                marcarPalabra(p);
            } else {
                lblResultado.setText("Palabra NO encontrada");
                lblFrecuencia.setText("");
                borrarMarcado();
            }
        }
    }

    public void marcarPalabra(Palabra p){
        lvwTexto.scrollTo(p);
        lvwTexto.getSelectionModel().select(p);
        lvwNuevasPalabras.scrollTo(p);
        lvwNuevasPalabras.getSelectionModel().select(p);
    }

    public void borrarMarcado(){
        lvwTexto.getSelectionModel().select(null);
        lvwNuevasPalabras.getSelectionModel().select(null);
    }

    public void limpiar (ActionEvent actionEvent){
        limpiarBusqueda();
        borrarMarcado();
    }
}