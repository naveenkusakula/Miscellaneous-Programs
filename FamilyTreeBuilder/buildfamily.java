import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class buildfamily {

	ArrayList<String> list = new ArrayList<String>();
	HashMap<String, person> allpersons = new HashMap<String, person>();
	ArrayList<String> inputs = new ArrayList<String>();
	ArrayList<String> tree1 = new ArrayList<String>();
	ArrayList<String> tree2 = new ArrayList<String>();

//Method to create Hashmap of unique persons for the given persons 

	void uniquelist(String name) {
		String line = name;

		String[] thisLine = line.split("	");
		if (thisLine.length == 2) {
			inputs.add(line);
			String parent = thisLine[0];
			String child = thisLine[1];
			boolean temp = alreadycreated(parent);
			if (!temp) {
				person tmppre = new person();
				tmppre.setname(parent);
				allpersons.put(parent, tmppre);
				list.add(parent);
			}
			temp = alreadycreated(child);
			if (!temp) {
				person tmppre = new person();
				tmppre.setname(child);
				allpersons.put(child, tmppre);
				list.add(child);
			}
		}

	}

// Method to create whole family tree by adding parents and childrens to the respective persons

	void buildfamilygraph() {

		for (int i = 0; i < inputs.size(); i++) {
			String temp = inputs.get(i);
			String[] thisLine = temp.split("	");
			String parent = thisLine[1];
			String child = thisLine[0];

			person tempper = (person) allpersons.get(parent);
			// System.out.println(tempper);
			tempper.childrens.add((person) allpersons.get(child));
			allpersons.put(parent, tempper);
			// System.out.println(allpersons);

			person tempper2 = (person) allpersons.get(child);
			// System.out.println(tempper);
			tempper2.parents.add((person) allpersons.get(parent));
			allpersons.put(child, tempper2);
		}

	}

//Method to find relation between two given persons by creating binary tree of parents for each person and finding common
//ancestor of two by using level order traversal of both trees and by counting the length to common ancestor i.e., n1 & n2
//and find and return the relation using them.	
	public String findrelation(String name1, String name2) {

		String tem = "no";
		node root = new node();
		root.name = name1;
		createtree(root);

		tree1 = levelorder(root);

		node root1 = new node();
		root1.name = name2;
		createtree(root1);

		tree2 = levelorder(root1);

		String a = checkchild(tree1, name2);

		tem = a;
		String b = "";
		if (a.equals("")) {
			b = checkparent(tree2, name1);
			tem = b;
		}

		if (a.equals("") && b.equals("")) {
			String common = "";
			for (int i = 0; i < tree1.size(); i++) {
				for (int j = 0; j < tree2.size(); j++) {
					if (tree2.get(j) == null || tree1.get(i) == null) {
						continue;
					}

					if (tree2.get(j) == tree1.get(i)) {
						common = tree2.get(j);
						break;
					}
				}
				if (common != "") {

					break;
				}
			}

			if (common != "") {
				int n1 = findlength(common, tree1);
				int n2 = findlength(common, tree2);
				tree1.clear();
				tree2.clear();

				if (n1 == 0 || n2 == 0) {
					int temprel = n1 - n2;
					if (temprel == 0) {

						tem = "sibling";
					} else if (temprel > 0) {
						if (n1 > 1) {

							tem = tem + "grandnibling";
							for (int i = 1; i < temprel - 1; i++) {
								// System.out.print("great-");
								tem = "great-" + tem;
							}
						} else {

							tem = "nibling";
						}

					} else if (temprel < 0) {
						if (n2 > 1) {

							tem = tem + "grandpibling";
							int temppb = temprel;
							temppb = -temppb;
							for (int j = 1; j < temppb - 1; j++) {

								tem = "great-" + tem;
							}
						} else {

							tem = "pibling";
						}
					}
				} else if (n1 > 0 && n2 > 0 && n1 != 0 && n2 != 0) {
					if (n1 == n2) {
						// System.out.println(n1 + "Cousin");

						return n1 + "Cousin";
					} else {
						if (n1 < n2) {

							return n1 + "-Cousin" + " " + (n2 - n1) + "-removed";
						} else {

							return n2 + "-Cousin" + " " + (n1 - n2) + "-removed";
						}
					}

				}
			} else {

				tem = "not related";
			}
		} else {
			tree1.clear();
			tree2.clear();
		}
		return tem;
	}

	// method to check and return if given name is child/grand child/great grand
	// child by iterating through tree of the person
	private String checkchild(ArrayList<String> tree, String name) {
		// TODO Auto-generated method stub
		int length = 0;
		String temp = "";
		for (int i = 0; i < tree.size(); i++) {
			if (tree.get(i) == null) {
				length++;
			} else if (tree.get(i).equals(name)) {
				if (length > 1) {
					temp = temp + "grand";
				}
				for (int j = 2; j < length; j++) {

					temp = "great-" + temp;

				}

				temp = temp + "Child";

			}
		}
		return temp;
	}

	// method to check and return if given name is Parent/grand Parent/great grand
	// Parent by iterating through tree of the person
	private String checkparent(ArrayList<String> tree, String name) {
		// TODO Auto-generated method stub
		int length = 0;
		String temp = "";
		for (int i = 0; i < tree.size(); i++) {
			if (tree.get(i) == null)
				length++;
			else if (tree.get(i).equals(name)) {
				if (length > 1) {
					temp = temp + "grand";
				}
				for (int j = 2; j < length; j++) {

					temp = "great-" + temp;
				}

				temp = temp + "parent";

			}
		}
		return temp;
	}

	// method to find the length to the common ancestor
	private int findlength(String common, ArrayList<String> temp) {
		int height = -1;
		for (int i = 0; i < temp.size(); i++) {
			if (temp.get(i) == null && temp.get(i + 1) == null) {

				break;
			}
			if (temp.get(i) == null) {
				height++;
				continue;
			}
			if (temp.get(i).equals(common)) {
				break;
			}
		}
		return height;

	}

//Method to perform Level Order Traversal of given tree using queue.
	private ArrayList<String> levelorder(node root) {
		Queue<node> q = new LinkedList<>();
		ArrayList<String> temp = new ArrayList<String>();
		q.add(root);
		q.add(null);
		int flag = 1;
		while (q != null) {
			node p = q.remove();

			if (p == null) {

				q.add(null);
				temp.add(null);
				flag++;

			} else {

				temp.add(p.getname());
				if (p.left != null)
					q.add(p.left);
				if (p.right != null)
					q.add(p.right);
				flag = 0;
			}
			if (flag == 2)
				break;
		}

		return temp;
	}

//Method to create parent binary tree for the given person
	private void createtree(node root) {
		String name = root.name;

		person person = allpersons.get(name);
		ArrayList<person> parents = person.parents;

		if (parents.size() == 0)
			return;

		if (parents.size() == 1) {
			node left = new node();

			left.name = parents.get(0).getname();
			root.left = left;
			createtree(root.left);

		}

		if (parents.size() == 2) {
			node left = new node();

			left.name = parents.get(0).getname();
			root.left = left;
			createtree(root.left);

			node right = new node();

			right.name = parents.get(1).getname();
			root.right = right;
			createtree(root.right);
		}

	}

//Method to check if a person is already created or not
	public boolean alreadycreated(String name) {
		int size = list.size();

		for (int i = 0; i < size; i++) {

			if (list.get(i) == name) {

				return true;
			}
		}
		return false;
	}

//Method to read from input file using Buffer Reader and handle exception if File is not found
	private ArrayList<String> readfile(String filename) {
		ArrayList<String> templist = new ArrayList<String>();
		try {

			FileInputStream fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String eachLine;

			while ((eachLine = br.readLine()) != null) {

				if (!eachLine.isEmpty()) {

					templist.add(eachLine);
				}
			}

			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found");
			System.exit(1);

		} catch (IOException e) {

			e.printStackTrace();
		}
		return templist;
	}

//Method to find the persons that fits the given order by finding the relation between him and all other persons
	private ArrayList<String> findindividuals(String name, int n1, int n2) {

		ArrayList<String> allper = new ArrayList<String>();
		allper.addAll(allpersons.keySet());
		int flag = 0;
		ArrayList<String> outs = new ArrayList<String>();

		for (int i = 0; i < allper.size(); i++) {
			String temp = findrelation(name, allper.get(i));

			if (n1 > 0 && n2 > 0) {
				String cousin = n1 + "-Cousin" + " " + n2 + "-removed";
				if (temp.equals(cousin)) {

					outs.add(allper.get(i));
					flag++;
				}
			} else if (n2 == 0 && n1 > 0) {
				String cousinn2 = n1 + "Cousin";
				if (temp.equals(cousinn2)) {

					outs.add(allper.get(i));
					flag++;
				}
			} else if (n1 == 0 && n2 == 1) {
				if (temp.equals("pibling") || temp.equals("nibling")) {

					outs.add(allper.get(i));
					flag++;
				}
			} else if (n1 == 0 && n2 == 2) {
				if (temp.equals("grandpibling") || temp.equals("grandnibling")) {

					outs.add(allper.get(i));
					flag++;
				}
			} else if (n1 == 0 && n2 > 2) {
				String str = temp;
				String findStr = "great-";
				int lastIndex = 0;
				int count = 0;

				while (lastIndex != -1) {

					lastIndex = str.indexOf(findStr, lastIndex);

					if (lastIndex != -1) {
						count++;
						lastIndex += findStr.length();
					}
				}

				if (count > 0) {
					String calstrp = "grandpibling";
					String calstrn = "grandnibling";
					for (int v = 0; v < count; v++) {
						calstrp = "great-" + calstrp;
						calstrn = "great-" + calstrn;
					}

					if (temp.equals(calstrp) || temp.equals(calstrn))

					{

						outs.add(allper.get(i));
					}
				}
			} else if (n1 == 0 && n2 == 0) {
				if (temp.equals("sibling")) {

					outs.add(allper.get(i));
					flag++;
				}

			}
		}
		if (flag == 0) {

			outs.add("no relatives");
		}
		return outs;
	}

	// Driver method take two inputs 1.file with pairs 2.queries till done is
	// detected
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		buildfamily n = new buildfamily();
		Scanner input = new Scanner(System.in);
		System.out.println("Enter File name with full location:");
		String filename = input.next();
		ArrayList<String> pairs = new ArrayList<String>();
		pairs = n.readfile(filename);

		for (int i = 0; i < pairs.size(); i++) {
			n.uniquelist(pairs.get(i));
		}

		n.buildfamilygraph();

		ArrayList<String> output = new ArrayList<String>();
		System.out.println("Queries: ");
		Scanner scanner = new Scanner(System.in);
		ArrayList<String> checklist = new ArrayList<String>();
		checklist.addAll(n.allpersons.keySet());
		while (true) {
			String iptemp = scanner.nextLine();

			String[] thisLine = iptemp.split("	");
			if (thisLine.length == 1) {
				if (thisLine[0].equals("done"))
					break;

			}

			else if (thisLine.length == 2) {
				String ip1 = thisLine[0];
				String ip2 = thisLine[1];

				int a = 0;
				for (int i = 0; i < checklist.size(); i++) {
					if (ip1.equals(checklist.get(i))) {
						a++;
					}
				}
				for (int i = 0; i < checklist.size(); i++) {
					if (ip2.equals(checklist.get(i))) {
						a++;
					}
				}

				if (a == 2) {
					output.add(n.findrelation(ip1, ip2));
				} else {
					output.add("not related");
				}
			} else if (thisLine.length == 3) {
				String ip1 = thisLine[0];
				String ip2 = thisLine[1];
				String ip3 = thisLine[2];
				int b = 0;
				for (int v = 0; v < checklist.size(); v++) {
					if (ip1.equals(checklist.get(v))) {
						b++;
					}
				}
				if (b == 1) {

					int n1 = Integer.parseInt(ip2);
					int n2 = Integer.parseInt(ip3);
					ArrayList<String> temp = new ArrayList<String>();
					temp = n.findindividuals(ip1, n1, n2);
					for (int m = 0; m < temp.size(); m++) {
						output.add(temp.get(m));
					}
				} else {
					output.add("no relatives");
				}
			}

		}

		for (int i = 0; i < output.size(); i++) {
			System.out.println(output.get(i));
		}
		input.close();
		scanner.close();
	}

