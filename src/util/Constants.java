package util;

import java.io.IOException;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class Constants {
    
    public enum ConditionEffects {
        DEAD(1 << 0),
        QUIET(1 << 1),
        WEAK(1 << 2),
        SLOWED(1 << 3),
        SICK(1 << 4), 
        DAZED(1 << 5),
        STUNED(1 << 6),
        BLIND(1 << 7),
        HALLUCINATING(1 << 8),
        DRUNK(1 << 9),
        CONFUSED(1 << 10),
        STUN_IMMUNE(1 << 11),
        INVISIBLE(1 << 12),
        PARALYZED(1 << 13),
        SPEEDY(1 << 14),
        BLEEDING(1 << 15),
        NOT_USED(1 << 16),
        HEALING(1 << 17),
        DAMAGING(1 << 18),
        BERSERK(1 << 19),
        PAUSED(1 << 20),
        STATIS(1 << 21),
        STATIS_IMMUNE(1 << 22),
        INVINCIBLE(1 << 23),
        INVULNERABLE(1 << 24),
        ARMORED(1 << 25),
        ARMOR_BROKEN(1 << 26),
        HEXED(1 << 27),
        ANOTHER_SPEEDY(1 << 28),
        UNSTABLE(1 << 29),
        DARKNESS(1 << 30),
        CURSE(1 << 31);
        
        private final int val;
        private ConditionEffects(int val) {
            this.val = val;
        }
        public int getVal() {
            return this.val;
        }
    }
    
    public enum EffectType {
        HEAL((byte)1),
        TELEPORT((byte)2),
        STREAM((byte)3),
        THROW((byte)4),
        NOVA((byte)5),
        POISON((byte)6),
        LINE((byte)7),
        BURST((byte)8),
        FLOW((byte)9),
        RING((byte)10),
        LIGHTNING((byte)11),
        COLLAPSE((byte)12),
        CONEBLAST((byte)13),
        EARTHQUAKE((byte)14),
        FLASH((byte)15),
        BEACHBALL((byte)16),
        ELECTRICBOLTS((byte)17),
        ELECTRICFLASHING((byte)18),
        RISINGFURY((byte)19);
        
        private byte val;
        private EffectType(byte val) {
            this.val = val;
        }
        public byte getVal() {
            return this.val;
        }
        public static EffectType getConst(byte b) {
            for(EffectType et : EffectType.values()) {
                if(et.val == b) {
                    return et;
                }
            }
            return null;
        }
    }
    
    public static class ARGB {
        public byte a;
        public byte b;
        public byte g;
        public byte r;
        
        public ARGB () {}
        public ARGB(long argb) {    //uint param
            this.a = (byte)((argb & 0xff000000) >> 24);
            this.r = (byte)((argb & 0x00ff0000) >> 16);
            this.g = (byte)((argb & 0x0000ff00) >> 8);
            this.b = (byte)((argb & 0x000000ff) >> 0);
        }
        public ARGB(byte a, byte r, byte g, byte b) {
            this.a = a;
            this.r = r;
            this.g = g;
            this.b = b;
        }
        
        public static ARGB read(PacketReader r) throws IOException {
            ARGB ret = new ARGB();
            ret.a = r.readByte();
            ret.r = r.readByte();
            ret.g = r.readByte();
            ret.b = r.readByte();
            return ret;
        }
        public void write(PacketWriter w) throws IOException {
            w.writeByte(this.a);
            w.writeByte(this.r);
            w.writeByte(this.g);
            w.writeByte(this.b);
        }
    }
    
    public enum Bags {
        NORMAL((short)0x500),
        PURPLE((short)0x503),
        PINK((short)0x506),
        CYAN((short)0x509),
        RED((short)0x510),
        BLUE((short)0x50B),
        PURPLE2((short)0x507),
        EGG((short)0x508),
        WHITE((short)0x50C),
        WHITE2((short)0x50E),
        WHITE3((short)0x50F);
        
        private final short val;
        private Bags(short val) {
            this.val = val;
        }
        public short getVal() {
            return this.val;
        }
    }
    
    public enum Ability {
        ATTACK_CLOSE(402),
        ATTACK_MID(404),
        ATTACK_FAR(405),
        ELECTRIC(406),
        HEAL(407),
        MAGIC_HEAL(408),
        SAVAGE(409),
        DECOY(410),
        RISING_FURY(411);
        
        private final int val;  //uint
        private Ability(int val) {
            this.val = val;
        }
        public int getVal() {
            return this.val;
        }
    }
    
    public enum Classes {
        ROGUE((short)0x0300),
        ARCHER((short)0x0307),
        WIZARD((short)0x030E),
        PRIEST((short)0x0310),
        WARRIOR((short)0x031D),
        KNIGHT((short)0x031E),
        PALADIN((short)0x031F),
        ASSASSIN((short)0x0320),
        NECROMANCER((short)0x0321),
        HUNTRESS((short)0x0322),
        MYSTIC((short)0x0323),
        TRICKSTER((short)0x0324),
        SORCERER((short)0x0325),
        NINJA((short)0x0326);
        
        private final short val;
        private Classes(short val) {
            this.val = val;
        }
        public short getVal() {
            return this.val;
        }
        public static Classes shortToEnum(short s) {
            for(Classes c : Classes.values()) {
                if(c.val == s) {
                    return c;
                }
            }
            return null;
        }
    }
}
