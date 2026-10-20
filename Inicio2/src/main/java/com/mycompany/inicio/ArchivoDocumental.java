package com.mycompany.inicio;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Copias locales de documentos; la base conserva una referencia relativa. */
public final class ArchivoDocumental {
    private static final Set<String> EXTENSIONES = Set.of("pdf", "png", "jpg", "jpeg", "docx");
    private ArchivoDocumental() {}

    public static Path raiz() {
        return Path.of(System.getProperty("creditos.archivos.dir", "archivos")).toAbsolutePath().normalize();
    }

    public static String copiar(Path origen, String grupo) throws IOException {
        if (!Set.of("solicitudes", "garantias", "contratos").contains(grupo)) throw new IOException("Grupo documental no valido.");
        if (origen == null || !Files.isRegularFile(origen) || !Files.isReadable(origen)) {
            throw new IOException("Selecciona un archivo local que se pueda leer.");
        }
        comprobarExtension(origen);
        if (Files.size(origen) == 0 || Files.size(origen) > 20L * 1024 * 1024) {
            throw new IOException("El archivo debe tener contenido y no superar 20 MB.");
        }
        String nombre = origen.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        if (nombre.length() > 100) nombre = nombre.substring(nombre.length() - 100);
        String referencia = grupo + "/" + UUID.randomUUID() + "_" + nombre;
        Path destino = raiz().resolve(referencia);
        Files.createDirectories(destino.getParent());
        if (!destino.getParent().toRealPath().startsWith(raiz().toRealPath())) {
            throw new IOException("La carpeta documental no pertenece al repositorio local.");
        }
        Files.copy(origen, destino);
        return referencia;
    }

    public static Path resolver(String referencia) throws IOException {
        if (referencia == null || referencia.isBlank()) throw new IOException("El documento no tiene archivo asociado.");
        Path archivo = raiz().resolve(referencia).normalize();
        if (!archivo.startsWith(raiz()) || !Files.isRegularFile(archivo)) {
            throw new IOException("No se encontro el respaldo local. Adjunta el archivo desde esta pantalla.");
        }
        if (!archivo.toRealPath().startsWith(raiz().toRealPath())) throw new IOException("Referencia documental no valida.");
        comprobarExtension(archivo);
        return archivo;
    }

    public static void descartarCopia(String referencia) throws IOException {
        // Solo para una copia recien creada cuyo registro en la base haya fallado.
        if (referencia != null) Files.deleteIfExists(resolver(referencia));
    }

    private static void comprobarExtension(Path archivo) throws IOException {
        String nombre = archivo.getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || !EXTENSIONES.contains(nombre.substring(punto + 1).toLowerCase(Locale.ROOT))) {
            throw new IOException("Formatos permitidos: PDF, PNG, JPG, JPEG y DOCX.");
        }
    }
}
