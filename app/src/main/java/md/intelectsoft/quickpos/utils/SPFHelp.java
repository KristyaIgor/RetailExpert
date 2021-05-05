package md.intelectsoft.quickpos.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import md.intelectsoft.quickpos.POSApplication;

public class SPFHelp {

    private static SPFHelp instance;
    private static SharedPreferences sharedPreferences;

    private SPFHelp(){

    }

    public static SPFHelp getInstance() {
        if (instance == null){
            instance = new SPFHelp();
            sharedPreferences = POSApplication.getApplication().getSharedPreferences("AppParams", Context.MODE_PRIVATE);
        }
        return instance;
    }
    
    public String getString(String key, String defaultValue){
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue){
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public int getInt (String key, int defaultValue){
        return sharedPreferences.getInt(key, defaultValue);
    }

    public long getLong (String key, long defaultValue){
        return sharedPreferences.getLong(key, defaultValue);
    }

    public void putString(String key, String value){
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void putStrings(Map<String, String> stringMap){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, String> item: stringMap.entrySet()){
            editor.putString(item.getKey(), item.getValue());
        }
        editor.apply();
    }

    public void putBooleans(Map<String, Boolean> stringMap){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, Boolean> item: stringMap.entrySet()){
            editor.putBoolean(item.getKey(), item.getValue());
        }
        editor.apply();
    }

    public void putBoolean(String key, boolean value){
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public void putInt(String key, int value){
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value){
        sharedPreferences.edit().putLong(key, value).apply();
    }
}
