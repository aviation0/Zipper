package com.example.avi.zipper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

  private TextInputLayout mDisplayName;
  private TextInputLayout mEmail;
  private TextInputLayout mPassword;
  private Button mCreateButton;

  private Toolbar mToolbar;

  private DatabaseReference mDatabase;
  //ProgressDialog
  private ProgressDialog mRegProgress;

  //Firebase Auth
  private FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    //Toolbar Set
    mToolbar = findViewById(R.id.register_toolbar);
    setSupportActionBar(mToolbar);
    getSupportActionBar().setTitle("Create Account");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mRegProgress = new ProgressDialog(this);

    //Firebase Auth
    mAuth = FirebaseAuth.getInstance();

    //Android Fields
    mDisplayName = findViewById(R.id.reg_display_name);
    mEmail = findViewById(R.id.reg_email);
    mPassword = findViewById(R.id.reg_password);
    mCreateButton =findViewById(R.id.reg_create_btn);

    mCreateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String display_name = mDisplayName.getEditText().getText().toString();
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();

        if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

          mRegProgress.setTitle("Registering User");
          mRegProgress.setMessage("Please wait while we create your account !");
          mRegProgress.setCanceledOnTouchOutside(false);
          mRegProgress.show();
          register_user(display_name, email, password);
        }



      }
    });


  }

  private void register_user(final String display_name, final String email, String password) {

      mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {

          if (task.isSuccessful()) {

            //String current_user_id = mAuth.getCurrentUser().getUid();

            String deviceToken = FirebaseInstanceId.getInstance().getToken();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String uid = currentUser.getUid();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);



            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("device_token",deviceToken);
            userMap.put("name", display_name);
            userMap.put("status", "Hi there, I'm using Zipper.");
            userMap.put("image", "default");
            userMap.put("thumb_image", "default");

            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                  mRegProgress.dismiss();

                  Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                  mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                  startActivity(mainIntent);
                  finish();

                }
              }
            });


          } else {

            mRegProgress.hide();
            Toast.makeText(RegisterActivity.this, "Cannot Sign in. Please check the form and try again", Toast.LENGTH_LONG).show();

          }

        }
      });

  }

}
