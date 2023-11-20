package com.example.capstonchatting;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonchatting.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;



public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private String splash_background;
    private ImageView profile;
    private Uri imageUri;
    private Button backButton;
    private EditText passwordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        passwordConfirm = findViewById(R.id.signupActivity_edittext_confirm_password);

        backButton = findViewById(R.id.signupActivity_button_back);  // Added this line
        backButton.setOnClickListener(new View.OnClickListener() {  // Added this block
            @Override
            public void onClick(View view) {
                finish();  // Finish the current activity and go back
            }
        });

        profile = findViewById(R.id.signupActivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        email = findViewById(R.id.signupActivity_edittext_email);
        name = findViewById(R.id.signupActivity_edittext_name);
        password = findViewById(R.id.signupActivity_edittext_password);
        signup = findViewById(R.id.signupActivity_button_signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().isEmpty() || name.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    Toast.makeText(SignupActivity.this, "이메일, 이름, 비밀번호, 비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String enteredPassword = password.getText().toString();
                String enteredPasswordConfirm = passwordConfirm.getText().toString();

                if(!enteredPassword.equals(enteredPasswordConfirm)) {
                    Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    final String uid = task.getResult().getUser().getUid();
                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name.getText().toString())
                                            .build();

                                    task.getResult().getUser().updateProfile(userProfileChangeRequest);

                                    // 이미지 업로드 및 URL 얻기 (이미지가 없는 경우도 처리)
                                    uploadImageAndSaveData(uid);
                                } else {
                                    Toast.makeText(SignupActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // 이미지 업로드 및 URL 얻기
    private void uploadImageAndSaveData(final String uid) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("userImages").child(uid);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUri) {
                                        String imageUrl = downloadUri.toString();
                                        // 사용자 정보 업데이트 및 Realtime Database에 저장
                                        saveUserData(uid, imageUrl);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Firebase", "Image URL retrieval failed", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Image upload failed", e);
                    }
                });
    }

    // 사용자 정보 업데이트 및 Realtime Database에 저장
    private void saveUserData(final String uid, final String imageUrl) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(imageUrl))
                .build();

        auth.getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // 사용자 정보 업데이트 성공
                            // Realtime Database에 추가적인 정보 저장
                            UserModel userModel = new UserModel();
                            userModel.userName = name.getText().toString();
                            userModel.userEmail = email.getText().toString();
                            userModel.profileImageUrl = imageUrl;
                            userModel.uid = uid;

                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Realtime Database에 사용자 정보 저장 성공
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                            finish(); // 현재 화면 종료
                                            Toast.makeText(SignupActivity.this, "가입을 축하합니다!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Realtime Database에 사용자 정보 저장 실패
                                            Log.e("Firebase", "User data save failed", e);
                                        }
                                    });
                        } else {
                            // 사용자 정보 업데이트 실패
                            Log.e("Firebase", "User profile update failed");
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            profile.setImageURI(data.getData());
            imageUri = data.getData();
        }
    }
}




