package org.yaxim.androidclient.service;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
//import org.yaxim.androidclient.Smackable;

public final class MessagePacketListener implements PacketListener {

  SmackableImp smackable;
  Message msg;

  public MessagePacketListener(SmackableImp smackable) {
    this.smackable = smackable;
  }


  /*
     Packet lastPacket = null;
     long lastTime = 0;
     */

  public void processPacket(Packet packet) {
    /*
    // do equality check against looping bug in smack
    long time = System.currentTimeMillis();
    if (packet.equals(lastPacket) && time < lastTime + 100) {
    debugLog("processPacket: duplicate " + packet);
    return;
    } else lastPacket = packet;
    lastTime = time;
    */

    if (packet instanceof Message) {
      msg = (Message) packet;
      String chatMessage = msg.getBody();

      if (chatMessage == null) {
        return;
      }

      this.smackable.processMessage(msg);
    }
  }
}
