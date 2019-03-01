package listeners;

import gamedata.GameData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.Client;
import net.packets.Packet;
import net.packets.Packet.*;
import net.packets.client.*;
import net.packets.dataobjects.Entity;
import net.packets.dataobjects.Location;
import net.packets.dataobjects.LocationRecord;
import net.packets.server.*;

//This class maintains lists of all the listerners that need to be informed upon a certain event.
//This a centeral location where a client retreives a list of all the listeners.
public class Proxy {
    private ArrayList<PacketListener> serverPacketReceived = null;
    private Map<PacketListener, ArrayList<PacketType>> packetHooks = null;
    
    public Proxy() {
        this.packetHooks = new HashMap<PacketListener, ArrayList<PacketType>>(20);
        this.serverPacketReceived = new ArrayList<PacketListener>(5);
        //Add default listeners
        this.hookPacket(PacketType.MAPINFO, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                LoadPacket loadPacket = (LoadPacket)Packet.create(PacketType.LOAD);
                loadPacket.characterId = 85;
                loadPacket.isFromArena = false;
                client.sendQueue.add(loadPacket);
            }
        });
        this.hookPacket(PacketType.CREATESUCCESS, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                CreateSuccessPacket csp = (CreateSuccessPacket)packet;
                client.myObjectId = csp.objectId;
                client.myCharId = csp.charId;
            }
        });
        this.hookPacket(PacketType.UPDATE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                UpdateAckPacket uap = (UpdateAckPacket)Packet.create(PacketType.UPDATEACK);
                
                UpdatePacket up = (UpdatePacket)packet;
                for(Entity e : up.newObjs) {
                    if(e.status.objectId == client.myObjectId) {
                        try {
                            client.position = (Location)e.status.position.clone();
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                client.sendQueue.add(uap);                
            }
        });
        this.hookPacket(PacketType.NEWTICK, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                try {
                    MovePacket movePacket = (MovePacket)Packet.create(PacketType.MOVE);
                    
                    NewTickPacket ntp = (NewTickPacket)packet;
                    client.tickId = ntp.tickId;
                    client.previousTickTime = ntp.tickTime;
                    
                    movePacket.tickId = client.tickId;
                    movePacket.time = ((int)System.currentTimeMillis()) - client.clientStartTime;
                    movePacket.newPosition = (Location)client.position.clone();
                    movePacket.records = new ArrayList<LocationRecord>();
                    client.sendQueue.add(movePacket);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        this.hookPacket(PacketType.PING, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                PingPacket pingPacket = (PingPacket)packet;
                PongPacket pongPacket = (PongPacket)Packet.create(PacketType.PONG);
                
                client.pingSerial = pingPacket.serial;
                
                pongPacket.serial = client.pingSerial;
                pongPacket.time = ((int)System.currentTimeMillis()) - client.clientStartTime;
                client.sendQueue.add(pongPacket);
            }
        });
    }
    
    public void start() {}
    public void stop() {}
    
    //Registers listeners/callbacks specified packet types. A single listener/callback
    //can be associated with multiple packet types.
    public void hookPacket(PacketType pType, PacketListener listener) {
        //check for invalid packet types.
        if(GameData.packets.byName(pType.toString()).packetType == PacketType.UNKNOWN) {
            throw new java.lang.IllegalArgumentException();
        } else if(this.packetHooks.containsKey(listener)) {
            this.packetHooks.get(listener).add(pType);
        } else {
            ArrayList<PacketType> list = new ArrayList<PacketType>();
            list.add(pType);
            this.packetHooks.put(listener, list);
        }
    }
    
    //Notifys registered listeners/callbacks when a packet is received from the server.
    public void fireServerPacket(Client client, Packet packet) {
        //Notify listeners/callbacks when a server packet is received(regardless of type). --needs implementation
        
        //Notify hooked listeners/callbacks.
        Set<Map.Entry<PacketListener, ArrayList<PacketType>>> entrySet = this.packetHooks.entrySet();
        for(Map.Entry<PacketListener, ArrayList<PacketType>> pair : entrySet) {
            if(pair.getValue().contains(packet.type())) {
                pair.getKey().onPacketReceived(client, packet);
            }
        }        
    }    
}
