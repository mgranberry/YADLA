package com.kludgenics.cgmlogger.model.location;

import io.realm.RealmObject;

/**
 * Created by matthiasgranberry on 6/7/15.
 */
public class PlaceData extends RealmObject {
    private long cacheUntil;
    private String placeId;
    private String name;
    private Position positon;
    private int confirmedVisits;

    public long getCacheUntil() {
        return cacheUntil;
    }

    public void setCacheUntil(long cacheUntil) {
        this.cacheUntil = cacheUntil;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public int getConfirmedVisits() {
        return confirmedVisits;
    }

    public void setConfirmedVisits(int confirmedVisits) {
        this.confirmedVisits = confirmedVisits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPositon() {
        return positon;
    }

    public void setPositon(Position positon) {
        this.positon = positon;
    }

    public PlaceData() {
    }
}
