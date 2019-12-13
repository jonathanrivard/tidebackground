package io.github.jonathanrivard.autotidebackground;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //Instance Variables
    WallpaperManager manager;
    Display display;
    Point size;
    float totalImageScale = 1;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBackground(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Instantiate instance variables
        manager = WallpaperManager.getInstance(getApplicationContext());
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size); //Get size and store it into the point

        registerReceiver(receiver, new IntentFilter("UPDATE_TIDE_BACKGROUND_JR"));
    }

    //Button Onclicks
    public void seeImage(View view){
        Intent imageIntent = new Intent(this, SeeImageActivity.class);
        startActivity(imageIntent);
    }

    public void clearWallpaper(View view){
        try{
            manager.clear();
        }catch(IOException ex){
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Cleared Wallpaper", Toast.LENGTH_SHORT).show();
    }

    public void updateBackground(View view){
        new GetBitmapFromTwitter().execute("https://twitter.com/tide_app");
    }

    public void openSettings(View view){
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    //Other Functions
    public Bitmap calculateBackground(Bitmap bitmap){
        Paint paint = new Paint(); //Make new paint object
        paint.setColor(Color.rgb(1, 1, 1)); //Set the paint to black

        if(bitmap != null){
            //Get maths
            float scale = bitmap.getWidth() / bitmap.getHeight(); //Width to height ration for positioning image on background
            int pos = (size.y / 2) - (bitmap.getHeight() / 2); //Center placement point

            //Scale the bitmap to the correct resolution
            bitmap = Bitmap.createScaledBitmap(bitmap, (int)(size.x * totalImageScale), (int)(size.x * totalImageScale * scale), true);

            //Create the black(not yet) background bitmap
            Bitmap black = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(black); //Create a new canvas using the black(not yet) background bitmap
            canvas.drawRect(0, 0, size.x, size.y, paint); //Paint the entire black background bitmap black
            canvas.drawBitmap(bitmap, 0, pos, null); //Place the image bitmap onto the black bitmap

            bitmap = black; //Reassign the bitmap to the black bitmap with the image on it

            return bitmap;
        }

        return null;
    }

    public void setBackground(Bitmap bitmap){
        if(bitmap != null){
            try{
                manager.setBitmap(bitmap);
            }catch(IOException ex){
                ex.printStackTrace();
            }

            Toast.makeText(getApplicationContext(), "Set Background", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(), "Cannot Set Null Background", Toast.LENGTH_SHORT).show();
        }
    }

    //Async Task Class
    public class GetBitmapFromTwitter extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... parms){
            URL url = null;
            ArrayList<String> strippedUrls = new ArrayList<String>();

            //Make a URL based on the parameter
            try{
                url = new URL(parms[0]);
            }catch(MalformedURLException ex){
                ex.printStackTrace();
            }

            //If url was gotten successfully
            if(url != null){
                Log.i("Task", "Not Null");
                try{
                    //Get the HTML of the twitter page
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String input;
                    while ((input = in.readLine()) != null) {
                        if (input.contains("<img src=\"") == true) { //Only get images
                            strippedUrls.add(getStrippedUrl(input)); //Add image strings to array list
                        }
                    }
                    in.close(); //Close buffered reader
                }catch(IOException ex) {
                    ex.printStackTrace();
                }

                if(strippedUrls.size() > 0){
                    try{ //Try to download image and turn it into a bitmap
                        Bitmap download = BitmapFactory.decodeStream(new URL(strippedUrls.get(0)).openStream());
                        return download;
                    }catch(MalformedURLException ex) {
                        ex.printStackTrace();
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }else {
                    return null;
                }
            }

            //If the url or download didn't work
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            setBackground(calculateBackground(result)); //Send the download image to the set background function
        }

        public String getStrippedUrl(String url){
            url = url.trim();
            int first = url.indexOf("src");
            int last = url.indexOf(":thumb");
            if(last == -1){
                last = first + 10;
            }
            url = url.substring(first+5, last);

            return url;
        }
    }
}
