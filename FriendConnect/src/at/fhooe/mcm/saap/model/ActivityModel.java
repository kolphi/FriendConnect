package at.fhooe.mcm.saap.model;

/**
 * Model for representing activities
 * @author Philipp
 *
 */
public class ActivityModel {

	private String type;
	private String name;
	private String weather;
	private String location;
	
	/**
	 * Activity Model constructor
	 * @param name
	 * @param type
	 * @param weather
	 * @param location
	 */
	public ActivityModel(String name, String type, String weather, String location){
		this.type = type;
		this.name = name;
		this.weather = weather;
		this.location = location;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the weather
	 */
	public String getWeather() {
		return weather;
	}
	/**
	 * @param weather the weather to set
	 */
	public void setWeather(String weather) {
		this.weather = weather;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
