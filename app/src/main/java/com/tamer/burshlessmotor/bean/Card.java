package com.tamer.burshlessmotor.bean;

/**
 * Created by liangzr on 16-3-6.
 */
public class Card {

    public static final int CONTROL_BAR = 0;
    public static final int SWITCH = 1;
    private int type;

    public Card(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
