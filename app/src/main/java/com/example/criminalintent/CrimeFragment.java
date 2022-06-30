/*
CrimeFragment shows the details of one particular crime. See fragment_crime.xml. This is NOT
the same as CrimeListFragment.
 */

package com.example.criminalintent;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import java.io.Console;
import java.util.UUID;
import java.util.Date;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String REQUEST_DATE = "date_request_key";

    private Crime crime;
    private EditText titleField;
    private Button dateButton;
    private Button reportButton;
    private Button suspectButton;
    private CheckBox solvedCheckBox;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment frag = new CrimeFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        this.crime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(crime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.crime_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_crime) {
            CrimeLab.get(getActivity()).deleteCrime(crime.getId());
            getActivity().finish();
        }

        return true;
    }

    // Here we CREATE and CONFIGURE the fragment's view and return the INFLATED VIEW to the hosting
    // activity.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // (layout resource id xml, view's parent, whether to add inflated view to view's parent)
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        titleField = (EditText) v.findViewById(R.id.crime_title);
        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                crime.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        dateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getParentFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(crime.getDate());
                fm.setFragmentResultListener(REQUEST_DATE, getViewLifecycleOwner(), new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        Date date = (Date) result.getSerializable(REQUEST_DATE);
                        crime.setDate(date);
                        updateDate();
                    }
                });
                dialog.show(fm, DIALOG_DATE);
            }
        });

        solvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                crime.setSolved(b);
            }
        });

        reportButton = (Button) v.findViewById(R.id.report_crime);
        reportButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                /*
                Some important parts of the Intent: First, the ACTION trying to be performed: for
                sending something, we use ACTION_SEND
                We also dictate the TYPE of data that we want to send using the text/plain command
                If we want to explicitly define a "chooser" for the send action, we use the
                createChooser method and pass the intent + the message that you want to display
                above the choices.
                */
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }

        });

        /*
        Here we create an ActivityResultLauncher where we register an Activity to give us back some
        result (which we specify here). params for registerForActivityResult include a new
        ActivityResultsContract, specifically a PickContact contract, and then we must override
        the existing onActivityResult with some behavior for what to do with the final information.

        The parameter for the ActivityResultLauncher (in this case Void) is just the info you'd
        like to pass to the started activity as an argument.
        */
        ActivityResultLauncher<Void> getContact = registerForActivityResult(new ActivityResultContracts.PickContact(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                // Here we have to update the SQL layer with the info we got from the Uri result
                // We must scan (cursor) over the contacts database to get to the desired contact

                String[] queryFields = new String[] {ContactsContract.Contacts.DISPLAY_NAME};

                Cursor c = getActivity().getContentResolver().query(result, queryFields,null,null,null);

                if (c.getCount() == 0) {
                    return;
                } else {
                    c.moveToFirst();
                    String suspectName = c.getString(0);
                    crime.setSuspect(suspectName);
                    suspectButton.setText(suspectName);
                }

                c.close();

            }
        });



        suspectButton = (Button) v.findViewById(R.id.crime_suspect);

        suspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContact.launch(null);
            }
        });

        Intent getContactIntent = getContact.getContract().createIntent(getContext(), null);

//        if (!canResolveIntent(getContactIntent)) {
//            suspectButton.setEnabled(false);
//        }

        if (crime.getSuspect() != null) {
            suspectButton.setText(crime.getSuspect());
        }

        return v;
    }

    private void updateDate() {
        dateButton.setText(crime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (crime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, crime.getDate()).toString();
        
        String suspect = crime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        
        String combinedReport = getString(R.string.crime_report, crime.getTitle(), dateString,
                solvedString, suspect);
        
        return combinedReport;

    }

    /*
    Helper method to determine whether the OS can handle opening a Contacts app.
    @params  : Intent
    @returns : ResolveInfo returns the Activity which can handle the intent, null otherwise (bad)
    */
//    private boolean canResolveIntent(Intent intent) {
//        PackageManager pkgManager = getActivity().getPackageManager();
//        ResolveInfo resolvedActivity = pkgManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        System.out.println("resolved activity = " + resolvedActivity);
//        return resolvedActivity != null;
//    }


}
