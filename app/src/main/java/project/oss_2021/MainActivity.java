package project.oss_2021;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import project.oss_2021.Cards.arrayAdapter;
import project.oss_2021.Cards.cards;
import project.oss_2021.Matches.MatchesActivity;
import project.oss_2021.Choice.ChoiceActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private project.oss_2021.Cards.arrayAdapter arrayAdapter;
    private int distance;
    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb;

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private String university;
    private double latitude;
    private double longitude;

    private Button mSignout, mSetting, mChoice, mMatches;

    ListView listView;
    List<cards> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        checkUserInfo();

        mSignout = findViewById(R.id.signout);
        mSetting = findViewById(R.id.setting);
        mChoice = findViewById(R.id.choice);
        mMatches = findViewById(R.id.matches);
        mSignout.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, StartActivity.class); // MainActivity -> ChooseLoginRegistrationActivity
            startActivity(intent);
            finish();
            return;
        });
        mSetting.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return;
        });
        mChoice.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ChoiceActivity.class);
            startActivity(intent);
            return;
        });
        mMatches.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
            startActivity(intent);
            return;
        });


        gpsTracker = new GpsTracker(MainActivity.this);

        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);

        Map userInfo = new HashMap();
        userInfo.put("latitude", latitude);
        userInfo.put("longitude", longitude);
        usersDb.child(currentUId).updateChildren(userInfo);


        rowItems = new ArrayList<cards>(); // 카드 배열들
        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems); //카드마다 al의 텍스트를 출력, item은 텍스트뷰, 색깔
        //al.add("java") <- 실행은 되지만 이것만 쓰면 앱 상에선 출력이 되질않음
        //arrayAdapter.notifyDataSetChanged(); <- 변경되었다는 것을 써야함
        SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            // 카드를 없앰
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            //카드를 왼쪽으로 넘길 때
            @Override
            public void onLeftCardExit(Object dataObject) {
                cards object = (cards) dataObject;
                String userId = object.getUserId();
                usersDb.child(userId).child("connection").child("nope").child(currentUId).setValue(true); //왼쪽으로 넘기면 connection 노드 -> nope 노드의 값을 true
                Toast.makeText(MainActivity.this, "Nope!", Toast.LENGTH_SHORT).show();
            }

            //카드를 오른쪽으로 넘길 때
            @Override
            public void onRightCardExit(Object dataObject) {
                cards object = (cards) dataObject;
                String userId = object.getUserId();
                usersDb.child(userId).child("connection").child("like").child(currentUId).setValue(true); //오른쪽으로 넘기면 connection 노드 -> like 노드 -> 현재 유저의 노드 값을 true
                isConnectionMatch(userId);
                Toast.makeText(MainActivity.this, "Like!", Toast.LENGTH_SHORT).show();
            }

            //카드를 다 소진 시켰을 때
            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });
        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "Click!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //매칭 함수
    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connection").child("like").child(userId); // 나에게 like를 보낸 유저
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(MainActivity.this, "new Connection", Toast.LENGTH_SHORT).show();
                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();
                    usersDb.child(snapshot.getKey()).child("connection").child("choice").child(currentUId).setValue(null);
                    usersDb.child(currentUId).child("connection").child("choice").child(snapshot.getKey()).setValue(null);
                    usersDb.child(snapshot.getKey()).child("connection").child("matches").child(currentUId).child("ChatId").setValue(key); //"나"의 matches 노드 -> "나에게 like를 보낸 유저"의 노드 -> ChatId 추가
                    usersDb.child(currentUId).child("connection").child("matches").child(snapshot.getKey()).child("ChatId").setValue(key); //"나"에게 like를 보낸 유저"의 matches 노드 -> "나"의 노드 -> ChatId 추가
                    // usersDb.child(snapshot.getKey()).child("connection").child("matches").child(currentUId).setValue(true);  //"나"의 matches 노드에 "나에게 like를 보낸 유저"의 노드 값을 true
                    // usersDb.child(currentUId).child("connection").child("matches").child(snapshot.getKey()).setValue(true);  //"나에게 like를 보낸 유저"의 matches 노드에 "나"의 노드 값을 true
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String userSex; // 사용자의 성별
    private String userSexOpp; // 사용자의 반대 성별
    private String hobby1, hobby2, hobby3, purpose;
    private boolean hobbyCheck, purposeCheck;


    public void checkUserInfo() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // userDb의 이벤트리스너
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getKey().equals(user.getUid())) {
                    if (snapshot.exists()) {
                        Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                        if(map.get("hobby1")!=null){
                            hobby1 = map.get("hobby1").toString();

                        }
                        if(map.get("hobby2")!=null){
                            hobby2 = map.get("hobby2").toString();

                        }
                        if(map.get("hobby3")!=null){
                            hobby3 = map.get("hobby3").toString();

                        }
                        if(map.get("hobbyCheck")!=null){
                            hobbyCheck = Boolean.parseBoolean(map.get("hobbyCheck").toString());

                        }
                        if(map.get("purposeCheck")!=null){
                            purposeCheck = Boolean.parseBoolean(map.get("purposeCheck").toString());

                        }
                        if(map.get("purpose")!=null){
                            purpose = map.get("purpose").toString();

                        }
                        if(map.get("distance")!=null){
                            String dis = map.get("distance").toString();
                            System.out.println(dis);
                            distance = Integer.parseInt(dis);
                        }


                        if (snapshot.child("sex").getValue() != null) {
                            userSex = snapshot.child("sex").getValue().toString();
                            switch (userSex) {
                                case "Male":
                                    userSexOpp = "Female";
                                    break;
                                case "Female":
                                    userSexOpp = "Male";
                                    break;
                            }
                            getOppositeSexUsers();
                        }
                        if (snapshot.child("university").getValue() != null) {
                            university = snapshot.child("university").getValue().toString();

                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public boolean checkDistance(String t1, String t2, int distance) {
        long latitudeOpp = Long.parseLong(t1);
        long longitudeOpp = Long.parseLong(t2);


        if (Math.sqrt(Math.pow((latitudeOpp - latitude), 2) + Math.pow((longitudeOpp - longitude), 2)) < distance) {
            return true;
        } else {
            return false;
        }


    }

    //반대 성별의 유저함수
    public void getOppositeSexUsers() {
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //여기가 필터링
                if (snapshot.child("sex").getValue() != null) {
                    if (snapshot.exists() && !snapshot.child("connection").child("nope").hasChild(currentUId) && !snapshot.child("connection").child("like").hasChild(currentUId) && snapshot.child("sex").getValue().toString().equals(userSexOpp) && snapshot.child("university").getValue().toString().equals(university)) {
                        if (checkDistance(snapshot.child("latitude").getValue().toString(), snapshot.child("longitude").getValue().toString(), distance)) {   //거리 필터링
                            if(hobbyCheck){
                                String [] hobbys = {hobby1, hobby2, hobby3};
                                if(Arrays.asList(hobbys).contains(snapshot.child("hobby1").getValue().toString()) || Arrays.asList(hobbys).contains(snapshot.child("hobby2").getValue().toString()) || Arrays.asList(hobbys).contains(snapshot.child("hobby3").getValue().toString())){
                                    if(purposeCheck){
                                        if(purpose.equals(snapshot.child("purpose").getValue().toString())){
                                            String profileImageUrl = "default";
                                            if (!snapshot.child("profileImageUrl").getValue().equals("default")) {
                                                profileImageUrl = snapshot.child("profileImageUrl").getValue().toString();
                                            }
                                            cards item = new cards(snapshot.getKey(), snapshot.child("name").getValue().toString(), profileImageUrl);
                                            rowItems.add(item);
                                            arrayAdapter.notifyDataSetChanged();
                                        }


                                    }
                                    else{
                                        String profileImageUrl = "default";
                                        if (!snapshot.child("profileImageUrl").getValue().equals("default")) {
                                            profileImageUrl = snapshot.child("profileImageUrl").getValue().toString();
                                        }
                                        cards item = new cards(snapshot.getKey(), snapshot.child("name").getValue().toString(), profileImageUrl);
                                        rowItems.add(item);
                                        arrayAdapter.notifyDataSetChanged();
                                    }



                                }
                            }else{
                                if(purposeCheck){
                                    if(purpose.equals(snapshot.child("purpose").getValue().toString())){
                                        String profileImageUrl = "default";
                                        if (!snapshot.child("profileImageUrl").getValue().equals("default")) {
                                            profileImageUrl = snapshot.child("profileImageUrl").getValue().toString();
                                        }
                                        cards item = new cards(snapshot.getKey(), snapshot.child("name").getValue().toString(), profileImageUrl);
                                        rowItems.add(item);
                                        arrayAdapter.notifyDataSetChanged();
                                    }


                                }
                                else{
                                    String profileImageUrl = "default";
                                    if (!snapshot.child("profileImageUrl").getValue().equals("default")) {
                                        profileImageUrl = snapshot.child("profileImageUrl").getValue().toString();
                                    }
                                    cards item = new cards(snapshot.getKey(), snapshot.child("name").getValue().toString(), profileImageUrl);
                                    rowItems.add(item);
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    // -----------여기서 부턴 gps관련-----------------

    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults); //이거 내가 임의로 추가됫는데 오류나면 여기 확인
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress(double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}