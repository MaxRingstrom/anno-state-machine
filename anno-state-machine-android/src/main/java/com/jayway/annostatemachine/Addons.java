package com.jayway.annostatemachine;


import com.jayway.annostatemachine.android.AndroidAddon;

import java.util.ArrayList;

public class Addons implements AddonRepo {

    private ArrayList<FrameworkAddon> mAddons = new ArrayList<>();

    public Addons() {
        mAddons.add(new AndroidAddon());
    }

    @Override
    public ArrayList<FrameworkAddon> getAddons() {
        return mAddons;
    }
}
