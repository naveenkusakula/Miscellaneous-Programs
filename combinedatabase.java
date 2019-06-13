import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class combinedatabase {

	private BufferedReader in;
///Method to load data into databse from Helpinghands organisation   
	void loadhelpinghands(String filename, Statement stm) {
		try {
			FileInputStream inputfile;
//buffer reader to read data from file and split the data with tabs delimit
			inputfile = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(inputfile);
			in = new BufferedReader(isr);
			int fr = 0;
			int count = 0;
			String strtemp;
			String donationID = "";
			String donorID = "";
			ResultSet last;
			while ((strtemp = in.readLine()) != null) {
				if (fr == 0) {
					fr = 1;
					continue;
				}

				String[] fields = strtemp.split("\t", -1);
				trim(fields);
//System.out.println(fields.length);
				if (fields.length == 15) {
					count++;
					try {
						String donationdate = changedatetosql(fields[9]);
						int valdate = validatedate(donationdate);
						if (valdate == 1) {
							continue;
						}
//check if donor already exists in the table if yes the returns donorid if not -1 is returned
						int donorid = checkifnamepresent(stm, fields[0], fields[1], fields[2], fields[3], fields[4],
								fields[5], fields[6], fields[7], donationdate);
						
//Check to see if donor exists or not, if exists then Donationdetails are inserted into Donations table for the corresponding donor  
						if (donorid > 0) {

							int donationtype = getupdatedonationtype(stm, fields[10]);
						
							stm.execute(
									"INSERT INTO Donations (donor,donationamount,donationdate,donationtype,taxreceipt,program) values ('"
											+ donorid + "','" + fields[8] + "','" + donationdate + "','" + donationtype
											+ "','" + fields[11] + "','" + fields[12] + "');");
							last = stm.executeQuery("SELECT LAST_INSERT_ID()");
							while (last.next()) {
								donationID = last.getString("LAST_INSERT_ID()");

							}
							last.close();
//update followdate and notes if exists 
							if (fields[13] != null) {
								if (!fields[13].isEmpty()) {
									stm.execute("UPDATE Donations SET followupdate = '" + fields[13]
											+ "' WHERE donationID = '" + donationID + "';");
								}

							}
							if (fields[14] != null) {
								if (!fields[14].isEmpty()) {
									stm.execute("UPDATE Donations SET notes = '" + fields[14] + "' WHERE donationID = '"
											+ donationID + "';");
								}
							}
						}
						//If donor doesnot exists then insert new records into both Donors and Donations table 
						else {
							ResultSet lastdonor;

							stm.execute(
									"INSERT INTO Donors (firstname,lastname,address,city,province,postalcode) values ('"
											+ fields[0] + "','" + fields[1] + "','" + fields[2] + "','" + fields[3]
											+ "','" + fields[4] + "','" + fields[5] + "');");

							lastdonor = stm.executeQuery("SELECT LAST_INSERT_ID()");
							while (lastdonor.next()) {
								donorID = lastdonor.getString("LAST_INSERT_ID()");
								//System.out.println(donorID);

							}

							if (fields[6] != null) {
								stm.execute("UPDATE Donors SET email = '" + fields[6] + "' WHERE donorID = '" + donorID
										+ "';");
							}
							if (fields[7] != null) {
								stm.execute("UPDATE Donors SET homephone = '" + fields[7] + "' WHERE donorID = '"
										+ donorID + "';");
							}
							int donationtype = getupdatedonationtype(stm, fields[10]);
							stm.execute(
									"INSERT INTO Donations (donor,donationamount,donationdate,donationtype,taxreceipt,program) values ('"
											+ donorID + "','" + fields[8] + "','" + donationdate + "','" + donationtype
											+ "','" + fields[11] + "','" + fields[12] + "');");
							last = stm.executeQuery("SELECT LAST_INSERT_ID()");
							while (last.next()) {
								donationID = last.getString("LAST_INSERT_ID()");

							}
							last.close();

							if (fields[13] != null) {
								if (!fields[13].isEmpty())
								{
									String followupdate = changedatetosql(fields[13]);
								stm.execute("UPDATE Donations SET followupdate = '" + followupdate
										+ "' WHERE donationID = '" + donationID + "';");
								}
							}
							if (fields[14] != null) {
								if (!fields[14].isEmpty())
								{
								stm.execute("UPDATE Donations SET notes = '" + fields[14] + "' WHERE donationID = '"
										+ donationID + "';");}
							}
						}
					} catch (SQLException e) {

						e.printStackTrace();
					}
				}
				// break;
			}

		} catch (IOException e) {

			System.out.println("file not found");
		}
	}
private void trim(String[] fields) {
	for(int i=0;i<fields.length;i++)
	{
		fields[i]=fields[i].trim();
	}
	
}
//method to check if donor already exists in the table if yes the returns donorid if not -1 is returned
	
	private int checkifnamepresent(Statement stm, String first, String last, String adr, String city, String prv,
			String pos, String email, String phone, String donationdate) {
		int id = -1;
		// int donid=-1;
		int update = 0;
		/*
		 * int update=1; String idt; String adrt; String cityt; String emailt; String
		 * phonet; String cellt;
		 */
		ResultSet donors;
		ResultSet details;
		String fulladr = adr + city + prv;
		String emailtab = "";
		String phonetab = "";
		String adrtab = "";
		// Get donors with same first and last name  and all other details if exists
		try {
			donors = stm.executeQuery(
					"select * from Donors where firstname = '" + first + "' and lastname = '" + last + "'");
			while (donors.next()) {
				id = donors.getInt("donorID");
				adrtab = donors.getString("address") + donors.getString("city") + donors.getString("province");
				emailtab = donors.getString("email");
				phonetab = donors.getString("homephone");
				//System.out.println("req" + id);
			}

			donors.close();

			//System.out.println(phonetab + "   " + phone);
			
			//check if any of the fields(Email,Phone,Address) are equal if yes then update if not return donor id as -1 to create a new record  
			if (adrtab.equals(adr)) {
				//System.out.println("adrr");
				update = 1;
			} else if (emailtab.equals(email)) {
				if (emailtab != "") {
					if (email != "") {
						//System.out.println("email");
						update = 1;
					}
				}

			}

			else if (phonetab.equals(phone)) {
				if (phonetab != "") {
					if (phone != "") {
						//System.out.println("phone");
						update = 1;
					}
				}
			}

			if (update == 1) {
				details = stm.executeQuery(
						"select * from Donations where donor = '" + id + "' and donationdate > '" + donationdate + "'");
				while (details.next()) {
					//System.out.println("dategreater" + id);
					return id;

				}
				//System.out.println("inupdate");
				stm.execute("UPDATE Donors SET address = '" + adr + "', city = '" + city + "', province = '" + prv
						+ "', homephone = '" + phone + "', email = '" + email + "' WHERE donorID = '" + id + "';");
				return id;
			} else if (update == 0) {
				return -1;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}
//Method to load we are one file the functionality is same as loadhelping hands method
	void loadweareone(String filename, Statement stm) {
		try {
			FileInputStream inputfile;

			inputfile = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(inputfile);
			in = new BufferedReader(isr);
			int fr = 0;
			int count = 0;
			String strtemp;
			String donationID = "";
			String donorID = "";
			ResultSet last;
			while ((strtemp = in.readLine()) != null) {
				if (fr == 0) {
					fr = 1;
					continue;
				}

				String[] fields = strtemp.split("\t", -1);
				trim(fields);
//System.out.println(fields.length);
				
				if (fields.length == 17) {
					count++;
					try {
						String donationdate = changedatetosql(fields[11]);
						int valdate = validatedate(donationdate);
						if (valdate == 1) {
							continue;
						}
						String lastname = fields[0].substring(1, fields[0].indexOf(","));
						String firstname = fields[0].substring(fields[0].indexOf(",") + 2, fields[0].length() - 1);

						//System.out.println(firstname);
						//System.out.println(lastname);
						int donorid = checkifnamepresent2(stm, firstname, lastname, fields[2], fields[3], fields[4],
								fields[6], fields[7], fields[8], fields[9], donationdate,fields[1]);
						// System.out.println(donorid);
						
						if (donorid > 0) {

							// int donationtype = getupdatedonationtype(stm, fields[10]);
							// System.out.println(donationtype);
							stm.execute("UPDATE Donors SET salutation = '" + fields[1] + "' where salutation is NULL and donorid ='" + donorid + "';");
							stm.execute("UPDATE Donors SET preferredcontact = '" + fields[9] + "' where preferredcontact is NULL and donorid ='" + donorid + "';");
							stm.execute("UPDATE Donors SET cellphone = '" + fields[7] + "' where cellphone is NULL and donorid ='" + donorid + "';");
							stm.execute(
									"INSERT INTO Donations (donor,donationamount,donationdate,donationtype,taxreceipt,event,pledge,anonimity) values ('"
											+ donorid + "','" + fields[12] + "','" + donationdate + "','" + fields[13]
											+ "','" + fields[14] + "','" + fields[15] + "','" + fields[10] + "','"
											+ fields[16] + "');");

						} else {
							ResultSet lastdonor;

							stm.execute(
									"INSERT INTO Donors (firstname,lastname,salutation,address,city,province,postalcode,preferredcontact) values ('"
											+ firstname + "','" + lastname + "','" + fields[1] + "','" + fields[2]
											+ "','" + fields[3] + "','" + fields[4] + "','" + fields[5] + "','"
											+ fields[9] + "');");

							lastdonor = stm.executeQuery("SELECT LAST_INSERT_ID()");
							while (lastdonor.next()) {
								donorID = lastdonor.getString("LAST_INSERT_ID()");
								//System.out.println(donorID);

							}

							if (fields[6] != null) {
								stm.execute("UPDATE Donors SET homephone = '" + fields[6] + "' WHERE donorID = '"
										+ donorID + "';");
							}
							if (fields[7] != null) {
								stm.execute("UPDATE Donors SET cellphone = '" + fields[7] + "' WHERE donorID = '"
										+ donorID + "';");
							}
							if (fields[8] != null) {
								stm.execute("UPDATE Donors SET email = '" + fields[8] + "' WHERE donorID = '" + donorID
										+ "';");
							}

							stm.execute(
									"INSERT INTO Donations (donor,donationamount,donationdate,donationtype,taxreceipt,event,pledge,anonimity) values ('"
											+ donorID + "','" + fields[12] + "','" + donationdate + "','" + fields[13]
											+ "','" + fields[14] + "','" + fields[15] + "','" + fields[10] + "','"
											+ fields[16] + "');");

						}
					} catch (SQLException e) {

						e.printStackTrace();
					}
				}
				// break;
			}

		} catch (IOException e) {

			System.out.println("file not found");
		}
	}
//same functionality as checkinamepresent2
	private int checkifnamepresent2(Statement stm, String first, String last, String adr, String city, String prv,
			String phone, String cell, String email, String precont, String donationdate, String sal) {
		int id = -1;
		// int donid=-1;
		int update = 0;
		/*
		 * int update=1; String idt; String adrt; String cityt; String emailt; String
		 * phonet; String cellt;
		 */
		ResultSet donors;
		ResultSet details;
		String fulladr = adr + city + prv;
		String emailtab = "";
		String phonetab = "";
		String celltab = "";
		String adrtab = "";

		// ArrayList<String> donations = new ArrayList<String>();
		try {
			
			donors = stm.executeQuery(
					"select * from Donors where firstname = '" + first + "' and lastname = '" + last + "'");
			while (donors.next()) {
				
				id = donors.getInt("donorID");
				adrtab = donors.getString("address") + donors.getString("city") + donors.getString("province");
				emailtab = donors.getString("email");
				phonetab = donors.getString("homephone");
				celltab = donors.getString("cellphone");
				//System.out.println("req" + id);
			}

			donors.close();

			//System.out.println(phonetab + "   " + phone);
			if (adrtab.equals(adr)) {
				//System.out.println("adrr");
				update = 1;
			}
			if (email != null) {
				if (emailtab != null) {
					if (emailtab.equals(email)) {
						//System.out.println("email");
						update = 1;
					}
				}

			}

			if (phonetab != null) {
				if (phone != null) {
					if (phonetab.equals(phone)) {
						//System.out.println("phone");
						update = 1;
					}
				}
			}
			if (celltab != null) {
				if (cell != null) {
					if (celltab.equals(cell)) {
						//System.out.println("cellphone");
						update = 1;
					}
				}
			}

			if (update == 1) {
				details = stm.executeQuery(
						"select * from Donations where donor = '" + id + "' and donationdate > '" + donationdate + "'");
				while (details.next()) {
					//System.out.println("dategreater" + id);
					//stm.execute("UPDATE Donors SET salutation = '" + sal + "' where salutation is NULL and donor '" + id + "';");
					//stm.execute("UPDATE Donors SET preferredcontact = '" + precont + "' where preferredcontact is NULL and donor '" + id + "';");	
					
					return id;

				}
				//System.out.println("inupdate");
				stm.execute("UPDATE Donors SET address = '" + adr + "', city = '" + city + "', preferredcontact = '"
						+ precont + "', province = '" + prv + "', homephone = '" + phone + "', email = '" + email
						+ "', cellphone = '" + cell + "',salutation = '" + sal + "' WHERE donorID = '" + id + "';");
				return id;
			} else if (update == 0) {
				
				return -1;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}
//method to get paymenttype by querrying the payments table if type is not available the new record is inserted. 
	private int getupdatedonationtype(Statement stm, String field) {
		ResultSet donationtypes;
		int id = -1;

		try {
			donationtypes = stm.executeQuery("select paymentID from Payments where paymenttype = '" + field + "';");
			while (donationtypes.next()) {
				id = donationtypes.getInt("paymentID");

				return id;
			}
			stm.execute("INSERT INTO Payments (paymenttype) values ('" + field + "');");
			donationtypes = stm.executeQuery("select paymentID from Payments where paymenttype = '" + field + "';");
			while (donationtypes.next()) {
				id = donationtypes.getInt("paymentID");

				return id;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return id;
	}
//method to load events using insert statement
	void loadevents(String filename, Statement stm) {
		try {
			FileInputStream inputfile;

			inputfile = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(inputfile);
			in = new BufferedReader(isr);
			int fr = 0;
			int count = 0;
			String strtemp;
			while ((strtemp = in.readLine()) != null) {
				if (fr == 0) {
					fr = 1;
					continue;
				}

				String[] fields = strtemp.split("\t", -1);
				trim(fields);
				if (fields.length == 6) {
					count++;
					try {

						String date = changedatetosql(fields[2]);
						// System.out.println(date);

						stm.execute(
								"INSERT INTO Events (eventID,eventname,eventdate,venue,fundraisinggoal,volunteers) values ('"
										+ fields[0] + "','" + fields[1] + "','" + date + "','" + fields[3] + "','"
										+ fields[4] + "','" + fields[5] + "');");

					} catch (SQLException e) {

						continue;
					}
				}
				// break;
			}

		} catch (IOException e) {

			System.out.println("file not found");
		}
	}
	//method to load programs using insert statement
	void loadprograms(String filename, Statement stm) {
		try {
			FileInputStream inputfile;

			inputfile = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(inputfile);
			in = new BufferedReader(isr);
			int fr = 0;
			int count = 0;
			String strtemp;
			while ((strtemp = in.readLine()) != null) {
				if (fr == 0) {
					fr = 1;
					continue;
				}

				String[] fields = strtemp.split("\t", -1);
				trim(fields);
				if (fields.length == 5) {
					count++;
					try {

						String startdate = changedatetosql(fields[2]);
						// System.out.println("think"+fields[3]);

						String enddate = changedatetosql(fields[3]);
						int valdate = validatedate(enddate);
						if (valdate == 1) {
							continue;
						}
						if (enddate == null) {
							stm.execute("INSERT INTO Programs (programID,programname,startdate,annualgoal) values ('"
									+ fields[0] + "','" + fields[1] + "','" + startdate + "','" + fields[4] + "');");
						} else {
							stm.execute(
									"INSERT INTO Programs (programID,programname,startdate,enddate,annualgoal) values ('"
											+ fields[0] + "','" + fields[1] + "','" + startdate + "','" + enddate
											+ "','" + fields[4] + "');");
						}

					} catch (SQLException e) {

						e.printStackTrace();
					}
				}
				// break;
			}

		} catch (IOException e) {

			System.out.println("file not found");
		}
	}
	//method to load payments using insert statement
	void loadpayments(String filename, Statement stm) {
		try {
			FileInputStream inputfile;

			inputfile = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(inputfile);
			in = new BufferedReader(isr);
			int fr = 0;
			int count = 0;
			String strtemp;
			while ((strtemp = in.readLine()) != null) {
				if (fr == 0) {
					fr = 1;
					continue;
				}

				String[] fields = strtemp.split("\t", -1);
				trim(fields);
				if (fields.length == 2) {
					count++;
					try {

						stm.execute("INSERT INTO Payments (paymentID,paymenttype) values ('" + fields[0] + "','"
								+ fields[1].toLowerCase() + "');");

					} catch (SQLException e) {

						e.printStackTrace();
					}
				}
				// break;
			}

		} catch (IOException e) {

			System.out.println("file not found");
		}
	}
	//method to validatedate if the date is below 7 years or not by using simple if clause
	private int validatedate(String enddate) {
		if (enddate == null) {
			return 0;
		}
		int temp = Integer.valueOf(enddate.substring(0, 4));
		// System.out.println(temp);
		if (temp < 2012) {
			return 1;
		}
		return 0;
	}
//Method to change date format that is compatbile to insert into SQL database using simple string manipulation
	private String changedatetosql(String date) {
		// System.out.println(date);
		if (date.equals("")) {
			return null;
		}
		String months[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

		String month = date.substring(3, 6);
		for (int i = 0; i < months.length; i++) {
			if (month.equals(months[i])) {

				int temp = i + 1;
				month = String.valueOf(temp);
			}
		}

		String year = date.substring(date.length() - 2);
		int yearnum = Integer.valueOf(year);
		if (yearnum > 50) {
			year = "19" + date.substring(date.length() - 2);
		} else {
			year = "20" + date.substring(date.length() - 2);
		}

		String day;
		day = date.substring(0, 2);

		date = year + "-" + month + "-" + day;

		return date;
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		combinedatabase n = new combinedatabase();

		try {
			Connection connect = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "kusakula", "B00781205");
			Statement statement = null;
			statement = connect.createStatement();
			statement.execute("use kusakula");
			n.loadevents("C:\\Users\\Naveen\\Desktop\\3901_a6_We_Are_One_events.txt", statement);
			n.loadprograms("C:\\Users\\Naveen\\Desktop\\3901_a6_Helping_Hands_programs.txt", statement);
			n.loadpayments("C:\\Users\\Naveen\\Desktop\\3901_a6_We_Are_One_donation_types.txt", statement);
			n.loadhelpinghands("C:\\Users\\Naveen\\Desktop\\3901_a6_Helping_Hands_main.txt", statement);
			n.loadweareone("C:\\Users\\Naveen\\Desktop\\3901_a6_We_Are_One_main.txt", statement);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Databasenotfound or incorreect credentials");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Databasenotfound or incorreect credentials");
		}
		
	}

}
