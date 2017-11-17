package com.example.user.bluetoothcamera;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by User on 8/24/2017.
 */

public class RecyclerAdapter2 extends RecyclerView.Adapter<RecyclerAdapter2.VideoAdapter>  {




  Context context;
    ArrayList<String> devices=new ArrayList<>();
    Animation animation;
    MainActivity mainActivity;


  public   RecyclerAdapter2(Context context, ArrayList<String> devices)
    {


   this.context=context;
        this.devices=devices;
        this.mainActivity=new MainActivity();


    }




    @Override
    public VideoAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(parent.getContext());
        View view=inflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        VideoAdapter adapter=new VideoAdapter(view);

        return adapter;
    }

    @Override
    public void onBindViewHolder(final VideoAdapter holder, final int position) {

             holder.name.setText(devices.get(position));
             holder.name.setOnClickListener(new View.OnClickListener() {
                 @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                 @Override
                 public void onClick(View view) {
                     MainActivity.mProgressView.setVisibility(View.VISIBLE);

                  //MainActivity.relativeLayout.addView(MainActivity.mProgressView);
                    if (MainActivity.count==1)
                     MainActivity.mProgressView.startDeterminate();
                     else
                         MainActivity.mProgressView.resetLoading();
                     MainActivity.count++;

                    // mainActivity.startPercentMockThread(MainActivity.mProgressView);

                     //MainActivity.mProgressView.startIndeterminate();

                     holder.name.startAnimation(AnimationUtils.loadAnimation(context,R.anim.fadein));

                     String devicename=devices.get(position);
                     Log.e("devicename",devicename);


                     Log.e("Trying to pair with",devicename);
                    if (!mainActivity.paireddevices(MainActivity.bluetoothDevices.get(position)))
                     MainActivity.bluetoothDevices.get(position).createBond();
                     else
                     {
                         Toast.makeText(context,"Already paired",Toast.LENGTH_LONG).show();
                         MainActivity.mProgressView.stopOk();
                         MainActivity.mProgressView.startAnimation(AnimationUtils.loadAnimation(context,R.anim.fadein5));
                        // MainActivity.mProgressView.setVisibility(View.GONE);


                     }


                 }
             });



        }







    @Override
    public int getItemCount() {

        return devices.size();
    }




    public  class VideoAdapter extends RecyclerView.ViewHolder{
       public TextView name;

        public VideoAdapter(View itemView) {
            super(itemView);
            name=(TextView) itemView.findViewById(R.id.devicename);

        }
    }






}
