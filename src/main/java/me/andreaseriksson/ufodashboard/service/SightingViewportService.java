package me.andreaseriksson.ufodashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import me.andreaseriksson.ufodashboard.api.client.UfoApiClient;
import me.andreaseriksson.ufodashboard.api.dto.SightingResponse;

/**
 * Service that loads sightings from the upstream WT1 API in page-sized chunks and
 * filters them to the supplied map viewport and client-supplied filters.
 *
 * Reasoning: the full dataset can be very large. To keep memory and rendering
 * costs reasonable the service paginates requests to the upstream API and applies
 * lightweight filtering (country, shape and optional geographic bounds) until the
 * requested limit of sightings is collected or the upstream data is exhausted.
 *
 * Behaviour notes:
 * - getSightingsInViewport(Double, Double, Double, Double, String, String, Integer)
 *   will request pages from the UfoApiClient starting at page 0 and stop when
 *   the effective limit is reached or no more pages are returned.
 * - Country filtering supports a special token "__other__" which matches
 *   sightings whose country code is not in the configured supported set (AU/CA/GB/US).
 * - Longitude bound checks correctly handle bounding boxes that cross the antimeridian
 *   (wrap-around) by allowing west > east to indicate a wrapped box.
 */
@Service
public class SightingViewportService {

    private static final String OTHER_COUNTRY_FILTER = "__other__";
    private static final Set<String> SUPPORTED_COUNTRY_CODES = Set.of("AU", "CA", "GB", "US");

    private static final int PAGE_SIZE = 100;
    private static final int DEFAULT_LIMIT = 3000;
    private static final int MAX_LIMIT = 100000;

    private final UfoApiClient ufoApiClient;

    /**
     * Create a new service instance.
     *
     * @param ufoApiClient client used to fetch paged sightings, shapes and locations
     */
    public SightingViewportService(UfoApiClient ufoApiClient) {
        this.ufoApiClient = ufoApiClient;
    }

    /**
     * Collect sightings inside the visible map bounds.
     *
     * Parameters are optional; when any of the north/south/east/west
     * are null the method will not apply geographic bounding and will instead
     * collect matching sightings up to the supplied limit.
     *
     * @param north northern latitude boundary (may be null)
     * @param south southern latitude boundary (may be null)
     * @param east eastern longitude boundary (may be null)
     * @param west western longitude boundary (may be null)
     * @param countryCode optional ISO country code filter, or the special token "__other__"
     *                    to select countries outside the supported set (may be null)
     * @param shapeName optional shape name passed directly to the upstream API (may be null)
     * @param limit maximum number of sightings to return; when null or <= 0 a default is used
     * @return a list of sightings that match the supplied filters and bounds (may be empty)
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

    /**
     * Normalize a country code parameter to an uppercase ISO code or return null.
     *
     * Recognizes the special token "__other__" and returns it unchanged.
     *
     * @param countryCode the raw parameter value
     * @return normalized country code (uppercased), the "__other__" token, or null
     */
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

    /**
     * Trim and normalize a shape name parameter; returns null for blank input.
     *
     * @param shapeName raw shape name
     * @return trimmed shape name or null
     */
    private String normalizeShapeName(String shapeName) {
        if (shapeName == null || shapeName.isBlank()) {
            return null;
        }

        return shapeName.trim();
    }

    /**
     * Check whether a sighting matches the supplied country filter.
     *
     * @param sighting the sighting instance
     * @param countryCode normalized country code or __other__ token (may be null)
     * @return true when the sighting should be included for the filter
     */
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

    /**
     * Returns true when the supplied country code is either unknown or not one of the
     * explicitly supported country codes (AU, CA, GB, US).
     *
     * @param countryCode raw country code string
     * @return true when the country should be considered "other" or unknown
     */
    private boolean isOtherOrUnknownCountry(String countryCode) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);
        return normalizedCountryCode == null || !SUPPORTED_COUNTRY_CODES.contains(normalizedCountryCode);
    }

    /**
     * Check whether a sighting is located inside the viewport bounds.
     *
     * Latitude comparison uses min/max so the order of north/south
     * is not significant. Longitude comparison supports boxes that cross the antimeridian by
     * treating west > east as a wrapped box.
     *
     * @param sighting sighting to test (must have non-null coordinates)
     * @param north northern boundary
     * @param south southern boundary
     * @param east eastern boundary
     * @param west western boundary
     * @return true when the sighting is inside the bounds
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
