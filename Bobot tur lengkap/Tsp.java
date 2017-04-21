	import javax.swing.JFrame;
	import com.mxgraph.swing.mxGraphComponent;
	import com.mxgraph.view.mxGraph;
	import com.mxgraph.layout.*;
	import java.awt.BorderLayout;
	import java.io.*;
	import java.util.*;
	import java.util.Comparator;
	import java.util.PriorityQueue;
	/* Class Node.
	 */
	class Node {
		/* Constructor.
		 */
		public Node(){
			visited = new ArrayList<Integer>();
			bound = -99;
		}
		/* Constructor.
		 * @param v array berisi path dari simpul awal ke simpul node ini.
		 * @param b cost sesuai path yang telah dilalui.
		 */
		public Node(ArrayList<Integer> v, float b){
			Iterator<Integer> itr = v.iterator();
			visited = new ArrayList<Integer>();
			while (itr.hasNext()){
				visited.add(itr.next());
			}
			bound = b;
		}
		/* Path yang telah dilalui sejauh node ini dari simpul awal.
		 */
		public ArrayList<Integer> visited;
		/* Cost sesuai path yang telah dilalui node ini.
		 */
		public float bound;
	}
	/* Class Priority Comparator.
	 * Kelas yang menentukan pengaturan priority untuk priority queue.
	 */
	class PriorityComparator implements Comparator<Object> {
		public int compare(Object Q1, Object Q2){
			Node a1 = (Node) Q1;
			Node a2 = (Node) Q2;
			return Float.compare(a1.bound,a2.bound);
		}
	}
	/* Class Tsp
	 */
	public class Tsp extends JFrame{
		/* Constructor.
		 * @param filename nama file tempat input berada.
		 */
		Tsp(String filename) throws IOException {
			//set adjacency matrix
			Scanner in = new Scanner(new File(filename));
			row = in.nextInt();
			col = in.nextInt();
			matrix = new int [row][col];
			visitedEdges = new boolean[row][col];
			for (int i = 0; i < row; i++)
				for (int j = 0; j < col; j++){
					matrix[i][j] = in.nextInt();
					visitedEdges[i][j] = false;
				}
			//set priority queue
			Comparator<Object> comparator = new PriorityComparator();
			queue = new PriorityQueue<Node>(10,comparator);
			path = new ArrayList<Integer>();
			solusi = new Node();
		}
		/* Prosedur yang mengatur visualisasi graf dengan menggunakan library
		 */
		public void depictGraph() {
			//setup gambar graph menggunakan API
			mxGraph graph = new mxGraph();
			Object parent = graph.getDefaultParent();
			graph.getModel().beginUpdate();
			try
			{
				Object v[] = new Object[row+1];
				for (int i = 1; i <= row; i++)
					v[i] = graph.insertVertex(parent, null, Integer.toString(i), 20, 20, 80,
							30, "shape=ellipse;fontSize=18;");

				Iterator<Integer> itr = solusi.visited.iterator();
				int m = 1; // simpul awal
				int n;
				itr.next();
				while (itr.hasNext()){
					n = itr.next();
					graph.insertEdge(parent, null, Integer.toString(matrix[m-1][n-1]), v[m], v[n],
							"strokeColor=red;fontColor=red;rounded=1;endArrow=none;fontSize=13;");
					matrix[m-1][n-1] = -99;
					m = n;
				}
				for (int i = 0; i < row; i++)
					for (int j = i+1; j < col; j++)
						if (matrix[i][j] != -99 && matrix[j][i] != -99)
							graph.insertEdge(parent, null, Integer.toString(matrix[i][j]), v[i + 1], v[j + 1],
										"rounded=1;endArrow=none;fontSize=13;");
			}
			finally{
				graph.getModel().endUpdate();
			}

			mxGraphComponent graphComponent = new mxGraphComponent(graph);
			getContentPane().add(graphComponent, BorderLayout.CENTER);
			mxCircleLayout layout2 = new mxCircleLayout(graph);
			layout2.execute(parent);
		}
		/* Getter.
		 * Banyaknya Node yang telah dibangkitkan.
		 */
		public int getCountNode(){
			return countNode;
		}
		/* Getter.
		 * Path solusi akhir dari simpul awal ke semua simpul dan kembali lagi.
		 */
		public ArrayList<Integer> getPath(){
			for (int i = 0; i < solusi.visited.size(); i++)
				solusi.visited.set(i,solusi.visited.get(i) + 1);
			return solusi.visited;
		}
		/* Prosedur yang menghitung lower bound
		 */
		public float countBound(){
			int count;
			float bnd = 0;
			PriorityQueue<Integer> temp = new PriorityQueue<Integer>();
			for (int i = 0; i < row; i++){
				count = 0;
				for (int j = 0; j < col; j++){
					if (matrix[i][j] != -99){
						if (visitedEdges[i][j]){
							bnd += matrix[i][j];
							count++;
						}
						else{
							temp.offer(matrix[i][j]);
						}
					}
				}
				switch (count) {
					case 0: bnd += temp.poll();
							bnd += temp.poll();
							break;
					case 1: bnd += temp.poll();
							break;
					default: break;
				}
				temp.clear();
			}
			return bnd/2;
		}
		/* Prosedur yang mengatur isi dari visitedEdges sesuai path saat tertentu.
		 */
		public void checkVisitedEdges(){
			Iterator<Integer> itr = path.iterator();
			//reset visited edges
			for (int i = 0; i < row; i++)
				for (int j = 0; j < col; j++){
					visitedEdges[i][j] = false;
				}
			//store visited edges berdasarkan path
			int m = 0; // simpul awal
			int n;
			itr.next();
			while (itr.hasNext()){
				n = itr.next();
				visitedEdges[m][n] = true;
				visitedEdges[n][m] = true;
				m = n;
			}
		}
		/* Prosedur yang menghitung cost total perjalanan.
		 */
		public int countCost(){
			int sum = 0;
			Iterator<Integer> itr = solusi.visited.iterator();
			int m = 1; // simpul awal
			int n;
			itr.next();
			while (itr.hasNext()){
				n = itr.next();
				sum += matrix[m-1][n-1];
				m = n;
			}
			return sum;
		}
		/* Prosedur yang melakukan penyelesaian problem TSP
		 * menggunakan algoritma Branch n bound versi bobot tur lengkap
		 */
		public void solveBnB(){
			countNode++;
			path.add(0); //simpul awal
			float bound = countBound();
			queue.add(new Node(path,bound));
			while (!queue.isEmpty()){
				Node temp = queue.poll();
				path = temp.visited;
				checkVisitedEdges();
				if (path.size() == row){ //Daun/solusi dari state tree dicapai

					visitedEdges[path.get(path.size() - 1)][0] = true;
					visitedEdges[0][path.get(path.size() - 1)] = true;
					path.add(0); //ke simpul awal
					bound = countBound();
					if ((solusi.bound == -99) || (bound < solusi.bound)){
						solusi = new Node(path,bound);
						//hapus semua node yang lower bound nya lebih besar
						Iterator<Node> itr = queue.iterator();
						while (itr.hasNext()){
							Node temp2 = itr.next();
							if (temp2.bound > solusi.bound)
								itr.remove();
						}
					}
					visitedEdges[path.get(path.size() - 1)][0] = false;
					visitedEdges[0][path.get(path.size() - 1)] = false;
				}
				else {
					//generate cabang
					for (int i = 0; i < col; i++) {
						if (!path.contains(i)){ //belum dikunjungi
							countNode++;
							visitedEdges[path.get(path.size() - 1)][i] = true;
							visitedEdges[i][path.get(path.size() - 1)] = true;
							path.add(i);
							bound = countBound();
							queue.add(new Node(path,bound));
							path.remove(path.size() - 1);
							visitedEdges[path.get(path.size() - 1)][i] = false;
							visitedEdges[i][path.get(path.size() - 1)] = false;
						}
					}
				}
			}
		}

		public static void main(String arg[]) throws IOException {
			Tsp problem = new Tsp("graftidakberarah.txt");
			long startTime = System.currentTimeMillis();
			problem.solveBnB();
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);
			System.out.println("Solusi path : " +problem.getPath());
			System.out.println("Cost path : " +problem.countCost());
			System.out.println("Banyak node yang dibangitkan : " +problem.getCountNode());
			System.out.println("Lama waktu proses : " + duration + " milliseconds");
			problem.depictGraph();
			problem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			problem.setSize(400, 320);
			problem.setVisible(true);
		}

		private int col;
		private int row;
		private int matrix[][];
		private Node solusi;
		private PriorityQueue<Node> queue;
		private ArrayList<Integer> path;
		private boolean visitedEdges[][];
		private int countNode = 0;
	}