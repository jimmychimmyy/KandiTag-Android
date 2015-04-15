package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
//import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.facebook.Session;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class QRScanner extends Fragment {

    public static final String DatabaseName = "defaultDatabase";

    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    int fragValue;

    public static final String TAG = "QRScanner";

    private ArrayList<String> qrArray = new ArrayList<String>();

    private TextView myTextView;
    //private QRCodeReaderView myDecoderView;

    private Button toSettings;

    private Button profileDrawer;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private CharSequence title;
    private ActionBarDrawerToggle drawerToggle;

    static QRScanner init(int val) {
        QRScanner scannerFragment = new QRScanner();
        Bundle args = new Bundle();
        args.putInt("val", val);
        scannerFragment.setArguments(args);
        return scannerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragValue = getArguments() != null ? getArguments().getInt("val") : 1;

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.activity_qrscanner, container, false);

        //myDecoderView = (QRCodeReaderView) layoutView.findViewById(R.id.decoder);
        //myDecoderView.setOnQRCodeReadListener(this);

        myTextView = (TextView) layoutView.findViewById(R.id.text);

        toSettings = (Button) layoutView.findViewById(R.id.toSettings);
        toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Settings.class);
                startActivityForResult(intent, 1);
            }
        });

        profileDrawer = (Button) layoutView.findViewById(R.id.profileDrawer);
        profileDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Need to open profileDrawer");
                //Intent showProfile = new Intent(getActvity(), Profile.class);
                //startActivityForResult(showProfile, 1);

            }
        });

        return layoutView;
    }

    /**

    @Override
    public void onQRCodeRead(final String text, PointF[] points) {
        if (qrArray.isEmpty()) {
            qrArray.add(text);
            Log.i(TAG, text + " added to array");
            Toast.makeText(getActivity(), qrArray.get(0), Toast.LENGTH_SHORT).show();
            saveQr(text);
            removeQrs();
            myTextView.setText(text);
            //Toast.makeText(getActivity(), qrArray.get(0), Toast.LENGTH_SHORT).show();
            //System.out.println(qrArray.get(0));
        }
    }

    **/

    public void removeQrs() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //qrArray.remove(0);
                qrArray.clear();
                Log.d(TAG, "item removed");
            }
        }, 2 * 1000);
    }

    private void saveQr(String qr) {
        Log.d(TAG, "attempting to save qr");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPreferences = this.getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

         String user_id = sharedPreferences.getString(UserId, "");
         String user_name = sharedPreferences.getString(Name, "");

        Log.i(TAG, "user_id:" + user_id);
        Log.d(TAG, "user_name:" + user_name);

        String Url = "http://kandi.nodejitsu.com/qr";

        HttpClient client = new DefaultHttpClient();

        if (user_id !=null && user_name !=null) {

            try {
                HttpPost post = new HttpPost(Url);

                JSONObject qrObj = new JSONObject();
                qrObj.put("qrcode", qr);
                qrObj.put("user_id", user_id);
                qrObj.put("username", user_name);

                StringEntity entity = new StringEntity(qrObj.toString(), HTTP.UTF_8);

                entity.setContentType("application/json");
                post.setEntity(entity);

                HttpResponse response = client.execute(post);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    Log.i(TAG, line);
                    parseJSON(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void parseJSON(String jsonString) {
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory() {
            @Override
            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

            @Override
            public List creatArrayContainer() {
                return new LinkedList();
            }
        };
        try {
            Map json = (Map)parser.parse(jsonString, containerFactory);
            Iterator iterator = json.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Log.d(TAG, entry.getKey() + "=>" + entry.getValue());
                if (entry.getKey().equals("user_id")) {
                    Log.d(TAG, "user_id:" + entry.getValue());
                }
                if (entry.getKey().equals("qrcode_id")) {
                    Log.d(TAG, "qrcode_id:" + entry.getValue());
                }
                if (entry.getKey().equals("qrcode")) {
                    Log.d(TAG, "qrcode:" + entry.getValue());
                }
                if (entry.getKey().equals("placement")) {
                    Log.d(TAG, "placement:" + entry.getValue());
                }
            }

        } catch (ParseException e) {
            System.out.println(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "logged out, log in to continue");
                Intent login = new Intent(getActivity(), Login.class);
                startActivity(login);
            }
        }
    }


    /**
    // Called when your device have no camera
    @Override
    public void cameraNotFound() {

    }

    // Called when there's no QR codes in the camera preview image
    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    public void onResume() {
        super.onResume();
        myDecoderView.getCameraManager().startPreview();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onPause() {
        super.onPause();
        myDecoderView.getCameraManager().stopPreview();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    **/
}
