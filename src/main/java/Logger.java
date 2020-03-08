import java.util.Date;

public class Logger {

    static void logMsg(String msg){
        System.out.println("[ " + (new Date()).toString() + " ] - " + msg);
    }

    static void logError(String msg){
        System.err.println("[ " + (new Date()).toString() + " ] - ERROR - " + msg);
    }

}
