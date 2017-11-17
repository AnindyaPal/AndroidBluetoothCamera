package com.example.user.bluetoothcamera;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

/**
 * Created by User on 11/2/2017.
 */

public class CameraFragment extends Fragment {
    android.hardware.Camera mCamera;
    CameraPreview mPreview;
    FrameLayout camera_frame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_camera, container, false);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_camera, container, false);
        } catch (InflateException e) {

        }
        camera_frame=(FrameLayout) view.findViewById(R.id.camera_frame);
        /*getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        mPreview = new CameraPreview(getActivity(), mCamera);
        camera_frame.addView(mPreview);
        return view;
    }
    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        CameraFragment mapFragment = (Fragment) getFragmentManager().findFragmentById(R.id.fullParkMap);
        if (mapFragment != null) {
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }*/
   /* @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Activity a = getActivity();

             a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }*/

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (mCamera!=null)
            mCamera.release();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return CubeAnimation.create(CubeAnimation.DOWN, enter, 1600);
    }
}
