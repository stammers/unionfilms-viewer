package uk.co.coolalien.gridviewtest;

import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.annotation.TargetApi;
import android.os.Build;

public class FilmActivity extends Activity {

	private Film film;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_film);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		Bundle b = this.getIntent().getExtras();
		if(b!=null) {
		    film = (Film) b.getSerializable(MainActivity.EXTRA_MESSAGE);
		    setUp();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private void setUp(){
		this.setTitle(film.getName());
		//TextView name = (TextView) findViewById(R.id.name);
		TextView date = (TextView) findViewById(R.id.date);
		TextView score = (TextView) findViewById(R.id.score);
		TextView runtime = (TextView) findViewById(R.id.runtime);
		EditText info = (EditText) findViewById(R.id.info);
		
		//name.setText(film.getName());
		date.setText(film.getDate());
		score.setText(film.getScore());
		runtime.setText(film.getLength());
		info.setText(film.getInfo());
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//NavUtils.navigateUpFromSameTask(this);
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
