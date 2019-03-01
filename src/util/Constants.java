package util;

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
