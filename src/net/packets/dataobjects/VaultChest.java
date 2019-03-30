package net.packets.dataobjects;

import java.util.ArrayList;

public class VaultChest {
    public int id;
    public Location position;
    public ArrayList<Integer> items;
    
    public VaultChest(int id, Location position, ArrayList<Integer> items) {
        this.id = id;
        this.position = position;
        this.items = items;
    }
}
