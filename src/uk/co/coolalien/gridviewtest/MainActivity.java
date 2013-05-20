package uk.co.coolalien.gridviewtest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	FilmHarvester harvester;
	ImageAdapter adapter;
	private GuiThreadMessageHandler guiThreadMsgHandler;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        me = this;
       harvester = new FilmHarvester(me);
       guiThreadMsgHandler = new GuiThreadMessageHandler();
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                	 
                     films = harvester.loadFilms();
                     //for(int i = 0; i < films.size(); i++){
                    //	 String image = films.get(i).getImage();
                    //	 String name = image.substring(image.lastIndexOf("/")+1, image.length());
                    //	 filmPosition.put(name, i);
                    // }

                     
                     	for(int i = 0; i < films.size(); i++){
                     		
                     		//store images in correct order
                     		String image = films.get(i).getImage();
                     		String imageName = image.substring(image.lastIndexOf("/")+1, image.length());
                     		// get input stream
                             //InputStream ims = manager.open("films/" + imageName);
                     		File imageFile = new File(getFilesDir(), imageName);
                             // load image as Drawable
                             Drawable d = Drawable.createFromPath(imageFile.getPath());
                             images.add(d);
                             imageNames.add(imageName);
                             //ims.close();
                     	}
                        
                     Message msg = Message.obtain(guiThreadMsgHandler);
                     msg.sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        thread.start();
        
        Log.d("Debugger", String.valueOf(images.size()));
        GridView gridView = (GridView) findViewById(R.id.gridview);
        adapter = new ImageAdapter(this);
        gridView.setAdapter(adapter);
 
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
        Log.d("Debugger", "Done");
        
    }
    
    public class ImageAdapter extends BaseAdapter {
        private Context context;
 
        public ImageAdapter(Context c) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
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

    public class GuiThreadMessageHandler extends Handler {
        public void handleMessage(Message m){
            adapter.notifyDataSetChanged();
        }
    }

}

