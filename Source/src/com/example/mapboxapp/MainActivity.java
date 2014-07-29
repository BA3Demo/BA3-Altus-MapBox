package com.example.mapboxapp;

import java.io.InputStream;

import us.ba3.me.HaloPulse;
import us.ba3.me.MapView;
import us.ba3.me.markers.DynamicMarker;
import us.ba3.me.markers.DynamicMarkerMapDelegate;
import us.ba3.me.markers.DynamicMarkerMapInfo;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements DynamicMarkerMapDelegate {
	
	String mapBoxID = "ba3-demo.hi127abc";
	String defaltMap = "http://otile1.mqcdn.com/tiles/1.0.0/sat";
	
	boolean move = false;
	boolean firstLoc = true;
	boolean track = false;
	
	MapView mapView;
	
	private Spinner GPS;
	
	protected LocationManager locationManager;
	
	protected LocationListener locationListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		//Get the map view and add a map.
		mapView = (MapView) this.findViewById(R.id.mapView1);
		mapView.addInternetMap("mapQuest", "http://otile1.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.jpg", "", 20, 1, 3, true, false);
		
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		DynamicMarkerMapInfo location = new DynamicMarkerMapInfo();
		location.name = "Location";
		location.zOrder = 5;
		location.delegate = this;
		mapView.addMapUsingMapInfo(location);
		
		locationListener = new LocationListener() {
			@Override
		    public void onLocationChanged(Location l) {
		    	if(firstLoc){
					DynamicMarker marker = new DynamicMarker();
					marker.name = "loc";
					marker.setImage(loadBitmap("greendot.png"), false);
					marker.anchorPoint = new PointF(16,16);
					marker.location = new us.ba3.me.Location(l.getLatitude(),l.getLongitude());
					mapView.addDynamicMarkerToMap("Location", marker);
					
					HaloPulse beacon = new HaloPulse();
				    beacon.name = "beacon";
				    beacon.location = new us.ba3.me.Location(l.getLatitude(),l.getLongitude());
				    beacon.minRadius = 5;
				    beacon.maxRadius = 75;
				    beacon.animationDuration = 2.5f;
				    beacon.repeatDelay = 0;
				    beacon.fade = true;
				    beacon.fadeDelay = 1;
				    beacon.zOrder = 4;
				    beacon.lineStyle.strokeColor = Color.WHITE;
				    beacon.lineStyle.outlineColor = Color.rgb(0, 0, 255);
				    beacon.lineStyle.outlineWidth = 4;
				    mapView.addHaloPulse(beacon);
				    firstLoc = false;
				}else{
					mapView.setHaloPulseLocation("beacon", new us.ba3.me.Location(l.getLatitude(),l.getLongitude()));
					mapView.setDynamicMarkerLocation("Location", "loc", new us.ba3.me.Location(l.getLatitude(),l.getLongitude()), 0.0);
					if(track){
						us.ba3.me.Location loc = new us.ba3.me.Location(l.getLatitude(),l.getLongitude());
						mapView.setLocation(loc);
						mapView.setCameraOrientation(l.getBearing(), 0, 0, 100);
					}
					mapView.setTrackUp(track);
					mapView.setPanEnabled(!track);
					mapView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
				}
		    }

			@Override
			public void onProviderDisabled(String arg0) {
				
			}
	
			@Override
			public void onProviderEnabled(String arg0) {
				
			}
	
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				
			}
			
		};
		
		addListenerOnSpinnerItemSelection();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void addListenerOnSpinnerItemSelection() {
		
		GPS = (Spinner) findViewById(R.id.gps);
		GPS.setSelection(1);
		GPS.setOnItemSelectedListener(new CustomOnItemSelectedListener());
		GPS.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				TextView textView = (TextView)view;
				String gps = textView.getText().toString();
				if(gps.equalsIgnoreCase("GPS Off")){
					
					gpsOff();
					mapView.removeHaloPulse("beacon");
					mapView.removeDynamicMarkerFromMap("Location", "loc");
					firstLoc = true;
					track = false;
					
				} else if(gps.equalsIgnoreCase("GPS On")){
					
					gpsOn();
					track = false;
					
				} else if(gps.equalsIgnoreCase("GPS and Track On")){
					
					gpsOn();
					track = true;
					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
				
				
			}
		});
		GPS.bringToFront();
	}
	
	public void gpsOn() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
		Log.i("gps", "on");
	}
	
	public void gpsOff() {
		locationManager.removeUpdates(locationListener);
		Log.i("gps", "off");
	}
	
	public Bitmap loadBitmap(String assetName) {
		AssetManager assetManager = getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(assetName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
	}
	
	public void submit(View view){
		
		EditText text = (EditText) findViewById(R.id.mapBoxID);
		mapBoxID = text.getText().toString();
		
		if(mapBoxID.equals("")){
			
			mapBoxID = "ba3-demo.hi127abc";
			
		}
		
		mapView.removeMap("MapBox", true);
		mapView.removeMap("mapQuest", true);
		
		mapView.addInternetMap("MapBox", "https://{s}.tiles.mapbox.com/v3/" + mapBoxID + "/{z}/{x}/{y}.png", "a,b,c,d", 20, 1, 3, true, false);
		
	}
	
	public void revert(View view){
		
		mapView.removeMap("MapBox", true);
		mapView.removeMap("mapQuest", true);
		
		mapView.addInternetMap("mapQuest", "http://otile1.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.jpg", "", 20, 1, 3, true, false);
		
	}

	@Override
	public void tapOnMarker(String arg0, String arg1, PointF arg2, PointF arg3) {
		Log.i("marker", "tap");
	}

}
