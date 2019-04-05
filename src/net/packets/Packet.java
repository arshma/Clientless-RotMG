package net.packets;

import net.packets.client.*;
import net.packets.server.*;


public abstract class Packet {
    //protected int id = -1;
    //protected boolean send = true;    
    public Packet() {
    }
    
    public PacketType type() {return PacketType.UNKNOWN;}
    //public abstract PacketType type();
    public abstract void read(PacketReader r) throws java.io.IOException;
    public abstract void write(PacketWriter w) throws java.io.IOException;
    
    
    //Implement this when creation mechanism is setup.
    public static Packet create(PacketType type) {
        return Packet.createPacket(type);
    }
    
    private static Packet createPacket(PacketType pType) {
        switch(pType) {
            case HELLO:
                return new HelloPacket();
            case LOAD:
                return new LoadPacket();
            case MOVE:
                return new MovePacket();
            case PONG:
                return new PongPacket();
            case UPDATEACK:
                return new UpdateAckPacket();
            case ACCOUNTLIST:
                return new AccountListPacket();
            case CREATESUCCESS:
                return new CreateSuccessPacket();
            case MAPINFO:
                return new MapInfoPacket();
            case NEWTICK:
                return new NewTickPacket();
            case PING:
                return new PingPacket();
            case UPDATE:
                return new UpdatePacket();
            case GLOBALNOTIFICATION:
                return new GlobalNotificationPacket();
            case TEXT:
                return new TextPacket();
            case ALLYSHOOT:
                return new AllyShootPacket();
            case FAILURE:
                return new FailurePacket();
            case SHOWEFFECT:
                return new ShowEffectPacket();
            case NOTIFICATION:
                return new NotificationPacket();
            case GOTO:
                return new GotoPacket();
            case GOTOACK:
                return new GotoAckPacket();
            case REQUESTTRADE:
                return new RequestTradePacket();
            case ACCEPTTRADE:
                return new AcceptTradePacket();
            case CHANGETRADE:
                return new ChangeTradePacket();
            case CANCELTRADE:
                return new CancelTradePacket();
            case SERVERPLAYERSHOOT:
                return new ServerPlayerShootPacket();
            case TRADESTART:
                return new TradeStartPacket();
            case TRADEREQUESTED:
                return new TradeRequestedPacket();
            case TRADECHANGED:
                return new TradeChangedPacket();
            case TRADEACCEPTED:
                return new TradeAcceptedPacket();
            case TRADEDONE:
                return new TradeDonePacket();
            case PLAYERTEXT:
                return new PlayerTextPacket();
            case DAMAGE:
                return new DamagePacket();
            case INVSWAP:
                return new InvSwapPacket();
            case INVRESULT:
                return new InvResultPacket();
            case PLAYSOUND:
                return new PlaySoundPacket();
            default:
                System.out.println("ERROR::Packet.java: Unable to create packet of type [" + pType.toString() + "]");
                return null;
        } 
    }
    
    public enum PacketType {
        FAILURE,
        CREATESUCCESS,
        CREATE,
        PLAYERSHOOT,
        MOVE,
        PLAYERTEXT,
        TEXT,
        SERVERPLAYERSHOOT,
        DAMAGE,
        UPDATE,
        UPDATEACK,
        NOTIFICATION,
        NEWTICK,
        INVSWAP,
        USEITEM,
        SHOWEFFECT,
        HELLO,
        GOTO,
        INVDROP,
        INVRESULT,
        RECONNECT,
        PING,
        PONG,
        MAPINFO,
        LOAD,
        PIC,
        SETCONDITION,
        TELEPORT,
        USEPORTAL,
        DEATH,
        BUY,
        BUYRESULT,
        AOE,
        GROUNDDAMAGE,
        PLAYERHIT,
        ENEMYHIT,
        AOEACK,
        SHOOTACK,
        OTHERHIT,
        SQUAREHIT,
        GOTOACK,
        EDITACCOUNTLIST,
        ACCOUNTLIST,
        QUESTOBJID,
        CHOOSENAME,
        NAMERESULT,
        CREATEGUILD,
        GUILDRESULT,
        GUILDREMOVE,
        GUILDINVITE,
        ALLYSHOOT,
        ENEMYSHOOT,
        REQUESTTRADE,
        TRADEREQUESTED,
        TRADESTART,
        CHANGETRADE,
        TRADECHANGED,
        ACCEPTTRADE,
        CANCELTRADE,
        TRADEDONE,
        TRADEACCEPTED,
        CLIENTSTAT,
        CHECKCREDITS,
        ESCAPE,
        FILE,
        INVITEDTOGUILD,
        JOINGUILD,
        CHANGEGUILDRANK,
        PLAYSOUND,
        GLOBALNOTIFICATION,
        RESKIN,
        PETUPGRADEREQUEST,
        ACTIVEPETUPDATEREQUEST,
        ACTIVEPETUPDATE,
        NEWABILITY,
        PETYARDUPDATE,
        EVOLVEPET,
        DELETEPET,
        HATCHPET,
        ENTERARENA,
        IMMINENTARENAWAVE,
        ARENADEATH,
        ACCEPTARENADEATH,
        VERIFYEMAIL,
        RESKINUNLOCK,
        PASSWORDPROMPT,
        QUESTFETCHASK,
        QUESTREDEEM,
        QUESTFETCHRESPONSE,
        QUESTREDEEMRESPONSE,
        PETCHANGEFORMMSG,
        KEYINFOREQUEST,
        KEYINFORESPONSE,
        CLAIMLOGINREWARDMSG,
        LOGINREWARDMSG,
        QUESTROOMMSG,
        PETCHANGESKINMSG,
        REALMHEROLEFTMSG,
        UNKNOWN;    //defines packet type not defined above. Not part of actual game data.
    }
}
