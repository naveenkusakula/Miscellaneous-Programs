import java.util.*;
import java.util.ArrayList;
//person object will have both parents list and childrens list additional details of person can be added 
//to the person 
public class person {

	ArrayList<person> parents = new ArrayList<person>();
	ArrayList<person> childrens = new ArrayList<person>();
	String name ="";
	Date DOB;
	String gender; 
	public void setname (String n)
	{
		name = n;
	}
	public String getname()
	{
		return name;
	}
	
	
}
