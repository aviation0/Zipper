package com.example.avi.zipper;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
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

public class ProfileActivity extends AppCompatActivity {

  private ImageView mProfileImage;
  private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
  private Button mProfleSendRequestBtn;

  private DatabaseReference mUsersDatabase;

  private ProgressDialog mProgressDialog;

  private DatabaseReference mFriendReqDatabse;

  private DatabaseReference mFriendDatabase;

  private FirebaseUser mCurrent_user;

  private String mCurrent_state;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    final String user_id = getIntent().getStringExtra("user_id");

    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
    mFriendReqDatabse = FirebaseDatabase.getInstance().getReference().child("Friend_req");
    mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
    mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

    mProfileImage = findViewById(R.id.profile_image);
    mProfileName = findViewById(R.id.profile_displayName);
    mProfileStatus = findViewById(R.id.profile_status);
    mProfleSendRequestBtn = findViewById(R.id.profile_send_req_btn);


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

        mFriendReqDatabse.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.hasChild(user_id)){

              String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
              if(request_type.equals("received")){

                mCurrent_state = "req_received";
                mProfleSendRequestBtn.setText("Accept Friend Request");

              } else if (request_type.equals("sent")) {

                mCurrent_state = "req_sent";
                mProfleSendRequestBtn.setText("Cancel Friend Request");

              }

            }

            mProgressDialog.dismiss();
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

          mFriendReqDatabse.child(mCurrent_user.getUid()).child(user_id).child("request_type")
                  .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

              if(task.isSuccessful()) {

                mFriendReqDatabse.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {


                    mCurrent_state = "req_sent";
                    mProfleSendRequestBtn.setText("Cancel Friend Request");

                    //Toast.makeText(ProfileActivity.this,"Request Sent Successfully",Toast.LENGTH_LONG).show();

                  }
                });

              } else {
                Toast.makeText(ProfileActivity.this,"Failed Sending Request",Toast.LENGTH_LONG).show();
              }

              mProfleSendRequestBtn.setEnabled(true);

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

                }
              });

            }
          });

        }

        //---------------------- REQUEST RECEIVED STATE --------------------

        if(mCurrent_state.equals("req_received")){

          final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
          mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

              mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                  mFriendReqDatabse.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                      mFriendReqDatabse.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                          mProfleSendRequestBtn.setEnabled(true);
                          mCurrent_state = "friends";
                          mProfleSendRequestBtn.setText("Unfriend this person");

                        }
                      });

                    }
                  });

                }
              });

            }
          });


        }

      }
    });

  }
}
