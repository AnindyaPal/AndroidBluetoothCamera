package com.example.user.bluetoothcamera;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1122;
    public static BluetoothAdapter mbluetoothAdapter;
    Animation fadein, fadein2, recyler;
    Button turnOn, oepnCamera,connect;
    BluetoothDevice device;
    public BluetoothDevice device_connect;

    PermissoinForBluetooth permissoinForBluetooth;
    ArrayList<String> names = new ArrayList<>();
    RecyclerView recyclerView;
    Boolean a = false, b = false;
    RotateLoading elasticDownloadView;
    public static RelativeLayout relativeLayout, relativeLayout2;
    BluetoothConnectionService bluetoothConnectionService;

    public static ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    public static AnimatedCircleLoadingView mProgressView;
    ViewGroup container;
    public static int count = 1;
    private FragmentManager fm;
    ImageView capture;
    Camera.Parameters params;
    byte[] bytes;
    Dialog dialog;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissoinForBluetooth = new PermissoinForBluetooth(this);
        permissoinForBluetooth.request_bluetooth();
        mProgressView = (AnimatedCircleLoadingView) findViewById(R.id.circle_loading_view);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativelayout);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.relativelayout2);

        capture = (ImageView) findViewById(R.id.capture);
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect_layout);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new RecyclerAdapter2(this, names));
        turnOn = (Button) findViewById(R.id.turnOn);
        oepnCamera = (Button) findViewById(R.id.opencamera);
        elasticDownloadView = (RotateLoading) findViewById(R.id.elastic_download_view);

        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadein2 = AnimationUtils.loadAnimation(this, R.anim.fadein);
        recyler = AnimationUtils.loadAnimation(this, R.anim.fadein3);

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver2, filter1);

        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver3, filter2);
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        paireddevices1();
        if (mbluetoothAdapter.isEnabled())
        {
            if (device_connect!=null)
            {
                bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);


            }


        }

        //startConnection();

        /*device_connect.createBond();*/

        connect=(Button) dialog.findViewById(R.id.connect_button);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
                dialog.hide();
            }
        });
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Log.e("device_connectedddd", device_connect.getName());

                //startConnection();
                params = CameraPreview.mCamera.getParameters();
                List<Camera.Size> sizes = params.getSupportedPictureSizes();
                Camera.Size size = sizes.get(0);
                Camera.Size size1 = sizes.get(0);
                for (int i = 0; i < sizes.size(); i++) {

                    if (sizes.get(i).width > size.width)
                        size = sizes.get(i);


                }
                capture.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein));
                params.setPictureSize(size.width, size.height);
                params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                params.setExposureCompensation(0);
                params.setPictureFormat(ImageFormat.JPEG);
                params.setJpegQuality(100);
                params.setRotation(270);
                CameraPreview.mCamera.setParameters(params);
                CameraPreview.mCamera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(final byte[] data, Camera camera) {

                        bytes = data;
                        ApplicationUUID.bytes2 = data;
                        bluetoothConnectionService.write(bytes);
                        Log.e("MyCameraApp", "failed to create directory not not");
                        CameraPreview.mCamera.stopPreview();
                        CameraPreview.mCamera.startPreview();
                        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            /*bytes= new byte[(int)pictureFile.length()];
            bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());*/

                        if (pictureFile == null) {
                            Log.e("ErrorCreating", "Error creating media file, check storage permissions: ");
                            // e.getMessage());
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            // Log.d(TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            // Log.d(TAG, "Error accessing file: " + e.getMessage());
                        }
                    }

                });






               /* CameraPreview.mCamera.stopPreview();
                CameraPreview.mCamera.startPreview();*/
                // Log.e("bonding pair",""+devices.size());


                // startConnection();


                //  mBluetoothconnection.write(bytes);
                // byte[] bytes1=("ABC").getBytes();
                //  mBluetoothconnection.write(bytes1);

            }
        });

    }

    public void startConnection() {
        startBtConnection(device_connect, ApplicationUUID.uuid);

    }

    public void startBtConnection(BluetoothDevice device, UUID uuid) {

        bluetoothConnectionService.startClient(device, uuid);
    }

    public void bluetoothprompt(View view) {
        mProgressView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein5));
        turnOn.startAnimation(fadein);

        elasticDownloadView.setVisibility(View.VISIBLE);
        elasticDownloadView.start();


        if (!mbluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }
        else {

            bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);
        }
        if (mbluetoothAdapter.startDiscovery())
            Toast.makeText(this, "Device/Devices found", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Result_ok", "" + (resultCode == RESULT_OK));
        if (resultCode == RESULT_OK) {

            if (mbluetoothAdapter.startDiscovery())
                Toast.makeText(this, "Device/Devices found", Toast.LENGTH_SHORT).show();
            if (device_connect!=null)
            bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);

        } else {
            Toast.makeText(this, "Please turn on bluetooth.", Toast.LENGTH_SHORT).show();

        }

    }

    private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(device);

                String deviceHardwareAddress = device.getAddress();
                Log.e("Mac", deviceHardwareAddress);
                String deviceName = device.getName();
                elasticDownloadView.stop();
                elasticDownloadView.setVisibility(View.GONE);

                if (deviceName != null) {
                    Log.e("Name", deviceName);
                    if (names.size() == 0) {
                        names.add(deviceName);


                    } else {

                        if (!a) {
                            names.add(deviceName);


                        }
                        a = false;

                    }


                } else {
                    if (names.size() == 0) {
                        names.add(deviceHardwareAddress);

                    } else {
                        for (int i = 0; i < names.size(); i++) {
                            if ((names.get(i).equalsIgnoreCase(deviceHardwareAddress)))
                                b = true;
                        }
                        if (!b) {
                            names.add(deviceHardwareAddress);


                        }
                        b = false;

                    }
                }
                recyclerView.setAdapter(new RecyclerAdapter2(MainActivity.this, names));
                recyclerView.startAnimation(recyler);

                // Log.e("Device_name",deviceName);
                // MAC address
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void opencamera(View view) {
        oepnCamera.startAnimation(fadein2);

        relativeLayout2.setVisibility(View.VISIBLE);
        relativeLayout2.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein8));
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.blank_fragment, new CameraFragment());
        ft.commit();
        dialog.setCancelable(false);
        dialog.show();
        //bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);
        relativeLayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein6));
        //startActivity(new Intent(MainActivity.this,CameraActivity.class));

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver2);
        unregisterReceiver(mReceiver3);
        super.onDestroy();

    }

    private final BroadcastReceiver mReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) ;
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.e("bondedornot", "BroadcastReceiver bonded");

                mProgressView.setPercent(100);
                mProgressView.setTag("Paired");
                mProgressView.stopOk();
                startPercentMockThread();
                device_connect = device;
                Log.e("bonded", device_connect.getName());
                bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);

                Toast.makeText(MainActivity.this, "You have successfully paired", Toast.LENGTH_LONG).show();
                  /* mProgressView.setVisibility(View.GONE);*/
            }

            if (device.getBondState() == BluetoothDevice.BOND_BONDING)
                Log.e("bondedornot", "BroadcastReceiver BONDING");
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Log.e("bondedornot", "BroadcastReceiver BOND DONE");
                mProgressView.stopFailure();
                startPercentMockThread();
                device_connect = device;

            }


        }

    };

    public void startPercentMockThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {

                    for (int i = 0; i <= 100; i++) {
                        Thread.sleep(35);
                        //changePercent(i);
                        if (i == 100)
                            changePercent(i);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    private void changePercent(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein5));

            }
        });
    }

    public Boolean paireddevices(BluetoothDevice gdevice) {

        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mbluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(gdevice.getName()))
                    return true;
                String deviceName = device.getName();
                device_connect = device;
                Log.e("bondedornot", deviceName);

                String deviceHardwareAddress = device.getAddress(); // MAC address
            }

        }

        return false;

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (CameraPreview.mCamera != null) {
            relativeLayout2.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein9));
            fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.blank_fragment, new BlankFragment());
            ft.commit();
            relativeLayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein7));

            CameraPreview.mCamera = null;
            bluetoothConnectionService.canel();
            paireddevices1();
            bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this);
        } else
            super.onBackPressed();
    }

    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        Log.e("MyCameraApp", "failed to create directory not not");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyBluetoothApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        mediaStorageDir.mkdir();
        // Create a media file name
        String timeStamp = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        }
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");

        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Camera.PictureCallback mPicture1 = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.e("MyCameraApp", "failed to create directory not not");
            CameraPreview.mCamera.startPreview();
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            /*bytes= new byte[(int)pictureFile.length()];
            bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());*/

            if (pictureFile == null) {
                Log.e("ErrorCreating", "Error creating media file, check storage permissions: ");
                // e.getMessage());
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                // Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                // Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    @Override
    protected void onResume() {
        CameraPreview.mCamera = Camera.open(1);
        //CameraPreview.mCamera.setPreviewDisplay(holder);
        CameraPreview.mCamera.startPreview();
        //Camera.Parameters parameters = mCamera.getParameters();
        CameraPreview.mCamera.setDisplayOrientation(90);
        super.onResume();
    }

    public Boolean paireddevices1() {

        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mbluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {

                String deviceName = device.getName();
                device_connect = device;
                Log.e("paired", deviceName);

                String deviceHardwareAddress = device.getAddress(); // MAC address
            }

        }

        return false;

    }

    public void connect(View view) {

startConnection();
        Log.e("Clicked", "clicked");

    }

}