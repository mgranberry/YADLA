// automatically generated, do not modify

package com.kludgenics.cgmlogger.model.flatbuffers.path;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class BloodGlucose extends Table {
  public static BloodGlucose getRootAsBloodGlucose(ByteBuffer _bb) { return getRootAsBloodGlucose(_bb, new BloodGlucose()); }
  public static BloodGlucose getRootAsBloodGlucose(ByteBuffer _bb, BloodGlucose obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public BloodGlucose __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public float value() { int o = __offset(4); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public long date() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0; }
  public byte type() { int o = __offset(8); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public byte unit() { int o = __offset(10); return o != 0 ? bb.get(o + bb_pos) : 0; }

  public static int createBloodGlucose(FlatBufferBuilder builder,
      float value,
      long date,
      byte type,
      byte unit) {
    builder.startObject(4);
    BloodGlucose.addDate(builder, date);
    BloodGlucose.addValue(builder, value);
    BloodGlucose.addUnit(builder, unit);
    BloodGlucose.addType(builder, type);
    return BloodGlucose.endBloodGlucose(builder);
  }

  public static void startBloodGlucose(FlatBufferBuilder builder) { builder.startObject(4); }
  public static void addValue(FlatBufferBuilder builder, float value) { builder.addFloat(0, value, 0); }
  public static void addDate(FlatBufferBuilder builder, long date) { builder.addLong(1, date, 0); }
  public static void addType(FlatBufferBuilder builder, byte type) { builder.addByte(2, type, 0); }
  public static void addUnit(FlatBufferBuilder builder, byte unit) { builder.addByte(3, unit, 0); }
  public static int endBloodGlucose(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

