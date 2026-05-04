package me.andreaseriksson.ufodashboard.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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

