package DisplayPanels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import Computabillity.Computation;
import Computabillity.FullySequentialStrategy;
import OPHSAttributes.Hotel;
import OPHSAttributes.Node;
import OPHSAttributes.Tour;
import OPHSAttributes.Trip;

public class GraphMap extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static int start_x=100;
	final static int start_y=30;
	final static int range_y=35;
	final static int range_x=45;
	final static int hotel_radius=30;
	final static int poi_radius=20;
	private static Tour tour;
	
	
	public static void setTour(Tour tour) {
		GraphMap.tour = tour;
	}

	@Override
    public void paintComponent(Graphics g){
		super.paintComponent(g);
		setBackground(new java.awt.Color(255, 255, 204)); 
		ArrayList<Node> nodes=new ArrayList<Node>(Node.getTotal_population());
		Graphics2D tdg = (Graphics2D) g;
		for(Node node : nodes){
			float x_cor=(float) (start_x+range_x*node.getX_cord());
			float y_cor=(float) (start_y+range_y*node.getY_cord());
			if(node instanceof Hotel){
				Ellipse2D.Double shape = new Ellipse2D.Double(start_x+range_x*node.getX_cord(),start_y+range_y*node.getY_cord(), hotel_radius, hotel_radius);
				if(node.node_id==1){
					tdg.setColor(Color.RED);
				}else{
					tdg.setColor(Color.BLACK);
				}
		    	   //map hotel
				tdg.fill(shape);
				Font myFont = new Font("Serif", Font.BOLD, 15);
				tdg.setFont(myFont);
				tdg.drawString(Integer.toString(node.getNode_id()), x_cor,y_cor);
				
			}else{
				Ellipse2D.Double shape = new Ellipse2D.Double(start_x+range_x*node.getX_cord(), start_y+range_y*node.getY_cord(), poi_radius, poi_radius);
				tdg.setColor(Color.GREEN);                   
		    	   //map hotel
				tdg.fill(shape);
				tdg.setColor(Color.BLACK);
				Font myFont = new Font("Serif", Font.BOLD, 15);
				tdg.setFont(myFont);
				tdg.drawString(Integer.toString(node.getNode_id()), x_cor,y_cor);
				myFont = new Font("Serif", Font.BOLD, 20);
				tdg.setColor(Color.RED);
				tdg.setFont(myFont);
				Font mf = new Font("Serif", Font.BOLD, 20);
				tdg.setFont(mf);
				//tdg.drawString(Integer.toString(((POI)node).getScore()), 10+x_cor,10+y_cor);
			}
		}
		this.showEdges(g);
    } 
	
	public void showEdges(Graphics g){
		Graphics2D tdg = (Graphics2D) g;
		for(Trip t : tour.getTrips()){
			ArrayList<Node> nodes=t.getPermutation();
			for(int i=0; i<nodes.size()-1; i++){
				Shape l = new Line2D.Double(start_x+range_x*nodes.get(i).getX_cord(),start_y+range_y*nodes.get(i).getY_cord(),start_x+range_x*nodes.get(i+1).getX_cord(),start_y+range_y*nodes.get(i+1).getY_cord());
				tdg.setStroke(new BasicStroke(1));
				tdg.setColor(Color.BLACK);
				tdg.draw(l);
				Font myFont = new Font("Serif", Font.BOLD, 25);
				tdg.setFont(myFont);
				
			}
		}
		
	}
}
