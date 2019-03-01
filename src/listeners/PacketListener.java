package listeners;

import net.Client;
import net.packets.Packet;

public interface PacketListener {
    public void onPacketReceived(Client client, Packet packet);    
}
