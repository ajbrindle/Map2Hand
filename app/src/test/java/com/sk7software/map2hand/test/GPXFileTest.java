package com.sk7software.map2hand.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sk7software.map2hand.db.GPXFile;
import com.sk7software.map2hand.db.GPXFiles;

import org.junit.Test;

public class GPXFileTest {

    @Test
    public void testEquals() {
        GPXFile f1 = new GPXFile();
        f1.setName("F1.gpx");

        GPXFile f2 = new GPXFile();
        f2.setName("F2.gpx");
        assertFalse(f1.equals(f2));

        GPXFile f2s = new GPXFile();
        f2s.setName("F2.gpx");
        assertTrue(f2.equals(f2s));

        f2.setDescription("F2 desc");
        assertFalse(f2.equals(f2s));

        f2s.setDescription("F2 desc");
        assertTrue(f2.equals(f2s));
    }

    @Test
    public void testContains() {
        GPXFiles files = new GPXFiles();
        GPXFile f1 = new GPXFile();
        f1.setName("F1.gpx");
        files.addFile(f1);

        GPXFile f2 = new GPXFile();
        f2.setName("F2.gpx");
        assertFalse(files.getFiles().contains(f2));
        files.addFile(f2);

        GPXFile f2s = new GPXFile();
        f2s.setName("F2.gpx");
        assertTrue(files.getFiles().contains(f2s));

        f2s.setDescription("F2 desc");
        assertFalse(files.getFiles().contains(f2s));
    }
}
