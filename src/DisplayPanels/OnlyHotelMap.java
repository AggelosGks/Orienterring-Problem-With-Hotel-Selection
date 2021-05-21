package DisplayPanels;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import OPHSAttributes.Hotel;
import OPHSAttributes.Node;

public class OnlyHotelMap extends JPanel{
	public OnlyHotelMap() {
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static int start_x=0;
	final static int start_y=0;
	final static int range_y=20;
	final static int range_x=30;
	final static int hotel_radius=25;
	final static int poi_radius=15;
	
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
				tdg.setColor(Color.BLACK);
		    	   //map hotel
				tdg.fill(shape);
				Font myFont = new Font("Serif", Font.BOLD, 18);
				tdg.setFont(myFont);
				tdg.drawString(Integer.toString(node.getNode_id()), x_cor,y_cor);
				
			}
		}
		
    } 
}

