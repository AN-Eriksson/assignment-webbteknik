package me.andreaseriksson.ufodashboard.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing a geographic location.
 *
 * Represents a location entity from the UFO API, containing city, state, country, and GPS coordinates.
 * Includes HATEOAS links to the resource for API navigation.
 *
 * Primarily used for:
 *   - Populating country filters on the frontend
 *   - Providing location context for UFO sightings
 *   - Mapping sightings to geographic areas
 */
public class LocationResponse {
    private Long id;
    private String city;
    private String state;
    private String countryCode;
    private Double latitude;
    private Double longitude;

    @JsonProperty("_links")
    private Links links;

    public LocationResponse() {
    }

    public LocationResponse(Long id, String city, String state, String countryCode,
            Double latitude, Double longitude, Links links) {
        this.id = id;
        this.city = city;
        this.state = state;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.links = links;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    /**
     * HATEOAS links container for this location resource.
     * Wraps the self reference link pointing to this location's API endpoint.
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

