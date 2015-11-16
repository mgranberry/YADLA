// automatically generated, do not modify

package com.kludgenics.cgmlogger.model.flatbuffers.path;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class BloodGlucosePeriod extends Table {
  public static BloodGlucosePeriod getRootAsBloodGlucosePeriod(ByteBuffer _bb) { return getRootAsBloodGlucosePeriod(_bb, new BloodGlucosePeriod()); }
  public static BloodGlucosePeriod getRootAsBloodGlucosePeriod(ByteBuffer _bb, BloodGlucosePeriod obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public BloodGlucosePeriod __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public float average() { int o = __offset(4); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float median() { int o = __offset(6); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float rhMax() { int o = __offset(8); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float rlMax() { int o = __offset(10); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float hbgi() { int o = __offset(12); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float lbgi() { int o = __offset(14); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float adrr() { int o = __offset(16); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float stdDev() { int o = __offset(18); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public int countLow() { int o = __offset(20); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int countHigh() { int o = __offset(22); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public float lowThreshold() { int o = __offset(24); return o != 0 ? bb.getFloat(o + bb_pos) : 80; }
  public float highThreshold() { int o = __offset(26); return o != 0 ? bb.getFloat(o + bb_pos) : 180; }

  public static int createBloodGlucosePeriod(FlatBufferBuilder builder,
      float average,
      float median,
      float rhMax,
      float rlMax,
      float hbgi,
      float lbgi,
      float adrr,
      float stdDev,
      int countLow,
      int countHigh,
      float lowThreshold,
      float highThreshold) {
    builder.startObject(12);
    BloodGlucosePeriod.addHighThreshold(builder, highThreshold);
    BloodGlucosePeriod.addLowThreshold(builder, lowThreshold);
    BloodGlucosePeriod.addCountHigh(builder, countHigh);
    BloodGlucosePeriod.addCountLow(builder, countLow);
    BloodGlucosePeriod.addStdDev(builder, stdDev);
    BloodGlucosePeriod.addAdrr(builder, adrr);
    BloodGlucosePeriod.addLbgi(builder, lbgi);
    BloodGlucosePeriod.addHbgi(builder, hbgi);
    BloodGlucosePeriod.addRlMax(builder, rlMax);
    BloodGlucosePeriod.addRhMax(builder, rhMax);
    BloodGlucosePeriod.addMedian(builder, median);
    BloodGlucosePeriod.addAverage(builder, average);
    return BloodGlucosePeriod.endBloodGlucosePeriod(builder);
  }

  public static void startBloodGlucosePeriod(FlatBufferBuilder builder) { builder.startObject(12); }
  public static void addAverage(FlatBufferBuilder builder, float average) { builder.addFloat(0, average, 0); }
  public static void addMedian(FlatBufferBuilder builder, float median) { builder.addFloat(1, median, 0); }
  public static void addRhMax(FlatBufferBuilder builder, float rhMax) { builder.addFloat(2, rhMax, 0); }
  public static void addRlMax(FlatBufferBuilder builder, float rlMax) { builder.addFloat(3, rlMax, 0); }
  public static void addHbgi(FlatBufferBuilder builder, float hbgi) { builder.addFloat(4, hbgi, 0); }
  public static void addLbgi(FlatBufferBuilder builder, float lbgi) { builder.addFloat(5, lbgi, 0); }
  public static void addAdrr(FlatBufferBuilder builder, float adrr) { builder.addFloat(6, adrr, 0); }
  public static void addStdDev(FlatBufferBuilder builder, float stdDev) { builder.addFloat(7, stdDev, 0); }
  public static void addCountLow(FlatBufferBuilder builder, int countLow) { builder.addInt(8, countLow, 0); }
  public static void addCountHigh(FlatBufferBuilder builder, int countHigh) { builder.addInt(9, countHigh, 0); }
  public static void addLowThreshold(FlatBufferBuilder builder, float lowThreshold) { builder.addFloat(10, lowThreshold, 80); }
  public static void addHighThreshold(FlatBufferBuilder builder, float highThreshold) { builder.addFloat(11, highThreshold, 180); }
  public static int endBloodGlucosePeriod(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

