import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import org.json.*;

import java.util.*;

import java.net.InetAddress;

public class StaticHandler implements HttpHandler {
    final private String rpath;
    final private String fsPath;

    final private Map<String, String> headers = new HashMap<String, String>(){{
        put("html", "text/html");
        put("css", "text/css");
        put("js", "text/javascript");
        put("json", "application/json");
        put("svg", "image/svg+xml");
    }};

    public StaticHandler(String _path, String _fsPath){
        rpath = _path;
        fsPath = _fsPath;
    }

    private void handleGetRequest(HttpExchange http) throws IOException{
        OutputStream outputStream = http.getResponseBody();
        http.getRequestBody();
        String request = http.getRequestURI().getRawPath();
        if(request.length() == 1) {
            request = "/main.html";
        }
        byte[] result;
        int code;

        try {
            try {
                String path = fsPath + request.substring(rpath.length());
                result = read(new FileInputStream(path)).toByteArray();
                String ext = request.substring(request.lastIndexOf(".") + 1);
                if (headers.containsKey(ext))
                    http.getResponseHeaders().add("Content-Type", headers.get(ext));
                code = 200;
            } catch (IOException e) {
                result = (404 + " " + request).getBytes();
                code = 404;
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            result = sw.getBuffer().toString().getBytes();
            code = 500;
        }

        http.sendResponseHeaders(code, result.length);
        outputStream.write(result);
        outputStream.close();
    }

    private void handlePostRequest(HttpExchange http) throws IOException{
        boolean sendResponse = true;
        String str = read(http.getRequestBody()).toString();
        JSONObject obj = new JSONObject(str);
        str = obj.get("type").toString();
        int code = 200;
        switch (str) {
            case "search":
                ArrayList<InetAddress> checkList = check(Main.State.game);
                if (checkList.size() > 0) {
                    Main.wait_list.clear();
                    Main.ClientList.clear();
                    Game.flagOfMate = false;
                }

                ArrayList<InetAddress> search = check(Main.State.search);
                if(search.size() > 0) {
                    /*Objects.equals(search.get(0), http.getRemoteAddress().getAddress())*/
                    if(search.get(0) == http.getRemoteAddress().getAddress()){
                        return;
                    }

                    Main.game = new Game();

                    Random random = new Random();
                    boolean rand = random.nextBoolean();

                    Main.players.add(search.get(0));
                    Main.players.add(http.getRemoteAddress().getAddress());

                    add(search.get(0), Main.State.game, rand);
                    add(http.getRemoteAddress().getAddress(), Main.State.game, !rand);

                    JSONObject json = new JSONObject();
                    json.put("side", rand ? "white" : "black");
                    str = json.toString();
                    customSendResponse(search(search.get(0), Main.Reason.wait_game,true), 200, str);
                    json.clear();
                    json.put("side", rand ? "black" : "white");
                    str = json.toString();
                    Main.wait_list.clear();
                }
                else {
                    add(http.getRemoteAddress().getAddress(), Main.State.search);
                    Main.wait_list.add(new Main.WaitResponse(http, Main.Reason.wait_game));
                    sendResponse = false;
                }
                break;
            /*case "getdraw":
                JSONObject json2 = new JSONObject();
                json2.put("draw", "agreement");
                str = json2.toString();
                break;
            case "getsurrend":
                JSONObject sur = new JSONObject();
                sur.put("type", "sur");
                str = sur.toString();
                System.out.println(sur);
                Game.flagOfMate = true;
                CustomSendResponse(SearchInWaitList(Main.getEnemyAddress(http.getRemoteAddress().getAddress()),
                        Main.Reason.wait_sur, true), 200, str);
                sendResponse = false;
                break;
            case "setsurrend":
                Main.wait_list.add(new Main.WaitResponse(http, Main.Reason.wait_sur));
                sendResponse = false;
                break;*/
            case "wait":
                Main.wait_list.add(new Main.WaitResponse(http, Main.Reason.wait_turn));
                sendResponse = false;
                break;
            case "turn":
                JSONObject crd = obj.getJSONObject("coord");
                JSONObject json4 = new JSONObject();
                String resMoving = Main.game.moveFigure(crd.getInt("from_y"), crd.getInt("from_x"), crd.getInt("to_y"),
                        crd.getInt("to_x"), crd.getString("choose"));
                String res = (Game.flagOfMate) ? "true" : "false";
                json4.put("turn", resMoving);
                json4.put("from_y", crd.getInt("from_y"));
                json4.put("from_x", crd.getInt("from_x"));
                json4.put("to_y", crd.getInt("to_y"));
                json4.put("to_x", crd.getInt("to_x"));
                json4.put("choose", crd.getString("choose"));
                json4.put("mate", res);
                str = json4.toString();
                if (!resMoving.equals("false")) {
                    customSendResponse(search(Main.getEnemyAddress(http.getRemoteAddress().getAddress()),
                            Main.Reason.wait_turn, true), 200, str);
                }
                str = "{ \"turn\": \"" + resMoving + "\", \"mate\": \"" + res + "\" }";
                break;
            default:
                code = 404;
                break;
        }
        if(sendResponse)
            customSendResponse(http, code, str);
    }

    @Override
    public void handle(HttpExchange http) throws IOException{
        if("GET".equals(http.getRequestMethod())) {
            handleGetRequest(http);
        } else if("POST".equals(http.getRequestMethod())) {
            handlePostRequest(http);
        }

    }

    public HttpExchange search(InetAddress adr, Main.Reason reason, boolean del){
        HttpExchange result;
        for(int i = 0; i < Main.wait_list.size(); i++){
            Main.WaitResponse temp = Main.wait_list.get(i);
            if(temp.http.getRemoteAddress().getAddress().equals(adr) &&
                    temp.reason.equals(reason)){
                result = temp.http;
                if(del)
                    Main.wait_list.remove(i);
                return result;
            }
        }
        return null;
    }

    public void customSendResponse(HttpExchange http, int code, String str) throws IOException{
        OutputStream out = http.getResponseBody();
        http.getResponseHeaders().add("Content-Type", "application/json");
        http.sendResponseHeaders(code, str.length());
        out.write(str.getBytes());
        out.close();
    }

    public void add(InetAddress adr, Main.State state) {
        if(Main.ClientList.containsKey(adr)){
            Main.ClientList.get(adr).state = state;
        }else{
            Main.ClientList.put(adr, new Main.ClientState(state));
        }
    }

    public void add(InetAddress adr, Main.State state, boolean side) {
        if(Main.ClientList.containsKey(adr)) {
            Main.ClientList.get(adr).state = state;
            Main.ClientList.get(adr).setSide(side);
        }
        else {
            Main.ClientList.put(adr, new Main.ClientState(state));
        }
    }

    public ArrayList<InetAddress> check(Main.State state){
        ArrayList<InetAddress> result = new ArrayList<>();
        Set<Map.Entry<InetAddress, Main.ClientState>> entrySet = Main.ClientList.entrySet();

        for (Map.Entry<InetAddress, Main.ClientState> pair : entrySet) {
            if (pair.getValue().state == state) {
                result.add(pair.getKey());
            }
        }
        return result;
    }

    static ByteArrayOutputStream read(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer;
    }
}

