package Computabillity;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Random;

import OPHSAttributes.Hotel;
import OPHSAttributes.Node;
import OPHSAttributes.Trip;

public class TourPopulationStructure {
	private int[][] population;
	public final int length;
	public final int sols=10;
	private final TreeMap<Integer,Integer> degrees;
	private final TreeMap<Integer,ArrayList<Integer>> column_part;
	
		public TourPopulationStructure(){
		this.length=Trip.getNumber_of_trips()+1;
		this.population=new int[sols][length];
		this.degrees=new TreeMap<Integer,Integer>();
		this.column_part=new TreeMap<Integer,ArrayList<Integer>>();
	}
	
	
	public int[][] getPopulation() {
		return population;
	}


	public void setPopulation(int[][] population) {
		this.population = population;
	}


	public void fillVertically(){
		for(int i=0; i<sols; i++){
			population[i][0]=0;
			population[i][length-1]=1;
			
		}
		for(Hotel h : Hotel.getHotel_population()){
			degrees.put(h.node_id,0);
			
		}
		for(int j=1; j<length-1; j++){
			fillCollumnRand(j);
			
		}
		
	}
	
	public void fillHorizontally(){
		for(int i=0; i<sols; i++){
			population[i][0]=0;
			population[i][length-1]=1;
			
		}
		for(int i=0; i<sols; i++){
			population[i][0]=0;
			population[i][length-1]=1;
			
		}
		modify();
		
	}
	public void applychanges(int row,int column){
		Hotel previous=Hotel.getHotelById(population[row][column-1]);
		ArrayList<Hotel> selections=previous.getKNearestFromSet(column-1);
		ArrayList<Hotel> forb=new ArrayList<Hotel>();
		for(int j=0; j<=column;  j++){
			forb.add(Hotel.getHotelById(population[row][j]));
		}
		if(column<length-2){
			forb.add(Hotel.getStartDepot());
		}
		Node x=pickOne(selections,forb);
		if(x!=null){
			population[row][column]=pickOne(selections,forb).node_id;
		}
		
		
		
	}
	
	public void modify(){
		for(int a=0; a<sols; a++){
			for(int row=0; row<sols; row++){
				for(int v=1; v<length; v++){
					for(int col=1; col<length; col++){
						applychanges(row,col);
					    // nextInt is normally exclusive of the top value,
					    // so add 1 to make it inclusive
					}
				}
				
			}}
	}
	
	public int getRandomNumber(int max, int min){
		Random rand=new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
		
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    
	    return randomNum;
	}
	
	public void fillSeq(int row){
		for (int col=1; col<length-1; col++){
			column_part.put(col, new ArrayList<Integer>());
			Hotel previous=Hotel.getHotelById(population[row][col-1]);//get previous hotel
			ArrayList<Hotel> hotel=previous.getKNearestFromSet(col-1);
			///////
			ArrayList<Hotel> forbidden=new ArrayList<Hotel>();
			for(int k=0; k<col; k++){
				forbidden.add(Hotel.getHotelById(population[row][k]));
			}
			population[row][col]=pick(hotel,forbidden,col,previous).node_id;
			if(!column_part.get(col).contains(population[row][col])){
				column_part.get(col).add(population[row][col]);
			}
		}
	}
	
	public Hotel pick(ArrayList<Hotel> list,ArrayList<Hotel> forbiden,int id,Hotel previous){
		for(Hotel h : forbiden){
			if(list.contains(h)){
				list.remove(h);
			}
		}
		Hotel r=null;
		for(Hotel h : list){
			if(h.node_id!=0){
				if(h.visitors_limits.get(id)!=null){
					for(Hotel z : h.visitors_limits.get(id)){
						if(!forbiden.contains(z)){
							r=h;
							break;
						}
					}
				}
			}
			
		}
		return r;	
	}
	
	
	
	public void fillCollumnRand(int c){
		int previous_c=c-1;
		for(int i=0; i<sols; i++){
		
			Hotel previous=Hotel.getHotelById(population[i][previous_c]);
			ArrayList<Hotel> hotel=previous.getKNearestFromSet(previous_c);
			ArrayList<Hotel> forbidden=new ArrayList<Hotel>();
			
			for(int k=0; k<c; k++){
				forbidden.add(Hotel.getHotelById(population[i][k]));
			}
			population[i][c]=pickOne(hotel,forbidden).node_id;
			for(int k=0; k<c; k++){
				forbidden.add(Hotel.getHotelById(population[i][k]));
			}
			degrees.replace(population[i][c], degrees.get(population[i][c])+1);	
		}
	}
	

	
	public void printGrid()
	{
	   for(int i = 0; i < sols; i++)
	   {
	      for(int j = 0; j < length; j++)
	      {
	         System.out.printf("%5d ", population[i][j]);
	      }
	      System.out.println();
	   }
	}
	
	public Hotel pickOne(ArrayList<Hotel> hotels,ArrayList<Hotel> tabu){
		TreeMap<Integer,Hotel> map=new TreeMap<Integer,Hotel>();
	
		for(Hotel z : tabu){
			if(hotels.contains(z)){
				hotels.remove(z);
			}
		}
		for(int i=0; i<hotels.size(); i++){
			map.put(i,hotels.get(i));
		}
		Random rand=new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
		
	    int randomNum = rand.nextInt((hotels.size() - 0) + 1) + 0;
	    while(map.get(randomNum)==null&&hotels.size()!=0){
	    	 randomNum = rand.nextInt((hotels.size() - 0) + 1) + 0;
	    }
	    return map.get(randomNum);
	}

	
}
