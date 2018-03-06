package jp.happyandsmile.kidstaskapp;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hongoyuka on 2018/02/15.
 */

public class Task extends RealmObject implements Serializable {
    private String title;
    private String datestring;
    private byte[] mBitmapArray,mBelongingsBitmapArray;
    private Date date,displayTime,lastDate;
    private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    // id をプライマリーキーとして設定
    @PrimaryKey
    private int id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getImageBytes(){
        return mBitmapArray;
    }

    public void setImageBytes(byte[] image) {
        this.mBitmapArray = image;
    }

    public byte[] getBelongingsImageBytes(){
        return mBelongingsBitmapArray;
    }

    public void setBelongingsImageBytes(byte[] belongingsImage) {
        this.mBelongingsBitmapArray = belongingsImage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(Date displayTime) {
        this.displayTime = displayTime;
    }

    public String getDatestring() {
        return datestring;
    }

    public void setDatestring(String datestring) {
        this.datestring = datestring;
    }

    public boolean getMonday() {
        return monday;
    }

    public void setMonday(Boolean monday){
        this.monday = monday;
    }

    public boolean getTuesday() {
        return tuesday;
    }

    public void setTuesday(Boolean tuesday){
        this.tuesday = tuesday;
    }

    public boolean getWednesday() {
        return wednesday;
    }

    public void setWednesday(Boolean wednesday){
        this.wednesday = wednesday;
    }

    public boolean getThursday() {
        return thursday;
    }

    public void setThursday(Boolean thursday){
        this.thursday = thursday;
    }

    public boolean getFriday() {
        return friday;
    }

    public void setFriday(Boolean friday){
        this.friday = friday;
    }

    public boolean getSaturday() {
        return saturday;
    }

    public void setSaturday(Boolean saturday){
        this.saturday = saturday;
    }

    public boolean getSunday() {
        return sunday;
    }

    public void setSunday(Boolean sunday){
        this.sunday = sunday;
    }

    public Date getLastDate(){ return lastDate;}

    public void setLastDate(Date lastDate) {this.lastDate = lastDate; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}