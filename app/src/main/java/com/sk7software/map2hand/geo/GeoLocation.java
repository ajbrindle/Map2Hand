package com.sk7software.map2hand.geo;

public class GeoLocation {

	private double latitude;
	private double longitude;
	private double easting;
	private double northing;
	
	public GeoLocation() {
	}

	public GeoLocation(GeoLocation l) {
		this.setEasting(l.getEasting());
		this.setNorthing(l.getNorthing());
		this.setLatitude(l.getLatitude());
		this.setLongitude(l.getLongitude());
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
}
