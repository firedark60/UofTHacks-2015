package uofthacks2015.selfer.selfer;
import android.graphics.Bitmap;

public class Selfie {

    private String name;
    private double latitude;
    private double longitude;
    private Bitmap pic;
    private int points;

    public Selfie(String name, double latitude, double longitude, Bitmap pic, int points){
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.pic = pic;
            this.points = points;
    }

    public int getDist(){
        return 420; // TODO placeholder.
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

    public int getPoints(){
        return this.points;
    }

}
