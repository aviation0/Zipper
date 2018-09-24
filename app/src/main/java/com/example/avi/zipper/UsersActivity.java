package com.example.avi.zipper;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

  private Toolbar mToolbar;
  private RecyclerView mUsersList;
  private DatabaseReference mUsersDatabase;

  private FirebaseAuth mAuth;
  private DatabaseReference mUserRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_users);


    mToolbar = findViewById(R.id.users_appBar);
    setSupportActionBar(mToolbar);
    getSupportActionBar().setTitle("All Users");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mAuth = FirebaseAuth.getInstance();
    mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    mUsersList = findViewById(R.id.users_list);
    mUsersList.setHasFixedSize(true);
    mUsersList.setLayoutManager(new LinearLayoutManager(this));


  }

  @Override
  protected void onStart() {
    super.onStart();

    // Check if user is signed in (non-null) and update UI accordingly.
    FirebaseUser currentUser = mAuth.getCurrentUser();

    if(currentUser == null) {

    } else {

      mUserRef.child("online").setValue(true);

    }

    FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter =
            new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                    Users.class,
                    R.layout.users_single_layout,
                    UsersViewHolder.class,
                    mUsersDatabase
            ) {
              @Override
              protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setDisplayName(model.getName());
                viewHolder.setUserStatus(model.getStatus());
                viewHolder.setUserImage(model.getThumb_image(),getApplicationContext());

                final String user_id = getRef(position).getKey();// key of every single element in recycler view

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                    Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                    profileIntent.putExtra("user_id",user_id);
                    startActivity(profileIntent);

                  }
                });

              }
            };

    mUsersList.setAdapter(firebaseRecyclerAdapter);
  }

  public static  class UsersViewHolder extends RecyclerView.ViewHolder {

    private View mView;

    public UsersViewHolder(View itemView) {
      super(itemView);

      mView = itemView;
    }

    public void setDisplayName(String name) {

      TextView userNameView = mView.findViewById(R.id.user_single_name);
      userNameView.setText(name);

    }

    public void setUserStatus(String status) {

      TextView userStatusView = mView.findViewById(R.id.user_single_status);
      userStatusView.setText(status);

    }

    public void setUserImage(String thumb_image,Context ctx) {

      CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
      Picasso.get().load(thumb_image).placeholder(R.drawable.boy).into(userImageView);
      
    }

  }



  @Override
  protected void onPause() {
    super.onPause();

    mUserRef.child("online").setValue(false);
  }

}


