package uk.co.coolalien.gridviewtest;
import java.io.Serializable;


public class Film implements Serializable{

	private static final long serialVersionUID = -6190592075465569196L;
	private String name;
	private String info;
	private String image;
	private String date;
	private String score;
	private String length;
	
	public Film(String name, String info, String image, String date, String score, String length){
		this.name = name;
		this.info = info;
		this.image = image;
		this.date = date;
		this.score = score;
		this.length = length;
	}

	public String getName(){
		return name;
	}
	
	public String getInfo(){
		return info;
	}
	
	public String getImage(){
		return image;
	}

	public String getDate(){
		return date;
	}

	public String getScore(){
		return score;
	}

	public String getLength(){
		return length;
	}
	
	public String toString(){
		return name + "\n" + info + "\n" + image + "\n" + date + "\n" + score + "\n" + length + "\n";
	}
	
	
}
