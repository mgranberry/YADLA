// automatically generated, do not modify

package com.kludgenics.cgmlogger.model.flatbuffers.path;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class BloodGlucoseDay extends Table {
  public static BloodGlucoseDay getRootAsBloodGlucoseDay(ByteBuffer _bb) { return getRootAsBloodGlucoseDay(_bb, new BloodGlucoseDay()); }
  public static BloodGlucoseDay getRootAsBloodGlucoseDay(ByteBuffer _bb, BloodGlucoseDay obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public BloodGlucoseDay __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public long start() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public long end() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public float average() { int o = __offset(8); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float median() { int o = __offset(10); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float riMax() { int o = __offset(12); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float hbgi() { int o = __offset(14); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float riMin() { int o = __offset(16); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float lbgi() { int o = __offset(18); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float stdDev() { int o = __offset(20); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public int countLow() { int o = __offset(22); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int countHigh() { int o = __offset(24); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public BloodGlucose values(int j) { return values(new BloodGlucose(), j); }
  public BloodGlucose values(BloodGlucose obj, int j) { int o = __offset(26); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int valuesLength() { int o = __offset(26); return o != 0 ? __vector_len(o) : 0; }
  public PathDataBuffer trendline() { return trendline(new PathDataBuffer()); }
  public PathDataBuffer trendline(PathDataBuffer obj) { int o = __offset(28); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }

  public static int createBloodGlucoseDay(FlatBufferBuilder builder,
      long start,
      long end,
      float average,
      float median,
      float riMax,
      float hbgi,
      float riMin,
      float lbgi,
      float stdDev,
      int countLow,
      int countHigh,
      int values,
      int trendline) {
    builder.startObject(13);
    BloodGlucoseDay.addEnd(builder, end);
    BloodGlucoseDay.addStart(builder, start);
    BloodGlucoseDay.addTrendline(builder, trendline);
    BloodGlucoseDay.addValues(builder, values);
    BloodGlucoseDay.addCountHigh(builder, countHigh);
    BloodGlucoseDay.addCountLow(builder, countLow);
    BloodGlucoseDay.addStdDev(builder, stdDev);
    BloodGlucoseDay.addLbgi(builder, lbgi);
    BloodGlucoseDay.addRiMin(builder, riMin);
    BloodGlucoseDay.addHbgi(builder, hbgi);
    BloodGlucoseDay.addRiMax(builder, riMax);
    BloodGlucoseDay.addMedian(builder, median);
    BloodGlucoseDay.addAverage(builder, average);
    return BloodGlucoseDay.endBloodGlucoseDay(builder);
  }

  public static void startBloodGlucoseDay(FlatBufferBuilder builder) { builder.startObject(13); }
  public static void addStart(FlatBufferBuilder builder, long start) { builder.addLong(0, start, 0); }
  public static void addEnd(FlatBufferBuilder builder, long end) { builder.addLong(1, end, 0); }
  public static void addAverage(FlatBufferBuilder builder, float average) { builder.addFloat(2, average, 0); }
  public static void addMedian(FlatBufferBuilder builder, float median) { builder.addFloat(3, median, 0); }
  public static void addRiMax(FlatBufferBuilder builder, float riMax) { builder.addFloat(4, riMax, 0); }
  public static void addHbgi(FlatBufferBuilder builder, float hbgi) { builder.addFloat(5, hbgi, 0); }
  public static void addRiMin(FlatBufferBuilder builder, float riMin) { builder.addFloat(6, riMin, 0); }
  public static void addLbgi(FlatBufferBuilder builder, float lbgi) { builder.addFloat(7, lbgi, 0); }
  public static void addStdDev(FlatBufferBuilder builder, float stdDev) { builder.addFloat(8, stdDev, 0); }
  public static void addCountLow(FlatBufferBuilder builder, int countLow) { builder.addInt(9, countLow, 0); }
  public static void addCountHigh(FlatBufferBuilder builder, int countHigh) { builder.addInt(10, countHigh, 0); }
  public static void addValues(FlatBufferBuilder builder, int valuesOffset) { builder.addOffset(11, valuesOffset, 0); }
  public static int createValuesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startValuesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addTrendline(FlatBufferBuilder builder, int trendlineOffset) { builder.addOffset(12, trendlineOffset, 0); }
  public static int endBloodGlucoseDay(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

