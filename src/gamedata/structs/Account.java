package gamedata.structs;

public class Account {
    public String guid;
    public String password;
    public int charId = -1;      //0 => create new char; -1 => not specified; > 0 => use param char id
    public int nextCharId = -1;  //1 => when creating first char
    
    public Account(String guid, String password, int charId, int nextCharId) {
        this.guid = guid;
        this.password = password;
        this.charId = charId;
        this.nextCharId = nextCharId;
    }    
    public Account(String guid, String password) {
        this.guid = guid;
        this.password = password;
    }
    public Account(String guid, String password, int charId) {
        this(guid, password);
        this.charId = charId;
    }
}
