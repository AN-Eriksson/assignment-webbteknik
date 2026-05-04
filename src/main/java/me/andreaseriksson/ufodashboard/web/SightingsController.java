package me.andreaseriksson.ufodashboard.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import me.andreaseriksson.ufodashboard.api.client.UfoApiClient;
import me.andreaseriksson.ufodashboard.api.dto.LocationResponse;
import me.andreaseriksson.ufodashboard.api.dto.ShapeResponse;
import me.andreaseriksson.ufodashboard.api.dto.SightingResponse;

/**
 * Controller for bridging frontend requests to the UFO Sightings API.
 * All endpoints require OAuth2 authentication.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class SightingsController {

    private final UfoApiClient ufoApiClient;

    @Autowired
    public SightingsController(UfoApiClient ufoApiClient) {
        this.ufoApiClient = ufoApiClient;
    }

    /**
     * Get paginated list of sightings with optional filters.
     */
    @GetMapping("/sightings")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<SightingResponse>> getSightings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String shapeName) {

        List<SightingResponse> sightings = ufoApiClient.getSightings(page, size, city, state, countryCode, shapeName);
        return ResponseEntity.ok(sightings);
    }

    /**
     * Get a single sighting by ID.
     */
    @GetMapping("/sightings/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SightingResponse> getSightingById(@PathVariable Long id) {
        try {
            SightingResponse sighting = ufoApiClient.getSightingById(id);
            return ResponseEntity.ok(sighting);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get paginated list of shapes.
     */
    @GetMapping("/shapes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ShapeResponse>> getShapes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<ShapeResponse> shapes = ufoApiClient.getShapes(page, size);
        return ResponseEntity.ok(shapes);
    }

    /**
     * Get a single shape by ID.
     */
    @GetMapping("/shapes/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ShapeResponse> getShapeById(@PathVariable Long id) {
        try {
            ShapeResponse shape = ufoApiClient.getShapeById(id);
            return ResponseEntity.ok(shape);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get paginated list of locations.
     */
    @GetMapping("/locations")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LocationResponse>> getLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<LocationResponse> locations = ufoApiClient.getLocations(page, size);
        return ResponseEntity.ok(locations);
    }

    /**
     * Get a single location by ID.
     */
    @GetMapping("/locations/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {
        try {
            LocationResponse location = ufoApiClient.getLocationById(id);
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

