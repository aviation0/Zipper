package com.example.avi.zipper;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

  private TextInputLayout mDisplayName;
  private TextInputLayout mEmail;
  private TextInputLayout mPassword;
  private Button mCreateButton;

  //Firebase Auth
  private FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    //Firebase Auth
    mAuth = FirebaseAuth.getInstance();

    //Android Fields
    mDisplayName = findViewById(R.id.req_display_name);
    mEmail = findViewById(R.id.reg_email);
    mPassword = findViewById(R.id.reg_password);
    mCreateButton =findViewById(R.id.reg_create_btn);

    mCreateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String display_name = mDisplayName.getEditText().getText().toString();
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();

        register_user(display_name, email, password);

      }
    });


  }

  private void register_user(String display_name, String email, String password) {

    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
      @Override
      public void onComplete(@NonNull Task<AuthResult> task) {

        if(task.isSuccessful()){

          Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
          startActivity(mainIntent);
          finish();

        } else {

          Toast.makeText(RegisterActivity.this,"You got some error.",Toast.LENGTH_LONG).show();

        }

      }
    });

  }
}
