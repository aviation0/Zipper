package com.example.avi.zipper;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

  private RecyclerView mFriendsList;
  private DatabaseReference mFriendsDatabase;
  private DatabaseReference mUsersDatabase;
  private FirebaseAuth mAuth;
  private String mCurrent_user_id;
  private View mMainView;

  private Dialog myDialog;


  public FriendsFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

    mFriendsList = mMainView.findViewById(R.id.friends_list);

    mAuth = FirebaseAuth.getInstance();

    mCurrent_user_id = mAuth.getCurrentUser().getUid();

    mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
    mFriendsDatabase.keepSynced(true);
    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    mUsersDatabase.keepSynced(true);

    mFriendsList.setHasFixedSize(true);
    mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

    // Inflate the layout for this fragment
    return mMainView;
  }

  @Override
  public void onStart() {
    super.onStart();

    FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter =
            new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                    Friends.class,
                    R.layout.users_single_layout,
                    FriendsViewHolder.class,
                    mFriendsDatabase
            ) {
              @Override
              protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());


                final String list_user_id = getRef(position).getKey();



                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final String userName = dataSnapshot.child("name").getValue().toString();
                    final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                    final String userStatus = dataSnapshot.child("status").getValue().toString();

                    if(dataSnapshot.hasChild("online")){
                      String userOnline = dataSnapshot.child("online").getValue().toString();
                      viewHolder.setUserOnline(userOnline);
                    }

                    viewHolder.setName(userName);
                    viewHolder.setUserImage(userThumb, getContext());

                    myDialog = new Dialog(getContext());
                    myDialog.setContentView(R.layout.dialog_friend);
                    myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    myDialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlide;

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {

                        TextView dialog_name = myDialog.findViewById(R.id.dialog_name_id);
                        TextView dialog_status = myDialog.findViewById(R.id.dialog_status_id);
                        ImageView dialog_image = myDialog.findViewById(R.id.dialog_image);
                        dialog_name.setText(userName);
                        dialog_status.setText(userStatus);
                        Picasso.get().load(userThumb).placeholder(R.drawable.boy).into(dialog_image);

                        myDialog.show();

                        Button profileButton = myDialog.findViewById(R.id.dialog_btn_profile);
                        Button chatButton = myDialog.findViewById(R.id.dialog_btn_chat);

                        profileButton.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                            Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                            profileIntent.putExtra("user_id",list_user_id);
                            startActivity(profileIntent);
                          }
                        });

                        chatButton.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                            chatIntent.putExtra("user_id",list_user_id);
                            chatIntent.putExtra("user_name",userName);
                            startActivity(chatIntent);
                          }
                        });

                      }
                    });


                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
                });

              }
            };
    mFriendsList.setAdapter(firebaseRecyclerAdapter);

  }
  

  public static class FriendsViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public FriendsViewHolder(View itemView) {
      super(itemView);
      mView = itemView;
    }

    public void setDate(String date){

      TextView userStatusView = mView.findViewById(R.id.user_single_status);
      userStatusView.setText(date);

    }

    public void setName(String name){

      TextView userNameView = mView.findViewById(R.id.user_single_name);
      userNameView.setText(name);

    }

    public void setUserImage(String thumb_image,Context ctx) {

      CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
      Picasso.get().load(thumb_image).placeholder(R.drawable.boy).into(userImageView);

    }

    public void setUserOnline(String online_status){

      ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);

      if(online_status.equals("true")){
        userOnlineView.setVisibility(View.VISIBLE);
      } else {
        userOnlineView.setVisibility(View.INVISIBLE);
      }

    }


  }

}
