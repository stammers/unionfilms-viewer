package uk.co.coolalien.gridviewtest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "FILM";

	ArrayList<Drawable> images = new ArrayList<Drawable>();
	ArrayList<String> imageNames = new ArrayList<String>();
	ArrayList<Film> films;
	HashMap<String, Integer> filmPosition = new HashMap<String, Integer>();
	private MainActivity me;
	FilmHarvester harvester;
	ImageAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		me = this;
		harvester = new FilmHarvester(me);
		new LoadingInfo().execute("GO");
		
		
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
					Intent intent = new Intent(me, FilmActivity.class);
					Bundle b = new Bundle();
					b.putSerializable(EXTRA_MESSAGE, films.get(position));
					intent.putExtras(b);
					startActivity(intent);
				}else{

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
			View v = convertView;
	        ImageView picture;
	        LayoutInflater inflater = LayoutInflater.from(context);
	        if(v == null) {
	        	v = inflater.inflate(R.layout.griditem, parent, false);
	        	v.setTag(R.id.picture, v.findViewById(R.id.picture));
	        }

	        picture = (ImageView)v.getTag(R.id.picture);
			picture.setImageDrawable(images.get(position));
			return v;
		}
	}    

	public class LoadingInfo extends AsyncTask<String, Integer, Boolean> {

		ProgressDialog progressBar;

		@Override
		protected Boolean doInBackground(String... params) {
			films = harvester.loadFilms();
			if(films.size() == 0){
				return false;
			}

			for(int i = 0; i < films.size(); i++){

				//store images in correct order
				String image = films.get(i).getImage();
				String imageName = image.substring(image.lastIndexOf("/")+1, image.length());
				// get input stream
				File imageFile = new File(getFilesDir(), imageName);
				// load image as Drawable
				Drawable d = Drawable.createFromPath(imageFile.getPath());
				images.add(d);
				imageNames.add(imageName);
				float percentage = ((i+1)/films.size()) * 100;
				publishProgress((int)Math.ceil(percentage));
			}
			publishProgress(100);
			return true;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			//dismisses the progress bar and makes the image adapter update
			//to show the images
			progressBar.dismiss();
			adapter.notifyDataSetChanged();
			if(!result){
				setContentView(R.layout.activity_main_nonetwork);
			}
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			//creates a progress bar
			progressBar = new ProgressDialog(MainActivity.this);
			progressBar.setMessage("File downloading ...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressBar.setProgress(0);
			progressBar.setMax(100);
			progressBar.show();
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			//updates the progress bar
			progressBar.setProgress(values[0]);

		}

	}


}

