package com.example.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mainactivity.Cards.arrayAdapter;
import com.example.mainactivity.Cards.cards;
import com.example.mainactivity.Choice.ChoiceActivity;
import com.example.mainactivity.Matches.MatchesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private com.example.mainactivity.Cards.arrayAdapter arrayAdapter;
    private int i;

    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb;

    private Button mSignout, mSetting, mMatches, mLikes;

    ListView listView;
    List<cards> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        checkUserSex();

        mSignout = findViewById(R.id.signout);
        mSetting = findViewById(R.id.setting);
        mMatches = findViewById(R.id.matches);
        mLikes = findViewById(R.id.likes);

        mSignout.setOnClickListener(view->{
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class); // MainActivity -> ChooseLoginRegistrationActivity
            startActivity(intent);
            finish();
            return;
        });
        mSetting.setOnClickListener(view->{
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return;
        });
        mLikes.setOnClickListener(view->{
            Intent intent = new Intent(MainActivity.this, ChoiceActivity.class);
            startActivity(intent);
            return;
        });
        mMatches.setOnClickListener(view->{
            Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
            startActivity(intent);
            return;
        });

        rowItems = new ArrayList<cards>(); // 카드 배열들
        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems ); //카드마다 al의 텍스트를 출력, item은 텍스트뷰, 색깔

        //al.add("java") <- 실행은 되지만 이것만 쓰면 앱 상에선 출력이 되질않음
        //arrayAdapter.notifyDataSetChanged(); <- 변경되었다는 것을 써야함

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

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
            public void onAdapterAboutToEmpty(int itemsInAdapter) { }

            @Override
            public void onScroll(float scrollProgressPercent) { }
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
                    Toast.makeText(MainActivity.this, "new Connection" , Toast.LENGTH_SHORT).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(snapshot.getKey()).child("connection").child("matches").child(currentUId).child("ChatId").setValue(key); //"나"의 matches 노드 -> "나에게 like를 보낸 유저"의 노드 -> ChatId 추가
                    usersDb.child(currentUId).child("connection").child("matches").child(snapshot.getKey()).child("ChatId").setValue(key); //"나"에게 like를 보낸 유저"의 matches 노드 -> "나"의 노드 -> ChatId 추가
                    //usersDb.child(currentUId).child("connection").child("matches").child(snapshot.getKey()).setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private String userSex; // 사용자의 성별
    private String userSexOpp; // 사용자의 반대 성별

    public void checkUserSex() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // userDb의 이벤트리스너
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getKey().equals(user.getUid())) {
                    if (snapshot.exists()){
                        if (snapshot.child("sex").getValue() != null){
                            userSex = snapshot.child("sex").getValue().toString();
                            switch (userSex){
                                case "Male":
                                    userSexOpp = "Female";
                                    break;
                                case "Female":
                                    userSexOpp = "Male";
                                    break;
                            }
                            getOppositeSexUsers();
                        }
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //반대 성별의 유저함수
    public void getOppositeSexUsers() {
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // nope 또는 like의 유저 값이 true가 아니면 카드 배열에 추가
               if(snapshot.exists() && !snapshot.child("connection").child("nope").hasChild(currentUId) && !snapshot.child("connection").child("like").hasChild(currentUId) && snapshot.child("sex").getValue().toString().equals(userSexOpp)) {
                   cards item = new cards(snapshot.getKey(), snapshot.child("name").getValue().toString()); //반대 성별의 name을 카드 배열에 추가한다
                   rowItems.add(item);
                   arrayAdapter.notifyDataSetChanged();
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

/*
    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class); // MainActivity -> ChooseLoginRegistrationActivity
        startActivity(intent);
        finish();
        return;
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    } */
}