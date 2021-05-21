package DataStructures;

import java.util.ArrayList;

import OPHSAttributes.Hotel;

public class TreeNode {
	private static int share=0;
	protected final Hotel node;
	protected final int id;
	
	public TreeNode(Hotel node){
		this.node=node;
		this.id=share;
		share++;
	}

	public Hotel getNode() {
		return node;
	}

	public int getId() {
		return id;
	}
	
	
	public void setLeaves(ArrayList<Hotel> hotel_leaves){
		if(this instanceof Root){
			System.out.println("H");
			((Root)this).setLeaves(hotel_leaves);
		}else{
			((Leaf)this).setLeaves(hotel_leaves);
		}
	}
	
	public ArrayList<TreeNode> getLeaves(){
		if(this instanceof Root){
			return ((Root)this).getLeaves();
		}else{
			return ((Leaf)this).getLeaves();
		}
	}
	
	
	
	
}
