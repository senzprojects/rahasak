package com.score.chatz.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.UserPermission;
import com.score.chatz.ui.ChatFragment;
import com.score.chatz.utils.PreferenceUtils;
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

        ContentValues permValues = new ContentValues();
        permValues.put(SenzorsDbContract.Permission.COLOMN_NAME_USER, user.getUsername());
        permValues.put(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION, true);
        permValues.put(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA, false);

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_USERNAME, values);
        //db.insertOrThrow(SenzorsDbContract.Permission.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_USERNAME, values);


        //db.close();
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
            //db.close();

            Log.d(TAG, "have user, so return it: " + username);
            return new User(_id, _username);
        } else {
            // no matching user
            // so create user
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, username);

            // inset data
            long id = db.insert(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_USERNAME, values);
            //db.close();

            Log.d(TAG, "no user, so user created: " + username + " " + id);
            return new User(Long.toString(id), username);
        }
    }




    public void updateSenz(User user, String value) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Location.COLUMN_NAME_VALUE, value);

        // update
        db.update(SenzorsDbContract.Location.TABLE_NAME,
                values,
                SenzorsDbContract.Location.COLUMN_NAME_USER + " = ?",
                new String[]{String.valueOf(user.getId())});

        //db.close();
    }

    public void updatePermissions(User user, String camPerm, String locPerm) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        int _camPerm = 0;
        int _locPerm = 0;
        if(camPerm != null) {
            _camPerm = camPerm.equalsIgnoreCase("true") ? 1 : 0;
        }
        if(locPerm != null) {
            _locPerm = locPerm.equalsIgnoreCase("true") ? 1 : 0;
        }
        // content values to inset
        ContentValues values = new ContentValues();
        if(camPerm != null) {
            values.put(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA, _camPerm);
        }
        if(locPerm != null) {
            values.put(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION, _locPerm);
        }

        // update
        db.update(SenzorsDbContract.Permission.TABLE_NAME,
                values,
                SenzorsDbContract.Permission.COLOMN_NAME_USER + " = ?",
                new String[]{user.getUsername()});

        //db.close();
    }


    public void updateConfigurablePermissions(User user, String camPerm, String locPerm) {

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        int _camPerm = 0;
        int _locPerm = 0;
        if(camPerm != null) {
            _camPerm = camPerm.equalsIgnoreCase("true") ? 1 : 0;
        }
        if(locPerm != null) {
            _locPerm = locPerm.equalsIgnoreCase("true") ? 1 : 0;
        }
        // content values to inset
        ContentValues values = new ContentValues();
        if(camPerm != null) {
            values.put(SenzorsDbContract.PermissionConfiguration.COLUMN_NAME_CAMERA, _camPerm);
        }
        if(locPerm != null) {
            values.put(SenzorsDbContract.PermissionConfiguration.COLUMN_NAME_LOCATION, _locPerm);
        }

        // update
        db.update(SenzorsDbContract.PermissionConfiguration.TABLE_NAME,
                values,
                SenzorsDbContract.PermissionConfiguration.COLOMN_NAME_USER + " = ?",
                new String[]{user.getUsername()});

        //db.close();
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
            values.put(SenzorsDbContract.Location.COLUMN_NAME_NAME, "Location");
        } else if (senz.getAttributes().containsKey("gpio13") || senz.getAttributes().containsKey("gpio15")) {
            // store json string in db
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("GPIO13", "OFF");
                jsonObject.put("GPIO15", "OFF");

                values.put(SenzorsDbContract.Location.COLUMN_NAME_NAME, "GPIO");
                values.put(SenzorsDbContract.Location.COLUMN_NAME_VALUE, jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        values.put(SenzorsDbContract.Location.COLUMN_NAME_USER, senz.getSender().getId());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Location.TABLE_NAME, SenzorsDbContract.Location.COLUMN_NAME_NAME, values);
        //db.close();
    }

    public void createSecret (Secret secret) {
        Log.d(TAG, "AddSecret, adding secret from - " + secret.getSender().getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        Log.i("SECRET", "Secret created: text - " + secret.getText() + ", sender - " + secret.getSender().getUsername() + ", receiver - " + secret.getReceiver().getUsername());
        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_TEXT, secret.getText());
        values.put(SenzorsDbContract.Secret.COLOMN_NAME_IMAGE, secret.getImage());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_RECEIVER, secret.getReceiver().getUsername());
        values.put(SenzorsDbContract.Secret.COLUMN_NAME_SENDER, secret.getSender().getUsername());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Secret.TABLE_NAME, null, values);
        //db.close();
    }


    public void createPermissionsForUser(Senz senz){
        Log.d(TAG, "Add New Permission: adding permission from - " + senz.getSender());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        if (senz.getAttributes().containsKey("permCam")) {
            values.put(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA, ((String)senz.getAttributes().get("permCam")).equalsIgnoreCase("true") ? 1 : 0);
        }
        if (senz.getAttributes().containsKey("permLoc")) {
            values.put(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION, ((String)senz.getAttributes().get("permLoc")).equalsIgnoreCase("true") ? 1 : 0);
        }
        values.put(SenzorsDbContract.Permission.COLOMN_NAME_USER, senz.getSender().getUsername());
        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Permission.TABLE_NAME, null, values);
        //db.close();

        createConfigurablePermissionsForUser(senz);
    }

    public void createConfigurablePermissionsForUser(Senz senz){
        Log.d(TAG, "Add New Permission: adding permission from - " + senz.getSender());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        if (senz.getAttributes().containsKey("permCam")) {
            values.put(SenzorsDbContract.PermissionConfiguration.COLUMN_NAME_CAMERA, ((String)senz.getAttributes().get("permCam")).equalsIgnoreCase("true") ? 1 : 0);
        }
        if (senz.getAttributes().containsKey("permLoc")) {
            values.put(SenzorsDbContract.PermissionConfiguration.COLUMN_NAME_LOCATION, ((String)senz.getAttributes().get("permLoc")).equalsIgnoreCase("true") ? 1 : 0);
        }
        values.put(SenzorsDbContract.PermissionConfiguration.COLOMN_NAME_USER, senz.getSender().getUsername());
        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.PermissionConfiguration.TABLE_NAME, null, values);
        //db.close();
    }






    public ArrayList<Secret> getAllSecretz(User sender, User receiver) {
        Log.i(TAG, "Get Secrets: getting all secret messages, sender - " + sender.getUsername() + ", receiver - " + receiver.getUsername());
        ArrayList<Secret> secretList = new ArrayList();
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query = "SELECT _id, text, image, sender, receiver " +
                "FROM secret WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY _id DESC";
        Cursor cursor = db.rawQuery(query,  new String[] {sender.getUsername(), receiver.getUsername(), receiver.getUsername(), sender.getUsername()});
        // secret attr
        String _secretText;
        String _secretImage;
        String _secretSender;
        String _secretReceiver;

        // extract attributes
        while (cursor.moveToNext()) {
            HashMap<String, String> chatzAttributes = new HashMap<>();

            // get chatz attributes
            //_chatzId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Chatz._ID));
            _secretText = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_TEXT));
            _secretImage = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLOMN_NAME_IMAGE));
            _secretSender = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_SENDER));
            _secretReceiver = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_RECEIVER));

            // create secret
            Secret secret = new Secret(_secretText, _secretImage, new User("", _secretSender), new User("", _secretReceiver));

            // fill secret list
            secretList.add(secret);
        }

        // clean
        cursor.close();
        //db.close();

        Log.d(TAG, "GetSecretz: secrets count " + secretList.size());
        return secretList;
    }







    /**
     * Get all chatz, two types of sensors here
     * 1. my sensors
     * 2. friends sensors
     *
     * @return sensor list
     */
    public ArrayList<Secret> getSecretz(User sender, User receiver) {
        Log.i(TAG, "Get Secrets: getting all secret messages, sender - " + sender.getUsername() + ", receiver - " + receiver.getUsername());
        ArrayList<Secret> secretList = new ArrayList();
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query = "SELECT _id, text, image, sender, receiver " +
                "FROM secret WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY _id DESC";
        Cursor cursor = db.rawQuery(query,  new String[] {sender.getUsername(), receiver.getUsername(), receiver.getUsername(), sender.getUsername()});
        // secret attr
        String _secretText;
        String _secretImage;
        String _secretSender;
        String _secretReceiver;

        // extract attributes
        while (cursor.moveToNext()) {
            HashMap<String, String> chatzAttributes = new HashMap<>();

            // get chatz attributes
            //_chatzId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Chatz._ID));
            _secretText = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_TEXT));
            _secretImage = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLOMN_NAME_IMAGE));
            _secretSender = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_SENDER));
            _secretReceiver = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_RECEIVER));

            // create secret
            Secret secret = new Secret(_secretText, _secretImage, new User("", _secretSender), new User("", _secretReceiver));

            // fill secret list
            secretList.add(secret);
        }

        // clean
        cursor.close();
        //db.close();

        Log.d(TAG, "GetSecretz: secrets count " + secretList.size());
        return secretList;
    }


    public ArrayList<Secret> getAllOtherSercets(User sender) {
        ArrayList<Secret> secretList = new ArrayList();
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query = "SELECT MAX(_id), text, image, sender, receiver " +
                "FROM secret WHERE sender != ? GROUP BY sender ORDER BY _id DESC";
        Cursor cursor = db.rawQuery(query,  new String[] {sender.getUsername()});
        // secret attr
        String _secretText;
        String _secretImage;
        String _secretSender;
        String _secretReceiver;

        // extract attributes
        while (cursor.moveToNext()) {
            HashMap<String, String> chatzAttributes = new HashMap<>();

            // get chatz attributes
            //_chatzId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Chatz._ID));
            _secretText = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_TEXT));
            _secretImage = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLOMN_NAME_IMAGE));
            _secretSender = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_SENDER));
            _secretReceiver = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Secret.COLUMN_NAME_RECEIVER));

            // create secret
            Secret secret = new Secret(_secretText, _secretImage, new User("", _secretSender), new User("", _secretReceiver));

            // fill secret list
            secretList.add(secret);
        }

        // clean
        cursor.close();
        //db.close();

        Log.d(TAG, "GetSecretz: secrets count " + secretList.size());
        return secretList;
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
            _senzId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Location._ID));
            _senzName = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Location.COLUMN_NAME_NAME));
            _senzValue = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Location.COLUMN_NAME_VALUE));

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
        //db.close();

        Log.d(TAG, "GetSensors: sensor count " + sensorList.size());
        return sensorList;
    }



    public UserPermission getUserAndPermission(User user){
        Log.d(TAG, "GetPermission: get single permission");
        UserPermission userPerm = null;

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // join query to retrieve data
        String query = "SELECT permission.camera, permission.location, user.image " +
                "FROM user " +
                "INNER JOIN permission " +
                "ON user.username = permission.user WHERE permission.user = ?";
        Cursor cursor = db.rawQuery(query,  new String[] {user.getUsername()});

        // user attributes
        String _userimage;
        boolean _location;
        boolean _camera;

        // extract attributes
        if (cursor.moveToFirst()) {
            // get permission attributes
            _camera = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA))== 1 ? true : false ;
            _location = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION)) == 1 ? true : false ;
            // create senz
            _userimage = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLOMN_NAME_IMAGE));
            user.setUserImage(_userimage);
            userPerm = new UserPermission(user, _camera, _location);
            // fill senz list
        }
        // clean
        cursor.close();
        //db.close();

        return userPerm;
    }



    public UserPermission getUserPermission(User user){
        Log.d(TAG, "GetPermission: get single permission");
        UserPermission userPerm = null;

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // join query to retrieve data
        String query = "SELECT permission.location, permission.camera " +
                "FROM permission " +
                "WHERE permission.user = ?";
        Cursor cursor = db.rawQuery(query,  new String[] {user.getUsername()});

        // user attributes
        String _username;
        boolean _location;
        boolean _camera;
        String _userId;

        // extract attributes
        if (cursor.moveToFirst()) {
            // get permission attributes
            _location = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION)) == 1 ? true : false ;
            _camera = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA))== 1 ? true : false ;
            // create senz
            userPerm = new UserPermission(user, _camera, _location);
            // fill senz list
        }
        // clean
        cursor.close();
        //db.close();

        return userPerm;
    }



    public UserPermission getUserConfigPermission(User user){
        Log.d(TAG, "GetPermission: get single permission");
        UserPermission userPerm = null;

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // join query to retrieve data
        String query = "SELECT permission_config.location, permission_config.camera " +
                "FROM permission_config " +
                "WHERE permission_config.user = ?";
        Cursor cursor = db.rawQuery(query,  new String[] {user.getUsername()});

        // user attributes
        String _username;
        boolean _location;
        boolean _camera;
        String _userId;

        // extract attributes
        if (cursor.moveToFirst()) {
            // get permission attributes
            _location = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION)) == 1 ? true : false ;
            _camera = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA))== 1 ? true : false ;
            // create senz
            userPerm = new UserPermission(user, _camera, _location);
            // fill senz list
        }
        // clean
        cursor.close();
        //db.close();

        return userPerm;
    }




    public List<UserPermission> getUsersAndTheirPermissions() {
        Log.d(TAG, "GetPermission: getting all permissions");
        List<UserPermission> permissionList = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // join query to retrieve data
        String query = "SELECT user._id, user.username, permission.location, permission.camera " +
                "FROM user " +
                "INNER JOIN permission " +
                "ON user.username = permission.user";
        Cursor cursor = db.rawQuery(query, null);

        // sensor/user attributes
        String _username;
        boolean _location;
        boolean _camera;
        String _userId;

        // extract attributes
        while (cursor.moveToNext()) {
            // get permission attributes
            _location = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION)) == 1 ? true : false ;
            _camera = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA))== 1 ? true : false ;
            // get user attributes
            _userId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            // create senz
            User user = new User(_userId, _username);
            UserPermission userPerm = new UserPermission(user, _camera, _location);
            // fill senz list
            permissionList.add(userPerm);
        }
        // clean
        cursor.close();
        //db.close();

        Log.d(TAG, "GetSensors: sensor count " + permissionList.size());
        return permissionList;
    }


    public List<UserPermission> getUsersAndTheirConfigurablePermissions() {
        Log.d(TAG, "GetSensors: getting all sensor");
        List<UserPermission> permissionList = new ArrayList();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // join query to retrieve data
        String query = "SELECT user._id, user.username, permission_config.location, permission_config.camera " +
                "FROM user " +
                "INNER JOIN permission_config " +
                "ON user.username = permission_config.user";
        Cursor cursor = db.rawQuery(query, null);

        // sensor/user attributes
        String _username;
        boolean _location;
        boolean _camera;
        String _userId;

        // extract attributes
        while (cursor.moveToNext()) {
            // get permission attributes
            _location = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_LOCATION)) == 1 ? true : false ;
            _camera = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Permission.COLUMN_NAME_CAMERA))== 1 ? true : false ;
            // get user attributes
            _userId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            // create senz
            User user = new User(_userId, _username);
            UserPermission userPerm = new UserPermission(user, _camera, _location);
            // fill senz list
            permissionList.add(userPerm);
        }
        // clean
        cursor.close();
        //db.close();

        Log.d(TAG, "GetSensors: sensor count " + permissionList.size());
        return permissionList;
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
        //db.close();

        Log.d(TAG, "user count " + userList.size());

        return userList;
    }

    /**
     * Delete chatz from database,
     *
     * @param
     */
    public void deleteChatz(Senz senz) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        /*db.delete(SenzorsDbContract.Chatz.TABLE_NAME,
                SenzorsDbContract.Chatz.COLUMN_NAME_USER + "=?" + " AND " +
                        SenzorsDbContract.Senz.COLUMN_NAME_NAME + "=?",
                new String[]{senz.getSender().getId(), senz.getAttributes().keySet().iterator().next()});*/
        //db.close();
    }

    /**
     * Delete senz from database,
     *
     * @param senz senz
     */
    public void deleteSenz(Senz senz) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete senz of given user
        db.delete(SenzorsDbContract.Location.TABLE_NAME,
                SenzorsDbContract.Location.COLUMN_NAME_USER + "=?" + " AND " +
                        SenzorsDbContract.Location.COLUMN_NAME_NAME + "=?",
                new String[]{senz.getSender().getId(), senz.getAttributes().keySet().iterator().next()});
        //db.close();
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
        //db.close();
        return image;

    }

}
