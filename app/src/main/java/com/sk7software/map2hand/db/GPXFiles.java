package com.sk7software.map2hand.db;

import android.util.Log;

import com.google.gson.Gson;
import com.sk7software.map2hand.MapFile;
import com.sk7software.map2hand.geo.GPXRoute;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GPXFiles {
    private List<GPXFile> files;

    public static final String ROUTE_EXT = ".mhr";
    private static final String TAG = GPXFiles.class.getSimpleName();

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

    public void addLocalFiles() {
        // Get all files in MAP_DIR with .mhr extension
        File directory = new File(MapFile.MAP_DIR);
        Gson gson = new Gson();
        GPXFiles gpxFiles = new GPXFiles();

        List<File> files = Arrays.asList(directory.listFiles()).stream()
                .filter(file -> file.getName().endsWith(ROUTE_EXT)).collect(Collectors.toList());

        for (File f : files) {
            // Load the file
            Log.d(TAG, "Found local route: " + f.getAbsolutePath());
            GPXRoute route = GPXRoute.readFromFile(f);

            if (route != null) {
                GPXFile file = new GPXFile();
                file.setName(f.getName());
                file.setDescription(route.getName());
                file.setLocal(true);
                addFile(file);
            }
        }
    }

}
