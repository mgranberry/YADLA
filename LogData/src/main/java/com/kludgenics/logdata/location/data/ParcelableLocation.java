package com.kludgenics.logdata.location.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by matthiasgranberry on 5/18/15.
 */
public class ParcelableLocation implements Parcelable, Location {
    private String mId;
    private String mName;
    private String mLocationTypes;
    private String mAddress;
    private String mAttributionSnippet;
    private Position mPosition;

    public ParcelableLocation(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        mLocationTypes = in.readString();
        mAddress = in.readString();
        mAttributionSnippet = in.readString();
        mPosition = (Position) in.readValue(null);
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getLocationTypes() {
        return mLocationTypes;
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public Position getPosition() {
        return mPosition;
    }

    @Override
    public String getAttributionSnippet() {
        return mAttributionSnippet;
    }

    public static final Parcelable.Creator<ParcelableLocation> CREATOR
            = new Creator<ParcelableLocation>() {
        @Override
        public ParcelableLocation createFromParcel(Parcel source) {
            return new ParcelableLocation(source);
        }

        @Override
        public ParcelableLocation[] newArray(int size) {
            return new ParcelableLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getName());
        dest.writeString(getLocationTypes());
        dest.writeString(getAddress());
        dest.writeString(getAttributionSnippet());
        dest.writeValue(getPosition());
    }
}
