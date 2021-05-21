package OPHSAttributes;



	public  class NodeWithScore implements Comparable<NodeWithScore> {
		
		public final POI node;
		public final int score;
		
		
		public NodeWithScore(POI node) {
			this.node = node;
			this.score=node.getScore();
		}
		
		
		
		public int compareTo(NodeWithScore other) {
			//check distances first
			if (score < other.score)
				return 1;
			else if (score > other.score)
				return -1;
			//if distances are the same break the tie with node with maximal score
			else if(node.node_id<other.node.node_id){
				return 1;
			}else if(node.node_id>other.node.node_id){
				return -1;
			}
			else
				return 0;
		}
		
	}