//Tried One more Method to find individuals but some cases are failing  
	/*
	 * ArrayList<String> temptree = new ArrayList<String>(); ArrayList<String>
	 * templast = new ArrayList<String>();
	 * 
	 * private void findindividuals(String name, int i, int j) { ArrayList<String>
	 * finvals = new ArrayList<String>(); node root = new node(); root.name = name;
	 * createtree(root); temptree = levelorder(root); //
	 * System.out.println(temptree);
	 * 
	 * int level = -1; for (int k = 0; k < temptree.size(); k++) { if
	 * (temptree.get(k) == null) { level++; } else if (level == i) {
	 * templast.add(temptree.get(k)); } } System.out.println(templast); int down = i
	 * + j;
	 * 
	 * int lev = -1;
	 * 
	 * Queue<String> que = new LinkedList<>(); int p = 0; for (int y = 0; y <
	 * templast.size(); y++) { que.add(templast.get(y)); } que.add(null);
	 * 
	 * while (lev <= down) {
	 * 
	 * String temp = que.peek(); if (temp != null) { String temp23 = que.remove();
	 * // System.out.println(temp23); finvals.add(temp23); person tempper =
	 * allpersons.get(temp23); ArrayList<person> temparr = new ArrayList<person>();
	 * temparr = tempper.childrens; for (int e = 0; e < temparr.size(); e++) {
	 * que.add(temparr.get(e).getname()); }
	 * 
	 * }
	 * 
	 * else if (temp == null) {
	 * 
	 * // System.out.println(que.remove()); finvals.add(que.remove());
	 * que.add(null); lev++;
	 * 
	 * }
	 * 
	 * } System.out.println(finvals); String temp = ""; if (i > 0 && j > 0) { temp =
	 * i + "-Cousin" + " " + j + "-removed"; } else { temp = i + "Cousin"; } int
	 * flag = 0; for (int m = 0; m < finvals.size(); m++) { if (finvals.get(m) ==
	 * null) { continue; } String temmm = findrelation(finvals.get(m), name); //
	 * System.out.println(temmm); if (temmm.equals(temp)) {
	 * System.out.println(finvals.get(m)); flag++; }
	 * 
	 * } if (flag == 0) { System.out.println("no relatives"); } }
	 */

}
