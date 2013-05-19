package uk.co.coolalien.gridviewtest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	ArrayList<Drawable> images = new ArrayList<Drawable>();
	ArrayList<String> imageNames = new ArrayList<String>();
	ArrayList<Film> films;
	HashMap<String, Integer> filmPosition = new HashMap<String, Integer>();
	private MainActivity me;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        me = this;
        
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                	 FilmHarvester harvester = new FilmHarvester(me);
                     films = harvester.loadFilms();
                     //for(int i = 0; i < films.size(); i++){
                    //	 String image = films.get(i).getImage();
                    //	 String name = image.substring(image.lastIndexOf("/")+1, image.length());
                    //	 filmPosition.put(name, i);
                    // }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        
        while(films == null){
        	
        }
        
        try {
        	AssetManager manager = getAssets();
        	String[] names = manager.list("films");
        	for(int i = 0; i < names.length; i++){
        		
        		//store images in correct order
        		String image = films.get(i).getImage();
        		String imageName = image.substring(image.lastIndexOf("/")+1, image.length());
        		// get input stream
                InputStream ims = manager.open("films/" + imageName);
                // load image as Drawable
                Drawable d = Drawable.createFromStream(ims, null);
                images.add(d);
                imageNames.add(imageName);
                ims.close();
        	}
           
        }
        catch(IOException ex) {
            return;
        }
        
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(this));
 
        gridView.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> parent, 
            View v, int position, long id) 
            {    
            	if(films != null){
            		Log.d("Debugging message", imageNames.get(position));
            		Log.d("Debugging message", String.valueOf(filmPosition.get(imageNames.get(position))));
            		Toast.makeText(getBaseContext(), 
                            films.get(position).getName() + " selected", 
                            Toast.LENGTH_SHORT).show();
            	}
                
            }
        });   
        
        
    }
    
    public class ImageAdapter extends BaseAdapter 
    {
        private Context context;
 
        public ImageAdapter(Context c) 
        {
            context = c;
        }
 
        //---returns the number of images---
        public int getCount() {
            return images.size();
        }
 
        //---returns the ID of an item--- 
        public Object getItem(int position) {
            return images.get(position);
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(220, 330));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0, 0, 0);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageDrawable(images.get(position));
            return imageView;
        }
    }    

}
