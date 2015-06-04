package com.kludgenics.cgmlogger.model.treatment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by matthiasgranberry on 6/4/15.
 */
public class Treatment extends RealmObject {
    @PrimaryKey
    private String id;
    private Date eventTime;
    private String eventType;
    private String enteredBy;

    private double glucose;
    private String glucoseType;

    private double insulin;
    private String units;

    private String notes;

    private int carbs;
    private int preBolus;

    public Treatment() {
        super();
    }

    public Treatment(String id, Date eventTime, String eventType, String enteredBy, double glucose, String glucoseType, double insulin, String units, String notes, int carbs, int preBolus) {
        this.id = id;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.enteredBy = enteredBy;
        this.glucose = glucose;
        this.glucoseType = glucoseType;
        this.insulin = insulin;
        this.units = units;
        this.notes = notes;
        this.carbs = carbs;
        this.preBolus = preBolus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(String enteredBy) {
        this.enteredBy = enteredBy;
    }

    public double getGlucose() {
        return glucose;
    }

    public void setGlucose(double glucose) {
        this.glucose = glucose;
    }

    public String getGlucoseType() {
        return glucoseType;
    }

    public void setGlucoseType(String glucoseType) {
        this.glucoseType = glucoseType;
    }

    public double getInsulin() {
        return insulin;
    }

    public void setInsulin(double insulin) {
        this.insulin = insulin;
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

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public int getPreBolus() {
        return preBolus;
    }

    public void setPreBolus(int preBolus) {
        this.preBolus = preBolus;
    }
}
