package listeners;

import gamedata.GameData;
import gamedata.structs.PacketNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import net.Client;
import net.packets.Packet;
import net.packets.Packet.*;
import net.packets.client.*;
import net.packets.dataobjects.Entity;
import net.packets.dataobjects.Location;
import net.packets.dataobjects.LocationRecord;
import net.packets.dataobjects.StatData;
import net.packets.dataobjects.Status;
import net.packets.dataobjects.VaultChest;
import net.packets.server.*;

//This class maintains lists listerners that need to be informed upon events.
public class Proxy {
    private static final java.util.logging.Logger logger = util.Logger.getLogger(Proxy.class.getSimpleName());
    private final ArrayList<ConnectionListener> clientBeginConnect;
    private final ArrayList<ConnectionListener> clientDisconnect;
    private final ArrayList<ConnectionListener> clientReconnect;
    private final ArrayList<PacketListener> serverPacketReceived;
    private final Map<PacketType, ArrayList<PacketListener>> packetHooks;
    
    
    public Proxy() {
        this.packetHooks = new HashMap<PacketType, ArrayList<PacketListener>>(20);
        this.serverPacketReceived = new ArrayList<PacketListener>(5);
        this.clientBeginConnect = new ArrayList<ConnectionListener>(5);
        this.clientDisconnect = new ArrayList<ConnectionListener>(5);
        this.clientReconnect = new ArrayList<ConnectionListener>(5);        
        
        this.defaultHooks();
        Proxy.logger.log(Level.FINE, () -> "Number of different packets being hooked: " + this.packetHooks.size());
    }    

    
    //Registers listeners/callbacks for specified packet types.
    //Single packet type can be associated with multiple callback/listeners objects.
    public void hookPacket(PacketType pType, PacketListener listener) {
        PacketNode pNode = GameData.packets.byName(pType.toString());
        if(pNode == null) {
            Proxy.logger.log(Level.SEVERE, () -> "Failed to hook null packet");
            throw new java.lang.IllegalArgumentException("Failed to hook null packet");
        }
            
        if(pNode.packetType == PacketType.UNKNOWN) {
            Proxy.logger.log(Level.SEVERE, () -> "Failed to hook UNKNOWN packet");
            throw new java.lang.IllegalArgumentException("Failed to hook UNKNOWN packet");
        } else if(this.packetHooks.containsKey(pType)) {
            this.packetHooks.get(pType).add(listener);
        } else {
            ArrayList<PacketListener> list = new ArrayList<PacketListener>();
            list.add(listener);
            this.packetHooks.put(pType, list);
        }
    }
    
    //Registers listeners for when client attempts connection to the server, but before it's connected
    public void hookBeginConnect(ConnectionListener listener) {
        this.clientBeginConnect.add(listener);
    }
    
    //Registers listeners for when client disconnects from server
    public void hookDisconnect(ConnectionListener listener) {
        this.clientDisconnect.add(listener);
    }
    
    //Registers listeners for when client attempts to reconnect to sever.
    public void hookReconnect(ConnectionListener listener) {
        this.clientReconnect.add(listener);
    }
    
    //Notifys registered listeners/callbacks when a packet is received from the server.
    public void fireServerPacket(Client client, Packet packet) {
        if(packet == null) { return; }        
        
        ArrayList<PacketListener> listeners = this.packetHooks.get(packet.type());
        if(listeners == null) {            
            Proxy.logger.log(Level.FINE, () -> "Unable to initiate listeners for packet of type '" + packet.type().toString() + "'");
            return;
        }
        for(PacketListener pl : listeners) {        
            pl.onPacketReceived(client, packet);
        }
    }
    
    //Notify registered listeners of when client attempts connection to the server, but before it's connected
    public void fireClientBeginConnect(Client client) {
        for(ConnectionListener listener : this.clientBeginConnect) {
            listener.onConnection(client);
        }
    }
    
    //Notify registered listeners of client disconnecting from server.
    public void fireClientDisconnect(Client client) {
        for(ConnectionListener listener : this.clientDisconnect) {
            listener.onConnection(client);
        }
    }
    
