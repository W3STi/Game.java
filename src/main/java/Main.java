import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    public static enum State { sleep, search, game, };
    public static enum Reason { wait_game, wait_turn };
    public static ArrayList<InetAddress> players = new ArrayList<>();

    public static InetAddress getEnemyAddress(InetAddress adress) {
        return players.get(0).equals(adress) ? players.get(1) : players.get(0);
    }

    public static Game game = null;

    public static class ClientState {
        public State state = State.sleep;
        public boolean side = false;

        public ClientState() {

        }

        public ClientState(State _state){
            state = _state;
        }

        public ClientState(State _state, boolean _side){
            state = _state;
            side = _side;
        }

        public boolean getSide() {
            return side;
        }

        public void setSide(boolean _side) {
            side = _side;
        }
    }

    public static class WaitResponse {
        HttpExchange http;
        Reason reason;

        public WaitResponse(HttpExchange _http, Reason _reason) {
            http = _http;
            reason = _reason;
        }
    }

    public static ArrayList<WaitResponse> wait_list = new ArrayList<>();
    public static Map<InetAddress, ClientState> ClientList = new HashMap<>();

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/" ,  new StaticHandler("/", "C:\\Users\\kolob\\IdeaProjects\\Chess\\client\\"));
            server.setExecutor((ThreadPoolExecutor)Executors.newFixedThreadPool(10));
            server.start();
            System.out.println(" Server started on port 8080");
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
