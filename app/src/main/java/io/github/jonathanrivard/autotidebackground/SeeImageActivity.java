package io.github.jonathanrivard.autotidebackground;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SeeImageActivity extends AppCompatActivity {
    WallpaperManager manager;
    ImageView imageView;
    MainActivity main;
    Toast toastOne;
    Toast toastTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_image);
        Intent intent = getIntent();
        manager = WallpaperManager.getInstance(getApplicationContext());
        imageView = (ImageView) findViewById(R.id.seeImageImageView);
        new GetBitmapFromTwitterSeeImage().execute("https://twitter.com/tide_app");
        toastOne = Toast.makeText(getApplicationContext(), "Wait for Image to Load", Toast.LENGTH_SHORT);
        toastTwo = null;
        toastOne.show();
    }

    public void back(View view){
        toastOne.cancel();
        if(toastTwo != null) {toastTwo.cancel();}
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    //Async Task Class
    public class GetBitmapFromTwitterSeeImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... parms){
            URL url = null;
            ArrayList<String> strippedUrls = new ArrayList<String>();

            //Make a URL based on the parameter
            try{
                url = new URL(parms[0]);
                Log.i("Task", "Made new URL");
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
                    while ((input = in.readLine()) != null){
                        if(input.contains("<img src=\"") == true) { //Only get images
                            strippedUrls.add(getStrippedUrl(input)); //Add image strings to array list
                            Log.i("Task", "Added url to strippedURLs");
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

            Log.i("Task", "Null");
            //If the url or download didn't work
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            if(result != null){
                imageView.setImageBitmap(result);
                toastTwo = Toast.makeText(getApplicationContext(), "Got Image", Toast.LENGTH_SHORT);
                toastOne.cancel();
                toastTwo.show();
            }else {
                toastTwo = Toast.makeText(getApplicationContext(), "Cannot Get Image", Toast.LENGTH_SHORT);
                toastOne.cancel();
                toastTwo.show();
            }

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
