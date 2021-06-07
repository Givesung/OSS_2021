package project.oss_2021;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FilteringActivity extends AppCompatActivity {
    private EditText mHobby1, mHobby2, mHobby3;
    private RadioGroup mRadioGroup;
    private RadioButton lP, sP;
    private CheckBox mHobbyCheck, mPurposeCheck;
    private SeekBar seekBar;
    private TextView mDistance;

    private Button mPrevious, mConfirm;


    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, hobby1, hobby2, hobby3, purpose, university, userSex;
    private int distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtering);

        mHobby1 = findViewById(R.id.Hobby1);
        mHobby2 = findViewById(R.id.Hobby2);
        mHobby3 = findViewById(R.id.Hobby3);
        mRadioGroup = findViewById(R.id.radioGroup);
        mHobbyCheck = findViewById(R.id.HobbyCheck);
        mPurposeCheck = findViewById(R.id.PurposeCheck);

        lP = findViewById(R.id.lightP);
        sP = findViewById(R.id.seriousP);
        mDistance = findViewById(R.id.distance);
        mPrevious = findViewById(R.id.Previous);
        mConfirm = findViewById(R.id.Confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        seekBar = findViewById(R.id.SeekBar);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);


        getUserInfo();

        seekBar.setMax(150);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // onProgressChange - Seekbar 값 변경될때마다 호출

                distance = seekBar.getProgress();
                mDistance.setText(distance + "km");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // onStartTeackingTouch - SeekBar 값 변경위해 첫 눌림에 호출
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // onStopTrackingTouch - SeekBar 값 변경 끝나고 드래그 떼면 호출
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
                finish();   //피니쉬 하면 첫화면으로 넘어가는지 확인
            }
        });
        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();       //
                return;
            }
        });

    }

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("hobby1")!=null){
                        hobby1 = map.get("hobby1").toString();
                        mHobby1.setText(hobby1);
                    }
                    if(map.get("hobby2")!=null){
                        hobby2 = map.get("hobby2").toString();
                        mHobby2.setText(hobby2);
                    }
                    if(map.get("hobby3")!=null){
                        hobby3 = map.get("hobby3").toString();
                        mHobby3.setText(hobby3);
                    }
                    if(map.get("hobbyCheck")!=null){
                        boolean check = Boolean.parseBoolean(map.get("hobbyCheck").toString());
                        mHobbyCheck.setChecked(check);
                    }
                    if(map.get("purposeCheck")!=null){
                        boolean check = Boolean.parseBoolean(map.get("purposeCheck").toString());
                        mPurposeCheck.setChecked(check);
                    }
                    if(map.get("purpose")!=null){
                        purpose = map.get("purpose").toString();

                        if(purpose.equals(lP.getText().toString())){
                            lP.setChecked(true);
                        }
                        else
                            sP.setChecked(true);
                    }
                    if(map.get("distance")!=null){
                        distance = Integer.parseInt(map.get("distance").toString());
                        mDistance.setText(distance + "km");
                        seekBar.setProgress(distance);
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void saveUserInformation() {
        hobby1 = mHobby1.getText().toString();
        hobby2 = mHobby2.getText().toString();
        hobby3 = mHobby3.getText().toString();


        int selectId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        /*
        if(radioButton.getText() == null){
            return;
        }*/

        Map userInfo = new HashMap();
        userInfo.put("hobby1", hobby1);
        userInfo.put("hobby2", hobby2);
        userInfo.put("hobby3", hobby3);

        if(mHobbyCheck.isChecked()){
            userInfo.put("hobbyCheck", "true");
        }else{
            userInfo.put("hobbyCheck", "false");
        }
        if(mPurposeCheck.isChecked())
            userInfo.put("purposeCheck", "true");
        else
            userInfo.put("purposeCheck", "false");

        userInfo.put("purpose", radioButton.getText().toString());
        userInfo.put("distance", distance);
        mUserDatabase.updateChildren(userInfo);

    }

}
