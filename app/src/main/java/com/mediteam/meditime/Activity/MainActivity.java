package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.mediteam.meditime.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private ImageButton profile, terms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        profile = findViewById(R.id.profile);
        terms = findViewById(R.id.detail);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            navigationView.setCheckedItem(R.id.dashboard);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.burger);
    }

    @Override
    public boolean onNavigationItemSelected (@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_medication:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                profile.setBackgroundResource(R.drawable.account_filled);
                terms.setBackgroundResource(R.drawable.help_filled);
                drawerLayout.setBackgroundColor(getResources().getColor(R.color.light10));
                break;

            case R.id.nav_privacy:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PrivacyFragment()).commit();
                profile.setBackgroundResource(R.drawable.account_filled);
                terms.setBackgroundResource(R.drawable.help_filled);
                drawerLayout.setBackgroundColor(getResources().getColor(R.color.light10));
                break;

            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                profile.setBackgroundResource(R.drawable.account_filled);
                terms.setBackgroundResource(R.drawable.help_filled);
                drawerLayout.setBackgroundColor(getResources().getColor(R.color.light10));
                break;

            case R.id.nav_logout:
                Toast.makeText(MainActivity.this, "Account Signed out. Redirecting to login page.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void profile(View view){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        profile.setBackgroundResource(R.drawable.account_outline);
        terms.setBackgroundResource(R.drawable.help_filled);
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.light10));
    }

    public void details(View view){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TutorialFragment()).commit();
        terms.setBackgroundResource(R.drawable.help_outline);
        profile.setBackgroundResource(R.drawable.account_filled);
        drawerLayout.setBackgroundColor(getResources().getColor(R.color.dark20));

    }
}