package uofthacks2015.selfer.selfer;
import android.graphics.Bitmap;

public class Selfie {

    private String name;
    private double latitude;
    private double longitude;
    private Bitmap pic;

    public Selfie(String name, double latitude, double longitude, Bitmap pic){
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.pic = pic;
    }

    public String getName(){
        return this.name;
    }

    public double getLat(){
        return this.latitude;
    }

    public double getLong(){
        return this.longitude;
    }

    public Bitmap getPic(){
        return this.pic;
    }

}
