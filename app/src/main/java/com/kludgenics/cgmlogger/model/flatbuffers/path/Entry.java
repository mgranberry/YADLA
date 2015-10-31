// automatically generated, do not modify

package com.kludgenics.cgmlogger.model.flatbuffers.path;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class Entry extends Table {
  public static Entry getRootAsEntry(ByteBuffer _bb) { return getRootAsEntry(_bb, new Entry()); }
  public static Entry getRootAsEntry(ByteBuffer _bb, Entry obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public Entry __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public byte recordType() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table record(Table obj) { int o = __offset(6); return o != 0 ? __union(obj, o) : null; }

  public static int createEntry(FlatBufferBuilder builder,
      byte record_type,
      int record) {
    builder.startObject(2);
    Entry.addRecord(builder, record);
    Entry.addRecordType(builder, record_type);
    return Entry.endEntry(builder);
  }

  public static void startEntry(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addRecordType(FlatBufferBuilder builder, byte recordType) { builder.addByte(0, recordType, 0); }
  public static void addRecord(FlatBufferBuilder builder, int recordOffset) { builder.addOffset(1, recordOffset, 0); }
  public static int endEntry(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishEntryBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

