package listeners;

import gamedata.GameData;
import gamedata.structs.PacketNode;
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
    private Map<PacketType, ArrayList<PacketListener>> packetHooks;
    
    
    public Proxy() {
        this.packetHooks = new HashMap<PacketType, ArrayList<PacketListener>>(20);
        this.serverPacketReceived = new ArrayList<PacketListener>(5);
        
        this.defaultHooks();
        System.out.println("Number of packets being hooked: " + this.packetHooks.size());
    }    

    
    //Registers listeners/callbacks for specified packet types.
    //Single packet type can be associated with multiple callback/listeners objects.
    public void hookPacket(PacketType pType, PacketListener listener) {
        //check invalid packet types.
        PacketNode pNode = GameData.packets.byName(pType.toString());
        if(pNode == null) {
            System.out.println("ERROR::Proxy.java: Packet type not supported for hooking.");
            return;
        }
            
        if(pNode.packetType == PacketType.UNKNOWN) {
            throw new java.lang.IllegalArgumentException();
        } else if(this.packetHooks.containsKey(pType)) {
            this.packetHooks.get(pType).add(listener);
        } else {
            ArrayList<PacketListener> list = new ArrayList<PacketListener>();
            list.add(listener);
            this.packetHooks.put(pType, list);
        }
    }
    
    //Notifys registered listeners/callbacks when a packet is received from the server.
    public void fireServerPacket(Client client, Packet packet) {
        //Notify listeners/callbacks when a server packet is received(regardless of type/hooks packets). --needs implementation        
        
        ArrayList<PacketListener> listeners = this.packetHooks.get(packet.type());
        if(listeners == null) {
            System.out.println("ERROR::proxy.java: Unable to initiate listeners for packet of type '" + packet.type().toString() + "'");
            return;
        }        
        for(PacketListener pl : listeners) {
            pl.onPacketReceived(client, packet);
        }
    }
    
    private void defaultHooks() {
        //Add default listeners
        this.hookPacket(PacketType.MAPINFO, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                LoadPacket loadPacket = (LoadPacket)Packet.create(PacketType.LOAD);
                loadPacket.characterId = client.charId;
                loadPacket.isFromArena = false;
                client.sendQueue.add(loadPacket);
            }
        });
        this.hookPacket(PacketType.CREATESUCCESS, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                CreateSuccessPacket csp = (CreateSuccessPacket)packet;
                client.objectId = csp.objectId;
                client.charId = csp.charId;
                client.loggedin = true;
            }
        });
        this.hookPacket(PacketType.UPDATE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                UpdateAckPacket uap = (UpdateAckPacket)Packet.create(PacketType.UPDATEACK);
                client.sendQueue.add(uap);
                
                //Update client state data.
                UpdatePacket up = (UpdatePacket)packet;
                for(Entity ent : up.newObjs) {
                    if(ent.status.objectId == client.objectId) {
                        try {
                            client.position = (Location)ent.status.position.clone();
                            System.out.println("NOTICE::Proxy.java: Client location updated by 'Update' packet." + client.position);
                            break;
                        } catch (CloneNotSupportedException e) {
                            System.out.println("ERROR::Proxy.java: Unable to set char position");
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        this.hookPacket(PacketType.NEWTICK, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                try {                    
                    NewTickPacket ntp = (NewTickPacket)packet;
                    client.lastTickId = ntp.tickId;
                    client.lastTickTime = ntp.tickTime; //time since last tick packet was sent (not very accurate).
                    client.currentTickTime = client.getTime();
                    for(Status s : ntp.statuses) {
                        if(client.objectId == s.objectId) {
                            client.position = (Location)s.position.clone();
                            System.out.println("NOTICE::Proxy.java: Client location updated by 'NewTick' packet." + client.position);
                            break;
                        }
                    }                   
                    //reply
                    MovePacket movePacket = (MovePacket)Packet.create(PacketType.MOVE);  
                    movePacket.tickId = client.lastTickId;
                    movePacket.time = client.getTime();
                    movePacket.newPosition = (Location)client.position.clone();
                    movePacket.records = new ArrayList<LocationRecord>();
                    client.sendQueue.add(movePacket);
                    
                    System.out.println("Replied to Tick [" + client.lastTickId + "] Time since last ticket: [" + client.lastTickTime + "]");
                } catch (CloneNotSupportedException e) {
                    System.out.println("ERROR::Proxy.java: Unable to set location. 'Move' packet not sent.");
                    e.printStackTrace();
                }
            }
        });
        this.hookPacket(PacketType.PING, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                PingPacket pingPacket = (PingPacket)packet;
                
                PongPacket pongPacket = (PongPacket)Packet.create(PacketType.PONG);                
                pongPacket.serial = pingPacket.serial;
                pongPacket.time = client.getTime();
                client.sendQueue.add(pongPacket);
            }
        });
        this.hookPacket(PacketType.FAILURE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                FailurePacket fp = (FailurePacket)packet;
                client.errorId = fp.errorId;
                client.errorMsg = fp.errorMessage + ""; //creates a new string rather than referencing old one.
            }
        });
        this.hookPacket(PacketType.GOTO, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                GotoAckPacket gap = (GotoAckPacket)Packet.create(PacketType.GOTOACK);
                gap.time = client.getTime();
                client.sendQueue.add(gap);
                
                try {
                    //Update own character position.
                    GotoPacket gp = (GotoPacket)packet;
                    if(client.objectId == gp.objectId) {
                        client.position = (Location)gp.location.clone();
                        System.out.println("NOTICE::Proxy.java: Client location updated by 'Goto' packet." + client.position);
                    }
                } catch(java.lang.CloneNotSupportedException e) {
                    System.out.println("ERROR::Proxy.java: Unable to update client location by 'Goto Packet'.");
                    e.printStackTrace();
                }
            }
        });
    }
}
