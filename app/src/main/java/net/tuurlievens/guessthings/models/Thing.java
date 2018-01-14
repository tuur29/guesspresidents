package net.tuurlievens.guessthings.models;

// TODO: add abiliy to search in recyclerview?

public class Thing {
    public final int id;
    public final String name;
    public final String tags;
    public final String descr;
    public final String imageurl;

    public Thing(int id, String name, String tags, String descr, String imageurl) {
        this.id = id;
        this.name = name;
        this.tags = tags;
        this.descr = descr;
        this.imageurl = imageurl;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
