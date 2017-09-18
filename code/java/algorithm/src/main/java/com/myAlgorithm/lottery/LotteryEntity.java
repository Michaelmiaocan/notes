package com.myAlgorithm.lottery;

import java.io.Serializable;

public class LotteryEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int ID;
    private Double rate;

    public LotteryEntity(String name, int ID, Double rate) {
        this.name = name;
        this.ID = ID;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", ID='" + ID + '\'' +
                ", rate='" + rate + '\'' +
                '}';
    }
}
