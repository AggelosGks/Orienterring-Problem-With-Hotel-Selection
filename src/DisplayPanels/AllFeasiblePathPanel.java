
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
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JPanel;

import HeurAlgorithms.AllFeasiblePathsAlgorithm;
import OPHSAttributes.Hotel;
import OPHSAttributes.Node;
import OPHSAttributes.OPHSGraph;
import OPHSAttributes.Trip;

public class AllFeasiblePathPanel extends JPanel{
	public AllFeasiblePathPanel() {

	}
	public AllFeasiblePathPanel(int trip_restart){
		trip_id_shown=0;
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
	private static int trip_id_shown=0;
	
	@Override
    public void paintComponent(Graphics g){
		super.paintComponent(g);
		setBackground(new java.awt.Color(255, 255, 204)); 
		ArrayList<Hotel> hotels=new ArrayList<Hotel>(Hotel.getHotel_population());
		Graphics2D tdg = (Graphics2D) g;
		TreeMap<Integer,Color> map_color=new TreeMap<Integer,Color>();
		int number_of_trips=Trip.getNumber_of_trips();
		ArrayList<Node> nodes=new ArrayList<Node>(Node.getTotal_population());
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
		AllFeasiblePathsAlgorithm algo=new AllFeasiblePathsAlgorithm(OPHSGraph.getGraph());
		algo.execute();
			for(Hotel hotel: hotels){
				
				if(hotel.visitors_limits.get(trip_id_shown)!=null){
					for(Hotel hotel_end : hotel.visitors_limits.get(trip_id_shown)){
						Shape l = new Line2D.Double(start_x+range_x*hotel.getX_cord(),start_y+range_y*hotel.getY_cord(),start_x+range_x*hotel_end.getX_cord(),start_y+range_y*hotel_end.getY_cord());
						tdg.setStroke(new BasicStroke(3));
						tdg.setColor(Color.BLACK);
						tdg.draw(l);
					}
				}
			}
    }

	public static int getTrip_id_shown() {
		return trip_id_shown;
	}

	public static void increase() {
		AllFeasiblePathPanel.trip_id_shown++;
	}
	public static void decrease() {
		AllFeasiblePathPanel.trip_id_shown--;
	} 
	
}

