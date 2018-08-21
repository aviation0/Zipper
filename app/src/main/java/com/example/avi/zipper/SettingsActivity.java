package com.example.avi.zipper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

  private DatabaseReference mUserDatabse;
  private FirebaseUser mCurrentUser;

  //Android Layout

  private CircleImageView mImage;
  private TextView mDisplayName;
  private TextView mStatus;

  private Button mStatusButton;
  private Button mImageButton;

  private static final int GALLEY_PICK = 1;

  private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

  //Storage Firebase
  private StorageReference mImageStorage;

  private ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    mImage = findViewById(R.id.settings_image);
    mDisplayName = findViewById(R.id.settings_display_name);
    mStatus = findViewById(R.id.settings_status);
    mStatusButton = findViewById(R.id.settings_status_button);
    mImageButton = findViewById(R.id.settings_image_button);

    mImageStorage = FirebaseStorage.getInstance().getReference();

    mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

    String current_uid = mCurrentUser.getUid();

    mUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

    mUserDatabse.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        String name = dataSnapshot.child("name").getValue().toString();
        String image = dataSnapshot.child("image").getValue().toString();
        String status = dataSnapshot.child("status").getValue().toString();
        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

        mDisplayName.setText(name);
        mStatus.setText(status);
        if(image.equals("default")){
          mImage.setImageResource(R.drawable.boy);
        } else {
          Picasso.get().load(image).into(mImage); //picasso library
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {


      }
    });

    mStatusButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String status_value = mStatus.getText().toString();

        Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
        statusIntent.putExtra("status_value",status_value);
        startActivity(statusIntent);

      }
    });

    mImageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {


        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLEY_PICK);



      }
    });

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == GALLEY_PICK && resultCode == RESULT_OK) {

      Uri imageUri = data.getData();

      CropImage.activity(imageUri)
              .setAspectRatio(1,1)
              .start(this);

      //Toast.makeText(SettingsActivity.this,imageUri,Toast.LENGTH_LONG).show();

    }

    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

      CropImage.ActivityResult result = CropImage.getActivityResult(data);

      if (resultCode == RESULT_OK) {

        mProgressDialog = new ProgressDialog(SettingsActivity.this);
        mProgressDialog.setTitle("Uploading Image");
        mProgressDialog.setMessage("Please wait while we upload the image.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        Uri resultUri = result.getUri();

        String current_uid = mCurrentUser.getUid();

        final StorageReference filepath = mImageStorage.child("profile_images").child(current_uid + ".jpg");

        filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

            if(task.isSuccessful()) {

              mImageStorage.child("profile_images").child(mCurrentUser.getUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                @Override
                public void onSuccess(Uri uri) {

                  String downloadUrl = uri.toString();
                  Log.i("URL",downloadUrl);

                  mUserDatabse.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                      if (task.isSuccessful()) {

                        mProgressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this,"Succesfully done",Toast.LENGTH_LONG).show();

                      } else {
                        Toast.makeText(SettingsActivity.this,"Error in database uploading.",Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                      }

                    }
                  });
                }
              });
            }
            else {

              Toast.makeText(SettingsActivity.this,"Error in uploading.",Toast.LENGTH_LONG).show();
              mProgressDialog.dismiss();
            }

          }
        });

      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

        Exception error = result.getError();

      }

    }

  }
}
