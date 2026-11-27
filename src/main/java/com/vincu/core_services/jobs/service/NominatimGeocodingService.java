package com.vincu.core_services.jobs.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class NominatimGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocodingService.class);
    private static final long MIN_INTERVAL_MS = 1100L;

    private final RestClient restClient;
    private final ReentrantLock rateLimitLock = new ReentrantLock();
    private long lastRequestTimestamp = 0L;

    public record Coordinates(BigDecimal latitude, BigDecimal longitude) {
        public static Coordinates zero() {
            return new Coordinates(BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimPlace(String lat, String lon) {}

    public NominatimGeocodingService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(4));

        this.restClient = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, "VincuMarketplaceMVP/1.0 (contacto@vincu.pe)")
                .build();
    }

    public Coordinates geocodeAddress(String specificAddress, String districtName, String departmentName) {
        String fullQuery = String.format("%s, %s, %s, Peru", specificAddress, districtName, departmentName);
        Coordinates exact = search(fullQuery);
        if (exact != null) {
            return exact;
        }

        // Fallback: si no reconoce el número de puerta o lote, ubica el centroide del distrito
        String districtQuery = String.format("%s, %s, Peru", districtName, departmentName);
        Coordinates districtCenter = search(districtQuery);
        return districtCenter != null ? districtCenter : Coordinates.zero();
    }

    private Coordinates search(String query) {
        rateLimitLock.lock();
        try {
            long elapsed = System.currentTimeMillis() - lastRequestTimestamp;
            if (elapsed < MIN_INTERVAL_MS) {
                Thread.sleep(MIN_INTERVAL_MS - elapsed);
            }

            NominatimPlace[] results = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", query)
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", 1)
                            .queryParam("countrycodes", "pe")
                            .build())
                    .retrieve()
                    .body(NominatimPlace[].class);

            lastRequestTimestamp = System.currentTimeMillis();

            if (results != null && results.length > 0) {
                return new Coordinates(
                        new BigDecimal(results[0].lat()),
                        new BigDecimal(results[0].lon())
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Geocodificación interrumpida para consulta: {}", query);
        } catch (Exception e) {
            log.warn("No se pudo geocodificar '{}': {}", query, e.getMessage());
        } finally {
            rateLimitLock.unlock();
        }
        return null;
    }
}
