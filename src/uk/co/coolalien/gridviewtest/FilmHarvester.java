package uk.co.coolalien.gridviewtest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class FilmHarvester{

	private ArrayList<Film> films = new ArrayList<Film>();;
	private HashMap<String, String> images = new HashMap<String, String>();
	private MainActivity activity;

	public FilmHarvester(MainActivity activity){
		this.activity = activity;
	}

	public ArrayList<Film> loadFilms(){
		filmInfo();
		try {
			cacheImages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return films;
	}
	
	public void loadImages(){
		try {
			cacheImages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void filmInfo(){
		//will only reload from internet if there is a connection and the file is at least a day old
		boolean connection = checkConnection();
		Log.d("Debugging Message", String.valueOf(connection));
		connection = checkDate();
		Log.d("Debugging Message", String.valueOf(connection));
		if(connection){
			String url = "http://www.unionfilms.org/films";
			Document document = null;
			try {
				document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1468.0 Safari/537.36").get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(document == null){
				System.out.println("Null");
				return;
			}

			Elements answerers = document.select("div#background .film_container");

			for (Element answerer : answerers) {
				String[] data = new String[6];
				String image = answerer.select("img").first().absUrl("src");
				Element link = answerer.select("a").first();
				String date = answerer.select(".date").first().text();
				Element info = answerer.select(".text").first();
				String summary = "";
				String name = link.text();
				if(info != null){
					summary = info.text();
				}
				data[0] = name;
				data[1] = summary;
				data[2] = image;
				data[3] = date;
				extraInfo(link, data);
				films.add(new Film(data[0], data[1], data[2], data[3], data[4], data[5]));
			}
			try {
				cache();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			try {
				read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void extraInfo(Element link, String[] data){
		String newUrl = link.absUrl("href");
		Document document = null;
		try {
			document = Jsoup.connect(newUrl).userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1468.0 Safari/537.36").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(document == null){
			System.out.println("Null");
			return;
		}
		Elements info = document.select("div#background");
		Element score = info.select(".square_score").first();
		String scoreText = "";
		if(score != null){
			scoreText = score.text();
		}
		data[4] = scoreText;
		Element runtime = info.select("div[class=grid_8 filminfo]").select("#runtime").first();
		String time = "";
		if(runtime != null){
			time = runtime.text();
		}
		data[5] = time;
	}

	private void cache() throws IOException{
		File output = new File(activity.getFilesDir(), "films.txt");
		FileOutputStream fout = new FileOutputStream(output);  
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(films);
	}

	private void read() throws IOException, ClassNotFoundException{
		File input = new File(activity.getFilesDir(), "films.txt");
		FileInputStream fin = new FileInputStream(input);
		ObjectInputStream ois = new ObjectInputStream(fin);
		films = (ArrayList<Film>) ois.readObject();
	}

	private void cacheImages() throws IOException{
		for(Film film : films){
			String[] parts = film.getImage().split("/");
			int part = parts.length;
			String name = parts[part-1];

			File imageFile = new File(activity.getFilesDir(), name);
			if(imageFile.exists()){
				Log.d("Debuggin message", "File exists");
				images.put(film.getName(), imageFile.getName());
			}else{
				URL url = null;
				try {
					url = new URL(film.getImage());
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Bitmap image = BitmapFactory.decodeStream(new FlushedInputStream(url.openConnection().getInputStream()));
				
				OutputStream stream = activity.openFileOutput(imageFile.getName(), Context.MODE_PRIVATE);
			    /* Write bitmap to file using JPEG and 80% quality hint for JPEG. */
			    boolean written = image.compress(CompressFormat.JPEG, 80, stream);
			    Log.d("Debugging Message", String.valueOf(written));
				
				
				images.put(film.getName(), imageFile.getName());
			}

		}
		listFiles("");

	}

	private void listFiles(String directoryName){

		File directory = new File(activity.getFilesDir(), directoryName);

		//get all the files from a directory
		File[] fList = directory.listFiles();

		for (File file : fList){

			if (file.isFile()){
				if(!images.containsValue(file.getName()) && !file.getName().equals("films.txt")){
					file.delete();
				}
			}
		}
	}
	
	private boolean checkConnection(){
		 ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		 return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private boolean checkDate(){
		File output = new File(activity.getFilesDir(), "films.txt");
		Date date = new Date();
		SimpleDateFormat dateFormat  = new SimpleDateFormat("dd/MM/yyyy");
		int result = 0;
		try {
			Date test = (Date) dateFormat.parse(dateFormat.format(date));
			Date test2 = (Date) dateFormat.parse(dateFormat.format(output.lastModified()));;
			result = test.compareTo(test2);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result <= 0){
			return false;
		}else{
			return true;
		}
	}
}
