package pt.ua.icm.bringme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.ua.icm.bringme.datastorage.StaticDatabase;
import pt.ua.icm.bringme.helpers.AddressHelper;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link RequestDeliverySourceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events. Use the
 * {@link RequestDeliverySourceFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class RequestDeliverySourceFragment extends Fragment {
	private OnFragmentInteractionListener mListener;
	private Geocoder coder;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @return A new instance of fragment RequestDeliverySourceFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static RequestDeliverySourceFragment newInstance() {
		RequestDeliverySourceFragment fragment = new RequestDeliverySourceFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public RequestDeliverySourceFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			// TODO: Implement
		}

		coder = new Geocoder(getActivity());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_request_delivery_source,
				container, false);

		SupportMapFragment supMapFragment = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.sourceMap);
		final GoogleMap sourceMap = supMapFragment.getMap();

		// TODO: Make this async
		/*LocationManager mLocationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		Location actualLocation = mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);*/

		// Default location Aveiro
		sourceMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(40.6273385, -8.6395553)));
		sourceMap.animateCamera(CameraUpdateFactory.zoomTo(8));

		final AutoCompleteTextView sourceLocationAutoComplete = 
				(AutoCompleteTextView) v.findViewById(R.id.sourceMapAddress);

		sourceMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng coordinates) {
				sourceMap.addMarker(new MarkerOptions().position(coordinates)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_origin)));
				
				StaticDatabase.setOriginCoordinates(coordinates);
				
				try {
					Address address = coder.getFromLocation(coordinates.latitude, 
							coordinates.longitude, 1).get(0);

					String prettyAddress = AddressHelper.getPrettyAddress(address); 

					sourceLocationAutoComplete.setText(prettyAddress);
					
					StaticDatabase.setOriginAddress(prettyAddress);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		sourceLocationAutoComplete.addTextChangedListener(new TextWatcher() {
			List<String> addressNameList = new ArrayList<String>();

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() < 3){
					StaticDatabase.originAddress = null;
					StaticDatabase.originCoordinates = null;
					sourceMap.clear();
					return;
				}
					

				try {
					addressNameList.clear();
					List<Address> addressList = coder.getFromLocationName(s.toString(), 3);

					for (Address address : addressList) {
						addressNameList.add(AddressHelper.getPrettyAddress(address));
					}

					sourceLocationAutoComplete.setAdapter(
							new ArrayAdapter<String>(
									getActivity(),
									android.R.layout.simple_dropdown_item_1line,
									addressNameList));
					sourceLocationAutoComplete.showDropDown();

					if (!addressList.isEmpty()) {
						LatLng address = new LatLng(addressList.get(0)
								.getLatitude(), addressList.get(0)
								.getLongitude());
						
						StaticDatabase.setOriginCoordinates(address);
						
						sourceMap.clear();

						sourceMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pin_origin))
								.position(address).flat(true).rotation(0));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				StaticDatabase.setOriginAddress(s.toString());
			}
		});
		return v;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

}
