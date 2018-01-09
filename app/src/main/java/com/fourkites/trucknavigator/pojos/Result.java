
package com.fourkites.trucknavigator.pojos;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Avinash on 09/01/18.
 */

public class Result {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("highlightedTitle")
    @Expose
    private String highlightedTitle;
    @SerializedName("vicinity")
    @Expose
    private String vicinity;
    @SerializedName("highlightedVicinity")
    @Expose
    private String highlightedVicinity;
    @SerializedName("position")
    @Expose
    private List<Double> position = null;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("href")
    @Expose
    private String href;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("resultType")
    @Expose
    private String resultType;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("chainIds")
    @Expose
    private List<String> chainIds = null;

    private boolean isCurrentLocation;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHighlightedTitle() {
        return highlightedTitle;
    }

    public void setHighlightedTitle(String highlightedTitle) {
        this.highlightedTitle = highlightedTitle;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getHighlightedVicinity() {
        return highlightedVicinity;
    }

    public void setHighlightedVicinity(String highlightedVicinity) {
        this.highlightedVicinity = highlightedVicinity;
    }

    public List<Double> getPosition() {
        return position;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getChainIds() {
        return chainIds;
    }

    public void setChainIds(List<String> chainIds) {
        this.chainIds = chainIds;
    }

    public boolean isCurrentLocation() {
        return isCurrentLocation;
    }

    public void setCurrentLocation(boolean currentLocation) {
        isCurrentLocation = currentLocation;
    }
}
