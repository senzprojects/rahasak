package com.score.chatz.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.siyamed.shapeimageview.CircularImageView;

import com.score.chatz.R;
import com.score.chatz.pojo.UserPermission;

import java.util.ArrayList;

/**
 * Created by Lakmal on 8/6/16.
 */
    class UserPermissionListAdapter extends ArrayAdapter<UserPermission> {
        Context context;
        ArrayList<UserPermission> userPermissionList;

        public UserPermissionListAdapter(Context _context, ArrayList<UserPermission> userPermsList) {
            super(_context, R.layout.single_user_card_row, R.id.user_name, userPermsList);
            context = _context;
            userPermissionList = userPermsList;
        }


        /**
         * Create list row view
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

            final UserPermission userPerm = (UserPermission) getItem(i);

            if (view == null) {
                //inflate sensor list row layout
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.single_user_card_row, viewGroup, false);

                //create view holder to store reference to child views
                holder = new ViewHolder();
                holder.userImageView = (CircularImageView) view.findViewById(R.id.user_image);
                holder.usernameView = (TextView) view.findViewById(R.id.user_name);
                holder.userLocationPermView = (ImageView) view.findViewById(R.id.perm_locations);
                holder.userCameraPermView = (ImageView) view.findViewById(R.id.perm_camera);

                view.setTag(holder);
            } else {
                //get view holder back_icon
                holder = (ViewHolder) view.getTag();
            }

            setUpRow(i, userPerm, view, holder);

            return view;
        }

        private void setUpRow(int i, UserPermission userPerm, View view, ViewHolder viewHolder) {
            // enable share and change color of view
            viewHolder.usernameView.setText(userPerm.getUser().getUsername());

            if(userPerm.getCamPerm() == true){
                viewHolder.userCameraPermView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.perm_camera_active, null));
            }else{
                viewHolder.userCameraPermView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.perm_camera_deactive, null));
            }

            if(userPerm.getLocPerm() == true){
                viewHolder.userLocationPermView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.perm_locations_active, null));
            }else{
                viewHolder.userLocationPermView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.perm_locations_deactive, null));
            }
            viewHolder.userImageView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.default_user, null));
            //Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_user);
            //viewHolder.userImageView.setImageBitmap(largeIcon);
        }

        /**
         * Keep reference to children view to avoid unnecessary calls
         */
        static class ViewHolder {
            CircularImageView userImageView;
            TextView usernameView;
            ImageView userCameraPermView;
            ImageView userLocationPermView;
        }



    }

