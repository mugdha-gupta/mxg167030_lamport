/*
 * Reference for server client relationship: https://cs.lmu.edu/~ray/notes/javanetexamples/#capitalize
 */
public class FileServer {
    static int SERVER_PORT = 11888;

    static String serverOneAddress = "dc01.utdallas.edu";
    static String serverTwoAddress = "dc02.utdallas.edu";
    static String serverThreeAddress = "dc03.utdallas.edu";

    public static void main(String[] args) throws Exception {

        if(args.length != 1){
            System.err.println("Please pass which server number this should be");
            return;
        }

        int serverNum = Integer.parseInt(args[0]);

        switch (serverNum){
            case 1:
                Server1 server1 = new Server1();
                break;
            case 2:
                Server2 server2 = new Server2();
                break;
            case 3:
                Server3 server3 = new Server3();

                break;
            default:
                System.err.println("Server number is invalid");
        }

    }


}
