import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Sistema de digiturno para un consultorio médico.
 * CÓDIGO BASE - actividad inicial de unidad (Listas, Pilas y Colas).
 *
 * Debes completar los métodos marcados con // TODO.
 * No modifiques las firmas de los métodos ni los atributos.
 *
 * Esta clase contiene ÚNICAMENTE la lógica del digiturno (las estructuras
 * de datos y las operaciones sobre ellas). NO contiene menú de consola ni
 * método main. Para PROBAR los métodos que completes aquí, ejecuta la
 * clase Main (archivo Main.java), que ya está completamente implementada
 * y te ofrece un menú de texto: cada opción del menú llama directamente
 * a uno de los métodos que debes completar en esta clase.
 *
 * Estructuras (TAD) que se usan en esta clase: NO son las colecciones de
 * java.util. Son implementaciones propias de este proyecto, ya
 * completamente resueltas en los archivos Cola.java, Pila.java y
 * Lista.java. Antes de completar los métodos de esta clase, revisa el
 * Javadoc de esas tres clases para conocer los nombres exactos de sus
 * métodos (encolar/desencolar, apilar/desapilar, agregar/obtener, etc.).
 *
 *  - Cola  -> orden de llegada dentro de cada tipo de paciente (FIFO).
 *  - Lista -> historico de atención del día.
 *  - Pila  -> deshacer el último turno generado (LIFO).
 *
 * Persistencia (archivos binarios):
 *  - Lectura de datos de prueba desde "datos/datos_prueba.dat" (ya incluido
 *    en este proyecto) para poder probar la aplicación sin digitar datos.
 *    El archivo contiene un arreglo Paciente[] serializado.
 *  - Escritura del histórico de atención en "datos/historico.dat".
 */
public class Digiturno {

    private Cola<Paciente> colaGeneral = new Cola<>();
    private Cola<Paciente> colaPremium = new Cola<>();
    private Cola<Paciente> colaPrioritaria = new Cola<>();

    private Lista<Paciente> historico = new Lista<>();
    private Pila<Paciente> pilaDeshacer = new Pila<>();

    private static final String ARCHIVO_DATOS_PRUEBA = "datos/datos_prueba.dat";
    private static final String ARCHIVO_HISTORICO = "datos/historico.dat";

    /**
     * Genera un nuevo turno para un paciente y lo agrega a la cola
     * correspondiente según su tipo.
     *
     * Se utiliza una Cola porque los pacientes de cada categoría deben
     * ser atendidos respetando el orden de llegada (FIFO). Además, el
     * paciente se almacena en una Pila para permitir deshacer el último
     * turno generado, aplicando el comportamiento LIFO de esta estructura.
     *
     * @param nombre nombre del paciente que solicita el turno.
     * @param tipo categoría de atención del paciente.
     */
    public void generarTurno(String nombre, TipoPaciente tipo) {
        // TODO: completar este método
        Paciente paciente = new Paciente(nombre, tipo);
        if (tipo == TipoPaciente.GENERAL){
            colaGeneral.encolar(paciente);
        }
        if (tipo == TipoPaciente.PREMIUM){
            colaPremium.encolar(paciente);
        }
        if(tipo == TipoPaciente.PRIORITARIO){
            colaPrioritaria.encolar(paciente);
        }

        pilaDeshacer.apilar(paciente);

        System.out.println("Turno generado a nombre de: "+paciente);

    }

    /**
     * Atiende al siguiente paciente aplicando el orden de prioridad
     * establecido para el consultorio.
     *
     * Primero se consulta la cola PRIORITARIA, luego la cola PREMIUM y
     * finalmente la cola GENERAL. Dentro de cada categoría se utiliza
     * una Cola, por lo que se conserva el orden FIFO de llegada.
     *
     * El paciente retirado de la cola se agrega al histórico mediante
     * la Lista, conservando el orden real en que fueron atendidos.
     */
    public void atenderSiguiente() {
        // TODO: completar este método
        Paciente paciente;

        if(!colaPrioritaria.estaVacia()) {
            paciente = colaPrioritaria.desencolar();
        } else if (!colaPremium.estaVacia()) {
            paciente = colaPremium.desencolar();
        } else if (!colaGeneral.estaVacia()) {
            paciente = colaGeneral.desencolar();
        } else {
            System.out.println("No hay pacientes en espera!!");
            return;
        }

        historico.agregar(paciente);
        System.out.println("Atendiendo al paciente: "+paciente);
    }

