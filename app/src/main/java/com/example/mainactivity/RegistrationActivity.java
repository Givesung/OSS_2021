package com.example.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private Button mRegister;
    private EditText mEmail, mPassword, mName;

    private RadioGroup mRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = mAuth.getCurrentUser();
                if(user != null) { //회원가입이 성공하면
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class); // RegistrationActivity에서 MainActivity를 호출
                    startActivity(intent);
                    finish(); //액티비티 종료
                    return;
                }
            }
        };

        mRegister = findViewById(R.id.register);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mName = findViewById(R.id.name);

        mRadioGroup = findViewById(R.id.radioGroup);

        mRegister.setOnClickListener(view -> {
            int selectId = mRadioGroup.getCheckedRadioButtonId(); // 라디오버튼 체크 확인

            final RadioButton radioButton = (RadioButton) findViewById(selectId);

            if(radioButton.getText() == null) {
                return;
            } // 성별을 정하지 않을 경우 이 리스너는 여기서 종료

            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();
            final String name = mName.getText().toString();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(RegistrationActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String userId = mAuth.getCurrentUser().getUid(); //파이어베이스 Authentication의 User ID를 가져옴
                        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                        //DB의 Users 노드 -> 라디오버튼에 해당하는 성별 노드(Female, Male) -> (사용자의 ID에 기반한 노드 생성) ->name 노드
                        Map userInfo = new HashMap<>();
                        userInfo.put("name", name);
                        userInfo.put("sex", radioButton.getText().toString());
                        userInfo.put("profileImageUrl", "defalut");

                        currentUserDb.updateChildren(userInfo); // 사용자가 입력한 이름을 DB의 name노드에 저장함
                    }
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}