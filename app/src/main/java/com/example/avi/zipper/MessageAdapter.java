package com.example.avi.zipper;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

  private List<Messages> mMessageList;
  private FirebaseAuth mAuth;

  public MessageAdapter(List<Messages> mMessageList) {

    this.mMessageList = mMessageList;
  }

  @Override
  public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.messages_single_layout, parent, false);

    return new MessageViewHolder(v);

  }

  public class MessageViewHolder extends RecyclerView.ViewHolder {

    public TextView messageText;
    public CircleImageView profileImage;

    public MessageViewHolder(View view) {
      super(view);

      messageText = view.findViewById(R.id.message_text_layout);

    }

  }

  @Override
  public int getItemViewType(int position) {

    return position%2*2;
  }


  @Override
  public void onBindViewHolder(MessageViewHolder viewHolder, int i) {

    mAuth = FirebaseAuth.getInstance();

    String current_user_id = mAuth.getCurrentUser().getUid();

    Messages c = mMessageList.get(i);

    String from_user = c.getFrom();

    if(from_user.equals(current_user_id)){

      viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background_2);
      //viewHolder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
      viewHolder.messageText.setTextColor(Color.BLACK);

    } else {

      viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
      //viewHolder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
      viewHolder.messageText.setTextColor(Color.WHITE);

    }

    viewHolder.messageText.setText(c.getMessage());

  }

  @Override
  public int getItemCount() {
    return mMessageList.size();
  }


}
