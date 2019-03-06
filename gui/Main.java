package gui;

import crypto.RC4;
import gamedata.GameData;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import listeners.Proxy;
import listeners.Proxy;
import net.Client;
import net.Client;
import net.packets.PacketWriter;
import net.packets.client.*;

public class Main {
    public static class Test1 {
        public int joe;
    }
    public static void main(String[] args) throws UnsupportedEncodingException, Exception {  
        
        ///*       
        GameData.load();
        Proxy proxy = new Proxy();
        Client client = new Client(proxy);
        //client.sendQueue.clear();
        client.connect();        
        
        HelloPacket hp = new HelloPacket();
        hp.buildVersion = "X31.3.1";
        hp.gameId = -2;
        hp.guid = new crypto.RSA().encrypt("alecmendonca0200@gmail.com");
        hp.random1 = (int)Math.floor(Math.random()*1000000000);
        hp.password = new crypto.RSA().encrypt("catandmouse789p");
        hp.random2 = (int)Math.floor(Math.random()*1000000000);
        hp.secret = "";
        hp.keyTime = -1;
        hp.key = new byte[0];
        hp.mapJson = "";
        hp.entryTag = "";
        hp.gameNet = "rotmg";
        hp.gameNetUserId = "";
        hp.playPlatform = "rotmg";
        hp.platformToken = "";
        hp.userToken = "";
        
        client.sendQueue.add(hp);
        System.out.println("size of send queue: " + client.sendQueue.size());
        //*/
        
        
    }
}
