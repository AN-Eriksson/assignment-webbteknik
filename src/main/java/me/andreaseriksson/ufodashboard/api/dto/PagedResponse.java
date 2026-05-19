package me.andreaseriksson.ufodashboard.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic wrapper for paginated API responses in HATEOAS format.
 *
 * Wraps paginated responses from the UFO API, handling Spring Data REST style HATEOAS format
 * with _embedded and _links properties. Supports multiple content types by detecting which
 * list is populated (sightings, shapes, or locations).
 *
 * HATEOAS Structure:
 *   _embedded: contains the actual list of items (Embedded)
 *   _links: contains navigation links to other pages and related resources (Links)
 *
 * Type parameter T is the item type within the embedded list (typically SightingResponse,
 * ShapeResponse, or LocationResponse).
 *
 * Usage:
 *   PagedResponse<SightingResponse> response = fetch("/api/sightings?page=0&size=50");
 *   List<SightingResponse> items = response.getContent(); // Handles _embedded parsing
 *
 * Primarily used for:
 *   - Parsing paginated responses from the UFO API
 *   - Extracting content from different list types dynamically
 *   - Building pagination controls on the frontend
 */
public class PagedResponse<T> {
    
    @JsonProperty("_embedded")
    private Embedded<T> embedded;
    
    @JsonProperty("_links")
    private Links links;
    
    public PagedResponse() {
    }
    
    public PagedResponse(Embedded<T> embedded, Links links) {
        this.embedded = embedded;
        this.links = links;
    }
    
    public Embedded<T> getEmbedded() {
        return embedded;
    }
    
    public void setEmbedded(Embedded<T> embedded) {
        this.embedded = embedded;
    }
    
    public Links getLinks() {
        return links;
    }
    
    public void setLinks(Links links) {
        this.links = links;
    }
    
    /**
     * Extract the list from _embedded, handling any list type.
     */
    public List<T> getContent() {
        if (embedded != null) {
            if (embedded.sightingResponseList != null) {
                return embedded.sightingResponseList;
            }
            if (embedded.shapeResponseList != null) {
                return embedded.shapeResponseList;
            }
            if (embedded.locationResponseList != null) {
                return embedded.locationResponseList;
            }
        }
        return List.of();
    }
    
    /**
     * Container for the actual list of items in a paginated response.
     *
     * The API can return different list types depending on the endpoint.
     * This class dynamically holds the appropriate list and the @JsonAnySetter
     * handles any unknown properties from the API for forward compatibility.
     *
     * One of sightingResponseList, shapeResponseList, or locationResponseList will be
     * populated depending on which endpoint was called.
     */
    public static class Embedded<T> {
        private List<T> sightingResponseList;
        private List<T> shapeResponseList;
        private List<T> locationResponseList;
        
        public Embedded() {
        }
        
        public Embedded(List<T> sightingResponseList, List<T> shapeResponseList, List<T> locationResponseList) {
            this.sightingResponseList = sightingResponseList;
            this.shapeResponseList = shapeResponseList;
            this.locationResponseList = locationResponseList;
        }
        
        public List<T> getSightingResponseList() {
            return sightingResponseList;
        }
        
        public void setSightingResponseList(List<T> sightingResponseList) {
            this.sightingResponseList = sightingResponseList;
        }
        
        public List<T> getShapeResponseList() {
            return shapeResponseList;
        }
        
        public void setShapeResponseList(List<T> shapeResponseList) {
            this.shapeResponseList = shapeResponseList;
        }
        
        public List<T> getLocationResponseList() {
            return locationResponseList;
        }
        
        public void setLocationResponseList(List<T> locationResponseList) {
            this.locationResponseList = locationResponseList;
        }
        
        @JsonAnySetter
        public void handleUnknown(String key, Object value) {
            // Handle any other list-based embedded responses dynamically
        }
    }
    
    /**
     * HATEOAS links container for the paginated response.
     *
     * Contains navigation links including:
     *   - self: link to the current page
     *   - shapes, locations, sightings: links to related resources
     *
     * These links are useful for implementing pagination and navigation controls
     * on the frontend without hardcoding API URLs.
     */
    public static class Links {
        private Link self;
        private Link shapes;
        private Link locations;
        private Link sightings;
        
        public Links() {
        }
        
        public Links(Link self, Link shapes, Link locations, Link sightings) {
            this.self = self;
            this.shapes = shapes;
            this.locations = locations;
            this.sightings = sightings;
        }
        
        public Link getSelf() {
            return self;
        }
        
        public void setSelf(Link self) {
            this.self = self;
        }
        
        public Link getShapes() {
            return shapes;
        }
        
        public void setShapes(Link shapes) {
            this.shapes = shapes;
        }
        
        public Link getLocations() {
            return locations;
        }
        
        public void setLocations(Link locations) {
            this.locations = locations;
        }
        
        public Link getSightings() {
            return sightings;
        }
        
        public void setSightings(Link sightings) {
            this.sightings = sightings;
        }
    }
    
    /**
     * Represents a single HATEOAS link with an href URL.
     * Used to provide navigational references to API endpoints.
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

