package util;

public class PacketIdList {
    public static boolean packetFileCreated = false;
    
    public static class PacketNode implements java.lang.Comparable<PacketNode> {
        public String name;
        public short id;
        public net.packets.Packet.PacketType packetType;
        
        public PacketNode(String name, short id, net.packets.Packet.PacketType packetType) {
            this.name = name;
            this.id = id;
            this.packetType = packetType;
        }
        @Override
        public String toString() {
            return "[" + this.name + ", " + this.id + ", " + this.packetType.toString() + "]";
        }
        @Override
        public int compareTo(PacketNode p) {
            return this.id - p.id;
        }
    }
    
    private PacketIdList() {
    }
    
    public static void createPacketIdFile() {
        java.io.PrintWriter out = null;
        javax.xml.parsers.DocumentBuilderFactory factory = null;
        javax.xml.parsers.DocumentBuilder builder = null;
        org.w3c.dom.Document doc = null;
        org.w3c.dom.NodeList packets = null;
        String eol = System.getProperty("line.separator");
        
        try {
            out = new java.io.PrintWriter(new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream("includes/temp/packets.txt", false))));
            
            factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //factory.setValidating(true);
            //factory.setIgnoringElementContentWhitespace(true);
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new java.io.File("includes/res/packets.xml"));
            packets = doc.getElementsByTagName("Packet");
            for(int i = 0; i < packets.getLength(); i++) {
                out.write(packets.item(i).getChildNodes().item(1).getTextContent().trim() + " ");
                out.write(packets.item(i).getChildNodes().item(3).getTextContent().trim() + eol);
            }
            out.flush();
            PacketIdList.packetFileCreated = true;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            e.printStackTrace();
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    
    public static java.util.ArrayList<PacketNode> getPacketIdList() {
        if(!PacketIdList.packetFileCreated) {
            PacketIdList.createPacketIdFile();
        }
        if(new java.io.File("includes/temp/packets.txt").exists()) {
            java.util.ArrayList<PacketNode> list = new java.util.ArrayList<>();
            java.util.Scanner in = null;
            
            try {
                in = new java.util.Scanner(new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream("includes/temp/packets.txt"))));
                while(in.hasNext()) {
                    String name = in.next("[a-zA-Z0-9]+");
                    short id = in.nextShort();
                    net.packets.Packet.PacketType packetType = null;
                    for(net.packets.Packet.PacketType pType : net.packets.Packet.PacketType.values()) {
                        if(name.matches(pType.name())) {
                            packetType = pType;
                        }
                    }
                    PacketNode n = new PacketNode(name, id, packetType);
                    list.add(n);
                }
                
            } catch (java.io.FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
            return list;
        }
        return null;
    }
}
