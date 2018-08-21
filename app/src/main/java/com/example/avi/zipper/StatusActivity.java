package com.example.avi.zipper;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

  private Toolbar mToolBar;
  private TextInputLayout mStatus;
  private Button mSaveButton;


  //Firebase
  private DatabaseReference mStatusDatabse;
  private FirebaseUser mCurrentUser;

  //Progress
  private ProgressDialog mProgress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_status);

    //Firebase
    mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    String current_uid = mCurrentUser.getUid();

    mStatusDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

    mToolBar = findViewById(R.id.status_appBar);
    setSupportActionBar(mToolBar);
    getSupportActionBar().setTitle("Account Status");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    String status_value = getIntent().getStringExtra("status_value");

    mStatus = findViewById(R.id.status_input);
    mSaveButton = findViewById(R.id.status_save_button);

    mStatus.getEditText().setText(status_value);

    mSaveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        //Progress
        mProgress = new ProgressDialog(StatusActivity.this);
        mProgress.setTitle("Saving Changes");
        mProgress.setMessage("Please wait while we save the changes.");
        mProgress.show();

        String status = mStatus.getEditText().getText().toString();

        mStatusDatabse.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {

            if (task.isSuccessful()) {
              mProgress.dismiss();
            } else {
              Toast.makeText(getApplicationContext(),"There was some error in saving changes.",Toast.LENGTH_LONG).show();
            }
            finish();

          }
        });


      }
    });

  }
}
