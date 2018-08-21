package com.example.avi.zipper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public class UsersActivity extends AppCompatActivity {

  private Toolbar mToolbar;
  private RecyclerView mUsersList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_users);

    mToolbar = findViewById(R.id.users_appBar);
    setSupportActionBar(mToolbar);
    getSupportActionBar().setTitle("All Users");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mUsersList = findViewById(R.id.users_list);

  }
}
