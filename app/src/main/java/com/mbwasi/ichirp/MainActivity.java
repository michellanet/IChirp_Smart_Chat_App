package com.mbwasi.ichirp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mbwasi.ichirp.Fragments.ChatsFragment;
import com.mbwasi.ichirp.Fragments.ProfileFragment;
import com.mbwasi.ichirp.Fragments.UsersFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";

    DatabaseReference dbRef;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        if (user == null || !user.isEmailVerified()) {
            //TODO: split into separate checks and show notice if email not verified
            Log.d(TAG, "onCreate: User is NULL or email unverified");
            //Go to login screen
            //TODO: Take user to login screen
            Intent intent = new Intent( this, LoginActivity.class);
            startActivity(intent);
        }else {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            String token = task.getResult().getToken();

                            Log.d(TAG, "Token:" + token);
                            if (user != null) {
                                dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("token", token);
                                dbRef.updateChildren(data);
                            }
                            //  Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                        }
                    });

            TabLayout tabLayout = findViewById(R.id.tab_layout);
            ViewPager viewPager = findViewById(R.id.view_paper);

            ViewPaperAdapter viewPaperAdapter = new ViewPaperAdapter(getSupportFragmentManager());

            viewPaperAdapter.addFragment(new ChatsFragment(), "Chats");
            viewPaperAdapter.addFragment(new UsersFragment(), "Users");
            viewPaperAdapter.addFragment(new ProfileFragment(), "Profile");

            viewPager.setAdapter(viewPaperAdapter);

            tabLayout.setupWithViewPager(viewPager);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                return true;
        }

        return false;
    }

    class ViewPaperAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPaperAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }

    private void setStatus(String status) {
        if (user != null) {
            dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

            HashMap<String, Object> data = new HashMap<>();
            data.put("status", status);

            dbRef.updateChildren(data);

//        dbRef.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "onComplete: Status saved to FB");
//                } else {
//                    Log.d(TAG, "onComplete: Failes to save Status FB");
//                }
//            }
//        });
        }
    }
}