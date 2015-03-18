package com.kanditag.kanditag;

import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

//import com.dlazaro66.qrcodereaderview.QRCodeReaderView;


public class Menu extends Fragment {

    RelativeLayout relativeLayout;
    View rootView;
    ImageView backgroundIV;
    ImageView shadedBackground;
    ImageView toProfile, toMessage, toBrowse, toFollow;

    final static String TAG = "MenuFragment";

    private final int MESSAGE_INTENT_NUMBER = 1;
    private final int PROFILE_INTENT_NUMBER = 5;
    private final int FOLLOW_INTENT_NUMBER = 3;
    private final int BROWSE_INTENT_NUMBER = 2;

    private ViewGroup contain;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_menu, container, false);

        this.contain = container;

        try {
            relativeLayout = (RelativeLayout) rootView.findViewById(R.id.mainRelativeLayout);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        toProfile = (ImageView) rootView.findViewById(R.id.toProfile);
        //relativeLayout.addView(toProfile);
        toProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfile();
                container.setVisibility(View.GONE);
                //Toast.makeText(getActivity(), "toProfile", Toast.LENGTH_SHORT).show();
            }
        });
        toMessage = (ImageView) rootView.findViewById(R.id.toMessage);
        toMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
                container.setVisibility(View.GONE);
                //Toast.makeText(getActivity(), "toMessage", Toast.LENGTH_SHORT).show();
            }
        });
        toBrowse = (ImageView) rootView.findViewById(R.id.toBrowse);
        toBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBrowse();
                container.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "toBrowse", Toast.LENGTH_SHORT).show();
            }
        });
        toFollow = (ImageView) rootView.findViewById(R.id.toFollow);
        toFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFollow();
                container.setVisibility(View.GONE);
            }
        });

        return rootView;
    }

    /**
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            Log.d(TAG, "visible");
        } else {
            Log.d(TAG, "invisible");
        }
    }
    **/

    public void showProfile() {
        Intent intent = new Intent(getActivity(), Settings.class);
        startActivityForResult(intent, PROFILE_INTENT_NUMBER);
    }

    public void showMessage() {
        Intent intent = new Intent(getActivity(), Message.class);
        startActivityForResult(intent, MESSAGE_INTENT_NUMBER);
    }

    public void showBrowse() {
        Intent intent = new Intent(getActivity(), Browse.class);
        startActivityForResult(intent, BROWSE_INTENT_NUMBER);
    }

    public void showFollow() {
        Intent intent = new Intent(getActivity(), Kandi.class);
        startActivityForResult(intent, FOLLOW_INTENT_NUMBER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_INTENT_NUMBER) {
            relativeLayout.setVisibility(View.VISIBLE);
            if (resultCode == getActivity().RESULT_OK) {
                Toast.makeText(getActivity(), "You are now logged out", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "result ok from setting");
                Intent loginIntent = new Intent(getActivity(), Login.class);
                startActivityForResult(loginIntent, 1);
                getActivity().finish();
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Log.i(TAG, "returned from profile");
                this.contain.setVisibility(View.VISIBLE);
            }
        }

        if (requestCode == MESSAGE_INTENT_NUMBER) {
            this.contain.setVisibility(View.VISIBLE);
            Log.i(TAG, "returned from message");
        }

        if (requestCode == FOLLOW_INTENT_NUMBER) {
            this.contain.setVisibility(View.VISIBLE);
            Log.i(TAG, "returned from follow");
        }

        if (requestCode == BROWSE_INTENT_NUMBER) {
            this.contain.setVisibility(View.VISIBLE);
            Log.i(TAG, "returned from browse");
        }
    }

    /**

    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    public void cameraNotFound() {

    }

    @Override
    public void onQRCodeRead(final String string, PointF[] points) {

    }
    **/
}
