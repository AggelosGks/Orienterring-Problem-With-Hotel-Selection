package DataStructures;

import java.util.ArrayList;
import java.util.TreeMap;

import OPHSAttributes.Hotel;
import OPHSAttributes.Trip;

public class GraphTree {
	
	private final TreeNode root;
	public final TreeMap<Integer,ArrayList<TreeNode>> layer_nodes;
	
	public GraphTree(TreeNode root){
		this.root=root;//assing root field
		layer_nodes=new TreeMap<Integer,ArrayList<TreeNode>>();//initialize structure
		this.layer_nodes.put(0,new ArrayList<TreeNode>());
		this.layer_nodes.get(0).add(root);//add root only
		this.layer_nodes.put(1,new ArrayList<TreeNode>());
		ArrayList<Hotel> roots_visitors=root.node.visitors_limits.get(0);
		for(Hotel visitor : roots_visitors){
			this.layer_nodes.get(1).add(new Leaf(visitor,(Root)root));
		}
		
	}
	
	
	
	public TreeNode getRoot() {
		return root;
	}
	
	public void createSequences(){
		int last=Trip.getNumber_of_trips();//last layer on tree
		ArrayList<TreeNode> last_layer=this.layer_nodes.get(last);
		for(TreeNode node : last_layer){//for each end depot
			ArrayList<Hotel> sequence=new ArrayList<Hotel>();
			for(TreeNode previous : ((Leaf)node).getPrevious()){
				sequence.add(previous.node);//add all previous
			}
			sequence.add(node.node);//and itself
			new HotelSequence(sequence);
		}
	}
		
	public void printLayers(){
		for(int i=0; i<=Trip.getNumber_of_trips(); i++){
			System.out.println("For trip id: "+i+" starters are :");
			for(TreeNode node : this.layer_nodes.get(i)){
				System.out.println("            "+node.node.getNode_id());
				if(i!=0){
					
					String x="";
					for(TreeNode treenode : ((Leaf)node).getPrevious()){
							x=x+Integer.toString(treenode.node.getNode_id());
					}
					System.out.println("And previous:"+x);
				}
			}
			
		}
	}
	
		
}

