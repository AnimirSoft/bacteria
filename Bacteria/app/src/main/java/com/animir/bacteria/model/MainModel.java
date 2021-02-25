package com.animir.bacteria.model;

import android.content.Context;
import android.content.SharedPreferences;

public class MainModel {
    private static MainModel instance = null;

    private String key_Name = "VACTERIA_SF";
    private String key_Player = "KEY_PLAYER";

    private String value_Player = "PLAYER";
    private String value_AI = "AI";

    private MainModel() {}
    public static MainModel getInstance(){
        if(instance == null){
            instance = new MainModel();
        }
        return instance;
    }

    public String getKeyPlayer(){
        return key_Player;
    }
    public String getValuePlayer(){
        return value_Player;
    }
    public String getValueAI(){
        return value_AI;
    }


    public void setStringPref(Context context, String key, String value){
        SharedPreferences preferences = context.getSharedPreferences(key_Name, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public String getStringPref(Context context, String key){
        SharedPreferences preferences = context.getSharedPreferences(key_Name, context.MODE_PRIVATE);
        return preferences.getString(key,"");
    }




}
