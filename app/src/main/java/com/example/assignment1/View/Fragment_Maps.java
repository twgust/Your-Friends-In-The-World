package com.example.assignment1.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.assignment1.R;
import com.example.assignment1.ViewModel.MemberData;
import com.example.assignment1.ViewModel.UserData;
import com.example.assignment1.controller.entity.MemberLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Fragment_Maps extends Fragment
        implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    public static final String TAG = "Fragment_Maps";
    public final boolean locationPermissions;
    private GoogleMap map;
    private BitmapDescriptor descriptor = null;

    public Fragment_Maps(boolean locationPermissions){
        this.locationPermissions = locationPermissions;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapsInitializer.initialize(requireActivity());
        descriptor = bitmapDescriptorFromVector(requireActivity(), R.drawable.xml_icon_person_24    );


        if (locationPermissions) {
            MemberData memberModel = new ViewModelProvider(requireActivity()).get(MemberData.class);
            memberModel.getMemberLocationsList().observe(getViewLifecycleOwner(), memberLocations -> {
                if (memberLocations.isEmpty()){
                    Log.d(TAG, "MemberModel Observe: no member locations to display");
                }
                else{
                    Log.d(TAG, "MemberModel Observe: iterating over #" + memberLocations.size());
                    int count = 0;
                    for (MemberLocation m : memberLocations) {
                        count++;
                        System.out.println("Processing member nbr #" + count);
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(m.getLatitude(),m.getLongitude()))
                                .title(m.getName())
                                .icon(descriptor));
                    }
                }
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(!locationPermissions)
        { Toast.makeText(getContext(),"Locations disabled", Toast.LENGTH_LONG  ).show(); }

        MapView mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        super.onViewCreated(view, savedInstanceState);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        System.out.println("map ready");
        this.map = googleMap;
        map.setMinZoomPreference(6.0f);
        map.setMaxZoomPreference(14.0f);

        if(locationPermissions){
            // Add a large overlay at Newark on top of the smaller overlay.
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);
            googleMap.setOnMyLocationButtonClickListener(this);
            UserData userData = new ViewModelProvider(requireActivity()).get(UserData.class);
            userData.getLocation().observe(getViewLifecycleOwner(), location -> {
                double longitude = Double.parseDouble (location.getLongitude());
                double latitude = Double.parseDouble(location.getLatitude());
                CameraPosition position =
                        CameraPosition.builder().target(new LatLng(latitude,longitude))
                        .build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            });
        }
        else {
            System.out.println("failed");
            googleMap.setMyLocationEnabled(false);
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth()  , vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT)
                .show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG)
                .show();
    }
}
