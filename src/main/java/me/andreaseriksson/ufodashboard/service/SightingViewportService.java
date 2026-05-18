package me.andreaseriksson.ufodashboard.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import me.andreaseriksson.ufodashboard.api.client.UfoApiClient;
import me.andreaseriksson.ufodashboard.api.dto.SightingResponse;

/**
 * Loads sightings from the WT1 API in chunks and filters them to the current map viewport.
 * This keeps the browser from having to render the full dataset at once.
 */
@Service
public class SightingViewportService {

    private static final int PAGE_SIZE = 100;
    private static final int DEFAULT_LIMIT = 3000;
    private static final int MAX_LIMIT = 100000;

    private final UfoApiClient ufoApiClient;

    public SightingViewportService(UfoApiClient ufoApiClient) {
        this.ufoApiClient = ufoApiClient;
    }

    /**
     * Collect sightings inside the visible map bounds.
     *
     * @param north northern latitude boundary
     * @param south southern latitude boundary
     * @param east eastern longitude boundary
     * @param west western longitude boundary
     * @param countryCode optional country filter
     * @param limit maximum number of sightings to return
     * @return sightings inside the current viewport
     */
    public List<SightingResponse> getSightingsInViewport(Double north, Double south, Double east, Double west,
            String countryCode, Integer limit) {
        int effectiveLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        boolean useViewportBounds = north != null && south != null && east != null && west != null;
        List<SightingResponse> matches = new ArrayList<>();

        for (int page = 0; matches.size() < effectiveLimit; page++) {
            List<SightingResponse> pageSightings = ufoApiClient.getSightings(page, PAGE_SIZE, null, null, countryCode, null);
            if (pageSightings.isEmpty()) {
                break;
            }

            for (SightingResponse sighting : pageSightings) {
                if (sighting == null || sighting.getLatitude() == null || sighting.getLongitude() == null) {
                    continue;
                }

                if (!useViewportBounds || isInsideViewport(sighting, north, south, east, west)) {
                    matches.add(sighting);
                    if (matches.size() >= effectiveLimit) {
                        break;
                    }
                }
            }

            if (pageSightings.size() < PAGE_SIZE) {
                break;
            }
        }

        return matches;
    }

    /**
     * Check whether a sighting is located inside the viewport bounds.
     */
    private boolean isInsideViewport(SightingResponse sighting, Double north, Double south, Double east, Double west) {

        double latitude = sighting.getLatitude();
        double longitude = sighting.getLongitude();
        double minLatitude = Math.min(north, south);
        double maxLatitude = Math.max(north, south);

        boolean latitudeMatches = latitude >= minLatitude && latitude <= maxLatitude;
        boolean longitudeMatches = west <= east
                ? longitude >= west && longitude <= east
                : longitude >= west || longitude <= east;

        return latitudeMatches && longitudeMatches;
    }
}

