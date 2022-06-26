package com.example.criminalintent;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
        Recall! super keyword allows us to access the parent class (in this case AppCompatActivity)
        super.onCreate(...) calls the onCreate method of AppCompatActivity
        super(...) calls the CONSTRUCTOR of the parent class, and passes through (...) args
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        // FragmentManager handles a list of fragments
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
