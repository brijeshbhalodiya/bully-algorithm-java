import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
        try {
            int priority = 1;
            ServerListener server;
            String envPriority = System.getenv("PRIORITY");

            if(envPriority.length() != 0){
                try {
                    priority = Integer.parseInt(envPriority);
                }catch(Exception ex){
                    Logger.logError("Invalid priority must be integer");
                    System.exit(-1);
                }
            }

            //Get whether server want to join the cluster or want to create new cluster
            // if true -> Join cluster
            String envWantToJoin = System.getenv("JOINCLUSTER");

            //if server want to join cluster than IP of node which belongs to cluster
            String hostAddress = System.getenv("CLUSTER_NODE_IP");

            if(envWantToJoin.equalsIgnoreCase("true")){
                server = new ServerListener(hostAddress, priority);
            }else{
                server = new ServerListener(priority);
            }

            server.start();
        } catch (Exception e) {
            Logger.logMsg("Error when starting the server");
        }
    }
}