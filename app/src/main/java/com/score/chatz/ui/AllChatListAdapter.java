package com.score.chatz.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.pojo.Secret;
import com.score.chatz.utils.CameraUtils;
import com.score.chatz.utils.PreferenceUtils;
import com.score.senzc.pojos.User;

import java.util.ArrayList;

/**
 * Created by lakmalcaldera on 8/19/16.
 */
public class AllChatListAdapter extends ArrayAdapter<Secret> {

    private static final String TAG = ChatFragmentListAdapter.class.getName();
    Context context;
    ArrayList<Secret> userSecretList;
    static final int TEXT_MESSAGE = 0;
    static final int IMAGE_MESSAGE = 1;
    static User currentUser;
    private LayoutInflater mInflater;

    public AllChatListAdapter(Context _context, ArrayList<Secret> secretList) {
        super(_context, R.layout.single_user_card_row, R.id.user_name, secretList);
        context = _context;
        userSecretList = secretList;
        try {
            currentUser = PreferenceUtils.getUser(getContext());
        }catch (NoUserException e) {
            e.printStackTrace();
        }
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        //Log.i(TAG, "WHO IS SENDER: " + ((Secret)getItem(position)).getSender().getUsername() + ", currentUser: " + currentUser.getUsername());
        if(((Secret)getItem(position)).getImage() == null){
            return TEXT_MESSAGE;
        }else {
            return IMAGE_MESSAGE;
        }
    }


    /**
     * Create list row viewv
     *
     * @param i         index
     * @param view      current list item view
     * @param viewGroup parent
     * @return view
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;
        final Secret secret = (Secret) getItem(i);
        int type = getItemViewType(i);
        //Log.i("SECRETS" ,"Secret : Text - " + secret.getText() + ", Sender - " + secret.getSender().getUsername() + ", Receiver - " + secret.getReceiver().getUsername());
        if (view == null || (view != null && ((ViewHolder) view.getTag()).messageType != type)) {
            //inflate sensor list row layout
            //create view holder to store reference to child views
            holder = new ViewHolder();
            switch (type) {
                case IMAGE_MESSAGE:
                    view = mInflater.inflate(R.layout.rahas_image_row_layout, viewGroup, false);
                    holder.image = (ImageView) view.findViewById(R.id.image);
                    holder.sender = (TextView) view.findViewById(R.id.sender);
                    holder.userImage = (com.github.siyamed.shapeimageview.CircularImageView) view.findViewById(R.id.user_image);
                    holder.messageType = IMAGE_MESSAGE;
                    break;
                case TEXT_MESSAGE:
                    view = mInflater.inflate(R.layout.rahas_row_layout, viewGroup, false);
                    holder.message = (TextView) view.findViewById(R.id.message);
                    holder.sender = (TextView) view.findViewById(R.id.sender);
                    holder.userImage = (com.github.siyamed.shapeimageview.CircularImageView) view.findViewById(R.id.user_image);
                    holder.messageType = TEXT_MESSAGE;
                    break;
            }
            view.setTag(holder);
        } else {
            //get view holder back_icon
            holder = (ViewHolder) view.getTag();
        }
        setUpRow(i, secret, view, holder);
        return view;
    }

    private void setUpRow(int i, Secret secret, View view, ViewHolder viewHolder) {
        // enable share and change color of view
        viewHolder.sender.setText(secret.getSender().getUsername());
        if (viewHolder.messageType == TEXT_MESSAGE){
            viewHolder.message.setText(secret.getText());
        }else{
            if(secret.getImage() != null) {
                Log.i(TAG, "IMAGE YOOOO : " + secret.getImage().getBytes());
                Log.i(TAG, "IMAGE YOOOO2222 : " + CameraUtils.getRotatedImage(CameraUtils.getBitmapFromBytes(secret.getImage().getBytes()), -90));
                viewHolder.image.setImageBitmap(CameraUtils.getRotatedImage(CameraUtils.getBitmapFromBytes(secret.getImage().getBytes()), -90));
            }
        }

        //Extracting user image
        if(secret.getSender().getUserImage() != null) {
            Log.i(TAG, "IMAGE YOOOO : " + secret.getSender().getUserImage().getBytes());
            Log.i(TAG, "IMAGE YOOOO2222 : " + CameraUtils.getRotatedImage(CameraUtils.getBitmapFromBytes(secret.getSender().getUserImage().getBytes()), -90));
            viewHolder.userImage.setImageBitmap(CameraUtils.getRotatedImage(CameraUtils.getBitmapFromBytes(secret.getSender().getUserImage().getBytes()), -90));
        }

        //User name
        viewHolder.sender.setText(secret.getSender().getUsername());
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView message;
        TextView sender;
        Integer messageType;
        ImageView image;
        com.github.siyamed.shapeimageview.CircularImageView userImage;

    }
}
