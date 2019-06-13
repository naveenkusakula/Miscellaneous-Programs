import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PaymentManagement {

//Method used to upgrade database
	void reconcilePayments(Connection database) {
		try {
			if(database==null)
			{
				return;
			}

			Statement statement = null;

			ResultSet resultSet;
			ResultSet resultSet2;
			statement = database.createStatement();
			Statement sts = database.createStatement();
			statement.execute("use kusakula;");
// query to get cheques that are matching with single order
			resultSet2 = statement.executeQuery(
					"select * from payments right join (select orderNumber,invval,customerNumber,orderDate from orders natural join (select orderNumber,sum(quantityOrdered*priceEach) as invval from orderdetails where orderNumber in (select orderNumber from orders) group by orderNumber) as p ) as last on payments.amount=last.invval where orderDate<paymentDate;");
//Query to update newly created table with corresponding order Number
			while (resultSet2.next()) {

				String chknum = resultSet2.getString(2);
				String ordnum = resultSet2.getString(5);
				sts.execute("UPDATE invoices SET orderNumber = '" + ordnum + "' WHERE checkNumber = '" + chknum + "';");

			}
//query to get all remaining orderNumber that is null to check and match with exsisting payments
			resultSet = statement.executeQuery("Select * from invoices where orderNumber is null");

			while (resultSet.next()) {
				ArrayList<Integer> validorders = new ArrayList<Integer>();

				String temp = resultSet.getString("checkNumber");

				//System.out.println(temp);
//method to get customer Number if check name is provided
				int custnum = getcustomernumber(temp, database);
//method to get valid orders if checknam and custnam are provided
				validorders = getorders(custnum, temp, database);

				// System.out.println(validorders);
//method to get all combination of the remaining orders that are to be matched with payments
				getcombinations(validorders, temp, database);

				// break;

			}

			resultSet.close();
			resultSet2.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}

	}

	private ArrayList<Integer> getorders(int custnum, String chknum, Connection database) {

		ArrayList<Integer> ordtemp = new ArrayList<Integer>();
		ArrayList<Integer> invtemp = new ArrayList<Integer>();
		ArrayList<Integer> invdatetemp = new ArrayList<Integer>();

		Statement tmpst = null;
		Statement payst = null;
		int ordernum = 0;
		int invnum = 0;
		try {
			tmpst = database.createStatement();
			payst = database.createStatement();
			Statement datst = database.createStatement();
			ResultSet resultSet;
			ResultSet invordnum;
			ResultSet dates;
//query to get orders from newly created table 
			invordnum = payst.executeQuery("select orderNumber from invoices");
			// query to get orders that are paid before the order date and to get sale value
			// of each order
			dates = datst.executeQuery(
					"select * from payments right join (select orderNumber,invval,customerNumber,orderDate from orders natural join (select orderNumber,sum(quantityOrdered*priceEach) as invval from orderdetails where orderNumber in (select orderNumber from orders) group by orderNumber) as p ) as last on payments.amount=last.invval where orderDate>paymentDate;");

			while (dates.next()) {

				String tem = dates.getString("orderNumber");
				if (tem != null) {
					int dateordnum = Integer.parseInt(tem);
					invdatetemp.add(dateordnum);
				}

			}
//check if orders are already updated
			while (invordnum.next()) {
				String temer = invordnum.getString("orderNumber");
				if (temer != null) {
					invnum = Integer.parseInt(temer);
					invtemp.add(invnum);
				}
			}
			// System.out.println(invtemp);
			// System.out.println(invdatetemp);
//query to get orderNumber/status/orderDate for comparison
			resultSet = tmpst.executeQuery(
					"select orderNumber,status,orderDate from orders where customerNumber = '" + custnum + "'");
//System.out.println(ordtemp);
			while (resultSet.next()) {
				int add = 0;
				ordernum = Integer.parseInt(resultSet.getString(1));
				// System.out.println(ordernum);
				String tmpstatus = resultSet.getString("status");
				// check status that are cancelled and Disputed
				if (tmpstatus.equals("Cancelled") || tmpstatus.equals("Disputed")) {
					continue;
				}
//check if orders are already updated with corresponding payments if yes then it will be exempted from valid orders/unpaid orders
				for (int w = 0; w < invdatetemp.size(); w++) {
					// System.out.println("in datecheck");
					if (ordernum == invdatetemp.get(w)) {
						add++;
					}
				}
//check if ordernum is already in invoice table if yes then it will be used to exempt it form valid orders/unpaid orders
				for (int q = 0; q < invtemp.size(); q++) {
					// System.out.println("in invcheck");
					if (ordernum == invtemp.get(q)) {
						add++;
					}
				}

				if (add == 0) {
					ordtemp.add(ordernum);
				}

			}

			tmpst.close();
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}

		return ordtemp;
	}

//method to get customer number for given check number
	private int getcustomernumber(String temp, Connection database) {
		Statement tmpst = null;
		int ordernum = 0;
		try {
			tmpst = database.createStatement();
			ResultSet resultSet;
// query to customer Number
			resultSet = tmpst.executeQuery("select customerNumber from payments where checkNumber = '" + temp + "'");
			while (resultSet.next()) {

				ordernum = Integer.parseInt(resultSet.getString(1));
				return ordernum;
			}

			tmpst.close();
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}
		return ordernum;
	}

//Method to pay for the orders
	boolean payOrder(Connection database, float amount, String cheque_number, ArrayList<Integer> orders) {
		if(cheque_number.equals(null) || orders.isEmpty())
		{
			return false;
		}
		boolean tem = true;
		ArrayList<String> customernames = new ArrayList<String>();
		ArrayList<String> finorders = new ArrayList<String>();
		ResultSet resultSet;
		ResultSet resultSet2;
		ResultSet resultSet3;
		ResultSet resultSet4;
		try {

			Statement stm = database.createStatement();
			stm.execute("use kusakula;");
			// query to get the check number
			resultSet3 = stm.executeQuery("Select checkNumber from payments where checkNumber='" + cheque_number + "'");

			// to check if cheque is in payments table
			while (resultSet3.next()) {
				String temp = resultSet3.getString("checkNumber");
				if (temp != null) {
//					System.out.println("in12n");
					return false;
				}
			}
			// to check if cheque is in newly created table
			resultSet4 = stm.executeQuery("Select checkNumber from invoices where checkNumber='" + cheque_number + "'");
			while (resultSet4.next()) {

				String temp = resultSet4.getString("checkNumber");
				//System.out.println(temp);
				if (temp != null) {
//					System.out.println("ttytu");
					return false;
				}
			}
//query to get the orders details of the order
			for (int i = 0; i < orders.size(); i++) {
				resultSet = stm.executeQuery("select * from orders where orderNumber='" + orders.get(i) + "'");

				while (resultSet.next()) {
					String tem1 = resultSet.getString("customerNumber");

					customernames.add(tem1);

				}

			}
			// System.out.println(customernames);
//to check if all cutomernumber are legit
			if (customernames.size() != orders.size()) {
				return false;
			}
			// to check if all the orders are from same customers
			boolean same = true;
			for (String s : customernames) {
				if (!s.equals(customernames.get(0)))
					same = false;
			}
			if (!same) {
				return false;
			} else {

				for (int i = 0; i < orders.size(); i++) {
					resultSet2 = stm.executeQuery("select * from invoices where orderNumber='" + orders.get(i) + "'");
					while (resultSet2.next()) {
						String temp = resultSet2.getString("orderNumber");
						if (temp != null) {
							// System.out.println("innn");
							return false;
						}
					}
				}
				// get the summ of all the given orders and campare with given amount
				float total = 0;
				for (int am = 0; am < orders.size(); am++) {
					float temp = (float) getsalevalue(orders.get(am), database);
					// System.out.println(temp);
					total = total + temp;
				}
//				System.out.println(total);
				// method to insert new orders
				if (total == amount) {
					for (int j = 0; j < orders.size(); j++) {
						stm.execute("INSERT INTO invoices (orderNumber,checkNumber) values ('" + orders.get(j) + "','"
								+ cheque_number + "');");

					}
				
				}

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}

		return tem;
	}

//Method to get unpaid orders 
	ArrayList<Integer> unpaidOrders(Connection database) {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		if(database == null)
		{
			return temp;
		}
		

		ResultSet resultSet3;

		Statement stm;
		try {
			stm = database.createStatement();
			stm.execute("use kusakula;");
			// query to join both
			resultSet3 = stm.executeQuery(
					"select orders.orderNumber as unpaid,invoices.checkNumber,orders.status from orders left join invoices on orders.orderNumber=invoices.orderNumber where checkNumber is null  and status <>'Cancelled' and status<>'Disputed';");

			while (resultSet3.next()) {
				int temp1 = resultSet3.getInt("unpaid");
				temp.add(temp1);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}
		return temp;
	}

//method to get Unknown payments 
	ArrayList<String> unknownPayments(Connection database) {
		ArrayList<String> temp = new ArrayList<String>();
		if(database == null)
		{
			return temp;
		}
		ResultSet resultSet3;

		Statement stm;
		try {
			stm = database.createStatement();
			stm.execute("use kusakula;");

			// query used to get unknown payments by get all checknumber for which
			// corresponding orderNumber is null
			resultSet3 = stm.executeQuery("select * from invoices where orderNumber is null");

			while (resultSet3.next()) {
				String temp1 = resultSet3.getString("checkNumber");
				temp.add(temp1);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}

		return temp;
	}

//get all acombinations of the ordernumber to match with the payments
	void getcombinations(ArrayList<Integer> ip, String chknum, Connection database) {
		Statement sts = null;
		try {
			sts = database.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}
		int n = ip.size();
//binary combiantions are created to get all possible combinations
		ArrayList<String> templi = new ArrayList<String>();
		for (int i = 0; i < Math.pow(2, n); i++) {
			String B = "";
			int temp = i;
			for (int j = 0; j < n; j++) {
				if (temp % 2 == 1)
					B = '1' + B;
				else
					B = '0' + B;
				temp = temp / 2;
			}
			templi.add(B);
		}
		// System.out.println(templi);
		int siz = (int) Math.pow(2, n);

		ArrayList<Integer>[] combi = new ArrayList[siz];
		// System.out.println(n);
		// initializing
		for (int i = 0; i < siz; i++) {
			combi[i] = new ArrayList<Integer>();
		}
//Index of each 1 is taken and updated with coresponding order Number
		for (int y = 0; y < templi.size(); y++) {
			// int count = templi.get(y).split("1", -1).length - 1;
			// System.out.println(count);
			// if (count > 0) {
			int index = templi.get(y).indexOf('1');
			// System.out.println(index);
			while (index >= 0) {

				combi[y].add(ip.get(index));

				index = templi.get(y).indexOf('1', index + 1);
			}

		}
		// DecimalFormat df = new DecimalFormat("###.##");
		int count = 0;
		for (int b = 0; b < combi.length; b++) {
			double sum = 0;
			// Method to set the threshold to check the possible combinations as its
			// exhaustive and taking so much i am
			// putting a threshold to move one to next combination
			if (count > 250) {
				break;
			}
			if (combi[b].size() == 1 || combi[b].size() == 0) {
				continue;
			}

//			System.out.println(combi[b].size());
			// System.out.println(combi[b]);

			for (int a = 0; a < combi[b].size(); a++) {
				double temp = getsalevalue(combi[b].get(a), database);

				// System.out.println(temp);
				sum = sum + temp;
			}
			// System.out.println(sum);
			// System.out.println(Math.floor(sum * 100) / 100);
			double payamount = getpaymentamount(chknum, database);
			// System.out.println(payamount);
			if ((Math.floor(sum * 100) / 100) == payamount) {

				// System.out.println("Match" + combi[b]);
//checking if the combiantion amount is matching with payments and update in invioces table
				try {
					sts.execute("UPDATE invoices SET orderNumber = '" + combi[b].get(0) + "' WHERE checkNumber = '"
							+ chknum + "';");
					for (int it = 1; it < combi[b].size(); it++) {
						sts.execute("INSERT INTO invoices (orderNumber,checkNumber) values ('" + combi[b].get(it)
								+ "','" + chknum + "'); ");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count = 0;
				break;
			}
			count++;
//			System.out.println(count);
		}
		// sum=0;

		try {
			sts.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Database might be downn");
		}
	}

//Method is used query payment amount from payments table for given check number
	double getpaymentamount(String chknum, Connection database) {
		Statement tmpst = null;
		double value = 0;
		try {
			tmpst = database.createStatement();
			ResultSet resultSet;

			resultSet = tmpst.executeQuery(
					"select amount,customerNumber from payments orders where checkNumber = '" + chknum + "';");
			while (resultSet.next()) {

				value = Double.parseDouble(resultSet.getString(1));
				return value;
			}

			tmpst.close();
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

//Method used to get the saleval or invoice value by joing orders and order details table
	double getsalevalue(int ordernumber, Connection database) {
		// TODO Auto-generated method stub
		Statement tmpst = null;
		double value = 0;
		try {
			tmpst = database.createStatement();
			ResultSet resultSet;
			// DecimalFormat df2 = new DecimalFormat(".##");
			resultSet = tmpst.executeQuery(
					"select sum(priceEach*quantityOrdered)as saleamount ,orderNumber  from orderdetails natural join orders where orderNumber='"
							+ ordernumber + "' group by orderNumber ;");
			while (resultSet.next()) {
				String temp = resultSet.getString(1);

				value = Double.parseDouble(temp);

				return value;
			}

			tmpst.close();
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Connection connect = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306", "kusakula", "B00781205");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PaymentManagement na = new PaymentManagement();
		//na.reconcilePayments(connect);
		ArrayList<Integer> orders = new ArrayList<Integer>();
		orders.add(10424);
		orders.add(10347);
		//orders.add(10);
		 boolean pay = na.payOrder(connect, (float) 29310.30, "AU60889", orders);
		 System.out.println(pay);
	     System.out.println(na.unknownPayments(connect));
		 System.out.println(na.unpaidOrders(connect));
	}

}
