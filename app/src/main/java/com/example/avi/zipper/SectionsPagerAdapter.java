package com.example.avi.zipper;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPagerAdapter extends FragmentPagerAdapter{


  public SectionsPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int position) {

    switch (position) {

      case 2:
        RequestsFragment requestsFragment = new RequestsFragment();
        return  requestsFragment;
      case 0:
        ChatsFragment chatsFragment = new ChatsFragment();
        return  chatsFragment;
      case 1:
        FriendsFragment friendsFragment = new FriendsFragment();
        return friendsFragment;

        default:
          return null;


    }
  }

  @Override
  public int getCount() {
    return 3; //as we have 3 tabs
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    super.getPageTitle(position);

    switch (position) {
      case 2:
        return "REQUESTS";
      case 0:
        return "CHATS";
      case 1:
        return "FRIENDS";
        default:
          return null;
    }

  }
}
