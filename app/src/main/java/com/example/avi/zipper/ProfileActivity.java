package com.example.avi.zipper;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

  private ImageView mProfileImage;
  private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
  private Button mProfleSendRequestBtn, mDeclineBtn;

  private DatabaseReference mUsersDatabase;
  private FirebaseAuth mAuth;
  private DatabaseReference mUserRef;

  private ProgressDialog mProgressDialog;

  private DatabaseReference mFriendReqDatabse;
  private DatabaseReference mFriendDatabase;
  private DatabaseReference mNotificationDatabase;

  private DatabaseReference mRootRef;

  private FirebaseUser mCurrent_user;

  private String mCurrent_state;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    final String user_id = getIntent().getStringExtra("user_id");

    mRootRef = FirebaseDatabase.getInstance().getReference();

    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
    mFriendReqDatabse = FirebaseDatabase.getInstance().getReference().child("Friend_req");
    mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
    mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
    mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
    mAuth = FirebaseAuth.getInstance();
    mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


    mProfileImage = findViewById(R.id.profile_image);
    mProfileName = findViewById(R.id.profile_displayName);
    mProfileStatus = findViewById(R.id.profile_status);
    mProfleSendRequestBtn = findViewById(R.id.profile_send_req_btn);
    mDeclineBtn = findViewById(R.id.profile_decline_btn);


    mCurrent_state = "not_friends";


    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle("Loading User Data");
    mProgressDialog.setMessage("Please while we load the user data.");
    mProgressDialog.setCanceledOnTouchOutside(false);
    mProgressDialog.show();


    mUsersDatabase.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        String display_name = dataSnapshot.child("name").getValue().toString();
        String status = dataSnapshot.child("status").getValue().toString();
        String image = dataSnapshot.child("image").getValue().toString();

        mProfileName.setText(display_name);
        mProfileStatus.setText(status);

        Picasso.get().load(image).placeholder(R.drawable.boy).into(mProfileImage);

        //---------------------- FRIENDS LIST / REQUEST FEATURE --------------------

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mFriendReqDatabse.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.hasChild(user_id)){

              String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
              if(request_type.equals("received")){

                mCurrent_state = "req_received";
                mProfleSendRequestBtn.setText("Accept Friend Request");

                mDeclineBtn.setVisibility(View.VISIBLE);
                mDeclineBtn.setEnabled(true);

              } else if (request_type.equals("sent")) {

                mCurrent_state = "req_sent";
                mProfleSendRequestBtn.setText("Cancel Friend Request");

                mDeclineBtn.setVisibility(View.INVISIBLE);
                mDeclineBtn.setEnabled(false);
              }

              mProgressDialog.dismiss();

            } else {

              mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                  if (dataSnapshot.hasChild(user_id)){

                    mCurrent_state = "friends";
                    mProfleSendRequestBtn.setText("Unfriend this person");

                    mDeclineBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setEnabled(false);

                  }

                  mProgressDialog.dismiss();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                  mProgressDialog.dismiss();

                }
              });

            }

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
        });

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    mProfleSendRequestBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        mProfleSendRequestBtn.setEnabled(false);

        //---------------------- NOT FRIENDS STATE --------------------

        if(mCurrent_state.equals("not_friends")) {

          DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
          String newNotificationId = newNotificationRef.getKey();

          HashMap<String,String> notificationData = new HashMap<>();
          notificationData.put("from",mCurrent_user.getUid());
          notificationData.put("type","request");

          Map requestMap = new HashMap();
          requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type","sent");
          requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type","received");
          requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

          mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

              if(databaseError != null){

                Toast.makeText(ProfileActivity.this,"There was some error in sending request",Toast.LENGTH_SHORT).show();

              }
              mProfleSendRequestBtn.setEnabled(true);

              mCurrent_state = "req_sent";
              mProfleSendRequestBtn.setText("Cancel Friend Request");
              //mDeclineBtn.setVisibility(View.INVISIBLE);
              //mDeclineBtn.setEnabled(false);

            }
          });

        }

        //---------------------- CANCEL REQUEST STATE --------------------

        if(mCurrent_state.equals("req_sent")){

          mFriendReqDatabse.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

              mFriendReqDatabse.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                  mProfleSendRequestBtn.setEnabled(true);
                  mCurrent_state = "not_friends";
                  mProfleSendRequestBtn.setText("Send Friend Request");

                  mDeclineBtn.setVisibility(View.INVISIBLE);
                  mDeclineBtn.setEnabled(false);

                }
              });

            }
          });

        }

        //---------------------- REQUEST RECEIVED STATE --------------------

        if(mCurrent_state.equals("req_received")){

          final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
          Map friendsMap = new HashMap();
          friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
          friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

          friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
          friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

          mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

              if (databaseError == null){

                mProfleSendRequestBtn.setEnabled(true);
                mCurrent_state = "friends";
                mProfleSendRequestBtn.setText("Unfriend this person");

                mDeclineBtn.setVisibility(View.INVISIBLE);
                mDeclineBtn.setEnabled(false);

              } else {

                String error = databaseError.getMessage();

                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

              }

            }
          });

        }

        //---------------------- FRIENDS STATE --------------------

        if(mCurrent_state.equals("friends")){

          Map unfriendMap = new HashMap();
          unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
          unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);


          mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

              if (databaseError == null){

                mCurrent_state = "not_friends";
                mProfleSendRequestBtn.setText("Send Friend Request");

                mDeclineBtn.setVisibility(View.INVISIBLE);
                mDeclineBtn.setEnabled(false);

              } else {

                String error = databaseError.getMessage();

                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

              }
              mProfleSendRequestBtn.setEnabled(true);

            }
          });

        }

      }
    });

    mDeclineBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {


        if(mCurrent_state.equals("req_received")){

          Map unfriendMap = new HashMap();
          unfriendMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
          unfriendMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


          mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

              if (databaseError == null){

                mCurrent_state = "not_friends";
                mProfleSendRequestBtn.setText("Send Friend Request");

                mDeclineBtn.setVisibility(View.INVISIBLE);
                mDeclineBtn.setEnabled(false);

              } else {

                String error = databaseError.getMessage();

                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

              }
              mProfleSendRequestBtn.setEnabled(true);

            }
          });

        }

      }
    });

  }
  /*
  @Override
  public void onStart() {
    super.onStart();
    // Check if user is signed in (non-null) and update UI accordingly.
    FirebaseUser currentUser = mAuth.getCurrentUser();

    if(currentUser == null) {

    } else {

      mUserRef.child("online").setValue(true);

    }

  }

  @Override
  protected void onStop() {
    super.onStop();

    mUserRef.child("online").setValue(false);

  }
  */

}
