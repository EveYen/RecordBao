package com.eveyen.RecordBao.SQL;

import android.graphics.Color;

/**
 * Created by eveyen on 2016/9/8.
 */
public class SQL_Item {
    private long id;
    private long datetime;
    private int color;
    private int top;
    private String title;
    private String content;
    private String fileName;
    private String scheduleDate;
    private String scheduleLocation;
    private String schedule;
    private String contact;

    public SQL_Item() {
        title = "";
        content = "";
        top = 0;
        color = Color.LTGRAY;
    }

    public SQL_Item(long id, long datetime, int color, String title,
                    String content, String fileName ,String sdate ,String sloca ,String sche ,String cont,int top) {
        this.id = id;
        this.datetime = datetime;
        this.color = color;
        this.title = title;
        this.content = content;
        this.fileName = fileName;
        this.scheduleDate = sdate;
        this.scheduleLocation = sloca;
        this.schedule = sche;
        this.contact = cont;
        this.top = top;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String Sdate) {
        this.scheduleDate = Sdate;
    }

    public String getScheduleLocation() {
        return scheduleLocation;
    }

    public void setScheduleLocation(String Sloca) {
        this.scheduleLocation = Sloca;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String Sche) {
        this.schedule = Sche;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String cont) {
        this.contact = cont;
    }

    public int getTop(){
        return top;
    }

    public void setTop(int topp){
        this.top = topp;
    }

}
