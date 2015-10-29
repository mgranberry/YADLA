// automatically generated, do not modify

package com.kludgenics.cgmlogger.model.flatbuffers.path;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class PathDataNodeBuffer extends Table {
  public static PathDataNodeBuffer getRootAsPathDataNodeBuffer(ByteBuffer _bb) { return getRootAsPathDataNodeBuffer(_bb, new PathDataNodeBuffer()); }
  public static PathDataNodeBuffer getRootAsPathDataNodeBuffer(ByteBuffer _bb, PathDataNodeBuffer obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public PathDataNodeBuffer __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public byte command() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public float params(int j) { int o = __offset(6); return o != 0 ? bb.getFloat(__vector(o) + j * 4) : 0; }
  public int paramsLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer paramsAsByteBuffer() { return __vector_as_bytebuffer(6, 4); }

  public static int createPathDataNodeBuffer(FlatBufferBuilder builder,
      byte command,
      int params) {
    builder.startObject(2);
    PathDataNodeBuffer.addParams(builder, params);
    PathDataNodeBuffer.addCommand(builder, command);
    return PathDataNodeBuffer.endPathDataNodeBuffer(builder);
  }

  public static void startPathDataNodeBuffer(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addCommand(FlatBufferBuilder builder, byte command) { builder.addByte(0, command, 0); }
  public static void addParams(FlatBufferBuilder builder, int paramsOffset) { builder.addOffset(1, paramsOffset, 0); }
  public static int createParamsVector(FlatBufferBuilder builder, float[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addFloat(data[i]); return builder.endVector(); }
  public static void startParamsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endPathDataNodeBuffer(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

