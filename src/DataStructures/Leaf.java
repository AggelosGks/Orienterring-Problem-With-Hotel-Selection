package DataStructures;

import java.util.ArrayList;

import OPHSAttributes.Hotel;

public class Leaf extends TreeNode{//implements a child on a tree data structure
	
	
	private  ArrayList<TreeNode> leaves;
	private  ArrayList<TreeNode> previous;
	
	public Leaf(Hotel n,Leaf previous){
		super(n); 
		this.leaves=new ArrayList<TreeNode>();
		this.previous=new ArrayList<TreeNode>(previous.getPrevious());
		this.previous.add(previous);
	}
	
	public Leaf(Hotel n,Root previous){
		super(n); 
		this.leaves=new ArrayList<TreeNode>();
		this.previous=new ArrayList<TreeNode>();
		this.previous.add(previous);
	}

	public void setLeaves(ArrayList<Hotel> hotel_leaves) {
		for(Hotel hotel : hotel_leaves){
			this.leaves.add(new Leaf(hotel,this));
		}
	}

	
	
	public ArrayList<TreeNode> getLeaves() {
		return leaves;
	}

	public ArrayList<TreeNode> getPrevious() {
		return previous;
	}

	public void getFeasibleNonPrevious(ArrayList<Hotel> visitors){
		ArrayList<Hotel> feasible_visits=new ArrayList<Hotel>(visitors);
		for(TreeNode previous : previous){//iterate previous of leaf
			//if previous exists in next visitors and its not start/final depot then remove from visitors
			if(feasible_visits.contains(previous.node)&&(previous.node.getNode_id()!=0)&&(previous.node.getNode_id()!=1)){
				feasible_visits.remove(previous.node);
			}
		}
		this.setLeaves(feasible_visits);
		
	}
	
	
	
	
	
	
	
}
