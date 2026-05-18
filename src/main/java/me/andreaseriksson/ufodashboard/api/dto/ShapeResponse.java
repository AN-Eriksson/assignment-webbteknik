package me.andreaseriksson.ufodashboard.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing a UFO shape type.
 *
 * Represents a UFO shape classification (e.g. "circle", "triangle", "star") from the UFO API.
 * Shapes are referenced by sightings to categorize the observed object's appearance.
 * Includes HATEOAS links for API navigation.
 *
 * Primarily used for:
 *   - Categorizing and filtering sightings by reported shape
 *   - Building shape distribution visualizations (pie charts)
 *   - Providing shape context in sighting details
 */
public class ShapeResponse {
    private Long id;
    private String name;
    
    @JsonProperty("_links")
    private Links links;
    
    public ShapeResponse() {
    }
    
    public ShapeResponse(Long id, String name, Links links) {
        this.id = id;
        this.name = name;
        this.links = links;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Links getLinks() {
        return links;
    }
    
    public void setLinks(Links links) {
        this.links = links;
    }
    
    /**
     * HATEOAS links container for this shape resource.
     * Wraps the self reference link pointing to this shape's API endpoint.
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

