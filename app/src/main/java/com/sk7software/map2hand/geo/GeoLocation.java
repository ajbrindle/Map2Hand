package com.sk7software.map2hand.geo;

public class GeoLocation {

	private double latitude;
	private double longitude;
	private double easting;
	private double northing;
	private double bearing;
	
	public GeoLocation() {
	}

	public GeoLocation(double e, double n) {
		this.easting = e;
		this.northing = n;
	}

	public GeoLocation(GeoLocation l) {
		this.setEasting(l.getEasting());
		this.setNorthing(l.getNorthing());
		this.setLatitude(l.getLatitude());
		this.setLongitude(l.getLongitude());
		this.setBearing(l.getBearing());
	}
	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getEasting() {
		return easting;
	}

	public void setEasting(double easting) {
		this.easting = easting;
	}

	public double getNorthing() {
		return northing;
	}

	public void setNorthing(double northing) {
		this.northing = northing;
	}

	public double getBearing() { return bearing; }

	public void setBearing(double bearing) { this.bearing = bearing; }
}
