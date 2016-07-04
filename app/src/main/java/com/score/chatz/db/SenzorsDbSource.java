package com.score.chatz.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.receivers.DatabaseChangedReceiver;
import com.score.chatz.ui.ChatFragment;
import com.score.chatz.utils.PreferenceUtils;
import com.score.chatz.viewholders.Message;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Do all database insertions, updated, deletions from here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbSource {

    private static final String TAG = SenzorsDbSource.class.getName();
    private static Context context;

    /**
     * Init db helper
     *
     * @param context application context
     */
    public SenzorsDbSource(Context context) {
        Log.d(TAG, "Init: db source");
        this.context = context;
    }

    /**
     * Insert user to database
     *
     * @param user user
     */
    public void createUser(User user) {
        Log.d(TAG, "AddUser: adding user - " + user.getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, user.getUsername());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_USERNAME, values);
        db.close();
    }

    /**
     * Get user if exists in the database, other wise create user and return
     *
     * @param username username
     * @return user
     */
    public User getOrCreateUser(String username) {
        Log.d(TAG, "GetOrCreateUser: " + username);

        // get matching user if exists
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, SenzorsDbContract.User.COLUMN_NAME_USERNAME + "=?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        if (cursor.moveToFirst()) {
            // have matching user
            // so get user data
            // we return id as password since we no storing users password in database
            String _id = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            String _image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLOMN_NAME_IMAGE));

            // clear
            cursor.close();
            db.close();

            Log.d(TAG, "have user, so return it: " + username);
            return new User(_id, _username);
        } else {
            // no matching user
            // so create user
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, username);

            // inset data
            long id = db.insert(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_USERNAME, values);
            db.close();

            Log.d(TAG, "no user, so user created: " + username + " " + id);
            return new User(Long.toString(id), username);
        }
    }


    /**
     * Add chatz to the database
     *
     * @param chatz chatz object
     */

    public void createChatz (Message message) {
        Log.d(TAG, "AddSensor: adding senz from - " + message.getSender().getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Chatz.COLUMN_NAME_MESSAGE, message.getText());
        values.put(SenzorsDbContract.Chatz.COLUMN_NAME_OWNER, message.getSender().getUsername());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Chatz.TABLE_NAME, null, values);
        db.close();
        context.sendBroadcast(new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED));
    }

    public void updateSenz(User user, String value) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Senz.COLUMN_NAME_VALUE, value);

        // update
        db.update(SenzorsDbContract.Senz.TABLE_NAME,
                values,
                SenzorsDbContract.Senz.COLUMN_NAME_USER + " = ?",
                new String[]{String.valueOf(user.getId())});

        db.close();
    }

    /**
     * Add senz to the database
     *
     * @param senz senz object
     */
    public void createSenz (Senz senz) {
        Log.d(TAG, "AddSensor: adding senz from - " + senz.getSender());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        if (senz.getAttributes().containsKey("lat")) {
            values.put(SenzorsDbContract.Senz.COLUMN_NAME_NAME, "Location");
        } else if (senz.getAttributes().containsKey("gpio13") || senz.getAttributes().containsKey("gpio15")) {
            // store json string in db
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("GPIO13", "OFF");
                jsonObject.put("GPIO15", "OFF");

                values.put(SenzorsDbContract.Senz.COLUMN_NAME_NAME, "GPIO");
                values.put(SenzorsDbContract.Senz.COLUMN_NAME_VALUE, jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        values.put(SenzorsDbContract.Senz.COLUMN_NAME_USER, senz.getSender().getId());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Senz.TABLE_NAME, SenzorsDbContract.Senz.COLUMN_NAME_NAME, values);
        db.close();
    }

    /**
     * Get all chatz, two types of sensors here
     * 1. my sensors
     * 2. friends sensors
     *
     * @return sensor list
     */
    public ArrayList<Message> getChatz() {
        Log.d(TAG, "GetSensors: getting all chatz messages");
        ArrayList<Message> chatzList = new ArrayList();
        User currentUser;
        try {
            currentUser = PreferenceUtils.getUser(context);
        } catch (NoUserException e) {
            e.printStackTrace();
            return chatzList;
        }

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // join query to retrieve data
        String query = "SELECT chatz.message, chatz.owner " +
                "FROM chatz ";
        Cursor cursor = db.rawQuery(query, null);

        // sensor/user attributes
        String _chatzId;
        String _chatzMessage;
        String _chatzOwner;
        String _chatzUsername;

        // extract attributes
        while (cursor.moveToNext()) {
            HashMap<String, String> chatzAttributes = new HashMap<>();

            // get chatz attributes
            //_chatzId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Chatz._ID));
            _chatzMessage = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Chatz.COLUMN_NAME_MESSAGE));
            _chatzOwner = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Chatz.COLUMN_NAME_OWNER));

            // get user attributes
            //_chatzUsername = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            /*chatzAttributes.put(_chatzMessage, _chatzOwner);

            // create senz
            Senz chatz = new Senz();
            chatz.setId(_chatzId);
            chatz.setAttributes(chatzAttributes);
            chatz.setSender(new User(_chatzOwner, _chatzUsername));*/
            Log.d(TAG, "currentUSER id : " + currentUser.getUsername());
            //Log.d(TAG, "currentUSER id : " + _chatzUsername);

            boolean isMine = currentUser.getUsername().equalsIgnoreCase(_chatzOwner);
            Message message = new Message(_chatzMessage, new User(_chatzOwner, _chatzOwner), isMine);



            // fill senz list
            chatzList.add(message);
        }

        // clean
        cursor.close();
        db.close();

        Log.d(TAG, "GetChatz: chatz count " + chatzList.size());
        return chatzList;
    }

    /**
     * Get all sensors, two types of sensors here
     * 1. my sensors
     * 2. friends sensors
     *
     * @return sensor list
     */
    public List<Senz> getSenzes() {
        Log.d(TAG, "GetSensors: getting all sensor");
        List<Senz> sensorList = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // join query to retrieve data
        String query = "SELECT senz._id, senz.name, senz.value, user._id, user.username " +
                "FROM senz " +
                "LEFT OUTER JOIN user " +
                "ON senz.user = user._id";
        Cursor cursor = db.rawQuery(query, null);

        // sensor/user attributes
        String _senzId;
        String _senzName;
        String _senzValue;
        String _userId;
        String _username;

        // extract attributes
        while (cursor.moveToNext()) {
            HashMap<String, String> senzAttributes = new HashMap<>();

            // get senz attributes
            _senzId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Senz._ID));
            _senzName = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Senz.COLUMN_NAME_NAME));
            _senzValue = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Senz.COLUMN_NAME_VALUE));

            // get user attributes
            _userId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            senzAttributes.put(_senzName, _senzValue);

            // create senz
            Senz senz = new Senz();
            senz.setId(_senzId);
            senz.setAttributes(senzAttributes);
            senz.setSender(new User(_userId, _username));

            // fill senz list
            sensorList.add(senz);
        }

        // clean
        cursor.close();
        db.close();

        Log.d(TAG, "GetSensors: sensor count " + sensorList.size());
        return sensorList;
    }

    public List<User> readAllUsers() {
        Log.d(TAG, "GetSensors: getting all sensor");
        List<User> userList = new ArrayList<User>();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, null, null, null, null, null, null);

        // user attributes
        String _id;
        String _username;

        // extract attributes
        while (cursor.moveToNext()) {
            _id = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            // we don't add mysensors user as a friend(its a server :))
            if (!_username.equalsIgnoreCase("mysensors")) userList.add(new User(_id, _username));
        }

        // clean
        cursor.close();
        db.close();

        Log.d(TAG, "user count " + userList.size());

        return userList;
    }

    /**
     * Delete chatz from database,
     *
     * @param chatz chatz
     */
    public void deleteChatz(Senz senz) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        /*db.delete(SenzorsDbContract.Chatz.TABLE_NAME,
                SenzorsDbContract.Chatz.COLUMN_NAME_USER + "=?" + " AND " +
                        SenzorsDbContract.Senz.COLUMN_NAME_NAME + "=?",
                new String[]{senz.getSender().getId(), senz.getAttributes().keySet().iterator().next()});*/
        db.close();
    }

    /**
     * Delete senz from database,
     *
     * @param senz senz
     */
    public void deleteSenz(Senz senz) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Senz.TABLE_NAME,
                SenzorsDbContract.Senz.COLUMN_NAME_USER + "=?" + " AND " +
                        SenzorsDbContract.Senz.COLUMN_NAME_NAME + "=?",
                new String[]{senz.getSender().getId(), senz.getAttributes().keySet().iterator().next()});
        db.close();
    }

    public void insertImageToDB(String username, String encodedImage) {

        ContentValues cv = new ContentValues();

        cv.put(SenzorsDbContract.User.COLOMN_NAME_IMAGE, encodedImage);


        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        int p = db.update(SenzorsDbContract.User.TABLE_NAME, cv, SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = ?", new String[]{username});
        if (p == 0) {
            cv.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, username);
            db.insert(SenzorsDbContract.User.TABLE_NAME, null, cv);
        }


    }

    public String getImageFromDB(String username) {

        String selectQuery = "SELECT image from  " + SenzorsDbContract.User.TABLE_NAME + " where " + SenzorsDbContract.User.COLUMN_NAME_USERNAME + " = '" + username + "'";
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        String image = null;
        if (cursor.moveToFirst()) {
            do {
                image = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLOMN_NAME_IMAGE));
                // get  the  data into array,or class variable
            } while (cursor.moveToNext());
        }
        db.close();
        return image;

    }

}
