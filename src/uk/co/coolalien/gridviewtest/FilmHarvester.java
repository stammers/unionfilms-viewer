package uk.co.coolalien.gridviewtest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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

	private void filmInfo(){
		//if Internet connect do the following
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
		//else read from file
	}

	public void extraInfo(Element link, String[] data){
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

	public void cache() throws IOException{
		FileOutputStream fout = new FileOutputStream("films.txt");  
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(films);
	}

	public void read() throws IOException, ClassNotFoundException{
		FileInputStream fin = new FileInputStream("films.txt");
		ObjectInputStream ois = new ObjectInputStream(fin);
		films = (ArrayList<Film>) ois.readObject();
	}

	public void cacheImages() throws IOException{
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
				Bitmap image = null;
				image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

				OutputStream stream = activity.openFileOutput(imageFile.getName(), Context.MODE_PRIVATE);
			    /* Write bitmap to file using JPEG and 80% quality hint for JPEG. */
			    image.compress(CompressFormat.JPEG, 80, stream);
				images.put(film.getName(), imageFile.getName());
			}

		}
		//listFiles("images");

	}

	public void listFiles(String directoryName){

		File directory = new File(activity.getFilesDir(), directoryName);

		//get all the files from a directory
		File[] fList = directory.listFiles();

		for (File file : fList){

			if (file.isFile()){
				if(!images.containsValue(file.getName())){
					file.delete();
				}
			}
		}
	}
}
