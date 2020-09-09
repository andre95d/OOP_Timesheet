package timesheet;

import com.sun.glass.ui.Clipboard;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

public class Timesheet {
	
	private Collection<Day> days = new TreeSet<>();
	private Collection<Project> projects = new ArrayList<>();
	private Collection<Activity> activities = new ArrayList<>();
	private Collection<Worker> workers = new ArrayList<>();
	private Map<String, List<Integer>> profiles = new TreeMap<>();
	private Integer workerId=0;
	private Integer profileId=0;
	private Collection<Report> reports= new ArrayList<>();

	//	for(int i=0; i<=364 ; i++) {
//		days.add(new Day(i+1));
//	}
	public Timesheet() {
		for(int i=0; i<=364 ; i++) {
			this.days.add(new Day(i+1));
		}
	}
	// R1
	public void setHolidays(int... holidays) {
		for(Integer h : holidays) {
			for(Day d:days) {
				if(h.equals(d.getValue())) {
					d.setAsHoliday(true);
				}
			}
		}
	}
	
	public boolean isHoliday(int day) {
		for(Day d : days) {
			if (d.isHoliday()) return true;
		}
		return false;
	}
	
	public void setFirstWeekDay(int weekDay) throws TimesheetException {
		if(weekDay < 0 || weekDay>6 ) {
			throw new TimesheetException("Weekday not valid");
		}
		for (Day d:days) {
			d.setWeekDay(weekDay);
			if(weekDay>6) {
				weekDay=0;
			}else {
				weekDay++;
			}
		}
	}
	
	public int getWeekDay(int day) throws TimesheetException {
		if(day < 1 || day>365 ) {
			throw new TimesheetException("day of year not valid");
		}
		for(Day d: days) {
	    	if(day == d.getValue()) return d.getWeekDay();
	    }
		return -1;
	}
	
	// R2
	public void createProject(String projectName, int maxHours) throws TimesheetException {
		if(maxHours<0) throw new TimesheetException();
		projects.add(new Project(projectName, maxHours));
	}
	
	public List<String> getProjects() {
        return projects.stream().map(a-> a.toString()).collect(Collectors.toList());
	}
	
	public void createActivity(String projectName, String activityName) throws TimesheetException {
		Optional<Project> value = projects.stream()
				.filter(a-> projectName.equals(a.toString())).findAny();
		if(!value.isPresent()) throw new TimesheetException();
		Activity a = new Activity(activityName, value.get());
		activities.add(a);
		a.setProject(value.get());
	}
	
	public void closeActivity(String projectName, String activityName) throws TimesheetException {
		Optional<Project> p = projects.stream()
				.filter(i-> projectName.equals(i.toString())).findAny();
		if(!p.isPresent()) throw new TimesheetException();
		Optional<Activity> a = activities.stream()
				.filter(b-> activityName.equals(b.toString())).findAny();
		if(!a.isPresent()) throw new TimesheetException();
		a.get().setOpen(false);
//		activities.
	}
	
	public List<String> getOpenActivities(String projectName) throws TimesheetException {
		Optional<Project> p = projects.stream()
				.filter(i-> projectName.equals(i.toString())).findAny();
		if(!p.isPresent()) throw new TimesheetException();
		List<String> a = activities.stream()
				.filter(b-> b.getProject().equals(p.get()))
				.map(Activity::toString).sorted(String::compareTo)
				.collect(Collectors.toList());
		return a;
	}

	// R3
	public String createProfile(int... workHours) throws TimesheetException {
		String var = String.valueOf(profileId++);
		profiles.put(String.valueOf(var),
				Arrays.stream(workHours).boxed().collect(Collectors.toList()));
        return var;
	}
	
	public String getProfile(String profileID) throws TimesheetException {

        Optional<String> p = profiles.entrySet().stream().map(x-> x.getKey())
				.filter(y-> y.equals(profileID)).findAny();
        if(!p.isPresent()){throw new TimesheetException();};
        return p.get();
	}
	
	public String createWorker(String name, String surname, String profileID) throws TimesheetException {
		Worker w = new Worker(name, surname, Integer.toString(workerId++),
				Arrays.stream(profileID.split(":")).toArray(Integer[]::new));
		workers.add(w);
		return w.getID();
	}
	
	public String getWorker(String workerID) throws TimesheetException {
        Optional<Worker> w= workers.stream()
				.filter(a-> workerID.equals(a.getID())).findAny();
        if(!w.isPresent()) throw new TimesheetException();
        return w.get().toString();
	}
	
	// R4
	public void addReport(String workerID, String projectName, String activityName, int day, int workedHours) throws TimesheetException {
		Optional<Activity> a = activities.stream().filter(x-> activityName.equals(x.toString()))
				.filter(Activity::isOpen).findAny();
		Optional<Worker> w = workers.stream().filter(y-> y.getID().equals(workerID))
				.filter(y -> y.isCompatible(this.getWeekDay(day), workedHours)).findAny();
		if((!a.isPresent()||!w.isPresent())
				&& this.isHoliday(day) && day < 1 && workedHours < 1) {throw new TimesheetException();}
		reports.add(new Report(w.get(), a.get().getProject(), a.get(), day, workedHours));
	}
	public int getProjectHours(String projectName) throws TimesheetException {
		Optional<Integer> p = Optional.of(reports.stream()
				.filter(y-> projectName.equals(y.activity.getProject()))
				.collect(Collectors.summingInt(x-> x.workedHours)));
		if(!p.isPresent()){throw new TimesheetException();}
		return p.get();
	}
	
	public int getWorkedHoursPerDay(String workerID, int day) throws TimesheetException {
		Optional<Worker> w = workers.stream().filter(x-> x.getID().equals(workerID)).findAny();
		if(day <1 && !w.isPresent()){throw new TimesheetException();}
		return reports.stream().filter(y -> y.day == day).mapToInt(z -> z.workedHours).sum();
	}
	
	// R5
	public Map<String, Integer> countActivitiesPerWorker() {
        return reports.stream().collect(Collectors.groupingBy(Report::getWorker,TreeMap::new, Collectors.reducing(0, e-> 1, Integer::sum)));
	} //TODO: not complete
	
	public Map<String, Integer> getRemainingHoursPerProject() {
        return reports.stream().collect(Collectors.groupingBy(x-> x.activity.getProject().name,TreeMap::new,
				Collectors.reducing(y-> y.activity.getProject().maxHours,z-> -z.workedHours,Integer::sum)));
	}
	
	public Map<String, Map<String, Integer>> getHoursPerActivityPerProject() {
        return reports.stream().collect(
        		Collectors.groupingBy(x-> x.activity.getProject().name,TreeMap::new,
						Collectors.groupingBy(y->y.activity.toString(),TreeMap::new,
								Collectors.summingInt(z-> z.workedHours))));
	}
}
