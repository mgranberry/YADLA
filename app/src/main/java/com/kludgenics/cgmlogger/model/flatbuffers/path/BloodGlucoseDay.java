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

  public BloodGlucosePeriod period() { return period(new BloodGlucosePeriod()); }
  public BloodGlucosePeriod period(BloodGlucosePeriod obj) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }
  public BloodGlucose values(int j) { return values(new BloodGlucose(), j); }
  public BloodGlucose values(BloodGlucose obj, int j) { int o = __offset(6); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int valuesLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public PathDataBuffer trendline() { return trendline(new PathDataBuffer()); }
  public PathDataBuffer trendline(PathDataBuffer obj) { int o = __offset(8); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }

  public static int createBloodGlucoseDay(FlatBufferBuilder builder,
      int period,
      int values,
      int trendline) {
    builder.startObject(3);
    BloodGlucoseDay.addTrendline(builder, trendline);
    BloodGlucoseDay.addValues(builder, values);
    BloodGlucoseDay.addPeriod(builder, period);
    return BloodGlucoseDay.endBloodGlucoseDay(builder);
  }

  public static void startBloodGlucoseDay(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addPeriod(FlatBufferBuilder builder, int periodOffset) { builder.addOffset(0, periodOffset, 0); }
  public static void addValues(FlatBufferBuilder builder, int valuesOffset) { builder.addOffset(1, valuesOffset, 0); }
  public static int createValuesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startValuesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addTrendline(FlatBufferBuilder builder, int trendlineOffset) { builder.addOffset(2, trendlineOffset, 0); }
  public static int endBloodGlucoseDay(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

