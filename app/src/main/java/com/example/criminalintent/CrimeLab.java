package com.example.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab crimeLab;
    private Context context;
    private SQLiteDatabase db;

    private CrimeLab(Context context) {
        this.context = context.getApplicationContext();
        db = new CrimeBaseHelper(this.context).getWritableDatabase();
        // When we call getWriteableDatabase here, our CrimeBaseHelper will getOrDefault the db,
        // save out the latest version number, and if the db exists will upgrade the version number.

    }

    public List<Crime> getCrimes() {

        List<Crime> crimes_v2 = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            crimes_v2.add(cursor.getCrime());
            cursor.moveToNext();
        }

        cursor.close();

        return crimes_v2;

    }

    public void addCrime(Crime c) {
        ContentValues values = getContentValues(c);
        db.insert(CrimeTable.NAME, null, values);
    }

    public Crime getCrime(UUID id) {

        Crime c;

        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[] {id.toString()}
        );

        if (cursor.getCount() == 0) {
            return null;
        } else {
            cursor.moveToFirst();
            c = cursor.getCrime();
            cursor.close();
        }

        return c;
    }

    public void deleteCrime(UUID id) {
        db.delete(CrimeTable.NAME, CrimeTable.Cols.UUID + " = ?",
                new String[] {id.toString()}
        );
    }
    
    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        
        db.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = db.query(
                CrimeTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new CrimeCursorWrapper(cursor);
    }

    public static CrimeLab get(Context context) {
        if (crimeLab == null) {
            crimeLab = new CrimeLab(context);
        }
        return crimeLab;
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
        
        return values;
    }

}
