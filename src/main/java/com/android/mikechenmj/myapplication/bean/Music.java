package com.android.mikechenmj.myapplication.bean;

/**
 * Created by mikechenmj on 17-2-17.
 */

public class Music {

    private int id;
    private String title;
    private String artist;
    private String data;

    public Music(int id, String name, String artist,String data) {
        this.id = id;
        this.title = name;
        this.artist = artist;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("歌名: ").append(title).append("\n")
                .append("歌手: ").append(artist).toString();
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
