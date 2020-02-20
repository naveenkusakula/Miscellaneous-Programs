import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class Parenthesization {

	String valip;

//Constructor to initialize expected value
	Parenthesization(String ipval) {
		valip = ipval;
	}

	// Method to evaluate different combinations by calculating results for
	// combinations and print result
	public void evaluate(ArrayList<String> com) {
		int explen = 0;
		boolean possible = true;
		for (int i = 0; i < com.size(); i++) {
			ArrayList<String> tempary = new ArrayList<String>();
			Stack<String> stack = new Stack<>();
			tempary = rescal(com.get(i));

			if (i == 0) {
				explen = tempary.size();
				explen = (explen - 1) + explen;
			}

			if (tempary.size() == explen) {

				for (int w = 0; w < tempary.size(); w++) {
					String temp0 = tempary.get(w);
					if (temp0.equals(")")) {
						String left;
						String right;
						String opr;

						right = stack.pop();
						opr = stack.pop();
						left = stack.pop();
						stack.pop();

						stack.push(calculate(left, opr, right));

					} else {
						stack.push(temp0);
					}
				}
				String tem1234 = stack.peek();

				if (tem1234.equals(valip)) {
					String temp = "";
					for (int x = 0; x < tempary.size(); x++) {

						temp = temp + tempary.get(x);
					}

					System.out.println(temp);

					possible = false;
					break;
				}

			}

		}
		if (possible == true) {
			System.out.println("not possible");
		}
	}

	// Perform arithmetic operations on given operands
	String calculate(String lef, String op, String rit) {
		float l = Float.parseFloat(lef);
		float r = Float.parseFloat(rit);
		float res = 0;
		if (op.equals("+")) {
			res = l + r;
		} else if (op.equals("-")) {
			res = l - r;
		} else if (op.equals("*")) {
			res = l * r;
		} else if (op.equals("/")) {
			res = l / r;
		}

		return (Float.toString(res));

	}

//Method to create ArrayList of a each combination for calculation 
	ArrayList<String> rescal(String temp) {

		ArrayList<String> tempstr = new ArrayList<String>();

		int y = 0;
		while (y < temp.length()) {

			if (y == 0) {
				String temp129 = "" + temp.charAt(y);
				tempstr.add(temp129);
				y++;
				continue;
			}
			if (temp.charAt(y) == '(' || temp.charAt(y) == '*' || temp.charAt(y) == '-' || temp.charAt(y) == '+'
					|| temp.charAt(y) == '/' || temp.charAt(y) == ')') {
				String temp129 = "" + temp.charAt(y);
				tempstr.add(temp129);
			}
			if (!(temp.charAt(y) == '(' || temp.charAt(y) == '*' || temp.charAt(y) == '-' || temp.charAt(y) == '+'
					|| temp.charAt(y) == '/' || temp.charAt(y) == ')')) {
				if (!(temp.charAt(y - 1) == '(' || temp.charAt(y - 1) == '*' || temp.charAt(y - 1) == '-'
						|| temp.charAt(y - 1) == '+' || temp.charAt(y - 1) == '/' || temp.charAt(y - 1) == ')')) {
					int size = tempstr.size();
					String temp1235 = tempstr.get(size - 1) + temp.charAt(y);
					tempstr.set(size - 1, temp1235);
				} else {
					tempstr.add("" + temp.charAt(y));
				}
			}
			y++;
		}

		return tempstr;
	}

	// Method to calculate all combinations
	ArrayList<String> combinations(String exp) {

		ArrayList<String> tempcombi = new ArrayList<String>();
		ArrayList<String> left = new ArrayList<String>();
		ArrayList<String> right = new ArrayList<String>();

		for (int i = 0; i < exp.length(); i++) {
			if (exp.length() == 0) {
				return tempcombi;
			}

			else if (tempcombi.size() == 0) {
				tempcombi.add(exp);

			}

			else if (exp.charAt(i) == '*' || exp.charAt(i) == '+' || exp.charAt(i) == '-' || exp.charAt(i) == '/') {

				left = combinations(exp.substring(0, i));
				right = combinations(exp.substring(i + 1));
				for (int z = 0; z < left.size(); z++) {
					for (int x = 0; x < right.size(); x++) {
						String subcombi = "(" + left.get(z) + exp.charAt(i) + right.get(x) + ")";
						tempcombi.add(subcombi);

					}
				}
			}

		}

		return tempcombi;

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Enter an unparenthesized formula");
		Scanner scan = new Scanner(System.in);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		String inputwithsps = buffer.readLine();
		String input = inputwithsps.replaceAll("\\s", "");
		System.out.println("Enter a resulting value");
		String ipval = scan.next();
		if (!ipval.contains(".")) {
			ipval = ipval + ".0";
		}
		Parenthesization n = new Parenthesization(ipval);
		ArrayList<String> t = new ArrayList<String>();
		t = n.combinations(input);
		n.evaluate(t);
	}

}
