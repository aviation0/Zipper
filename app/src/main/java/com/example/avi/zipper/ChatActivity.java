package com.example.avi.zipper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

  private String mChatUser;

  private Toolbar mChatToolbar;

  private DatabaseReference mRootRef;

  private TextView mTitleView;
  private TextView mLastSeenView;
  private CircleImageView mProfileImage;
  private FirebaseAuth mAuth;
  private DatabaseReference mUserRef;
  private String mCurrentUserId;

  private ImageButton mChatAddBtn;
  private ImageButton mChatSendBtn;
  private EditText mChatMessageView;

  private RecyclerView mMessagesList;
  //private SwipeRefreshLayout mRefreshLayout;

  private final List<Messages> messagesList = new ArrayList<>();
  private LinearLayoutManager mLinearLayout;

  private MessageAdapter mAdapter;

  private static final int TOTAL_ITEMS_TO_LOAD = 10;
  private int mCurrentPage = 1;

  private int itemPosition = 0;
  private String mLastKey = "";
  private String mPrevKey = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    mChatToolbar = findViewById(R.id.chat_app_bar);
    setSupportActionBar(mChatToolbar);

    ActionBar actionBar = getSupportActionBar();

    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setDisplayShowCustomEnabled(true);

    mRootRef = FirebaseDatabase.getInstance().getReference();
    mAuth = FirebaseAuth.getInstance();
    mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
    mCurrentUserId = mAuth.getCurrentUser().getUid();

    mChatUser = getIntent().getStringExtra("user_id");
    String userName = getIntent().getStringExtra("user_name");

    getSupportActionBar().setTitle(userName);

    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
    View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

    actionBar.setCustomView(action_bar_view);

    //----------Custom action bar items------------
    mTitleView = findViewById(R.id.custom_bar_title);
    mLastSeenView = findViewById(R.id.custom_bar_seen);
    mProfileImage = findViewById(R.id.custom_bar_image);

    mChatAddBtn = findViewById(R.id.chat_add_button);
    mChatSendBtn = findViewById(R.id.chat_send_button);
    mChatMessageView = findViewById(R.id.chat_message_view);

    mAdapter = new MessageAdapter(messagesList);

    mMessagesList = findViewById(R.id.message_list);
    //mRefreshLayout = findViewById(R.id.message_swipe_layout);

    mLinearLayout = new LinearLayoutManager(this);

    mMessagesList.setHasFixedSize(true);
    mMessagesList.setLayoutManager(mLinearLayout);

    mMessagesList.setAdapter(mAdapter);

    loadMessages();


    mTitleView.setText(userName);

    mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        String online = dataSnapshot.child("online").getValue().toString();
        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

        CircleImageView userImage = findViewById(R.id.custom_bar_image);
        Picasso.get().load(thumb_image).placeholder(R.drawable.boy).into(userImage);

        if(online.equals("true")) {

          mLastSeenView.setText("Online");

        } else {

          GetTimeAgo getTimeAgo = new GetTimeAgo();

          long lastTime = Long.parseLong(online);

          String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());

          mLastSeenView.setText(lastSeenTime);
        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        if(!dataSnapshot.hasChild(mChatUser)){

          Map chatAddMap = new HashMap();
          chatAddMap.put("seen",false);
          chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

          Map chatUserMap = new HashMap();
          chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
          chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

          mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

              if(databaseError != null){

                Log.d("CHAT_LOG",databaseError.getMessage().toString());

              }

            }
          });

        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

    //--------------send button--------------

    mChatSendBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        sendMessage();

      }
    });


    /*
    mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {

        mCurrentPage++;

        itemPosition = 0;

        loadMoreMessages();

      }
    });
    */


  }


  /*
  private void loadMoreMessages() {

    DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

    Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

    messageQuery.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


        Messages message = dataSnapshot.getValue(Messages.class);
        String messageKey = dataSnapshot.getKey();
        messagesList.add(itemPosition++,message);

        if(mPrevKey.equals(messageKey)) {

          messagesList.add(itemPosition++, message);

        } else {

          mPrevKey = mLastKey;

        }

        if(itemPosition == 1) {

          mLastKey = messageKey;
        }

        mAdapter.notifyDataSetChanged();

        mRefreshLayout.setRefreshing(false);

        mLinearLayout.scrollToPositionWithOffset(10,0);

      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

  }*/


  private void loadMessages() {

    DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

    Query messageQuery = messageRef;//.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


    messageQuery.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        Messages message = dataSnapshot.getValue(Messages.class);

        itemPosition ++;

        if(itemPosition == 1){
          String messageKey = dataSnapshot.getKey();

          mLastKey = messageKey;
          mPrevKey = messageKey;
        }

        messagesList.add(message);
        mAdapter.notifyDataSetChanged();

        mMessagesList.scrollToPosition(messagesList.size()-1);

        //mRefreshLayout.setRefreshing(false);

      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });

  }

  private void sendMessage() {

    String message = mChatMessageView.getText().toString();

    if(!TextUtils.isEmpty(message)){

      String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
      String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;


      DatabaseReference user_message_push = mRootRef.child("messages")
              .child(mCurrentUserId).child(mChatUser).push();

      String push_id = user_message_push.getKey();

      Map messageMap = new HashMap();
      messageMap.put("message", message);
      messageMap.put("seen", false);
      messageMap.put("type", "text");
      messageMap.put("time", ServerValue.TIMESTAMP);
      messageMap.put("from", mCurrentUserId);

      Map messageUserMap = new HashMap();
      messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
      messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

      mChatMessageView.setText("");

      mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
        @Override
        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

          if(databaseError != null){

            Log.d("CHAT_LOG",databaseError.getMessage().toString());

          }

        }
      });

    }

  }

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
  protected void onPause() {
    super.onPause();

    mUserRef.child("online").setValue(false);
  }

}
