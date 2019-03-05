package gamedata;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class GameData {
    public static org.w3c.dom.Document rawPacketsXML;
    //public static org.w3c.dom.Document rawObjectsXML;
    //public static org.w3c.dom.Document rawTilesXML;
    public static org.w3c.dom.Document rawServersXML;
    public static GameDataMap<Byte, gamedata.structs.PacketNode> packets; 
    public static GameDataMap<String, gamedata.structs.ServerNode> servers; //maps server abbr to server nodes.
    
    //Store XML documents in memory since accessors are slow.
    static{
        try {
            System.out.println("NOTICE::GameData.java: Loading xml data...");
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
    
    public static void load() {
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool();
        pool.invoke(new java.util.concurrent.RecursiveAction() {
           @Override
           protected void compute() {
               GameData.packets = new GameDataMap<Byte, gamedata.structs.PacketNode>(gamedata.structs.PacketNode.load(rawPacketsXML));
               //Custom packet reprenets unknown packet types.
               packets.map.put((byte)255, new gamedata.structs.PacketNode(net.packets.Packet.PacketType.UNKNOWN.toString(), 255, net.packets.Packet.PacketType.UNKNOWN));
           }
        });
        
        pool.invoke(new java.util.concurrent.RecursiveAction() {
            @Override
            protected void compute() {
                GameData.servers = new GameDataMap<String, gamedata.structs.ServerNode>(gamedata.structs.ServerNode.load(rawServersXML));
            }
        });        
        
        pool.shutdown();
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
