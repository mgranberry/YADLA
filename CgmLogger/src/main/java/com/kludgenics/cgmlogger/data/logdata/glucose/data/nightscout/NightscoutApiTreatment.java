package com.kludgenics.cgmlogger.data.logdata.glucose.data.nightscout;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import java.util.Date;

/**
 * Created by matthiasgranberry on 5/24/15.
 */
public class NightscoutApiTreatment extends RealmObject {
    @Expose
    @PrimaryKey
    @SerializedName("_id")
    private String id;
    @Expose
    @SerializedName("created_at")
    private Date createdAt;
    @Expose
    private String eventType;
    @Expose
    private String enteredBy;
    @Expose
    private float glucose;
    @Expose
    private float insulin;
    @Expose
    private String glucoseType;
    @Expose
    private int preBolus;
    @Expose
    private String notes;
    @Expose
    private String units;

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getPreBolus() {
        return preBolus;
    }

    public void setPreBolus(int preBolus) {
        this.preBolus = preBolus;
    }

    public String getGlucoseType() {
        return glucoseType;
    }

    public void setGlucoseType(String glucoseType) {
        this.glucoseType = glucoseType;
    }

    public float getInsulin() {
        return insulin;
    }

    public void setInsulin(float insulin) {
        this.insulin = insulin;
    }

    public float getGlucose() {
        return glucose;
    }

    public void setGlucose(float glucose) {
        this.glucose = glucose;
    }

    public String getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(String enteredBy) {
        this.enteredBy = enteredBy;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Expose
    int carbs;

    public NightscoutApiTreatment() {
        super();
    }
}
