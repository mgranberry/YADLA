// automatically generated, do not modify

package com.kludgenics.cgmlogger.model.flatbuffers.path;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class AgpPathBuffer extends Table {
  public static AgpPathBuffer getRootAsAgpPathBuffer(ByteBuffer _bb) { return getRootAsAgpPathBuffer(_bb, new AgpPathBuffer()); }
  public static AgpPathBuffer getRootAsAgpPathBuffer(ByteBuffer _bb, AgpPathBuffer obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public AgpPathBuffer __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public PathDataBuffer outer() { return outer(new PathDataBuffer()); }
  public PathDataBuffer outer(PathDataBuffer obj) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }
  public PathDataBuffer inner() { return inner(new PathDataBuffer()); }
  public PathDataBuffer inner(PathDataBuffer obj) { int o = __offset(6); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }
  public PathDataBuffer median() { return median(new PathDataBuffer()); }
  public PathDataBuffer median(PathDataBuffer obj) { int o = __offset(8); return o != 0 ? obj.__init(__indirect(o + bb_pos), bb) : null; }

  public static int createAgpPathBuffer(FlatBufferBuilder builder,
      int outer,
      int inner,
      int median) {
    builder.startObject(3);
    AgpPathBuffer.addMedian(builder, median);
    AgpPathBuffer.addInner(builder, inner);
    AgpPathBuffer.addOuter(builder, outer);
    return AgpPathBuffer.endAgpPathBuffer(builder);
  }

  public static void startAgpPathBuffer(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addOuter(FlatBufferBuilder builder, int outerOffset) { builder.addOffset(0, outerOffset, 0); }
  public static void addInner(FlatBufferBuilder builder, int innerOffset) { builder.addOffset(1, innerOffset, 0); }
  public static void addMedian(FlatBufferBuilder builder, int medianOffset) { builder.addOffset(2, medianOffset, 0); }
  public static int endAgpPathBuffer(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

