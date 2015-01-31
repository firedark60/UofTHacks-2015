package uofthacks2015.selfer.selfer;

import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.location.Criteria;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import java.util.ArrayList;
import android.view.View;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.widget.ListView;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;



class MySimpleArrayAdapter extends ArrayAdapter<Selfie> {
    // private final Context context;
    private final ArrayList <Selfie> values;
    Context context;


    public MySimpleArrayAdapter(Context context, ArrayList <Selfie> values) {
        super(context, R.layout.row, values);
        this.context = context;
        this.values = values;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.row, parent, false);

        TextView name = (TextView) rowView.findViewById(R.id.User);
        name.setText(values.get(position).getName()+"\nDistance: "+values.get(position).getDist()+" Worth: "+values.get(position).getPoints()+"pts");

        ImageView pic = (ImageView) rowView.findViewById(R.id.userPic);
        pic.setImageBitmap(values.get(position).getPic());


        return rowView;
    }
}


public class MainActivity extends ActionBarActivity implements LocationListener{

    LocationManager locationMan;
    ListView lv;

    // The current users information.
    public String username = "Hunter";
    public int points = 0;
    public double latitude = 0;
    public double longitude = 0;

    // Get this list from the server.
    public ArrayList<Selfie> selfieList = new ArrayList<Selfie>();


    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationMan.removeUpdates((LocationListener) this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Selfer");

        TextView unameView = (TextView)findViewById(R.id.user);
        unameView.setText(username+"\n"+Integer.toString(points)+"pts.");

        lv = (ListView) findViewById(R.id.listView);

        ListAdapter adapter = new MySimpleArrayAdapter(this, selfieList);
        lv.setAdapter(adapter);

        locationMan = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationMan.getBestProvider(criteria, false);
        Location location = locationMan.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            onLocationChanged(location);
        } else {
            location = locationMan.getLastKnownLocation(locationMan.NETWORK_PROVIDER);
            if (location != null){
                onLocationChanged(location);
            } else {
                // TODO no locations work
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Login_Activity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void takePic(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            Selfie newSelfie = new Selfie(username, latitude, longitude, imageBitmap, 1);

            updateListView();
            // TODO send this selfie to the sever and then get the list back.
            selfieList.add(newSelfie);
        }
    }

    public void updateListView(){
        ListAdapter adapter = new MySimpleArrayAdapter(this, selfieList);
        lv.setAdapter(adapter);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}