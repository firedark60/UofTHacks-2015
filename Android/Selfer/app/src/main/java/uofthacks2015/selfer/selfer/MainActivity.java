package uofthacks2015.selfer.selfer;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.location.Criteria;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;


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

    public static String PICTURE_CAP_URL = "http://4d2380e3.ngrok.com/pictureCapture";
    public static String PICTURE_GET_URL = "http://4d2380e3.ngrok.com/updateNearest";
    public static int MAX_TRIES = 3000;

    LocationManager locationMan;
    ListView lv;

    public static boolean done = false;

    // The current users information.
    public static String username = "Hunter";
    public static int points = 0;
    public static double latitude = 0;
    public static double longitude = 0;

    // Get this list from the server.
    public ArrayList<Selfie> selfieList = new ArrayList<Selfie>();


    public void goToMap(View view){
        Intent intent = new Intent(this, SelfieMap.class);
        startActivity(intent);
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
        updateListView();
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationMan.removeUpdates((LocationListener) this);
    }

    public ArrayList<Selfie> getSelfies(){
        ArrayList<Selfie> selfs = new ArrayList<Selfie>();

        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost(PICTURE_GET_URL);
            StringEntity params =new StringEntity("body={\"latitude\":\""+Double.toString(latitude)+
                    "\",\"longitude\":\""+Double.toString(longitude)+"\"");
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String json = reader.readLine();

            JSONObject obj = new JSONObject(json);
            Iterator iterator = obj.keys();
            JSONObject object1= new JSONObject();
            if(iterator.hasNext()){
                object1 = new JSONObject(obj.getString(iterator.next().toString()));
            }

            String usr = object1.getString("Username");
            JSONObject visited = new JSONObject(object1.getString("Visited"));
            String picture = visited.getString("picture");
            JSONArray coord = visited.getJSONArray("location");
            String lat = coord.getString(0);
            String longd = coord.getString(1);
            String worth = visited.getString("worth");
            // Log.i("net", "user: "+usr+" lat: "+lat+" long: "+longd+" worth: "+worth);

            byte[] decodedString = Base64.decode(picture, Base64.URL_SAFE);
            Bitmap decoded = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            selfs.add(new Selfie(usr, Double.parseDouble(lat), Double.parseDouble(longd), decoded,
                    Integer.parseInt(worth)));


            // handle response here...
        }catch (Exception ex) {
            Log.i("net", "bug: "+ex);
            // handle exception here:
        } finally {
            httpClient.getConnectionManager().shutdown();
        }



        done = true;
        return selfs;
    }

    public void httpPostToServer(Bitmap pic){

        HttpClient httpClient = new DefaultHttpClient();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.URL_SAFE);

        try {
            HttpPost request = new HttpPost(PICTURE_CAP_URL);
            StringEntity params =new StringEntity("body={\"latitude\":\""+Double.toString(latitude)+
                    "\",\"longitude\":\""+Double.toString(longitude)+"\"," +
                    "\"username\":\""+username+"\",\"picture\":\""+encodedImage+"\"}");
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            // handle response here...
        }catch (Exception ex) {
            // handle exception here:
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

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
                // TODO when no locations work
            }
        }

        done = false;
        Thread thread2 = new Thread(){
            public void run(){
                selfieList = getSelfies();
            }
        };
        thread2.start();
        int tries = 0;
        while (!done || tries < MAX_TRIES && (latitude == 0 || longitude == 0)){
            tries++;
        }
        if (!done){
            // TODO Internet screwed up.
        }
        updateListView();

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
            final Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Selfie newSelfie = new Selfie(username, latitude, longitude, imageBitmap, 1);


            // TODO send this selfie to the sever and then get the list back.
            Thread thread = new Thread(){
                public void run(){
                    httpPostToServer(imageBitmap); // TODO
                }
            };
            thread.start();

            Thread thread2 = new Thread(){
                public void run(){
                    selfieList = getSelfies(); // TODO
                }
            };
            done = false;
            thread2.start();
            int tries = 0;
            while (!done || tries < MAX_TRIES && (latitude == 0 || longitude == 0)){
                tries++;
            }
            if (!done){
                // TODO Internet screwed up.
            }
            updateListView();


            updateListView();
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