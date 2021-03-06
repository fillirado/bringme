package pt.ua.icm.bringme;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import pt.ua.icm.bringme.helpers.AddressHelper;
import pt.ua.icm.bringme.helpers.BitmapHelper;
import pt.ua.icm.bringme.helpers.FacebookImageLoader;
import pt.ua.icm.bringme.helpers.RoundedImageView;
import pt.ua.icm.bringme.models.Delivery;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener, LocationListener, 
		CourierMenuFragment.OnLocationListener {

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ParseUser user;
	private LinkedList<ParseObject> deliveryList = new LinkedList<ParseObject>();

	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Parse.initialize(this, "99yFCBTgfHtYIhUVJrjmmu0BadhZizdif5tWZCaZ",
				"91wrcZYRC5rYdyKxSltowkKtI8nrpzCFMbwKYvUP");
		
		PushService.setDefaultPushCallback(this, MainActivity.class);
		//ParseAnalytics.trackAppOpened(getIntent());
		try {
			ParseInstallation.getCurrentInstallation().save();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 
				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
		
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		user = ParseUser.getCurrentUser();
		
		ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(user.getBoolean("courier")){
					updateLocation();
					
					Log.i(Consts.TAG, "Subscribed to channel: bringme"+user.getObjectId());
					PushService.subscribe(getApplicationContext(), "bringme" + user.getObjectId(), MainActivity.class);
				}
			}
		});
		
		Log.i(Consts.TAG, "Last location on Parse: " + AddressHelper.printParseGeoPoint(user.getParseGeoPoint("lastLocation")));
		
		final RoundedImageView drawerProfilePicture = 
				(RoundedImageView) findViewById(R.id.userImageDrawer);
		
		FacebookImageLoader profilePictureLoader = new FacebookImageLoader();
		
		Bitmap profilePicture = null;
		 
		try {
			if(user.has("facebookId")){
				profilePicture = profilePictureLoader.execute(user.getString("facebookId"),"small").get();
			}
		} catch (InterruptedException e) {
			Log.e(Consts.TAG, "Load profile picture was cancelled!");
			e.printStackTrace();
		} catch (ExecutionException e) {
			Log.e(Consts.TAG, "Execution Error!");
			e.printStackTrace();
		}
		 
		if (profilePicture != null) {
			drawerProfilePicture.setImageBitmap(profilePicture);
			drawerProfilePicture.setBorderColor(
					Color.parseColor(getString(R.color.green_peas)));
		} else {
			Bitmap defaultPicture = BitmapHelper.drawableToBitmap(
					R.drawable.default_profile_picture, this);
			//Bitmap roundedPicture = BitmapHelper
			//		.getRoundedCornerBitmap(defaultPicture);
			//drawerProfilePicture.setImageBitmap(roundedPicture);
		
			drawerProfilePicture.setImageBitmap(defaultPicture);
			drawerProfilePicture.setBorderColor(Color.parseColor(getString(R.color.green_peas)));
		}
		
		TextView profileName = (TextView) findViewById(R.id.drawerProfileName);
		profileName.setText(user.getString("firstName")+" "+user.getString("lastName"));

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	public void openProfileActivity(View view) {
		Intent profIntent = new Intent(getApplicationContext(),
				ProfileActivity.class);
		startActivity(profIntent);
	}

	public void openHistoryActivity(View view) {
		Intent histIntent = new Intent(getApplicationContext(),
				HistoryActivity.class);
		startActivity(histIntent);
	}
	
	public void onListDeliveriesActivity(View view){
		Intent requestListDeliveriesIntent = new Intent(this, DeliveryStatusActivity.class);
		startActivity(requestListDeliveriesIntent);
	}

	public void userLogout(View view){
		
		ParseUser.logOut();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		
		if(tab.getPosition() == 1){
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Delivery");
			query.whereEqualTo("courier", user);
			query.whereEqualTo("finished", false);
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> objects, ParseException e) {
					if(e == null){
						for(ParseObject delivery : objects){
							deliveryList.add(delivery);
						}
						
						Log.i(Consts.TAG, "Retrieve Delivery List with success!");
					}
					else{
						Log.e(Consts.TAG, "Failed to retrieve Delivery List!");
					}
				}
			});
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return (Fragment) RequestMenuFragment.newInstance();
			case 1:
				return (Fragment) CourierMenuFragment.newInstance();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section_request_menu)
						.toUpperCase(l);
			case 1:
				return getString(R.string.title_section_courier_menu)
						.toUpperCase(l);
			}
			return null;
		}
	}

	public void onRequestDeliveryClick(View v) {
		Intent requestDeliveryIntent = new Intent(this,
				RequestDeliveryActivity.class);
		startActivity(requestDeliveryIntent);
	}

	@Override
	public void onLocationChanged(Location newLocation) {
		updateLocation();
	}

	@Override
	public void setCourierMode(final boolean state) {
		user.put("courier", state);
		user.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if(e == null){
					if(state){
						Log.i(Consts.TAG, "Courier mode set to true.");
						
						updateLocation();
						PushService.subscribe(getApplicationContext(), "bringme" + user.getObjectId(),MainActivity.class);
					}
					else{
						Log.i(Consts.TAG, "Courier mode set to false.");
						
						PushService.unsubscribe(getApplicationContext(), "bringme" + user.getObjectId());
						Log.i(Consts.TAG, "Unsubscribed to my channel.");
					}
				}
				else{
					Log.e(Consts.TAG, e.getMessage());
				}
			}
		});
	}
	
	public void updateLocation(){
		ParseGeoPoint.getCurrentLocationInBackground(5000, new LocationCallback() {
			@Override
			public void done(ParseGeoPoint geoPoint, ParseException e) {
				if(e == null){
					Log.d(Consts.TAG, "Retrieved location!");
					
					if(geoPoint != null){
						Log.i(Consts.TAG, "My location is: " + geoPoint.getLatitude() + "," 
							+ geoPoint.getLongitude());
						
						user.put("lastLocation", geoPoint);
						user.saveInBackground(new SaveCallback() {
							
							@Override
							public void done(ParseException e) {
								if(e == null){
									Log.d(Consts.TAG, "Location saved successfully!");
								}
							}
						});
					}else{
						Log.e(Consts.TAG, "Location is null!");
					}
				}
			}
		});
	}
	

	public void creditsActivity(View view){
		Intent intCredits = new Intent(this, CreditsActivity.class);
		startActivity(intCredits);
	}
}