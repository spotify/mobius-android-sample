/*
 * Copyright 2016, The Android Open Source Project
 * Copyright (c) 2017-2018 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.statistics.ui.StatisticsActivity;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;

public class TasksActivity extends AppCompatActivity {

  private DrawerLayout mDrawerLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.tasks_act);

    // Set up the toolbar.
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar ab = getSupportActionBar();
    ab.setHomeAsUpIndicator(R.drawable.ic_menu);
    ab.setDisplayHomeAsUpEnabled(true);

    // Set up the navigation drawer.
    mDrawerLayout = findViewById(R.id.drawer_layout);
    mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
    NavigationView navigationView = findViewById(R.id.nav_view);
    if (navigationView != null) {
      setupDrawerContent(navigationView);
    }

    TasksFragment tasksFragment =
        (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
    if (tasksFragment == null) {
      // Create the fragment
      tasksFragment = TasksFragment.newInstance();
      ActivityUtils.addFragmentToActivity(
          getSupportFragmentManager(), tasksFragment, R.id.contentFrame);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // Open the navigation drawer when the home icon is selected from the toolbar.
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupDrawerContent(NavigationView navigationView) {
    navigationView.setNavigationItemSelectedListener(
        menuItem -> {
          switch (menuItem.getItemId()) {
            case R.id.list_navigation_menu_item:
              // Do nothing, we're already on that screen
              break;
            case R.id.statistics_navigation_menu_item:
              Intent intent = new Intent(TasksActivity.this, StatisticsActivity.class);
              startActivity(intent);
              break;
            default:
              break;
          }
          // Close the navigation drawer when an item is selected.
          menuItem.setChecked(true);
          mDrawerLayout.closeDrawers();
          return true;
        });
  }
}
