package com.example.capstonchatting;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonchatting.fragment.AccountFragment;
import com.example.capstonchatting.fragment.ChatFragment;
import com.example.capstonchatting.fragment.PeopleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.mainactivity_bottomnavigationview);

        // Lambda 표현식으로 변경
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int ItemId = item.getItemId();
            if (ItemId == R.id.action_people) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new PeopleFragment()).commit();
                return true;
            } else if (ItemId == R.id.action_chat) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new ChatFragment()).commit();
                return true;
            } else if (ItemId == R.id.action_account) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new AccountFragment()).commit();
                return true;
            }
            return false;
        });

    }

}