    /**
     * Deshace el último turno generado que aún se encuentra en espera.
     *
     * Se utiliza la Pila porque su comportamiento LIFO permite recuperar
     * el último turno generado antes que los anteriores. Una vez obtenido
     * el paciente, se intenta eliminar de la Cola correspondiente según
     * su tipo de atención.
     *
     * Si la pila está vacía, no existe ningún turno pendiente de deshacer
     * y el método finaliza sin realizar ninguna operación. Si el turno
     * ya fue atendido y no se encuentra en su cola, se informa al usuario
     * que no puede ser deshecho.
     *
     * TAD utilizados: Pila.estaVacia(), Pila.desapilar() y
     * Cola.eliminar().
     */
    public void deshacerUltimoTurno() {
        // TODO: completar este método

        if(pilaDeshacer.estaVacia()){
            System.out.println("No hay turnos por deshacer");
            return;
        }

        Paciente paciente = pilaDeshacer.desapilar();

        boolean eliminado = false;

        if (paciente.getTipo() == TipoPaciente.GENERAL){
            eliminado = colaGeneral.eliminar(paciente);
        } else if (paciente.getTipo() == TipoPaciente.PREMIUM) {
            eliminado = colaPremium.eliminar(paciente);
        } else if (paciente.getTipo() == TipoPaciente.PRIORITARIO) {
            eliminado = colaPrioritaria.eliminar(paciente);
        }

        if (eliminado){
            System.out.println("Turno deshecho para: "+ paciente);
        }else {
            System.out.println("El turno ya había sido atendido, no se puede deshacer: "+ paciente);
        }
    }

    /**
     * Carga los pacientes de prueba almacenados en el archivo binario
     * datos/datos_prueba.dat.
     *
     * Se utiliza ObjectInputStream para recuperar el arreglo de pacientes
     * serializado previamente. Cada paciente se registra mediante
     * generarTurno(), reutilizando la misma lógica utilizada cuando un
     * paciente solicita un turno manualmente.
     *
     * De esta forma, los datos cargados quedan ubicados en la Cola
     * correspondiente según el tipo de paciente.
     */
    public void cargarDatosPrueba() {
        // TODO: completar este método
        File archivo = new File(ARCHIVO_DATOS_PRUEBA);

        if (!archivo.exists()) {
            System.out.println("El archivo de datos de prueba no existe.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO_DATOS_PRUEBA))) {

            Paciente[] pacientesPrueba = (Paciente[]) ois.readObject();

            for (Paciente p : pacientesPrueba) {
                generarTurno(p.getNombre(), p.getTipo());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al cargar los datos de prueba: " + e.getMessage());
        }
    }

    /**
     * Guarda el histórico de pacientes atendidos en un archivo binario.
     *
     * Se utiliza ObjectOutputStream para serializar la Lista completa
     * del histórico y conservar los registros después de cerrar la
     * aplicación. La carpeta datos se crea automáticamente si no existe.
     *
     * La Lista es la estructura utilizada para el histórico porque permite
     * almacenar los pacientes en el orden en que fueron atendidos y
     * recorrer posteriormente todos los registros.
     */
    public void guardarHistoricoBinario() {
        // TODO: completar este método
        File carpeta = new File("datos");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        } try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_HISTORICO))){
            oos.writeObject(historico);

            System.out.println("Histórico guardado correctamente. Registros: "
                    + historico.tamano());
        } catch (IOException e){
            System.out.println("Error al guardar el histórico: " + e.getMessage());
        }
    }

// ----- Métodos de visualización (ya implementados, no los modifiques) -----

public void mostrarHistorico() {
    System.out.println("----- HISTORICO DE ATENCION -----");
    if (historico.estaVacia()) {
        System.out.println("Aun no se ha atendido a ningun paciente.");
    } else {
        int i = 1;
        for (Paciente p : historico) {
            System.out.println(i + ". " + p);
            i++;
        }
    }
}

public void mostrarEstadoColas() {
    System.out.println("----- ESTADO ACTUAL DE LAS COLAS -----");
    System.out.println("Prioritaria (" + colaPrioritaria.tamano() + "): " + colaPrioritaria);
    System.out.println("Premium (" + colaPremium.tamano() + "): " + colaPremium);
    System.out.println("General (" + colaGeneral.tamano() + "): " + colaGeneral);
}

// Esta clase NO tiene metodo main. Para probar los metodos que
// completes aqui, ejecuta la clase Main (Main.java), que ya
// esta lista y contiene el menu de consola.
}
