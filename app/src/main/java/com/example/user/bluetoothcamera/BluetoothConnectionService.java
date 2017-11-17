package com.example.user.bluetoothcamera;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Created by User on 11/11/2017.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MYAPP";

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
   // private static final UUID MY_UUID_INSECURE =
           // UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    int bytes=0;
    BluetoothServerSocket bt_server_socket;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
   // ProgressDialog mProgressDialog;
   byte[] buffer = new byte[2048];
   int bytesRead=0;
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.e("Starting","Starting");
        start();
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    public class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, ApplicationUUID.uuid);

                Log.e(TAG, "AcceptThread: Setting up Server using: " + ApplicationUUID.uuid);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
            bt_server_socket=mmServerSocket;
        }

        public void run(){
            Log.e(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.e(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.e(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            //talk about this is in the 3rd
            if(socket != null){
                connected(socket,mmDevice);
            }

            Log.e(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.e(TAG, "cancel: Canceling AcceptThread.");
            try {

                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.e(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.e(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.e(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +ApplicationUUID.uuid );
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

                Log.e(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.e(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.e(TAG, "run: ConnectThread: Could not connect to UUID: " + ApplicationUUID.uuid );
            }

            //will talk about this in the 3rd video
            connected(mmSocket,mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }



    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.e(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.e(TAG, "startClient: Started.");

        //initprogress dialog
       // mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth"
                //,"Please Wait...",true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.e(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                //mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
              // buffer store for the stream

             // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                      byte[] buffer=new byte[528];
                   /* byte[] buffer1 = new byte[8192];
                    int bytesRead;
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    while ((bytesRead = mmInStream.read(buffer1)) != -1) {
                        output.write(buffer1, 0, bytesRead);
                    }
                    byte file[] = output.toByteArray();*/

                    /*buffer= IOUtils.toByteArray(mmInStream);
                    bytes = bytes+mmInStream.read(buffer);*/
                    bytes =mmInStream.read(buffer);
                    //byte[] b = new byte[2048];
                   // ByteArrayOutputStream output = new ByteArrayOutputStream();
                        //bos.write(buffer,0,bytes);
                    /*while ((bytesRead = mmInStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytes);
                    }*/
                    output.write(buffer,0,bytes);
                    byte file[] = output.toByteArray();
                    onPictureTaken(file);
                    /*bos.reset();
                    bos.write(b, 0, bytes);*/
                    //byte[] bytes1 = bos.toByteArray();

                   // byte[] buffer1 = new byte[bytes];
                   /* Log.e(TAG, "InputStream: " +file.length);
                    */
                    //File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                    String incomingMessage = new String(buffer, 0, bytes);
                    //mHandler.obtainMessage(incomingMessage, -1, -1, buffer).sendToTarget();
                    Log.e(TAG, "InputStream: " + incomingMessage);

                } catch (Exception e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    cancel();
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.e(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.e(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.e(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }
   /* private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        Log.e("MyCameraApp", "failed to create directory not not");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyBluetoothApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
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
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");

        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }*/
   /*public void writeToFile(byte[] data, String fileName) throws IOException{
      *//* FileOutputStream out = new FileOutputStream(fileName);
       out.write(data);
       out.close();*//*
       String timeStamp = null;
       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
           timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
       }
       File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
               Environment.DIRECTORY_PICTURES), "MyBluetoothApp");
       File mediaFile;

           mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                   "IMG_"+ timeStamp + ".jpg");
       FileOutputStream fos = new FileOutputStream(mediaFile);
       fos.write(data);
       fos.close();

       }*/
    public void onPictureTaken(byte[] data) {
        Log.e("MyCameraApp", "failed to create directory not not");
        CameraPreview.mCamera.startPreview();
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            /*bytes= new byte[(int)pictureFile.length()];
            bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());*/

        if (pictureFile == null) {
            Log.e("ErrorCreating", "Error creating media file, check storage permissions: " );
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
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        Log.e("MyCameraApp", "failed to create directory not not");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyBluetoothApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
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
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");

        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    public void canel()
    {
        try {
            bt_server_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}


