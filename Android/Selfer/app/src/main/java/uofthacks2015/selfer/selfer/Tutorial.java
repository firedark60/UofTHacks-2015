package uofthacks2015.selfer.selfer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;



public class Tutorial extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
    }

    public void getStarted(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
