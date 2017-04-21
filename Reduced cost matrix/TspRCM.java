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
		cost = -99;
		matrix = new int[row][col];
	}
	/* Constructor.
	 * @param v array berisi path dari simpul awal ke simpul node ini.
	 * @param b cost sesuai path yang telah dilalui.
	 * @param temp matrix yang telah direduksi sesuai node ini.
	 * @param r baris matrix.
	 * @param c kolom matrix.
	 */
	public Node(ArrayList<Integer> v, int b, int[][] temp, int r, int c){
		row = r;
		col = c;
		Iterator<Integer> itr = v.iterator();
		visited = new ArrayList<Integer>();
		while (itr.hasNext()){
			visited.add(itr.next());
		}
		cost = b;
		matrix = new int[row][col];
		for (int i = 0; i < row; i++)
			for (int j = 0; j < col; j++)
				matrix[i][j] = temp[i][j];
	}
	/* Format Output kelas.
	 */
	public String toString(){
		String temp = new String("");
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++)
				temp += (Integer.toString(matrix[i][j]) + " ");
			temp += "\n";
		}
		return "path : " + visited + "\ncost : " + cost + "\n" + temp;
	}
	/* Path yang telah dilalui sejauh node ini dari simpul awal.
	 */
	public ArrayList<Integer> visited;
	/* Cost sesuai path yang telah dilalui node ini.
	 */
	public int cost;
	/* Matrix yang telah direduksi sesuai node ini.
	 */
	public int matrix[][];
	/* banyaknya baris dan kolom
	 */
	public int row, col;
}
/* Class Priority Comparator.
 * Kelas yang menentukan pengaturan priority untuk priority queue.
 */
class PriorityComparator implements Comparator<Object> {
	public int compare(Object Q1, Object Q2){
		Node a1 = (Node) Q1;
		Node a2 = (Node) Q2;
		return Float.compare(a1.cost,a2.cost);
	}
}
/* Class TspRCM
 */
