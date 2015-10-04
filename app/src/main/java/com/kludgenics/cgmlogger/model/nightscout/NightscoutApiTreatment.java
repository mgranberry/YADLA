package com.kludgenics.cgmlogger.model.nightscout;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kludgenics.cgmlogger.model.treatment.Treatment;

import org.joda.time.DateTime;

/**
 * Created by matthiasgranberry on 6/3/15.
 */
public class NightscoutApiTreatment {
    @Expose
    @SerializedName("_id")
    private String id;
    @Expose
    @SerializedName("created_at")
    private DateTime createdAt;
    @Expose
    private String eventType;
    @Expose
    private String enteredBy;
    @Expose
    private double glucose;
    @Expose
    private double insulin;
    @Expose
    private String glucoseType;
    @Expose
    private int preBolus;
    @Expose
    private String notes;
    @Expose
    private String units;
    @Expose
    private int carbs;

    public NightscoutApiTreatment() {
        super();
    }

    public NightscoutApiTreatment(Treatment treatment) {
        this();
        id = treatment.getId();
        createdAt = new DateTime(treatment.getEventTime());
        eventType = treatment.getEventType();
        enteredBy = treatment.getEnteredBy();
        glucose = treatment.getGlucose();
        glucoseType = treatment.getGlucoseType();
        insulin = treatment.getInsulin();
        units = treatment.getUnits();
        notes = treatment.getNotes();
        carbs = treatment.getCarbs();
        preBolus = treatment.getPreBolus();
    }

    public Treatment toTreatment() {
        return new Treatment(id, createdAt.toDate(), eventType, enteredBy, glucose, glucoseType,
                insulin, units, notes, carbs, preBolus);
    }
}
