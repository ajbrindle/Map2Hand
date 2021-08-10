package com.sk7software.map2hand.db;

public class GPXFile {
    public String name;
    public String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Override toString() to return text to display in the list view
     * @return
     */
    @Override
    public String toString() {
        if (description != null && !"".equals(description)) {
            return description;
        } else {
            return name;
        }
    }
}