public class TspRCM extends JFrame{
	/* Constructor.
	 * @param filename nama file tempat input berada.
	 */
	TspRCM(String filename) throws IOException {
		//set adjacency matrix
		Scanner in = new Scanner(new File(filename));
		row = in.nextInt();
		col = in.nextInt();
		matrixAwal = new int [row][col];
		for (int i = 0; i < row; i++)
			for (int j = 0; j < col; j++)
				matrixAwal[i][j] = in.nextInt();
		//set priority queue
		Comparator<Object> comparator = new PriorityComparator();
		queue = new PriorityQueue<Node>(10,comparator);
		//set variable lain
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
				graph.insertEdge(parent, null, Integer.toString(matrixAwal[m-1][n-1]), v[m], v[n],
						"strokeColor=red;fontColor=red;rounded=1;fontSize=13;");
				matrixAwal[m-1][n-1] = -99;
				m = n;
			}
			for (int i = 0; i < row; i++)
				for (int j = 0; j < col; j++)
					if (matrixAwal[i][j] != -99)
							graph.insertEdge(parent, null, Integer.toString(matrixAwal[i][j]), v[i + 1], v[j + 1],
									"rounded=1;fontSize=13;");
		}
		finally {
			graph.getModel().endUpdate();
		}
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent, BorderLayout.CENTER);
		mxCircleLayout layout2 = new mxCircleLayout(graph);
		layout2.execute(parent);
		mxParallelEdgeLayout layout1 = new mxParallelEdgeLayout(graph);
		layout1.execute(parent);
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
	/* Prosedur yang melakukan copy isi dari matrix.
	 */
	public void copyMatrix(int[][] src, int[][] dst){
		for (int i = 0; i < row; i++)
			for (int j = 0; j < col; j++)
				dst[i][j] = src[i][j];
	}
	/* Prosedur yang mencari nilai minimal pada row tertentu.
	 */
	public int findMinRow(int[][] m, int row){
		int i = 0;
		while (i < col && m[row][i] == -99)
			i++;
		if (i == col) {
			return -99;
		}
		else {
			int min = m[row][i];			
			for (int j = i+1; j < col; j++)
				if (m[row][j] != -99 && min > m[row][j])
					min = m[row][j];
			return min;
		}
	}
	/* Prosedur yang mencari nilai minimal pada col tertentu.
	 */
	public int findMinCol(int[][] m, int col){
		int i = 0;
		while (i < row && m[i][col] == -99)
			i++;
		if (i == row) {
			return -99;
		}
		else {
			int min = m[i][col];			
			for (int j = i+1; j < row; j++)
				if (m[j][col] != -99 && min > m[j][col])
					min = m[j][col];
			return min;
		}
	}
	/* Prosedur mereduksi baris tertentu.
	 */
	public void reduceRow(int[][] m, int row, int subtractor) {
		for (int i = 0; i < col; i++){
			if (m[row][i] != -99)
				m[row][i] -= subtractor; 
		}
	}
	/* Prosedur mereduksi kolom tertentu.
	 */
	public void reduceCol(int[][] m, int col, int subtractor) {
		for (int i = 0; i < row; i++){
			if (m[i][col] != -99)
				m[i][col] -= subtractor; 
		}
	}
	/* Prosedur yang mengassign nilai infinite pada baris tertentu.
	 */
	public void makeRowInfinite(int[][] m, int row) {
		for (int i = 0; i < col; i++)
			m[row][i] = -99;
	}
	/* Prosedur yang mengassign nilai infinite pada baris tertentu.
	 */
	public void makeColInfinite(int[][] m, int col) {
		for (int i = 0; i < row; i++)
			m[i][col] = -99;
	}
	/* Prosedur yang menghitung cost reduksi.
	 * @param m matrix yang akan dihitung.
	 * @param isRoot menentukan apakah node root atau bukan.
	 */
	public int countCost(int[][] m, boolean isRoot) {
		int temp;
		int cost = 0;
		
		if (!isRoot){
			int s  = path.get(path.size() - 1);
			int r  = path.get(path.size() - 2);
			cost += m[r][s];
			makeRowInfinite(m,r);
			makeColInfinite(m,s);
			m[s][0] = -99;

		}	
		for (int i = 0; i < row; i++){
			temp = findMinRow(m,i);
			if (temp != 0 && temp != -99){
				cost += temp;
				reduceRow(m,i,temp);
			}
		}
		for (int i = 0; i < col; i++){
			temp = findMinCol(m,i);
			if (temp != 0 && temp != -99){
				cost += temp;
				reduceCol(m,i,temp);
			}
		}
		return cost;
	}
	/* Prosedur yang menghitung cost total perjalanan.
	 */
	public int countSolutionCost() {
		int sum = 0;
		Iterator<Integer> itr = solusi.visited.iterator();
		int m = 1; // simpul awal
		int n;
		itr.next();
		while (itr.hasNext()) {
			n = itr.next();
			sum += matrixAwal[m - 1][n - 1];
			m = n;
		}
		return sum;
	}
	/* Prosedur yang melakukan penyelesaian problem TSP
	 * menggunakan algoritma Branch n bound versi reduksi matrix
	 */
	public void solveBnB(){
		int tempMatrix[][] = new int [row][col];
		//Set root node
		countNode++;
		path.add(0);
		copyMatrix(matrixAwal,tempMatrix);
		int cost = countCost(tempMatrix, true);
		queue.add(new Node(path,cost,tempMatrix,row,col));
		//Mulai eksekusi semua elemen queue
		while (!queue.isEmpty()) {
			Node temp = queue.poll();
			path = temp.visited;
			if (path.size() == row){ //Daun/solusi dari state tree dicapai
				path.add(0); //ke simpul awal
				if ((solusi.cost == -99) || (temp.cost < solusi.cost)){
					solusi = new Node(path,temp.cost,temp.matrix,row,col);
					//hapus semua node yang lower cost nya lebih besar
					Iterator<Node> itr = queue.iterator();
					while (itr.hasNext()){
						Node temp2 = itr.next();
						if (temp2.cost > solusi.cost)
							itr.remove();
					}
				}
			}
			else {
				//generate cabang
				for (int i = 0; i < col; i++) {
					if (!path.contains(i)){ //belum dikunjungi
						countNode++;
						copyMatrix(temp.matrix,tempMatrix);
						path.add(i);
						cost = countCost(tempMatrix, false) + temp.cost;
						Node tes = new Node(path,cost,tempMatrix,row,col);
						queue.add(tes);
						path.remove(path.size() - 1);
					}
				}
			}
		}
	}

	public static void main(String arg[]) throws IOException {
		TspRCM problem = new TspRCM("grafberarah.txt");
		long startTime = System.currentTimeMillis();
		problem.solveBnB();
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println("Solusi path : " +problem.getPath());
		System.out.println("Cost path : " +problem.countSolutionCost());
		System.out.println("Banyak node yang dibangitkan : " +problem.getCountNode());
		System.out.println("Lama waktu proses : " + duration + " milliseconds");
		problem.depictGraph();
		problem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		problem.setSize(400, 320);
		problem.setVisible(true);
	}

	private int col;
	private int row;
	private int matrixAwal[][];
	private Node solusi;
	private PriorityQueue<Node> queue;
	private ArrayList<Integer> path;
	private int countNode = 0;
}