package pt.ua.icm.bringme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import pt.ua.icm.bringme.helpers.AddressHelper;
import pt.ua.icm.bringme.helpers.BitmapHelper;
import pt.ua.icm.bringme.helpers.DirectionsJSONParser;
import pt.ua.icm.bringme.helpers.FacebookImageLoader;
import pt.ua.icm.bringme.helpers.MapHelper;
import pt.ua.icm.bringme.helpers.RoundedImageView;
import pt.ua.icm.bringme.models.Delivery;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class DeliveryDetailsActivity extends ActionBarActivity {

	private ParseObject delivery;
	private TextView expectedTime, courierName;
	private RoundedImageView drawerProfilePicture ;
	
	private GoogleMap map;
	private ArrayList<LatLng> markerPoints;
	
	private LinearLayout loader, details;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery_details);
		
		loader = (LinearLayout) findViewById(R.id.deliveryDetailsLoader);
		details = (LinearLayout) findViewById(R.id.deliveryDetailsContainer);

		showLoader();
		
		Intent received = getIntent();
		
		String deliveryId = received.getStringExtra("deliveryId");
		drawerProfilePicture = (RoundedImageView) findViewById(R.id.deliveryCourierUserImage);
		
		expectedTime = (TextView) findViewById(R.id.expectedTime);
		courierName = (TextView) findViewById(R.id.deliveryDetailsCourierName);
		
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.detailLastMapFragment);

		markerPoints = new ArrayList<LatLng>();

		// Getting Map for the SupportMapFragment
		map = fm.getMap();
		
		ParseQuery<ParseObject> queryDelivery = new ParseQuery<ParseObject>("Delivery");
		queryDelivery.getInBackground(deliveryId, new GetCallback<ParseObject>() {
			
			@Override
			public void done(ParseObject parseDelivery, ParseException e) {
				if(e == null){
					delivery = parseDelivery;
					
					LatLng startPoint = AddressHelper.parseGeoPointToLatLng(delivery.getParseGeoPoint("origin"));
					markerPoints.add(startPoint);
					LatLng endPoint = new LatLng(delivery.getDouble("destinationLat"),delivery.getDouble("destinationLng"));
					markerPoints.add(endPoint);
					
					MapHelper.updateMapCamera(map, startPoint);

					MarkerOptions startMarker = new MarkerOptions();
					MarkerOptions endMarker = new MarkerOptions();

					// Setting the position of the marker
					startMarker.position(startPoint);
					endMarker.position(endPoint);

					/**
					 * For the start location, the color of marker is GREEN and for the end
					 * location, the color of marker is RED.
					 */
					startMarker.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
			
					endMarker.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					// Add new marker to the Google Map Android API V2
					map.addMarker(startMarker);
					map.addMarker(endMarker);
			
					// Checks, whether start and end locations are captured
					if (markerPoints.size() >= 2) {
						LatLng origin = markerPoints.get(0);
						LatLng dest = markerPoints.get(1);
			
						// Getting URL to the Google Directions API
						String url = getDirectionsUrl(origin, dest);
			
						DownloadTask downloadTask = new DownloadTask();
			
						// Start downloading json data from Google Directions API
						downloadTask.execute(url);
					}
					
					showDetails();
					
					/*String fbId = null;
					
					try {
						fbId = delivery.getParseUser("courier").fetchIfNeeded().getString("facebookId");
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					drawerProfilePicture.setBorderColor(Color
							.parseColor(getString(R.color.green_peas)));

					if (fbId != null) {

						FacebookImageLoader profilePictureLoader = new FacebookImageLoader();

						Bitmap profilePicture = null;

						try {

							profilePicture = profilePictureLoader.execute(fbId, "normal")
									.get();

						} catch (InterruptedException e1) {
							Log.e(Consts.TAG, "Load profile picture was cancelled!");
							e1.printStackTrace();
						} catch (ExecutionException e2) {
							Log.e(Consts.TAG, "Execution Error!");
							e2.printStackTrace();
						}

						if (profilePicture != null) {
							drawerProfilePicture.setImageBitmap(profilePicture);
						} else {
							Bitmap defaultPicture = BitmapHelper.drawableToBitmap(
									R.drawable.default_profile_picture, this);
							drawerProfilePicture.setImageBitmap(defaultPicture);
							drawerProfilePicture.setBorderColor(Color
									.parseColor(getString(R.color.green_peas)));
						}
					} else {
						Bitmap defaultPicture = BitmapHelper.drawableToBitmap(
								R.drawable.default_profile_picture, this);
						drawerProfilePicture.setImageBitmap(defaultPicture);
						drawerProfilePicture.setBorderColor(Color
								.parseColor(getString(R.color.green_peas)));

					}*/
				}
			}
		});

	}
	
	private void showLoader(){
		loader.setVisibility(View.VISIBLE);
		details.setVisibility(View.GONE);
	}
	
	private void showDetails(){
		loader.setVisibility(View.GONE);
		details.setVisibility(View.VISIBLE);
	}

	private static String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();

				// Starts parsing data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		// Executes in UI thread, after the parsing process
		@Override
		public void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;
			MarkerOptions markerOptions = new MarkerOptions();
			String distance = "";
			String duration = "";

			/*
			 * if (result.size() < 1) { // Toast.makeText(getBaseContext(),
			 * "No Points", // Toast.LENGTH_SHORT).show(); return; }
			 */

			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();

				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					if (j == 0) { // Get distance from the list
						distance = (String) point.get("distance");
						continue;
					} else if (j == 1) { // Get duration from the list
						duration = (String) point.get("duration");
						TextView tvv = (TextView) findViewById(R.id.expectedTime);
						tvv.setText(duration);
						continue;
					}

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				// Adding all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(2);
				lineOptions.color(Color.RED);
			}

			map.addPolyline(lineOptions);
		}
	}

}
