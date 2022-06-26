package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {

    private RecyclerView crimeRecyclerView;
    private CrimeAdapter adapter;
    private boolean subtitleVisible;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view);

        // RecyclerView requires a LayoutManager to work. LayoutManager handles scrolling, and
        // how to position each item. RecyclerView doesn't do that job itself.
        crimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            subtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (subtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_crime) {
            Crime crime = new Crime();
            CrimeLab.get(getActivity()).addCrime(crime);
            Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
            startActivity(intent);
        } else if (item.getItemId() == R.id.show_subtitle) {
            subtitleVisible = !subtitleVisible;
            getActivity().invalidateOptionsMenu();
            updateSubtitle();
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, subtitleVisible);
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);

        if (!subtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (adapter == null) {
            adapter = new CrimeAdapter(crimes);
            crimeRecyclerView.setAdapter(adapter);
        } else { // an adapter already exists
            adapter.setCrimes(crimes);
            adapter.notifyDataSetChanged();
        }

        updateSubtitle();

    }

    /*
    The CrimeHolder (more generally, a ViewHolder) holds onto a View.
    ViewHolder --> itemView --> View
     */
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView titleTextView;
        private TextView dateTextView;
        private Crime crime;
        private ImageView solvedImageView;

        /*
        Inflates the View of a Crime. First inflates the parent .xml, then assigns the Views to
        their appropriate variables.
         */
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {

            /*
            Recall how "super" works. Here we are calling the CONSTRUCTOR of
            RecyclerView.ViewHolder and giving it a View to inflate.
             */
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);
            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);
            solvedImageView = itemView.findViewById(R.id.img_crime_solved);
        }

        /*
        Called by the Adapter to actually bind data to the CrimeHolder.
        Recall: RecyclerView --> Adapter --> Binds the data in the CrimeHolder.
         */
        public void bind(Crime crime) {
            this.crime = crime;
            titleTextView.setText(crime.getTitle());
            dateTextView.setText(crime.getDate().toString());
            solvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
            startActivity(intent);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> crimes;

        public CrimeAdapter(List crimes) {
            this.crimes = crimes;
        }

        /*
        The RecyclerView needs to know the size of all the Views it may show.
         */
        @Override
        public int getItemCount() {
            return crimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            this.crimes = crimes;
        }

        /*
        Here the RecyclerView is calling the Adapter to return a new CrimeHolder (or more generally,
        a ViewHolder). Note that right now the CrimeHolder is empty!! This CrimeHolder is then passed
        to onBindViewHolder later on to actually bind some data to that CrimeHolder.
         */
        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        /*
        Here is where the RecyclerView passes through a CrimeHolder that it wants data bound to.
         */
        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = crimes.get(position);
            holder.bind(crime);
        }

    }

}
