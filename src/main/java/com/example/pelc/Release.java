package com.example.pelc;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Release {
    
    private Integer id;
    private String name;
    private String product;
    @SerializedName("brew_tags")
    private List<String> brewTags;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public List<String> getBrewTags() {
        return brewTags;
    }
    public void setBrewTags(List<String> brewTags) {
        this.brewTags = brewTags;
    }
    
}
