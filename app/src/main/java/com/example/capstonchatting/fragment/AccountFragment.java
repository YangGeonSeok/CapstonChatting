package com.example.capstonchatting.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.capstonchatting.LoginActivity;
import com.example.capstonchatting.R;
import com.example.capstonchatting.model.NoticeModel;
import com.example.capstonchatting.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView commentButton;
    private TextView accountFragment_textView_status;
    private TextView changeNameButton;
    private DatabaseReference noticeRef;
    private LinearLayout noticeLayout;
    private LinearLayout noticeContentLayout;
    private TextView toggleNoticeButton;
    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        noticeRef = FirebaseDatabase.getInstance().getReference().child("notices");

        // 레이아웃 요소 초기화
        profileImageView = view.findViewById(R.id.accountFragment_imageView_profile);
        nameTextView = view.findViewById(R.id.accountFragment_textView_name);
        emailTextView = view.findViewById(R.id.accountFragment_textView_email);
        commentButton = view.findViewById(R.id.accountFragment_button_comment);
        accountFragment_textView_status = view.findViewById(R.id.accountFragment_textView_status);
        changeNameButton = view.findViewById(R.id.accountFragment_textView_changeName);
        noticeLayout = view.findViewById(R.id.accountFragment_layout_notice);
        noticeContentLayout = view.findViewById(R.id.accountFragment_layout_noticeContent);
        toggleNoticeButton = view.findViewById(R.id.accountFragment_button_toggleNotice);
        logoutButton = view.findViewById(R.id.accountFragment_button_logout);

        // 최근 5개의 공지사항을 불러와서 화면에 표시
        loadNotices();

        // 로그아웃 버튼 리스너
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }

            private void showLogoutConfirmationDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("로그아웃")
                        .setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 확인 버튼 클릭 시 로그아웃 처리
                                onLogoutButtonClick();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 취소 버튼 클릭 시 아무 동작 없음
                            }
                        })
                        .show();
            }
        });

        // 공지사항 바 클릭 시 공지사항 레이아웃 토글
        noticeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNoticeLayout();
            }
        });
        //토글 버튼 리스너
        toggleNoticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNoticeLayout();
            }
        });

        // 프로필 이미지 클릭 시 갤러리에서 이미지 선택
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        // 사용자 정보 불러오기
        loadUserInfo();

        // 상태 메시지 변경 다이얼로그 열기
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(view.getContext());
            }
        });

        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeNameDialog(view.getContext());
            }
        });

        return view;
    }
    // Firebase에 공지사항 저장
    private void saveNotice(String notice) {
        String noticeId = noticeRef.push().getKey();
        noticeRef.child(noticeId).setValue(notice);

    }

    // Firebase에서 최근 5개의 공지사항 불러오기
    private void loadNotices() {

        noticeRef.limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot noticeSnapshot : dataSnapshot.getChildren()) {
                    NoticeModel noticeModel = noticeSnapshot.getValue(NoticeModel.class);
                    if (noticeModel != null) {
                        String notice = noticeModel.getNotice();
                        // 화면에 공지사항 표시하는 메서드 호출
                        displayNotice(notice);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    // Firebase에서 불러온 공지사항을 화면에 표시
    private void displayNotice(String notice) {
        TextView noticeTextView = new TextView(requireContext());
        noticeTextView.setText(notice);
        noticeTextView.setTextSize(16);  // 텍스트 크기 조절 (원하는 크기로 변경)
        noticeTextView.setTextColor(Color.BLACK);  // 텍스트 색상 조절 (원하는 색상으로 변경)

        // 생성한 TextView를 레이아웃에 추가
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 8, 0, 8);  // 상하 여백 조절 (원하는 여백으로 변경)
        noticeTextView.setLayoutParams(layoutParams);

        // 레이아웃에 TextView 추가
        noticeContentLayout.addView(noticeTextView);
    }

    // 공지사항 레이아웃을 열고 닫는 메서드
    private void toggleNoticeLayout() {
        if (noticeContentLayout.getVisibility() == View.VISIBLE) {
            // 레이아웃이 열려있을 때 닫기
            noticeContentLayout.setVisibility(View.GONE);
            toggleNoticeButton.setText("열기");
        } else {
            // 레이아웃이 닫혀있을 때 열기
            noticeContentLayout.setVisibility(View.VISIBLE);
            toggleNoticeButton.setText("닫기");
        }
    }
    private void onLogoutButtonClick() {
        // Firebase에서 로그아웃 처리
        FirebaseAuth.getInstance().signOut();

        // 로그인 화면으로 이동
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish(); // 현재 액티비티 종료
    }


    private void showChangeNameDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_change_name, null);
        final EditText editText = view.findViewById(R.id.changeNameDialog_edittext);
        builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 이름을 Firebase에 저장
                updateUserName(editText.getText().toString());
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 취소 버튼 클릭 시 아무 동작 없음
            }
        });

        builder.show();
    }

    // 이름을 Firebase에 저장
    private void updateUserName(String userName) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("userName", userName);

        userRef.updateChildren(updateMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공적으로 업데이트됐을 때

                        // 새로운 사용자 이름을 TextView에 설정
                        nameTextView.setText(userName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 업데이트에 실패했을 때
                    }
                });
    }

    // 갤러리 열기
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1); // 1은 갤러리에서 이미지를 가져오는 요청 코드입니다.
    }

    // 갤러리에서 이미지를 선택한 후 호출되는 메소드
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            // 선택한 이미지를 Firebase에 업로드하여 프로필 이미지 변경
            Uri selectedImageUri = data.getData();
            uploadImage(selectedImageUri);
        }
    }

    // 선택한 이미지를 Firebase에 업로드하여 프로필 이미지 변경
    private void uploadImage(Uri imageUri) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("userImages").child(uid + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // 업로드 성공 시 이미지 다운로드 URL 가져오기
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // 다운로드 URL을 Firebase에 저장하고 프로필 이미지 업데이트
                                updateProfileImage(downloadUri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 업로드 실패 시 처리
                    }
                });
    }

    // Firebase에 저장된 프로필 이미지 URL로 업데이트
    private void updateProfileImage(String downloadUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("profileImageUrl", downloadUrl);

        userRef.updateChildren(updateMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공적으로 업데이트됐을 때
                        // 새로운 프로필 이미지를 Glide로 로드하여 ImageView에 설정
                        Glide.with(requireContext())
                                .load(downloadUrl)
                                .apply(new RequestOptions().circleCrop())
                                .into(profileImageView);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 업데이트에 실패했을 때
                    }
                });
    }

    // 사용자 정보 불러와서 화면에 표시
    private void loadUserInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    if (userModel != null) {
                        // 사용자 정보를 화면에 표시
                        displayUserInfo(userModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    // 사용자 정보를 화면에 표시
    private void displayUserInfo(UserModel userModel) {
        // 프로필 이미지 표시
        Glide.with(requireContext())
                .load(userModel.profileImageUrl)
                .apply(new RequestOptions().circleCrop())
                .into(profileImageView);

        // 사용자 이름, 이메일 표시
        nameTextView.setText(userModel.userName);
        emailTextView.setText(userModel.userEmail);

        // 상태 메시지 표시
        if (userModel.comment != null && !userModel.comment.isEmpty()) {
            // 상태 메시지가 있을 때만 표시
            String statusMessage = userModel.comment;
            // 상태 메시지를 화면에 표시
            accountFragment_textView_status.setText(statusMessage);
        } else {
            // 상태 메시지가 없을 때 기본 문구 표시
            accountFragment_textView_status.setText("상태 메시지를 설정하세요!");
        }
    }

    // 상태 메시지 변경 다이얼로그 표시
    private void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_comment, null);
        final EditText editText = view.findViewById(R.id.commentDialog_edittext);
        builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 상태 메시지를 Firebase에 저장
                updateComment(editText.getText().toString());
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 취소 버튼 클릭 시 아무 동작 없음
            }
        });

        builder.show();
    }

    // 상태 메시지를 Firebase에 저장
    private void updateComment(String comment) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("comment", comment);

        userRef.updateChildren(updateMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공적으로 업데이트됐을 때

                        // 새로운 상태 메시지를 TextView에 설정
                        accountFragment_textView_status.setText(comment);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 업데이트에 실패했을 때
                    }
                });
    }
}