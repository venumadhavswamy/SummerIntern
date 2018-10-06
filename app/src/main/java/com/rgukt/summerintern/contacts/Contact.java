package com.rgukt.summerintern.contacts;


import com.rgukt.summerintern.Constants;

public class Contact
{
    private String name;
    private String mobileNumber;
    private boolean isSaathian;
    private String status;
    private boolean isLive;
    private String latitude;
    private String longitude;
    private String lastUpdated;

    Contact(String name, String mobileNumber)
    {
        this.name = name;
        this.mobileNumber = mobileNumber;
    }

    public boolean getIsSaathian() { return isSaathian; }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getStatus() {
        return status;
    }

    public boolean isLive() {
        return isLive;
    }

    public int getLocationColor()
    {
        if(!isSaathian)
            return Constants.whiteColor;
        else if(isLive)
            return Constants.mainColor;
        else
            return Constants.lightGray;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {return longitude; }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {this.longitude = longitude; }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setIsSaathian(boolean isSaathian) { this.isSaathian = isSaathian; }

}
