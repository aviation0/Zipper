package com.example.avi.zipper;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class RequestsFragment extends Fragment {

  private Button mAcceptBtn, mDeclineBtn;

  private RecyclerView myRequestList;

  private View myMainView;

  private DatabaseReference mFriendRequestDatabase;

  private DatabaseReference mFriendsDatabase;

  private DatabaseReference mUsersDatabase;

  private FirebaseAuth mAuth;

  String online_user_id;


  public RequestsFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    myMainView = inflater.inflate(R.layout.fragment_requests, container, false);

    myRequestList = myMainView.findViewById(R.id.request_list);

    mAuth = FirebaseAuth.getInstance();

    online_user_id = mAuth.getCurrentUser().getUid();

    mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(online_user_id);

    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

    myRequestList.setHasFixedSize(true);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
    linearLayoutManager.setReverseLayout(true);
    linearLayoutManager.setStackFromEnd(true);

    myRequestList.setLayoutManager(linearLayoutManager);

    // Inflate the layout for this fragment
    return myMainView;
  }

  @Override
  public void onStart() {
    super.onStart();

    final FirebaseRecyclerAdapter<Request,RequestViewHolder> firebaseRecyclerAdapter
            = new FirebaseRecyclerAdapter<Request, RequestViewHolder>
            (
                    Request.class,
                    R.layout.request_single_layout,
                    RequestViewHolder.class,
                    mFriendRequestDatabase
    ) {
      @Override
      protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, final int position) {

        final String list_users_id = getRef(position).getKey();


        //add buttons
        mAcceptBtn = viewHolder.mView.findViewById(R.id.request_accept_btn);
        mDeclineBtn = viewHolder.mView.findViewById(R.id.request_decline_btn);


        DatabaseReference get_type_ref = getRef(position).child("request_type").getRef();

        get_type_ref.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.exists()) {

              String request_type = dataSnapshot.getValue().toString();

              if(request_type.equals("received")) {

                mUsersDatabase.child(list_users_id).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final String userName = dataSnapshot.child("name").getValue().toString();
                    final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();


                    viewHolder.setUserName(userName);
                    viewHolder.setThumbImage(userThumb, getContext());
                    viewHolder.setAcceptBtn("Confirm");



                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
                });

              } else {

                mUsersDatabase.child(list_users_id).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final String userName = dataSnapshot.child("name").getValue().toString();
                    final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                    viewHolder.setUserName(userName);
                    viewHolder.setThumbImage(userThumb, getContext());
                    viewHolder.setAcceptBtn("Sent  " + "\u2713");



                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
                });

              }

            }

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
        });

      }

      @Override
      public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {



        return super.onCreateViewHolder(parent, viewType);
      }
    };
    myRequestList.setAdapter(firebaseRecyclerAdapter);

  }

  public static class RequestViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public RequestViewHolder(View itemView) {
      super(itemView);

      mView = itemView;

    }



    public void setUserName(String userName) {

      TextView userNameDisplay = mView.findViewById(R.id.request_profile_name);
      userNameDisplay.setText(userName);

    }

    public void setThumbImage(String userThumb, Context context) {

      CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.request_profile_image);
      Picasso.get().load(userThumb).placeholder(R.drawable.boy).into(userImageView);

    }

    public void setAcceptBtn(String acceptBtn) {
      Button acceptButton = mView.findViewById(R.id.request_accept_btn);
      acceptButton.setText(acceptBtn);
    }
  }


}
