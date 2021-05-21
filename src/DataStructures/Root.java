package DataStructures;

import java.util.ArrayList;

import OPHSAttributes.Hotel;


public class Root extends TreeNode{
	
	private ArrayList<TreeNode> leaves;
	
	public Root(Hotel n){
		super(n);
		this.leaves=new ArrayList<TreeNode>();
	}

	public void setLeaves(ArrayList<Hotel> hotel_leaves) {
		for(Hotel hotel : hotel_leaves){
			System.out.println("   l");
			this.leaves.add(new Leaf(hotel,this));
		}
	}

	public ArrayList<TreeNode> getLeaves() {
		return leaves;
	}
	
	
	
}
