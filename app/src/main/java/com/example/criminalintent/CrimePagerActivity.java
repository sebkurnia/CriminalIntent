package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID = "com.example.criminalintent.crime_id";
    private ViewPager2 viewPager;
    private List<Crime> crimes;

    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        viewPager = findViewById(R.id.crime_view_pager);
        crimes = CrimeLab.get(this).getCrimes(); // how tf are we able to get access to the CrimeLab here?
        FragmentManager fm = getSupportFragmentManager();

        viewPager.setAdapter(new FragmentStateAdapter(fm, getLifecycle()) {
            @Override
            public int getItemCount() {
                return crimes.size();
            }

            @Override
            public Fragment createFragment(int position) {
                Crime crime = crimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }
        });

        for (int i = 0; i < crimes.size(); i++) {
            if (crimes.get(i).getId().equals(crimeId)) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }

}
