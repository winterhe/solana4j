package com.lmax.solana4j.encoding;

import com.lmax.solana4j.api.MessageVisitor;
import java.nio.ByteBuffer;

public final class MessageToSignVisitor implements MessageVisitor<byte[]> {
  @Override
  public byte[] visit(MessageView view) {
    // get unsigned message ByteBuffer(only for sign message,
    // do not include signatures part)
    // and create a new ByteBuffer view by duplicate, it will share memory
    // with previous Buffer, but its position/offset, limit, mark are independent
    ByteBuffer txBuf = view.transaction().duplicate();
    // get remain readable bytes (limit-position/offset)
    byte[] data = new byte[txBuf.remaining()];
    // read buffer remain data to data array,
    // after read, txBuf position/offset will move to end.
    txBuf.get(data);
    return data;
  }
}
