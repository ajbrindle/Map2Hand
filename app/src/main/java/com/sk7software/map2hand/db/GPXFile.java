package com.sk7software.map2hand.db;

import androidx.annotation.Nullable;

public class GPXFile {
    public String name;
    public String description;
    public boolean local;

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

    public void setLocal(boolean local) { this.local = local; }

    public boolean isLocal() { return local; }

    /**
     * Override toString() to return text to display in the list view
     * @return
     */
    @Override
    public String toString() {
        if (description != null && !"".equals(description)) {
            return (isLocal() ? "* " : "") + description;
        } else {
            return (isLocal() ? "* " : "") + name;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GPXFile)) {
            return false;
        }

        GPXFile f = (GPXFile)obj;
        if (name.equals(f.getName())) {
            if (description != null) {
                return (description.equals(f.getDescription()));
            } else {
                return f.getDescription() == null;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (description == null) {
            return name.hashCode();
        } else {
            return (name + description).hashCode();
        }
    }
}
