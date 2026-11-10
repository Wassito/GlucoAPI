package org.lasalle.rest;

import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lasalle.dao.UsuarioDAO;
import org.lasalle.dao.UsuarioDAOImpl;
import org.lasalle.model.Usuario;

/**
 * Endpoints REST para la gestión de usuarios.
 */
@Path("usuarios")
public class RestUsuario {

    private static final Logger LOGGER = Logger.getLogger(RestUsuario.class.getName());
    private static final Gson GSON = new Gson();

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    /**
     * Registra un nuevo usuario recibiendo los datos como form params.
     *
     * @param nombre nombre del usuario
     * @param primerApellido primer apellido
     * @param segundoApellido segundo apellido (opcional)
     * @param email correo electrónico
     * @return 201 con el usuario creado, 400 si hay datos inválidos o 500 si
     * falla la base de datos
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response guardarUsuario(
            @FormParam("nombre") String nombre,
            @FormParam("primerApellido") String primerApellido,
            @FormParam("segundoApellido") String segundoApellido,
            @FormParam("email") String email) {

        String errorValidacion = validar(nombre, primerApellido, email);
        if (errorValidacion != null) {
            return respuestaError(Response.Status.BAD_REQUEST, errorValidacion);
        }

        try {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre.trim());
            usuario.setPrimerApellido(primerApellido.trim());
            usuario.setSegundoApellido(segundoApellido == null || segundoApellido.trim().isEmpty()
                    ? null
                    : segundoApellido.trim());
            usuario.setEmail(email.trim());

            Usuario usuarioGuardado = usuarioDAO.save(usuario);

            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("success", true);
            cuerpo.put("message", "Usuario registrado correctamente");
            cuerpo.put("data", usuarioGuardado);

            return Response.status(Response.Status.CREATED)
                    .entity(GSON.toJson(cuerpo))
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar el usuario", e);
            return respuestaError(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar el usuario en la base de datos");
        }
    }

    /**
     * Devuelve todos los usuarios registrados.
     *
     * @return 200 con la lista de usuarios o 500 si falla la base de datos
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuarios() {
        try {
            List<Usuario> usuarios = usuarioDAO.getAllUsuarios();

            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("success", true);
            cuerpo.put("data", usuarios);

            return Response.ok(GSON.toJson(cuerpo), MediaType.APPLICATION_JSON).build();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al consultar los usuarios", e);
            return respuestaError(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudieron consultar los usuarios en la base de datos");
        }
    }

    private String validar(String nombre, String primerApellido, String email) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (primerApellido == null || primerApellido.trim().isEmpty()) {
            return "El primer apellido es obligatorio";
        }
        if (email == null || email.trim().isEmpty()) {
            return "El email es obligatorio";
        }
        if (!email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El email no tiene un formato válido";
        }
        return null;
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
