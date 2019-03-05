package listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.Client;
import net.packets.Packet;
import net.packets.Packet.*;
import net.packets.client.*;
import net.packets.dataobjects.Entity;
import net.packets.dataobjects.Location;
import net.packets.dataobjects.LocationRecord;
import net.packets.dataobjects.Status;
import net.packets.server.*;

//This class maintains lists of all the listerners that need to be informed upon a certain event.
public class Proxy {
    private ArrayList<PacketListener> serverPacketReceived;
    private Map<PacketType, PacketListener> packetHooks;
    
    public Proxy() {
        this.serverPacketReceived = new ArrayList<PacketListener>(5);
        this.packetHooks = new HashMap<PacketType, PacketListener>(40);
        this.setDefaultListeners();
    }
    
    
    //Registers listeners/callbacks specified packet types. A single listener/callback
    //can be associated with multiple packet types.
    public void hookPacket(PacketType pType, PacketListener listener) {
        this.packetHooks.put(pType, listener);
    }
    
    //Notifys registered listeners/callbacks when a packet is received from the server.
    public void fireServerPacket(Client client, Packet packet) {
        if(packet == null) {
            System.out.println("ERROR::Proxy.java: Packet is null");
        }
        PacketListener listener = this.packetHooks.get(packet.type());
        if(listener != null)
            listener.onPacketReceived(client, packet);
        else
            System.out.println("ERROR:Proxy.java: Unable to find listener for packet type [" + packet.type() + "]");
    }   
    
    private void setDefaultListeners() {
        this.packetHooks.put(PacketType.MAPINFO, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                LoadPacket loadPacket = new LoadPacket();
                loadPacket.characterId = 85;
                loadPacket.isFromArena = false;
                client.sendQueue.add(loadPacket);
            }
        });
        
        this.packetHooks.put(PacketType.CREATESUCCESS, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                CreateSuccessPacket csp = (CreateSuccessPacket)packet;
                client.myObjectId = csp.objectId;
                client.myCharId = csp.charId;
            }
        });
        
        this.packetHooks.put(PacketType.UPDATE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                UpdateAckPacket uap = new UpdateAckPacket();
                client.sendQueue.add(uap);
                
                UpdatePacket up = (UpdatePacket)packet;
                for(Entity ent : up.newObjs) {
                    if(ent.status.objectId == client.myObjectId) {
                        try {
                            client.position = (Location)ent.status.position.clone();
                            break;
                        } catch (CloneNotSupportedException e) {
                            System.out.println("ERROR::Proxy.java: Unable to set char position");
                            e.printStackTrace();
                        }
                    }
                }                                
            }
        });
        
        this.packetHooks.put(PacketType.NEWTICK, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                NewTickPacket ntp = (NewTickPacket)packet;                
                client.lastTickTime = ntp.tickTime;
                client.lastTickId = ntp.tickId;
                client.currentTickTime = client.getTime();
                
                //Get positino of char if not already attained.
                if(client.position.x == 0.0 && client.position.y == 0.0) {
                    try {
                        for(Status s : ntp.statuses) {
                            if(client.myObjectId == s.objectId) {
                                client.position = (Location)s.position.clone();
                                break;
                            }
                        }
                    } catch(Exception e) {
                        System.out.println("ERROR::Proxy.java: Unable to set location.");
                        e.printStackTrace();
                        //exit.
                    }
                }
                
                //reply with move packet
                MovePacket movePacket = new MovePacket();
                movePacket.tickId = ntp.tickId;
                movePacket.time = client.getTime();
                movePacket.newPosition = client.position;
                movePacket.records = new ArrayList<LocationRecord>();
                client.sendQueue.add(movePacket);                
            }
        });
        
        this.packetHooks.put(PacketType.PING, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                PingPacket pingPacket = (PingPacket)packet;
                
                PongPacket pongPacket = new PongPacket();
                pongPacket.serial = pingPacket.serial;
                pongPacket.time = client.getTime();
                client.sendQueue.add(pongPacket);
            }
        });
        
        this.packetHooks.put(PacketType.FAILURE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                FailurePacket fp = (FailurePacket)packet;
                System.out.println("Failure Packet: [errId: " + fp.errorId + ", " + fp.errorMessage + "]");
            }
        });
    }
}
