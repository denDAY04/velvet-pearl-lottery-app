package com.velvetpearl.lottery.dataaccess.models;

import java.lang.*;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Andreas "denDAY" Stensig on 20-Sep-16.
 */
public abstract class Entity implements  RealmModel{
    @PrimaryKey
    private int id;

    public int getId() {
        return id;
    }

    public <T extends RealmModel> void SetId(Realm realmDb, Class<T> tableClass) {
        java.lang.Number currMaxId = realmDb.where(tableClass).max("id");
        id = currMaxId == null ? 1 : currMaxId.intValue() + 1;
    }
}
