package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;


public class MiniProfileView extends android.app.Fragment {

    SharedPreferences sharedPreferences;

    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private View rootView;
    private ImageView profilePic;
    private Button dismiss, save, continueButton;
    private TextView info;
    private EditText nameKandiEditText;
    private String userFBID;
    private ArrayList<String> fb_idArray = new ArrayList<>();
    private ArrayList<MiniProfileViewItem> miniProfileViewItemArrayList;

    private ViewPager viewPager;
    private MiniProfileViewPagerAdapter miniProfileViewPagerAdapter;
    private MyPageAdapter pageAdapter;

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    public static MiniProfileView newInstance(ArrayList<MiniProfileViewItem> list) {

        MiniProfileView m = new MiniProfileView();

        try {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("data", list);
            m.setArguments(bundle);
        } catch (NullPointerException nulle) {}

        return m;
    }

    public MiniProfileView() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_mini_profile_view, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        Bundle bundle = getArguments();
        miniProfileViewItemArrayList = bundle.getParcelableArrayList("data");

        profilePic = (ImageView) rootView.findViewById(R.id.miniProfileView_profilePic);

        nameKandiEditText = (EditText) rootView.findViewById(R.id.miniProfileView_editText);

        dismiss = (Button) rootView.findViewById(R.id.miniProfileView_dismissButton);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(MiniProfileView.this).commit();
            }
        });

        save = (Button) rootView.findViewById(R.id.miniProfileView_saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO check the order of this
                System.out.println("saving kandi");
                getActivity().getFragmentManager().beginTransaction().remove(MiniProfileView.this).commit();
                MainActivity main = (MainActivity) getActivity();
                Boolean save = true;
                //TODO double check to make sure this method doesnt override the original name of the kanditag
                main.startQrSave(save, nameKandiEditText.getText().toString());
            }
        });

        continueButton = (Button) rootView.findViewById(R.id.miniProfileView_continueButton);
        continueButton.setVisibility(View.GONE);

        info = (TextView) rootView.findViewById(R.id.miniProfileView_infoTextView);
        nameKandiEditText = (EditText) rootView.findViewById(R.id.miniProfileView_editText);

        if (miniProfileViewItemArrayList == null) {
            System.out.println("miniProfileViewItemArrayList is null");
            info.setVisibility(View.GONE);
        } else if (miniProfileViewItemArrayList.size() <= 8 ) {
            nameKandiEditText.setVisibility(View.GONE);
            System.out.println("miniProfileViewItemArrayList.size() = " + miniProfileViewItemArrayList.size());
        } else if (miniProfileViewItemArrayList.size() > 8) {
            System.out.println("miniProfileViewItemArrayList.size() > 8, should not ever be in here");
        } else {
            System.out.println("should not be in here");
        }


// ************************************************************************************


        try {

            for (int i = 0; i < miniProfileViewItemArrayList.size(); i++) {
                fb_idArray.add(miniProfileViewItemArrayList.get(i).getFb_id());
            }

            System.out.println("miniProfileViewItemArray.size() = " + miniProfileViewItemArrayList.size());


            if (fb_idArray.size() >=8 ) {
                info.setText("Kandi is Full, can no longer be registered");
                save.setVisibility(View.GONE);
                dismiss.setVisibility(View.GONE);
                continueButton.setVisibility(View.VISIBLE);
                continueButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().getFragmentManager().beginTransaction().remove(MiniProfileView.this).commit();
                    }
                });
            } else if (fb_idArray.contains(MY_FB_ID)) {
                info.setText("You already own this Kandi");
                save.setVisibility(View.GONE);
                dismiss.setVisibility(View.GONE);
                continueButton.setVisibility(View.VISIBLE);
                continueButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().getFragmentManager().beginTransaction().remove(MiniProfileView.this).commit();
                    }
                });
            }

        } catch (NullPointerException nulle) {
            info.setText("This is a new Kandi, please give it a name!");
        }

        URL img_value = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + fb_idArray.get(fb_idArray.size() - 1) + "/picture?width=120&height=120");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());

            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

            profilePic.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            miniProfileViewItemArrayList.removeAll(miniProfileViewItemArrayList);
            System.out.println("onDestroy: miniProfileViewItemArrayList.size() = " + miniProfileViewItemArrayList.size());
        } catch (NullPointerException nulle) {}
    }
}
