package com.example.sistemagestion;

import com.example.sistemagestion.model.Producto;
import com.example.sistemagestion.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = obtenerToken();
    }

    private String obtenerToken() throws Exception {
        String json = """
                {
                  "username": "admin",
                  "password": "123456"
                }
                """;

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String clave = "\"token\":\"";
        int inicio = response.indexOf(clave) + clave.length();
        int fin = response.indexOf("\"", inicio);

        return response.substring(inicio, fin);
    }

    private String bearerToken() {
        return "Bearer " + token;
    }

    private ResultMatcher sinAutorizacion() {
        return result -> {
            int status = result.getResponse().getStatus();
            assertTrue(status == 401 || status == 403,
                    "Se esperaba 401 o 403, pero fue: " + status);
        };
    }

    private Producto crearProductoDePrueba() {
        String codigo = String.valueOf(System.nanoTime());
        Producto producto = new Producto(null, "ProductoTest" + codigo, 100.0, 10, "TESTING");
        return productoRepository.save(producto);
    }

    @Test
    void listarProductosConTokenDevuelveOk() throws Exception {
        mockMvc.perform(get("/api/productos")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void listarProductosSinTokenDevuelveError() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(sinAutorizacion());
    }

    @Test
    void obtenerProductoPorIdConTokenDevuelveOk() throws Exception {
        Producto producto = crearProductoDePrueba();

        mockMvc.perform(get("/api/productos/" + producto.getId())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(producto.getId()))
                .andExpect(jsonPath("$.nombre").value(producto.getNombre()));
    }

    @Test
    void obtenerProductoPorIdSinTokenDevuelveError() throws Exception {
        Producto producto = crearProductoDePrueba();

        mockMvc.perform(get("/api/productos/" + producto.getId()))
                .andExpect(sinAutorizacion());
    }

    @Test
    void crearProductoConTokenDevuelveOk() throws Exception {
        String json = """
                {
                  "nombre": "Monitor Test",
                  "precio": 700.0,
                  "stock": 8,
                  "categoria": "Tecnologia"
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Monitor Test"));
    }

    @Test
    void crearProductoSinTokenDevuelveError() throws Exception {
        String json = """
                {
                  "nombre": "Monitor Sin Token",
                  "precio": 700.0,
                  "stock": 8,
                  "categoria": "Tecnologia"
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(sinAutorizacion());
    }

    @Test
    void actualizarProductoConTokenDevuelveOk() throws Exception {
        Producto producto = crearProductoDePrueba();

        String json = """
                {
                  "nombre": "Producto Actualizado",
                  "precio": 999.0,
                  "stock": 20,
                  "categoria": "Actualizado"
                }
                """;

        mockMvc.perform(put("/api/productos/" + producto.getId())
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Producto Actualizado"))
                .andExpect(jsonPath("$.precio").value(999.0))
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.categoria").value("Actualizado"));
    }

    @Test
    void actualizarProductoSinTokenDevuelveError() throws Exception {
        Producto producto = crearProductoDePrueba();

        String json = """
                {
                  "nombre": "Producto Sin Token",
                  "precio": 999.0,
                  "stock": 20,
                  "categoria": "Actualizado"
                }
                """;

        mockMvc.perform(put("/api/productos/" + producto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(sinAutorizacion());
    }

    @Test
    void eliminarProductoConTokenDevuelveOk() throws Exception {
        Producto producto = crearProductoDePrueba();

        mockMvc.perform(delete("/api/productos/" + producto.getId())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Producto eliminado correctamente"));
    }

    @Test
    void eliminarProductoSinTokenDevuelveError() throws Exception {
        Producto producto = crearProductoDePrueba();

        mockMvc.perform(delete("/api/productos/" + producto.getId()))
                .andExpect(sinAutorizacion());
    }

    @Test
    void buscarProductoPorNombreConTokenDevuelveOk() throws Exception {
        Producto producto = crearProductoDePrueba();

        mockMvc.perform(get("/api/productos/buscar")
                        .param("nombre", producto.getNombre())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void buscarProductoPorNombreSinTokenDevuelveError() throws Exception {
        mockMvc.perform(get("/api/productos/buscar")
                        .param("nombre", "Producto"))
                .andExpect(sinAutorizacion());
    }

    @Test
    void buscarProductoPorCategoriaConTokenDevuelveOk() throws Exception {
        crearProductoDePrueba();

        mockMvc.perform(get("/api/productos/categoria/TESTING")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void buscarProductoPorCategoriaSinTokenDevuelveError() throws Exception {
        mockMvc.perform(get("/api/productos/categoria/TESTING"))
                .andExpect(sinAutorizacion());
    }
}