package at.fhooe.mcm.saap.model;

/**
 * Model for interests
 * @author Philipp
 *
 */
public class InterestModel {

	private String type;
	private String name;
	private String id;
	
	/**
	 * Interest Model constructor
	 * @param id
	 * @param name
	 * @param type
	 */
	public InterestModel(String id, String name, String type){
		this.id = id;
		this.type = type;
		this.name = name;
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
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
}
