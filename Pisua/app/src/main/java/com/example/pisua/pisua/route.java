package route_test;
import java.util.ArrayList;     
import java.util.List;     
    
    
public class route {     
    
    private static int INF=Integer.MAX_VALUE;     
         //dist[i][j]=INF<==>顶点i和j之间没有边     
    private int[][] dist;     
         //顶点i 到 j的最短路径长度，初值是i到j的边的权重       
    private int[][] path;       
    private List<Integer> result=new ArrayList<Integer>();     
         
    public static void main(String[] args) {     
        route graph=new route(18);     
        int[][] matrix={     
                {INF,5,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,},     
                {5,INF,5,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF},     
                {INF,5,INF,5,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF},     
                {INF,INF,5,INF,5,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF},     
                {INF,INF,INF,5,INF,5,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,5,INF,5,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,5,INF,5,INF,INF,5,INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,5,INF,5,INF,5,INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF,INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,INF,INF,5,INF,INF,INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF,5},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5},
                {INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,INF,5,INF,5,INF},
        };     
        int begin=13;     
        int end=17;     
        graph.findCheapestPath(begin,end,matrix);     
        List<Integer> list=graph.result;     
        System.out.println(begin+" to "+end+",the cheapest path is:");     
        System.out.println(list.toString());     
        System.out.println(graph.dist[begin][end]);     
    }     
    
    public void findCheapestPath(int begin,int end,int[][] matrix){     
        floyd(matrix);     
        result.add(begin);     
        findPath(begin,end);     
        result.add(end);     
    }     
         
    public void findPath(int i,int j){     
        int k=path[i][j];     
        if(k==-1)return;     
        findPath(i,k);   //递归  
        result.add(k);     
        findPath(k,j);     
    }     
    public void floyd(int[][] matrix){     
        int size=matrix.length;     
        //initialize dist and path     
        for(int i=0;i<size;i++){     
            for(int j=0;j<size;j++){     
                path[i][j]=-1;     
                dist[i][j]=matrix[i][j];     
            }     
        }     
        for(int k=0;k<size;k++){     
            for(int i=0;i<size;i++){     
                for(int j=0;j<size;j++){     
                    if(dist[i][k]!=INF&&     
                        dist[k][j]!=INF&&     
                        dist[i][k]+dist[k][j]<dist[i][j]){     
                        dist[i][j]=dist[i][k]+dist[k][j];     
                        path[i][j]=k;     
                    }     
                }     
            }     
        }     
             
    }     
         
    public route(int size){   //构造函数  
        this.path=new int[size][size];     
        this.dist=new int[size][size];     
    }     
}    