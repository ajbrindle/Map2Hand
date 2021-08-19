package com.sk7software.map2hand.db;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GPXFiles {
    private List<GPXFile> files;

    public List<GPXFile> getFiles() {
        return files;
    }

    public void setFiles(List<GPXFile> files) {
        this.files = files;
    }

    public void addFile(GPXFile file) {
        if (files == null) {
            files = new ArrayList<>();
        }
        if (!files.contains(file)) {
            files.add(file);
        }
    }

    public void addFiles(GPXFiles gpxFiles) {
        if (gpxFiles != null && gpxFiles.getFiles() != null) {
            for (GPXFile f : gpxFiles.getFiles()) {
                addFile(f);
            }
        }
    }
}
