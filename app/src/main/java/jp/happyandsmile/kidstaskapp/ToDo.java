package jp.happyandsmile.kidstaskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hongoyuka on 2018/02/25.
 */

public class ToDo extends RealmObject implements Serializable {
    private Date date, lastDate;
    private Integer status, taskId;
    private String datestring;

    // id をプライマリーキーとして設定
    @PrimaryKey
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getLastDate(){ return lastDate;}

    public void setLastDate(Date lastDate) {this.lastDate = lastDate; }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDatestring() {
        return datestring;
    }

    public void setDatestring(String datestring) {
        this.datestring = datestring;
    }

}
