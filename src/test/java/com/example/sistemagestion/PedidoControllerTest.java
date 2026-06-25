package com.example.sistemagestion;

import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.repository.PedidoRepository;
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
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

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

    private Pedido crearPedidoDePrueba() {
        String codigo = String.valueOf(System.nanoTime());
        Pedido pedido = new Pedido(null, "ClienteTest" + codigo, "2026-05-22", 500.0, "PENDIENTE");
        return pedidoRepository.save(pedido);
    }

    @Test
    void listarPedidosConTokenDevuelveOk() throws Exception {
        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void listarPedidosSinTokenDevuelveError() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(sinAutorizacion());
    }

    @Test
    void obtenerPedidoPorIdConTokenDevuelveOk() throws Exception {
        Pedido pedido = crearPedidoDePrueba();

        mockMvc.perform(get("/api/pedidos/" + pedido.getId())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedido.getId()))
                .andExpect(jsonPath("$.cliente").value(pedido.getCliente()));
    }

    @Test
    void obtenerPedidoPorIdSinTokenDevuelveError() throws Exception {
        Pedido pedido = crearPedidoDePrueba();

        mockMvc.perform(get("/api/pedidos/" + pedido.getId()))
                .andExpect(sinAutorizacion());
    }

    @Test
    void crearPedidoConTokenDevuelveOk() throws Exception {
        String json = """
                {
                  "cliente": "Luis Gomez Test",
                  "fecha": "2026-05-22",
                  "total": 950.0,
                  "estado": "PENDIENTE"
                }
                """;

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cliente").value("Luis Gomez Test"));
    }

    @Test
    void crearPedidoSinTokenDevuelveError() throws Exception {
        String json = """
                {
                  "cliente": "Pedido Sin Token",
                  "fecha": "2026-05-22",
                  "total": 950.0,
                  "estado": "PENDIENTE"
                }
                """;

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(sinAutorizacion());
    }

    @Test
    void actualizarPedidoConTokenDevuelveOk() throws Exception {
        Pedido pedido = crearPedidoDePrueba();

        String json = """
                {
                  "cliente": "Cliente Actualizado",
                  "fecha": "2026-05-23",
                  "total": 1200.0,
                  "estado": "ENTREGADO"
                }
                """;

        mockMvc.perform(put("/api/pedidos/" + pedido.getId())
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Cliente Actualizado"))
                .andExpect(jsonPath("$.total").value(1200.0))
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
    }

    @Test
    void actualizarPedidoSinTokenDevuelveError() throws Exception {
        Pedido pedido = crearPedidoDePrueba();

        String json = """
                {
                  "cliente": "Cliente Sin Token",
                  "fecha": "2026-05-23",
                  "total": 1200.0,
                  "estado": "ENTREGADO"
                }
                """;

        mockMvc.perform(put("/api/pedidos/" + pedido.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(sinAutorizacion());
    }

    @Test
    void eliminarPedidoConTokenDevuelveOk() throws Exception {
        Pedido pedido = crearPedidoDePrueba();

        mockMvc.perform(delete("/api/pedidos/" + pedido.getId())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pedido eliminado correctamente"));
    }

    @Test
    void eliminarPedidoSinTokenDevuelveError() throws Exception {
        Pedido pedido = crearPedidoDePrueba();

        mockMvc.perform(delete("/api/pedidos/" + pedido.getId()))
                .andExpect(sinAutorizacion());
    }

    @Test
    void buscarPedidoPorEstadoConTokenDevuelveOk() throws Exception {
        crearPedidoDePrueba();

        mockMvc.perform(get("/api/pedidos/estado/PENDIENTE")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void buscarPedidoPorEstadoSinTokenDevuelveError() throws Exception {
        mockMvc.perform(get("/api/pedidos/estado/PENDIENTE"))
                .andExpect(sinAutorizacion());
    }

    @Test
    void buscarPedidoPorClienteConTokenDevuelveOk() throws Exception {
        Pedido pedido = crearPedidoDePrueba();

        mockMvc.perform(get("/api/pedidos/buscar")
                        .param("cliente", pedido.getCliente())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void buscarPedidoPorClienteSinTokenDevuelveError() throws Exception {
        mockMvc.perform(get("/api/pedidos/buscar")
                        .param("cliente", "Cliente"))
                .andExpect(sinAutorizacion());
    }
}