package com.score.chatz.ui;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.UserPermission;
import com.score.chatz.utils.PreferenceUtils;
import com.score.senzc.pojos.User;

import java.util.ArrayList;

/**
 * Created by Lakmal on 8/9/16.
 */
public class ChatFragmentListAdapter extends ArrayAdapter<Secret> {
    Context context;
    ArrayList<Secret> userSecretList;
    static final int MY_MESSAGE_TYPE = 0;
    static final int NOT_MY_MESSAGE_TYPE = 1;
    static User currentUser;
    private LayoutInflater mInflater;

    public ChatFragmentListAdapter(Context _context, ArrayList<Secret> secretList) {
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
        if(((Secret)getItem(position)).getSender().getUsername().equalsIgnoreCase(currentUser.getUsername())){
            return MY_MESSAGE_TYPE;
        }else{
            return NOT_MY_MESSAGE_TYPE;
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

        if (view == null) {
            //inflate sensor list row layout
            //create view holder to store reference to child views
            holder = new ViewHolder();
            switch (type) {
                case MY_MESSAGE_TYPE:
                    view = mInflater.inflate(R.layout.my_message_layout, viewGroup, false);
                    holder.message = (TextView) view.findViewById(R.id.message);
                    holder.sender = (TextView) view.findViewById(R.id.sender);
                    break;
                case NOT_MY_MESSAGE_TYPE:
                    view = mInflater.inflate(R.layout.not_my_message_layout, viewGroup, false);
                    holder.message = (TextView) view.findViewById(R.id.message);
                    holder.sender = (TextView) view.findViewById(R.id.sender);
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
        viewHolder.message.setText(secret.getText());
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView message;
        TextView sender;
    }
}
