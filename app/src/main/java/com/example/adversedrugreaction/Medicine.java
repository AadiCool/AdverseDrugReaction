package com.example.adversedrugreaction;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Medicine {

    private String description;
    private String genericName;
    private ArrayList<String> moreCommon;
    private ArrayList<String> lessCommon;
    private ArrayList<String> rare;

     Medicine(@org.jetbrains.annotations.NotNull Map<String, Object> map) {
        this.description = (String) map.get("Description");
        this.genericName = (String) map.get("Generic Name");
        Log.e("GENERIC", this.genericName);
        this.moreCommon = (ArrayList<String>) map.get("More common");
        this.lessCommon = (ArrayList<String>) map.get("Less common");
        this.rare = (ArrayList<String>) map.get("Rare");
    }

    String getDescription() {
        return description;
    }

    String getGenericName() {
        return genericName;
    }

    ArrayList<HashMap<String, String>> getMoreCommon() {
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        for(int i=0; i<moreCommon.size(); i++) {
            HashMap<String, String> adr = new HashMap<>();
            adr.put("adr", moreCommon.get(i));
            data.add(adr);
        }
        return data;
    }

    ArrayList<HashMap<String, String>> getLessCommon() {
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        for(int i=0; i<lessCommon.size(); i++) {
            HashMap<String, String> adr = new HashMap<>();
            Log.e("DATA", lessCommon.get(i));
            adr.put("adr", lessCommon.get(i));
            data.add(adr);
        }
        return data;
    }

    ArrayList<HashMap<String, String>> getRare() {
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        for(int i=0; i<rare.size(); i++) {
            HashMap<String, String> adr = new HashMap<>();
            adr.put("adr", rare.get(i));
            data.add(adr);
        }
        return data;
    }
}
