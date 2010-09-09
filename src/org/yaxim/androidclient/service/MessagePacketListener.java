package org.yaxim.androidclient.service;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
//import org.yaxim.androidclient.Smackable;
import org.yaxim.androidclient.util.LogConstants;
import android.util.Log;

public final class MessagePacketListener implements PacketListener {

	final static private String TAG = "MessagePacketListener";
  SmackableImp smackable;
  Message msg;

  public MessagePacketListener(SmackableImp smackable) {
    this.smackable = smackable;
  }


  private String lastID = "";
  public void processPacket(Packet packet) {
    if (packet instanceof Message) {
      msg = (Message) packet;
      String chatMessage = msg.getBody();

      if (chatMessage == null)
        return;
      
      String newID = msg.getPacketID();
      if (newID=="")
        Log.i(TAG, "received message without ID!!!\n" + msg.toXML());

      if (newID == lastID)
        return;

      lastID = newID;

      if (LogConstants.LOG_DEBUG) {
        Log.d(TAG, "received " + msg.getPacketID());
        //Log.d(TAG, msg.toString());
        //Log.d(TAG, msg.toXML());
      }

      this.smackable.processMessage(msg);
    }
  }
}
