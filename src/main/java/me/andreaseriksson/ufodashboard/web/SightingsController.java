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
import me.andreaseriksson.ufodashboard.service.SightingViewportService;

/**
 * Controller for bridging frontend requests to the UFO Sightings API.
 * All endpoints require OAuth2 authentication.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class SightingsController {

    private final UfoApiClient ufoApiClient;
    private final SightingViewportService sightingViewportService;

    /**
     * Constructs a new SightingsController with dependencies for UFO API access and viewport filtering.
     *
     * @param ufoApiClient client for fetching sightings, shapes, and locations from the upstream UFO API
     * @param sightingViewportService service for filtering sightings by map viewport and map limit
     */
    @Autowired
    public SightingsController(UfoApiClient ufoApiClient, SightingViewportService sightingViewportService) {
        this.ufoApiClient = ufoApiClient;
        this.sightingViewportService = sightingViewportService;
    }

    /**
     * Retrieves a paginated list of UFO sightings with optional filtering.
     *
     * Fetches sightings from the upstream UFO API with pagination support and optional filters.
     * Filters can be combined to narrow results. All parameters except page and size are optional.
     *
     * @param page zero-indexed page number (default: 0)
     * @param size number of sightings per page (default: 20)
     * @param city filter by city name (optional)
     * @param state filter by state (optional)
     * @param countryCode filter by ISO country code (optional)
     * @param shapeName filter by UFO shape name (optional)
     * @return a ResponseEntity containing a list of sightings matching the query parameters
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
     * Retrieves sightings inside the currently visible map viewport.
     *
     * This endpoint is optimized for interactive map displays. It filters sightings by geographic bounds
     * (north/south latitude, east/west longitude) and respects the country and shape filters. The result
     * is limited to avoid overwhelming the frontend.
     *
     * Bounds are optional; when all four bounds are provided, only sightings within that rectangle are returned.
     * When bounds are missing, all sightings within the limit are returned (not geographically filtered).
     *
     * @param north northern latitude boundary (optional)
     * @param south southern latitude boundary (optional)
     * @param east eastern longitude boundary (optional)
     * @param west western longitude boundary (optional)
     * @param countryCode filter by country code, or {@code __other__} for non-standard countries (optional)
     * @param shapeName filter by UFO shape name (optional)
     * @param limit maximum number of sightings to return (default: 3000)
     * @return a ResponseEntity containing a list of sightings within the viewport and filters
     */
    @GetMapping("/sightings/map")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<SightingResponse>> getSightingsInViewport(
            @RequestParam(required = false) Double north,
            @RequestParam(required = false) Double south,
            @RequestParam(required = false) Double east,
            @RequestParam(required = false) Double west,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String shapeName,
            @RequestParam(defaultValue = "3000") Integer limit) {

        List<SightingResponse> sightings = sightingViewportService.getSightingsInViewport(
                north,
                south,
                east,
                west,
                countryCode,
                shapeName,
                limit);
        return ResponseEntity.ok(sightings);
    }

    /**
     * Retrieves a single UFO sighting by ID.
     *
     * @param id the unique identifier of the sighting to retrieve
     * @return a ResponseEntity containing the sighting details
     */
    @GetMapping("/sightings/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SightingResponse> getSightingById(@PathVariable Long id) {
        SightingResponse sighting = ufoApiClient.getSightingById(id);
        return ResponseEntity.ok(sighting);
    }

    /**
     * Retrieves a paginated list of UFO shape classifications.
     *
     * Shapes represent UFO appearance categories (e.g. "circle", "triangle", "star") used to classify
     * sightings. This endpoint is typically used to populate filter dropdowns in the frontend.
     *
     * @param page zero-indexed page number (default: 0)
     * @param size number of shapes per page (default: 50)
     * @return a ResponseEntity containing a list of shape records
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
     * Retrieves a single UFO shape classification by ID.
     *
     * @param id the unique identifier of the shape to retrieve
     * @return a ResponseEntity containing the shape details (name and metadata)
     */
    @GetMapping("/shapes/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ShapeResponse> getShapeById(@PathVariable Long id) {
        ShapeResponse shape = ufoApiClient.getShapeById(id);
        return ResponseEntity.ok(shape);
    }

    /**
     * Retrieves a paginated list of geographic locations.
     *
     * Locations represent cities, states, and countries where UFO sightings have been reported.
     * This endpoint is typically used to populate geographic filters and to understand the
     * geographic spread of sightings in the dataset.
     *
     * @param page zero-indexed page number (default: 0)
     * @param size number of locations per page (default: 50)
     * @return a ResponseEntity containing a list of location records with coordinates
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
     * Retrieves a single geographic location by ID.
     *
     * @param id the unique identifier of the location to retrieve
     * @return a ResponseEntity containing the location details (city, state, country, coordinates)
     */
    @GetMapping("/locations/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {
        LocationResponse location = ufoApiClient.getLocationById(id);
        return ResponseEntity.ok(location);
    }
}

