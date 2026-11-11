package org.lasalle.rest;

import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lasalle.dao.MedidaDAO;
import org.lasalle.dao.MedidaDAOImpl;
import org.lasalle.model.Medida;

/**
 * Endpoints REST para la gestión de medidas de glucosa.
 */
@Path("medidas")
public class RestMedida {

    private static final Logger LOGGER = Logger.getLogger(RestMedida.class.getName());
    private static final Gson GSON = new Gson();

    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA_HORA_SIN_SEGUNDOS
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MedidaDAO medidaDAO = new MedidaDAOImpl();

    /** Devuelve todas las medidas de un usuario, o una lista vacía si no tiene medidas. */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMedidasPorUsuario(@QueryParam("idUsuario") String idUsuario) {
        try {
            int idUsuarioNumero = parseEntero(idUsuario, "El idUsuario es obligatorio");
            if (idUsuarioNumero <= 0) {
                return respuestaError(Response.Status.BAD_REQUEST,
                        "El idUsuario debe ser mayor que cero");
            }

            List<Medida> medidas = medidaDAO.getByIdUsuario(idUsuarioNumero);
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("success", true);
            cuerpo.put("data", medidas);
            return Response.ok(GSON.toJson(cuerpo), MediaType.APPLICATION_JSON).build();

        } catch (IllegalArgumentException e) {
            return respuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al consultar las medidas", e);
            return respuestaError(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudieron consultar las medidas en la base de datos");
        }
    }

    /**
     * Registra una nueva medida de glucosa recibiendo los datos como form
     * params.
     *
     * @param idUsuario id del usuario al que pertenece la medida
     * @param glucosa valor de glucosa en mg/dL
     * @param fechaHora fecha y hora de la medida (opcional, por defecto ahora)
     * @param contexto contexto de la medida (opcional)
     * @param notas notas adicionales (opcional)
     * @return 201 con la medida creada, 400 si hay datos inválidos o 500 si
     * falla la base de datos
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response guardarMedida(
            @FormParam("idUsuario") String idUsuario,
            @FormParam("glucosa") String glucosa,
            @FormParam("fechaHora") String fechaHora,
            @FormParam("contexto") String contexto,
            @FormParam("notas") String notas) {

        try {
            int idUsuarioNumero = parseEntero(idUsuario, "El idUsuario es obligatorio");
            int glucosaNumero = parseEntero(glucosa, "La glucosa es obligatoria");

            if (idUsuarioNumero <= 0) {
                return respuestaError(Response.Status.BAD_REQUEST,
                        "El idUsuario debe ser mayor que cero");
            }
            if (glucosaNumero <= 0) {
                return respuestaError(Response.Status.BAD_REQUEST,
                        "La glucosa debe ser mayor que cero");
            }
            if (contexto != null && contexto.trim().length() > 50) {
                return respuestaError(Response.Status.BAD_REQUEST,
                        "El contexto no puede superar los 50 caracteres");
            }
            if (notas != null && notas.trim().length() > 100) {
                return respuestaError(Response.Status.BAD_REQUEST,
                        "Las notas no pueden superar los 100 caracteres");
            }

            LocalDateTime fecha = parseFechaHora(fechaHora);
            if (fecha == null) {
                fecha = LocalDateTime.now();
            }

            Medida medida = new Medida();
            medida.setIdUsuario(idUsuarioNumero);
            medida.setGlucosa(glucosaNumero);
            medida.setFechaHora(fecha.format(FORMATO_FECHA_HORA));
            medida.setContexto(limpiarOpcional(contexto));
            medida.setNotas(limpiarOpcional(notas));

            Medida medidaGuardada = medidaDAO.save(medida);

            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("success", true);
            cuerpo.put("message", "Medida registrada correctamente");
            cuerpo.put("data", medidaGuardada);

            return Response.status(Response.Status.CREATED)
                    .entity(GSON.toJson(cuerpo))
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } catch (IllegalArgumentException e) {
            return respuestaError(Response.Status.BAD_REQUEST, e.getMessage());

        } catch (SQLIntegrityConstraintViolationException e) {
            LOGGER.log(Level.WARNING, "Restricción de integridad al guardar la medida", e);
            return respuestaError(Response.Status.BAD_REQUEST,
                    "El idUsuario no existe o los datos violan una restricción de la base de datos");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar la medida", e);
            return respuestaError(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la medida en la base de datos");
        }
    }

    private int parseEntero(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(mensaje + " y debe ser un número entero");
        }
    }

    private LocalDateTime parseFechaHora(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        String fecha = valor.trim();

        try {
            return LocalDateTime.parse(fecha, FORMATO_FECHA_HORA);
        } catch (DateTimeParseException e) {
            // Se intenta con otro formato antes de reportar el error.
        }

        try {
            return LocalDateTime.parse(fecha, FORMATO_FECHA_HORA_SIN_SEGUNDOS);
        } catch (DateTimeParseException e) {
            // Se intenta con formato ISO antes de reportar el error.
        }

        try {
            return LocalDateTime.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "La fechaHora debe tener el formato yyyy-MM-dd HH:mm:ss o yyyy-MM-ddTHH:mm:ss");
        }
    }

    private String limpiarOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }

    private Response respuestaError(Response.Status estado, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("success", false);
        cuerpo.put("message", mensaje);

        return Response.status(estado)
                .entity(GSON.toJson(cuerpo))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
