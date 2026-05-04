package me.andreaseriksson.ufodashboard.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for paginated API responses.
 * Handles HATEOAS responses with _embedded and _links.
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

