/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author abrah
 */
@Path("glucosa")
public class RestGlucosa {
    
    @Path("saludar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludar(){
        String respuesta = """
                           {"response" : "Hola desde Java" }
                           """;
        return Response.ok(respuesta).build();
    }
    
    @Path("saludarPersonalizado")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarPersonalizado (
            @QueryParam("nombre") String nombre
    ){
        String respuesta = """
                           {"response" : "Hola %s desde Java"}
                           """;
        respuesta = String.format(respuesta, nombre);
        return Response.ok(respuesta).build();
    }
    
    @Path("cuadrado")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response cuadrado (
            @QueryParam ("numero") int numero
    ){
        int cuadrado = numero * numero;
        String respuesta = """
                        {"response" : "el cuadrado de %d es = %d"}
                        """;
        
        respuesta = String.format(respuesta, numero, cuadrado);
        return Response.ok(respuesta).build();
    }
}
