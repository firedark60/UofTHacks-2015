package uofthacks2015.selfer.selfer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class Login_Activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Register");


        setContentView(R.layout.activity_login_);

    }


    public void setUser(View v){ // TODO this should actually sign up the user.
        EditText mEdit = (EditText)findViewById(R.id.editText);
        MainActivity.username =  mEdit.getText().toString();
        MainActivity.points = 0;

        Toast.makeText(this, "User changed to "+mEdit.getText().toString(), Toast.LENGTH_SHORT).show();

    }

}
