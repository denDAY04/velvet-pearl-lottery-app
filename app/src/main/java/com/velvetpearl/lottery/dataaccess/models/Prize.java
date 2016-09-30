package com.velvetPearl.lottery.dataAccess.models;

import io.realm.RealmObject;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public class Prize {
    private Object id;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }
}
