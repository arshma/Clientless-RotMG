package gamedata;

import gamedata.structs.Account;
import gamedata.structs.AccountNode;
import gamedata.structs.PacketNode;
import gamedata.structs.ServerNode;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class GameData {
    public static org.w3c.dom.Document rawPacketsXML;
    //public static org.w3c.dom.Document rawObjectsXML;
    //public static org.w3c.dom.Document rawTilesXML;
    public static org.w3c.dom.Document rawServersXML;
    public static org.w3c.dom.Document rawCharListXML;
    
    public static GameDataMap<Byte, PacketNode> packets; 
    public static GameDataMap<String, ServerNode> servers; //maps server abbr to server nodes
    
    //IMPORTANT: Must always be checked to see if they are initialized.
    public static ArrayList<Integer> charIds;
    
    //Store XML documents in memory since accessors are slow.
    static{
        try {
            System.out.println("NOTICE::GameData: Loading xml data...");
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            
            rawPacketsXML = builder.parse(new java.io.BufferedInputStream(new java.io.FileInputStream("includes/res/packets.xml")));
            //rawObjectsXML = builder.parse(new java.io.BufferedInputStream(new java.io.FileInputStream("includes/res/objects.xml")));
            //rawTilesXML = builder.parse(new java.io.BufferedInputStream(new java.io.FileInputStream("includes/res/tiles.xml")));
            rawServersXML = builder.parse(new java.io.BufferedInputStream(new java.io.FileInputStream("includes/res/servers.xml")));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GameData.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    //Initialize static game data from XMLs.
    public static void load() {
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool();
        pool.invoke(new java.util.concurrent.RecursiveAction() {
           @Override
           protected void compute() {
               GameData.packets = new GameDataMap<Byte, PacketNode>(PacketNode.load(rawPacketsXML));
               //Custom packet represents unknown packet types.
               packets.map.put((byte)255, new PacketNode(net.packets.Packet.PacketType.UNKNOWN.toString(), 255, net.packets.Packet.PacketType.UNKNOWN));
           }
        });
        
        pool.invoke(new java.util.concurrent.RecursiveAction() {
            @Override
            protected void compute() {
                GameData.servers = new GameDataMap<String, ServerNode>(ServerNode.load(rawServersXML));
            }
        });        
        
        pool.shutdown();
    }
    
    public static void loadCharIds(Account account) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            String url = "http://www.realmofthemadgod.com/char/list?guid=" + account.guid.replaceAll("\\+", "%2B") + "&password=" + account.password + "&version=31";
            GameData.rawCharListXML = builder.parse(new java.io.BufferedInputStream(new java.net.URL(url).openStream()));
            GameData.charIds = AccountNode.allCharacterIdList(GameData.rawCharListXML);
            System.out.println("Printing character id list: ");
            for(int i : GameData.charIds) {
                System.out.println(i);
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    //Wraps Maps to allow for addition of new properties.
    public static class GameDataMap<IDType, NodeType extends gamedata.structs.DataNode<IDType>> {
        public java.util.Map<IDType, NodeType> map;
        
        public GameDataMap(java.util.Map<IDType, NodeType> map) {
            this.map = map;
        }
        
        public NodeType byId(IDType id) {
            return this.map.get(id);
        }
        
        public NodeType byName(String name) {
            for(NodeType p : this.map.values()) {
                if(name.matches(p.name())) {
                    return p;
                }
            }
            return null;
        }
        
        /*
        //NOTE: This requires that PacketType's string is equivalent to name of the packet.
        //NOTE2: This does not work for ServerNode since it has no 'PacketType' field.
        public NodeType byType(net.packets.Packet.PacketType packetType) {
            return byName(packetType.toString());
        }
        */
    }
}
