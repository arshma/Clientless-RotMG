package util;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerList {
    private static boolean servFileCreated = false;
    
    public static class ServerNode implements java.lang.Comparable<ServerNode> {
        public String name;
        public String ip;
        
        public ServerNode(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }
        @Override
        public String toString() {
            return "[" + this.name + ", " + this.ip + "]";
        }
        @Override
        public int compareTo(ServerNode s) {
            return this.name.compareTo(s.name);
        }
    }
    
    private ServerList() {
    }
    
    public static void createServerFile() {
        java.io.PrintWriter out = null;
        javax.xml.parsers.DocumentBuilderFactory factory = null;
        javax.xml.parsers.DocumentBuilder builder = null;
        org.w3c.dom.Document doc = null;
        org.w3c.dom.NodeList servers = null;
        String eol = System.getProperty("line.separator");
        
        try {
            out = new java.io.PrintWriter(new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream("includes/temp/servers.txt", false))));
            
            factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //factory.setValidating(true);
            //factory.setIgnoringElementContentWhitespace(true);
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new java.io.File("includes/res/servers.xml"));
            servers = doc.getElementsByTagName("Server");
            for(int i = 0; i < servers.getLength(); i++) {
                out.write(servers.item(i).getChildNodes().item(1).getTextContent().trim() + " ");
                out.write(servers.item(i).getChildNodes().item(3).getTextContent().trim() + eol);
            }
            out.flush();
            ServerList.servFileCreated = true;
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
    
    public static java.util.ArrayList<ServerNode> getServerList() {
        if(!ServerList.servFileCreated) {
            ServerList.createServerFile();
        }
        if(new java.io.File("includes/temp/servers.txt").exists()) {
            java.util.ArrayList<ServerNode> list = new java.util.ArrayList<>();
            java.util.Scanner in = null;
            
            try {
                in = new java.util.Scanner(new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream("includes/temp/servers.txt"))));
                while(in.hasNext()) {
                    String name = in.next("[a-zA-Z0-9]+");
                    String ip = in.nextLine();
                    ServerNode s = new ServerNode(name, ip);
                    list.add(s);
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
