// IPlaceService.aidl
package com.kludgenics.logdata.location.api;

parcelable ParcelableLocation;
parcelable Position;

interface IPlaceService {

    List<ParcelableLocation> findLocations();
    List<ParcelableLocation> findLocations(in Position position);
    List<ParcelableLocation> findLocations(float latitude, float longitude, float radius);
    List<ParcelableLocation> findLocations(float latitude, float longitude);

}
