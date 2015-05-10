package com.note4j.nlp.alda.modularity;

/**
 * Created by changwei on 15/4/16.
 */
public class Node {
    private int value;

    private int community;

    public Node() {
    }

    public Node(int value) {
        this.value = value;
    }

    public Node(int value, int community) {
        this.value = value;
        this.community = community;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


    public int getCommunity() {
        return community;
    }

    public void setCommunity(int comnunity) {
        this.community = comnunity;
    }
}

