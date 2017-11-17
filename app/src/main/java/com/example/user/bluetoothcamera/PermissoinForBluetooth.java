package com.example.user.bluetoothcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import static android.support.v4.app.ActivityCompat.requestPermissions;

/**
 * Created by User on 11/10/2017.
 */

public class PermissoinForBluetooth {

    public String[] permissions;
    public static final int bluetooth_perm_request=1112;
    Context context;

    public PermissoinForBluetooth(Context context)
    {
        this.permissions= new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.CAMERA
        ,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};

        this.context=context;
    }

    public void request_bluetooth()
    {
        requestPermissions((Activity) context,permissions,bluetooth_perm_request);

    }


}
