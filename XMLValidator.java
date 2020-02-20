import java.io.*;
import java.util.*;

public class XmlValidator {
	private static Scanner input;
	private static Reader in;

	// Read Contents of Input file and store in array list

	static ArrayList<Character> readfile(String inputfilename) {

		ArrayList<Character> outputchars = new ArrayList<Character>();

		try {

			FileInputStream xmlfile = new FileInputStream(inputfilename);
			InputStreamReader isr = new InputStreamReader(xmlfile, "UTF8");
			in = new BufferedReader(isr);

			int ch;
			char chrs;
			ch = in.read();
			chrs = (char) ch;
			outputchars.add(chrs);
			if (ch == -1) {
				System.out.println("Empty File");
				System.exit(1);
			}

			while ((ch = in.read()) != -1) {
				chrs = (char) ch;
				outputchars.add(chrs);

			}

		} catch (FileNotFoundException e) {
			System.out.println("File Not Found");
			System.exit(1);

		} catch (UnsupportedEncodingException f) {

			System.out.println("Content is not valid");

		} catch (IOException g) {

			System.out.println("Empty File");
		}

		return outputchars;
	}

	public static ArrayList<Character> valchars = new ArrayList<Character>();
	ArrayList<String> iden = new ArrayList<String>();

	// Separate tags from input file and store in Array list

	public static void validation(ArrayList<Character> Vals) {
		int size = Vals.size();

		ArrayList<String> allelem = new ArrayList<String>();

		for (int i = 0; i < size; i++) {
			if (Vals.get(i) == '<' && Vals.get(i + 1) == '/') {
				String tempstr1 = "";
				int k = i + 2;
				while (Vals.get(k) != '>') {

					tempstr1 = tempstr1 + Vals.get(k);
					k++;
				}
				i = k;
				allelem.add("/" + tempstr1);
			} else if (Vals.get(i) == '<') {
				String tempstr = "";
				int j = i + 1;
				while (Vals.get(j) != '>') {

					tempstr = tempstr + Vals.get(j);
					j++;

				}

				i = j;

				allelem.add(tempstr);
			}
		}

		if (allelem.isEmpty()) {
			System.out.println("File is nested properly as they are no tags");
			System.exit(1);
		}
		// Validation of nesting using stack

		Stack<String> finalvals = new Stack<>();

		if (allelem.get(0).charAt(allelem.get(0).length() - 1) == '/') {
			String str = allelem.get(0);
			str = str.substring(0, str.length() - 1);
			String temp = str + 1;
			allelem.set(0, temp);
		} else {
			finalvals.push(allelem.get(0));
			String tmp = allelem.get(0) + 1;
			allelem.set(0, tmp);
		}
		int depth = 1;
		int i;
		int z = 0;

		for (i = 1; i < allelem.size(); i++) {
			// start tag push into stack
			if (((allelem.get(i)).charAt(0) != '/') && (allelem.get(i).charAt(allelem.get(i).length() - 1) != '/')) {
				finalvals.push(allelem.get(i));
				depth++;
				String temp = allelem.get(i) + depth;
				allelem.set(i, temp);
			}

			// end tag validation and pop from stack
			if ((allelem.get(i)).charAt(0) == '/') {
				if (finalvals.empty()) {
					z++;
					break;
				} else {
					String temp1 = finalvals.peek();
					String temp2 = allelem.get(i).substring(1);

					if (temp1.equals(temp2)) {
						if (finalvals.empty()) {
							z = 1;
							break;
						}

						finalvals.pop();
						depth--;
					} else {
						z = 1;
						break;
					}
				}
			}

			// Stand alone tags Validation
			if (allelem.get(i).charAt(allelem.get(i).length() - 1) == '/') {
				String str = allelem.get(i);
				str = str.substring(0, str.length() - 1);
				int dep = depth + 1;
				String temp = str + dep;
				allelem.set(i, temp);
			}

		}

		// Print tags with Indentation
		for (int j = 0; j < i; j++) {
			if ((allelem.get(j)).charAt(0) != '/') {
				char iden = (allelem.get(j).charAt(allelem.get(j).length() - 1));
				int notabs = Character.getNumericValue(iden);

				for (int k = 1; k < notabs; k++)
					System.out.print("\t ");
				String str = allelem.get(j);
				str = str.substring(0, str.length() - 1);
				System.out.println(str);

			}
		}
		// Print File Nested properly or not
		if ((z == 1) || !(finalvals.empty()))
			System.out.println("\nFile is not nested correctly");
		else
			System.out.println("\nFile is nested correctly");

	}

	public static void main(String[] args) {
		System.out.print("Enter the file location with name:");
		input = new Scanner(System.in);
		String inputflenm = input.next();
		valchars = readfile(inputflenm);
		try {
			XmlValidator.validation(valchars);
		} catch (IndexOutOfBoundsException f) {
			System.out.println("< is not closed");
			System.exit(1);
		}

	}

}
