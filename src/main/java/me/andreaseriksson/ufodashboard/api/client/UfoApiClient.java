package me.andreaseriksson.ufodashboard.api.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import me.andreaseriksson.ufodashboard.api.dto.LocationResponse;
import me.andreaseriksson.ufodashboard.api.dto.PagedResponse;
import me.andreaseriksson.ufodashboard.api.dto.ShapeResponse;
import me.andreaseriksson.ufodashboard.api.dto.SightingResponse;

/**
 * Client for calling the UFO Sightings API (WT1 API).
 * All endpoints are public and require no authentication.
 */
@Service
public class UfoApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UfoApiClient(@Value("${ufo.api.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    /**
     * Get paginated list of sightings with optional filters.
     *
     * @param page zero-indexed page number
     * @param size page size
     * @param city filter by city (optional)
     * @param state filter by state (optional)
     * @param countryCode filter by country code (optional)
     * @param shapeName filter by UFO shape name (optional)
     * @return list of sightings
     */
    public List<SightingResponse> getSightings(int page, int size, String city, String state,
                                               String countryCode, String shapeName) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/sightings")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParamIfPresent("city", ofNullable(city))
                .queryParamIfPresent("state", ofNullable(state))
                .queryParamIfPresent("countryCode", ofNullable(countryCode))
                .queryParamIfPresent("shapeName", ofNullable(shapeName))
                .toUriString();

        ResponseEntity<PagedResponse<SightingResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedResponse<SightingResponse>>() {}
        );

        PagedResponse<SightingResponse> body = response.getBody();
        return body != null ? body.getContent() : List.of();
    }

    /**
     * Get a single sighting by ID.
     */
    public SightingResponse getSightingById(Long id) {
        String url = baseUrl + "/sightings/" + id;
        return restTemplate.getForObject(url, SightingResponse.class);
    }

    /**
     * Get paginated list of shapes.
     */
    public List<ShapeResponse> getShapes(int page, int size) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/shapes")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();

        ResponseEntity<PagedResponse<ShapeResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedResponse<ShapeResponse>>() {}
        );

        PagedResponse<ShapeResponse> body = response.getBody();
        return body != null ? body.getContent() : List.of();
    }

    /**
     * Get a single shape by ID.
     */
    public ShapeResponse getShapeById(Long id) {
        String url = baseUrl + "/shapes/" + id;
        return restTemplate.getForObject(url, ShapeResponse.class);
    }

    /**
     * Get paginated list of locations.
     */
    public List<LocationResponse> getLocations(int page, int size) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/locations")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();

        ResponseEntity<PagedResponse<LocationResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedResponse<LocationResponse>>() {}
        );

        PagedResponse<LocationResponse> body = response.getBody();
        return body != null ? body.getContent() : List.of();
    }

    /**
     * Get a single location by ID.
     */
    public LocationResponse getLocationById(Long id) {
        String url = baseUrl + "/locations/" + id;
        return restTemplate.getForObject(url, LocationResponse.class);
    }

    // Helper to convert String to java.util.Optional
    private static java.util.Optional<String> ofNullable(String value) {
        return java.util.Optional.ofNullable(value);
    }
}




