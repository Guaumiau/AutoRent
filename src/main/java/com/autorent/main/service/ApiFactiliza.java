package com.autorent.main.service; // O donde pongas tus servicios

import com.autorent.main.model.EstadoVehiculo;
import com.autorent.main.model.FactilizaResponseDTO; // El DTO que creamos
import com.autorent.main.model.Vehiculo; // Tu Entidad
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
public class ApiFactiliza {

    // 1. Inyecta el token desde application.properties
    @Value("${api.factiliza.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Para procesar el JSON

    /**
     * Consulta la placa en Factiliza y la traduce a tu Entidad Vehiculo.
     */
    public Vehiculo consultarPlacaFactiliza(String placa) throws Exception {

        String url = "https://api.factiliza.com/v1/placa/info/" + placa;

        // 2. Prepara los headers con el token seguro
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 3. Llama a la API de Factiliza desde el SERVIDOR
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.path("status").asInt() == 200 && root.has("data")) {
                JsonNode dataNode = root.path("data");

                // 4. Mapea el JSON (el nodo "data") a nuestro DTO
                FactilizaResponseDTO dto = objectMapper.treeToValue(dataNode, FactilizaResponseDTO.class);

                // 5. "Traduce" el DTO a tu Entidad Vehiculo
                return traducirDtoAEntidad(dto);

            } else {
                // La API de Factiliza devolvió un error (ej. placa no encontrada)
                throw new Exception(root.path("message").asText("Placa no encontrada en Factiliza"));
            }
        } else {
            // Falló la conexión con Factiliza
            throw new Exception("Error al conectar con la API externa");
        }
    }

    /**
     * Método "traductor"
     * Copia los datos del DTO (Respuesta de API) a tu Entidad (Base de Datos)
     */
    private Vehiculo traducirDtoAEntidad(FactilizaResponseDTO dto) {
        Vehiculo vehiculo = new Vehiculo();

        // Mapeo directo
        vehiculo.setPlaca(dto.getPlaca().toUpperCase());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setColor(dto.getColor());

        // Lógica para el año
        String anioApi = dto.getAnio();
        if (anioApi != null) {
            try {
                vehiculo.setAnio(Integer.parseInt(anioApi));
            } catch (NumberFormatException e) {
                vehiculo.setAnio(null); // O un valor por defecto, ej: 1990
            }
        }

        // Rellenamos valores por defecto de tu Entidad que la API no provee
        vehiculo.setEstveh(EstadoVehiculo.DISPONIBLE);
        vehiculo.setFecharegistro(LocalDate.now());
        // Kilometraje, Precio, Foto y Propietario quedan en 'null'
        // para que se rellenen en el formulario.

        return vehiculo;
    }
}