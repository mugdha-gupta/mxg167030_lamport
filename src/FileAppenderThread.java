import java.net.Socket;

public class FileAppenderThread implements Runnable {
    private Socket socket;

    FileAppenderThread(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