    //Notify registered listeners of client reconnecting.
    public void fireClientReconnect(Client client) {
        for(ConnectionListener listener : this.clientReconnect) {
            listener.onConnection(client);
        }
    }
    
    
    //Basic hooks needed to maintain connection and perfrom essential tasks.
    private void defaultHooks() {
        //Add default listeners
        this.hookPacket(PacketType.MAPINFO, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                LoadPacket loadPacket = (LoadPacket)Packet.create(PacketType.LOAD);
                loadPacket.characterId = client.charId;
                loadPacket.isFromArena = false;
                client.sendQueue.add(loadPacket);                
                Proxy.logger.log(Level.FINE, () -> "Connected to map: [" + ((MapInfoPacket)packet).name + "], loading Char ID: " + loadPacket.characterId);
            }
        });
        this.hookPacket(PacketType.CREATESUCCESS, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                CreateSuccessPacket csp = (CreateSuccessPacket)packet;
                client.objectId = csp.objectId;
                client.charId = csp.charId;
                client.moveToPos = null;
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
                            Proxy.logger.log(Level.INFO, () -> "Client location updated by 'Update' packet." + client.position);
                            if(client.moveToPos == null) {
                                client.moveToPos = (Location)ent.status.position.clone();
                            }
                            for(StatData sd : ent.status.data) {
                                if(sd.type == StatData.StatsType.HasBackpack) {
                                    client.hasBackpack = (sd.intValue > 0);                                    
                                    Proxy.logger.log(Level.INFO, () -> "Client has a backpack: [" + (sd.intValue > 0) + "]");
                                } else if(sd.type == StatData.StatsType.Name) {
                                    client.accountName = sd.stringValue;
                                }
                            }
                            break;
                        } catch (CloneNotSupportedException e) {
                            Proxy.logger.log(Level.WARNING, "Failed to clone 'Location' from update packet");
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
                            Proxy.logger.log(Level.INFO, () -> "Client location updated by 'NewTick' packet." + client.position);
                            break;
                        }
                    }                   
                    //reply
                    MovePacket movePacket = (MovePacket)Packet.create(PacketType.MOVE);  
                    movePacket.tickId = client.lastTickId;
                    movePacket.time = client.getTime();

                    //Determine new location to move to. Movement restricted by client spd.
                    if(client.position.isSameAs(client.moveToPos)) {
                        movePacket.newPosition = (Location)client.position.clone();
                    } else {
                        Location temp = (Location)client.moveToPos.clone();                        
                        float clientSpd = client.getClientSpeed();
                        //Finds correct x-coord
                        float coordDiff = temp.x - client.position.x;
                        if(Math.abs(coordDiff) > clientSpd) {
                            if(coordDiff < 0.0) {
                                temp.x = client.position.x - clientSpd;
                            } else {
                                temp.x = client.position.x + clientSpd;
                            }
                        }
                        coordDiff = temp.y - client.position.y;
                        if(Math.abs(coordDiff) > clientSpd) {
                            if(coordDiff < 0.0) {
                                temp.y = client.position.y - clientSpd;
                            } else {
                                temp.y = client.position.y + clientSpd;
                            }
                        }
                        movePacket.newPosition = temp;
                    }
                    
                    movePacket.records = new ArrayList<LocationRecord>();
                    client.sendQueue.add(movePacket);
                    
                    Proxy.logger.log(Level.FINE, () -> "Replied to Tick [" + client.lastTickId + "] Time since last ticket: [" + client.lastTickTime + "]");
                } catch (CloneNotSupportedException e) {
                    Proxy.logger.log(Level.WARNING, () -> "Unable to set location. 'Move' packet not sent.");
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
                client.errorMsg = fp.errorMessage;
                Proxy.logger.log(Level.INFO, () -> "Faliure ID: [" + fp.errorId + "], errMsg: [" + fp.errorMessage + "]");
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
                        Proxy.logger.log(Level.INFO, () -> "Client location updated by 'Goto' packet." + client.position);
                    }
                } catch(java.lang.CloneNotSupportedException e) {
                    Proxy.logger.log(Level.WARNING, "Failed to update client location by 'Goto' Packet.");
                }
            }
        });
        
        //Parses stat data about inventory, backpack, and vault chests; possibly new info.
        this.hookPacket(PacketType.UPDATE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                UpdatePacket up = (UpdatePacket)packet; 
                for(Entity ent : up.newObjs) {
                    //Loads inventory and backpack items
                    if(ent.status.objectId == client.objectId) {
                        for(StatData sd : ent.status.data) {
                            //Ignore first 4 inv slots which refer to player equip slots in this context
                            if(sd.type >= StatData.StatsType.Inventory4 && sd.type <= StatData.StatsType.Inventory11) {
                                client.inv.add(sd.type - StatData.StatsType.Inventory4, sd.intValue);
                                client.itemListsUpdated = true;
                            } else if(sd.type >= StatData.StatsType.Backpack0 && sd.type <= StatData.StatsType.Backpack7) {
                                client.backpack.add(sd.type - StatData.StatsType.Backpack0, sd.intValue);
                                client.itemListsUpdated = true;
                            }
                        }
                        if(client.itemListsUpdated && Proxy.logger.isLoggable(Level.FINE)) {
                            StringBuilder itemList = new StringBuilder(100);
                            Proxy.logger.log(Level.FINE, () -> "UPDATED INV: ");
                            for(int i = 0; i < client.inv.size(); i++) {
                                itemList.append(GameData.items.byId(client.inv.get(i)).name + (i == client.inv.size()-1? "]" : ", "));
                            }
                            Proxy.logger.log(Level.FINE, () -> itemList.toString());
                            
                            Proxy.logger.log(Level.FINE, () -> "UPDATED BACKPACK: ");
                            itemList.delete(0, itemList.length());
                            for(int i = 0; i < client.backpack.size(); i++) {
                                itemList.append(GameData.items.byId(client.backpack.get(i)).name + (i == client.backpack.size()-1? "]" : ", "));
                            }
                            Proxy.logger.log(Level.FINE, () -> itemList.toString());
                        }                        
                        //client.itemListsUpdated = true;
                    }
                    //Loads vault chest data
                    else if(ent.objectType == 0x0504) {
                        Proxy.logger.log(Level.INFO, () -> "Chest ID is: " + ent.status.objectId + "; Chest pos: " + ent.status.position);
                        ArrayList<Integer> chestItems = new ArrayList<>(8);
                        for(StatData sd : ent.status.data) {
                            if(sd.type >= StatData.StatsType.Inventory0 && sd.type <= StatData.StatsType.Inventory7) {
                                Proxy.logger.log(Level.INFO, () -> (sd.type - StatData.StatsType.Inventory0) + "\t" + GameData.items.byId(sd.intValue).name);
                                chestItems.add(sd.type - StatData.StatsType.Inventory0, sd.intValue);
                            }
                        }
                        VaultChest chest;
                        try {
                            chest = new VaultChest(ent.status.objectId, (Location)ent.status.position.clone(), chestItems);
                        } catch (CloneNotSupportedException e) {
                            Proxy.logger.log(Level.WARNING, e, () -> "Failed to clone chest location in UPDATE packet. Setting to default.");
                            chest = new VaultChest(ent.status.objectId, Location.empty(), chestItems);
                        }
                        client.vaultChests.put(ent.status.objectId, chest);
                        client.itemListsUpdated = true;
                        client.vaultDataLastUpdated = client.getTime();
                    }
                }
            }
        });
        
        //Parses stat data about inventory, backpack, and vault chests; updates old info.
        this.hookPacket(PacketType.NEWTICK, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                NewTickPacket ntp = (NewTickPacket)packet;
                for(Status s : ntp.statuses) {
                    //Update inventory and backpack items
                    if(s.objectId == client.objectId) {
                        for(StatData sd : s.data) {
                            //Ignore first 4 inventory slots which refer to player equip slots in this context
                            if(sd.type >= StatData.StatsType.Inventory4 && sd.type <= StatData.StatsType.Inventory11) {
                                client.inv.set(sd.type - StatData.StatsType.Inventory4, sd.intValue);
                                client.itemListsUpdated = true;
                            } else if(sd.type >= StatData.StatsType.Backpack0 && sd.type <= StatData.StatsType.Backpack7) {
                                client.backpack.set(sd.type - StatData.StatsType.Backpack0, sd.intValue);
                                client.itemListsUpdated = true;
                            }
                        }
                        if(client.itemListsUpdated && Proxy.logger.isLoggable(Level.FINE)) {
                            StringBuilder itemList = new StringBuilder(100);
                            Proxy.logger.log(Level.FINE, () -> "UPDATED INV: ");
                            for(int i = 0; i < client.inv.size(); i++) {
                                itemList.append(GameData.items.byId(client.inv.get(i)).name + (i == client.inv.size()-1? "]" : ", "));
                            }
                            Proxy.logger.log(Level.FINE, () -> itemList.toString());
                            
                            Proxy.logger.log(Level.FINE, () -> "UPDATED BACKPACK: ");
                            itemList.delete(0, itemList.length());
                            for(int i = 0; i < client.backpack.size(); i++) {
                                itemList.append(GameData.items.byId(client.backpack.get(i)).name + (i == client.backpack.size()-1? "]" : ", "));
                            }
                            Proxy.logger.log(Level.FINE, () -> itemList.toString());
                        }
                        //client.itemListsUpdated = true;
                    }
                    //Update vault chest data
                    else if(client.vaultChests.containsKey(s.objectId)) {
                        Proxy.logger.log(Level.INFO, () -> "Chest ID is: " + s.objectId + "; Chest pos: [" + s.position + "]");
                        VaultChest chest = client.vaultChests.get(s.objectId);
                        for(StatData sd : s.data) {
                            if(sd.type >= StatData.StatsType.Inventory0 && sd.type <= StatData.StatsType.Inventory7) {
                                Proxy.logger.log(Level.INFO, () -> (sd.type - StatData.StatsType.Inventory0) + "\t" + GameData.items.byId(sd.intValue).name);
                                chest.items.set(sd.type - StatData.StatsType.Inventory0, sd.intValue);                                
                            }
                        }
                        client.vaultChests.put(s.objectId, chest);
                        client.itemListsUpdated = true;
                        client.vaultDataLastUpdated = client.getTime();
                    }
                }
            }
        }); 
        
        
        /*
        this.hookPacket(PacketType.INVRESULT, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                InvResultPacket irp = (InvResultPacket)packet;
                Proxy.logger.log(Level.FINE, () -> "InvSwap result: [" + irp.result + "]");
            }
        });        
        this.hookPacket(PacketType.TRADEDONE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                TradeDonePacket tdp = (TradeDonePacket)packet;
                Proxy.logger.log(Level.FINE, () -> tdp.result + ", " + tdp.message);
            }
        });
        */
    }
    
}
