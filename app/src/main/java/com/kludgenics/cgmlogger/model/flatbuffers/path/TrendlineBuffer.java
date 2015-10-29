// automatically generated, do not modify

package com.kludgenics.cgmlogger.model.flatbuffers.path;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class TrendlineBuffer extends Table {
  public static TrendlineBuffer getRootAsTrendlineBuffer(ByteBuffer _bb) { return getRootAsTrendlineBuffer(_bb, new TrendlineBuffer()); }
  public static TrendlineBuffer getRootAsTrendlineBuffer(ByteBuffer _bb, TrendlineBuffer obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public TrendlineBuffer __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public PathDataBuffer trend() { return trend(new PathDataBuffer()); }
  public PathDataBuffer trend(PathDataBuffer obj) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }

  public static int createTrendlineBuffer(FlatBufferBuilder builder,
      int trend) {
    builder.startObject(1);
    TrendlineBuffer.addTrend(builder, trend);
    return TrendlineBuffer.endTrendlineBuffer(builder);
  }

  public static void startTrendlineBuffer(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addTrend(FlatBufferBuilder builder, int trendOffset) { builder.addOffset(0, trendOffset, 0); }
  public static int endTrendlineBuffer(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

