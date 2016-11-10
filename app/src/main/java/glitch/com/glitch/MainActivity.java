package glitch.com.glitch;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.NoSuchLayerException;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.utils.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Glitch";
    public static final String OUTER_POLY = "outer_polygon.geojson";
    public static final String FLOOR_0_POLY = "floor_0.geojson";
    public static final String FLOOR_1_POLY = "floor_1.geojson";

    MapboxMap mapboxMap;

    MapView mapView;

    Button floor0Btn;

    Button floor1Btn;

    String currentPoly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init mapbox
        MapboxAccountManager.start(this, getString(R.string.access_token));

        mapView = (MapView) findViewById(R.id.mapboxmap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap map) {
                mapboxMap = map;

                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                    .include(new LatLng(51.544484, -0.003781))
                    .include(new LatLng(51.541908, -0.011356))
                    .build();

                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, 10);
                mapboxMap.animateCamera(update);

                showPolygon(null, currentPoly = OUTER_POLY);
            }
        });

        floor0Btn = (Button) findViewById(R.id.btn0);
        floor0Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPolygon(currentPoly, currentPoly = FLOOR_0_POLY);
            }
        });

        floor1Btn = (Button) findViewById(R.id.btn1);
        floor1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPolygon(currentPoly, currentPoly = FLOOR_1_POLY);
            }
        });
    }

    private void showPolygon(String oldName, String newName) {

        Log.d(TAG, "in showPolygon old:" + oldName + ", new:" + newName);
        String json = null;
        try {
            json = getStringFromAssetsFile(newName);
            Log.d(TAG, "read polygons in " + newName);
        } catch (Exception e) {
            Log.e(TAG, "an error occurred reading " + newName, e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(oldName)) {
            try {
                Log.d(TAG, "removing old polygons / polylines " + oldName);
                mapboxMap.removeLayer(oldName + "_venue");
                mapboxMap.removeLayer(oldName + "_lines");
            } catch (NoSuchLayerException e) {
                Log.e(TAG, "an error occurred removing old polygons / polylines " + oldName, e);
            }
        }

        GeoJsonSource urbanAreasSource = new GeoJsonSource(newName, json);
        mapboxMap.addSource(urbanAreasSource);
        FillLayer fillLayer = new FillLayer(newName + "_venue", newName);
        fillLayer.setProperties(
            fillColor(ContextCompat.getColor(this, R.color.colorPrimary)),
            fillOpacity(0.7f)
        );

        Log.d(TAG, "adding fillLayer " + fillLayer.getId());
        mapboxMap.addLayer(fillLayer, "com.mapbox.annotations.points");

        //Add polygons again as linelayer because I can't see a way to set the stroke width of the fill layer
        Layer linesLayer = new LineLayer(newName + "_lines", newName);
        Log.d(TAG, "adding linesLayer " + linesLayer.getId());
        mapboxMap.addLayer(linesLayer, "com.mapbox.annotations.points");

        Log.d(TAG, "--------");

    }

    public String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        is.close();
        return sb.toString();
    }

    public String getStringFromAssetsFile(String filename) throws Exception {
        return convertStreamToString(getAssets().open(filename));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
