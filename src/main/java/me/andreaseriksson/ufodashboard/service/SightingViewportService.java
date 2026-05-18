package me.andreaseriksson.ufodashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import me.andreaseriksson.ufodashboard.api.client.UfoApiClient;
import me.andreaseriksson.ufodashboard.api.dto.SightingResponse;

/**
 * Loads sightings from the WT1 API in chunks and filters them to the current map viewport.
 * This keeps the browser from having to render the full dataset at once.
 */
@Service
public class SightingViewportService {

    private static final String OTHER_COUNTRY_FILTER = "__other__";
    private static final Set<String> SUPPORTED_COUNTRY_CODES = Set.of("AU", "CA", "GB", "US");

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
     * @param countryCode optional country filter, or {@code __other__} for countries outside AU, CA, GB, and US
     * @param shapeName optional shape filter passed through to the upstream API
     * @param limit maximum number of sightings to return
     * @return sightings inside the current viewport
     */
    public List<SightingResponse> getSightingsInViewport(Double north, Double south, Double east, Double west,
            String countryCode, String shapeName, Integer limit) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);
        boolean otherCountryFilter = OTHER_COUNTRY_FILTER.equals(normalizedCountryCode);
        String upstreamCountryCode = otherCountryFilter ? null : normalizedCountryCode;
        String upstreamShapeName = normalizeShapeName(shapeName);
        int effectiveLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        boolean useViewportBounds = north != null && south != null && east != null && west != null;
        List<SightingResponse> matches = new ArrayList<>();

        for (int page = 0; matches.size() < effectiveLimit; page++) {
            List<SightingResponse> pageSightings = ufoApiClient.getSightings(page, PAGE_SIZE, null, null, upstreamCountryCode, upstreamShapeName);
            if (pageSightings.isEmpty()) {
                break;
            }

            for (SightingResponse sighting : pageSightings) {
                if (sighting == null || sighting.getLatitude() == null || sighting.getLongitude() == null) {
                    continue;
                }

                if (matchesCountryFilter(sighting, normalizedCountryCode)
                        && (!useViewportBounds || isInsideViewport(sighting, north, south, east, west))) {
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

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }

        String trimmedCountryCode = countryCode.trim();
        if (OTHER_COUNTRY_FILTER.equalsIgnoreCase(trimmedCountryCode)) {
            return OTHER_COUNTRY_FILTER;
        }

        return trimmedCountryCode.toUpperCase(Locale.ROOT);
    }

    private String normalizeShapeName(String shapeName) {
        if (shapeName == null || shapeName.isBlank()) {
            return null;
        }

        return shapeName.trim();
    }

    private boolean matchesCountryFilter(SightingResponse sighting, String countryCode) {
        if (countryCode == null) {
            return true;
        }

        if (OTHER_COUNTRY_FILTER.equals(countryCode)) {
            return isOtherOrUnknownCountry(sighting.getCountryCode());
        }

        String sightingCountryCode = normalizeCountryCode(sighting.getCountryCode());
        return countryCode.equals(sightingCountryCode);
    }

    private boolean isOtherOrUnknownCountry(String countryCode) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);
        return normalizedCountryCode == null || !SUPPORTED_COUNTRY_CODES.contains(normalizedCountryCode);
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

