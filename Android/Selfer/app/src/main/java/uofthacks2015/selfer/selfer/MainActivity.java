package uofthacks2015.selfer.selfer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    public String username = "Hunter";
    public int points = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView unameView = (TextView)findViewById(R.id.user);
        unameView.setText(username+"\n"+Integer.toString(points)+"pts.");
    }

}