package timesheet;

import java.util.Arrays;

public class Worker {

	private String name;
	private String surname;
	private String id;
	private Integer[] hourprofile;
	public Activity activity;

	public Worker(String name, String surname, String profileID, Integer[] hrprofile) {
		// TODO Auto-generated constructor stub
		this.name=name;
		this.surname=surname;
		this.id=profileID;
		this.hourprofile=hrprofile;
	}


	public String getID() {
		// TODO Auto-generated method stub
		return this.id;
	}
	
	@Override
	public String toString() {
		return "{" +this.name + "} {" + this.surname + "} {(" + Arrays.toString(hourprofile) + ")";
	}

	public Project setActivity(Activity a){
		this.activity = a;
		return a.getProject();
	}

	public boolean isCompatible(int day, int workedHours) {
		return Integer.valueOf(hourprofile[day]) > workedHours;
	}
}
