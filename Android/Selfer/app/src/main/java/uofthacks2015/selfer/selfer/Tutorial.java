package uofthacks2015.selfer.selfer;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;



public class Tutorial extends ActionBarActivity {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // TODO check if the user's phone has a camera and bitch at them if it doesn't.
    }

    public void getStarted(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
