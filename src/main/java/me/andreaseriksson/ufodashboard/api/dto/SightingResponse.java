package me.andreaseriksson.ufodashboard.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing a UFO sighting event.
 *
 * Represents a complete UFO sighting record from the UFO API, containing temporal information
 * (when the sighting occurred and was reported), geographic data (location and coordinates),
 * event details (duration, shape, observer comments), and entity references (location and shape IDs).
 * Includes HATEOAS links for API navigation.
 *
 * Temporal fields:
 *   - sightedAt: exact date and time when the UFO was sighted
 *   - datePosted: date when the sighting was reported/posted
 *   - durationSeconds: duration in seconds (may be null)
 *   - durationText: human-readable duration string
 *
 * Geographic fields:
 *   - city, state, countryCode: location information
 *   - latitude, longitude: GPS coordinates for map visualization
 *   - locationId: reference to the Location entity
 *
 * Observable fields:
 *   - shapeName: description of the observed object's shape
 *   - shapeId: reference to the Shape entity
 *   - comments: witness observations and comments
 *
 * Primarily used for:
 *   - Populating the interactive map with UFO sightings
 *   - Displaying sighting details in popups and listings
 *   - Filtering/searching sightings by date, location, or shape
 *   - Analyzing temporal and geographic patterns
 */
public class SightingResponse {
    private Long id;
    private LocalDateTime sightedAt;
    private Integer durationSeconds;
    private String durationText;
    private String comments;
    private LocalDate datePosted;
    private String city;
    private String state;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private String shapeName;
    private Long locationId;
    private Long shapeId;

    @JsonProperty("_links")
    private Links links;

    public SightingResponse() {
    }

    public SightingResponse(Long id, LocalDateTime sightedAt, Integer durationSeconds, String durationText,
            String comments, LocalDate datePosted, String city, String state, String countryCode,
            Double latitude, Double longitude, String shapeName, Long locationId, Long shapeId, Links links) {
        this.id = id;
        this.sightedAt = sightedAt;
        this.durationSeconds = durationSeconds;
        this.durationText = durationText;
        this.comments = comments;
        this.datePosted = datePosted;
        this.city = city;
        this.state = state;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.shapeName = shapeName;
        this.locationId = locationId;
        this.shapeId = shapeId;
        this.links = links;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSightedAt() {
        return sightedAt;
    }

    public void setSightedAt(LocalDateTime sightedAt) {
        this.sightedAt = sightedAt;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDate getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(LocalDate datePosted) {
        this.datePosted = datePosted;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getShapeName() {
        return shapeName;
    }

    public void setShapeName(String shapeName) {
        this.shapeName = shapeName;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getShapeId() {
        return shapeId;
    }

    public void setShapeId(Long shapeId) {
        this.shapeId = shapeId;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    /**
     * HATEOAS links container for this sighting resource.
     * Wraps the self reference link pointing to this sighting's API endpoint.
     */
    public static class Links {
        private Link self;

        public Links() {
        }

        public Links(Link self) {
            this.self = self;
        }

        public Link getSelf() {
            return self;
        }

        public void setSelf(Link self) {
            this.self = self;
        }
    }

    /**
     * Represents a single link in HATEOAS format.
     * Contains the href URL string for accessing the related resource.
     */
    public static class Link {
        private String href;

        public Link() {
        }

        public Link(String href) {
            this.href = href;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }
}

