import java.io.*;
import java.net.Socket;
import java.util.*;


public class Session implements Runnable {

    private Socket socket;
    private InputStream in = null;
    private OutputStream out = null;
    private HttpRequest httpRequest = null;

    enum httpMethods {
        GET,
        POST,
        OPTIONS
    }

    Session(Socket socket) throws IOException {
        this.socket = socket;
        initialize();
    }

    private void initialize() throws IOException {
        in = socket.getInputStream();
        out = socket.getOutputStream();
        httpRequest =new HttpRequest();
    }

    @Override
    public void run() {
        try {
            readRequest();
            if (httpRequest.getHeader().equals("")) return;
            requestProcessing(httpRequest.getHeader());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestProcessing(String header) throws IOException {
        System.out.println(httpRequest.getHeader()+ "\n"+httpRequest.getBody());
        String method = header.substring(0,header.indexOf(" "));
        httpRequest.setHttpMethod(httpMethods.valueOf(method));
        String url = getURIFromHeader(httpRequest.getHeader());
        System.out.println("Resource: " + url + "\n");
        int code = getResponseCode(url);
        System.out.println("Result code: " + code + "\n");
        if (code!=200) return;

        if(method.equals(httpMethods.GET.toString())) {
            sendData(code,url);
            Map<String, String> parameters = getParametersFromHeader(getStrWithParametersFromHeader(header));
            if (parameters!=null) {
                out.write(System.getProperty("line.separator").getBytes());
                sendParameters(parameters);
            }
        }
        else if (method.equals(httpMethods.POST.toString()))
        {
                Map<String, String> parameters = getParametersFromHeader(getStrWithParametersFromHeader(header));
                if (parameters!=null)
                sendParameters(parameters);
                int index = httpRequest.getHeader().indexOf("Content-Type: ");

                if (httpRequest.getHeader().substring(
                        index+14, httpRequest.getHeader().indexOf(
                                "\n",index)).equals("application/x-www-form-urlencoded")) {
                    parameters = getParametersFromHeader(httpRequest.getBody());
                    if (parameters!=null)
                    sendParameters(parameters);
                }
        }

    }

    private void readRequest() throws IOException {

        DataInputStream reader = new DataInputStream(in);
        String line = "";
        StringBuilder header = new StringBuilder();
        int len = 0;
        do {
            line = reader.readLine();
            if (line==null) return;
            if ( line.contains("Content-Length")) {
                len = Integer.parseInt(line.split("\\D+")[1]);

            }
            header.append(line).append('\n');
        } while (!line.isEmpty());

        StringBuilder body= new StringBuilder();

        if (len>0) {
            byte[] buf = new byte[len];
            reader.readFully(buf);
            for (byte b:buf)  body.append((char) b);

        }
        httpRequest.setHeader(header.toString());
        httpRequest.setBody(body.toString());

    }

    private String getURIFromHeader(String header) {

        int from = header.indexOf(" ") + 1;
        int to = header.indexOf(" ", from);
        String uri = header.substring(from, to);
        int paramIndex = uri.indexOf("?");
        if (paramIndex != -1) {
            uri = uri.substring(0, paramIndex);
        }
        return  uri;
    }

    private String getStrWithParametersFromHeader(String header) {
        String parametersStr = "";
        int paramIndex = header.indexOf("?");
        if (paramIndex != -1)
            parametersStr = header.substring(paramIndex + 1, header.indexOf(" ", paramIndex + 1));
        return parametersStr;
    }

    private Map<String ,String> getParametersFromHeader(String parametersStr)  {
        if (parametersStr.equals(""))return null;
        Map<String, String> parameters = new HashMap<>();
            String[] pairs = parametersStr.split("[&]");

            for(String pair: pairs) {
                String[] paramWithValue = pair.split("[=]");
                String key = null;
                String value = null;
                if (paramWithValue.length > 0) {
                    key =paramWithValue[0];
                }
                if (paramWithValue.length > 1) {
                    value = paramWithValue[1];
                }
                parameters.put(key, value);
            }
        return  parameters;
    }

    private void sendParameters(Map<String,String> parameters) throws IOException {
        if (parameters != null) {


            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                String str = parameter.getKey() + " = " + parameter.getValue() + System.getProperty("line.separator");
                out.write(str.getBytes());
            }
        }
    }

    private int getResponseCode(String url) throws IOException {
        InputStream strm = HttpServer.class.getResourceAsStream(url);
        int code = (strm != null) ? 200 : 404;
        String header = getServerResponseHeader(code);
        //PrintStream answer = new PrintStream(out, true, "UTF-8");
        out.write(header.getBytes());

        return code;
    }


    private void sendData(int code, String url ) throws IOException {
        InputStream strm = HttpServer.class.getResourceAsStream(url);
        if (code == 200) {
                int count;
                byte[] buffer = new byte[1024];
                while ((count = strm.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                //out.write(System.getProperty("line.separator").getBytes());
                strm.close();
        }
    }


    private String getServerResponseHeader(int code) {

        String response = "HTTP/1.1 " + code + " " + getAnswer(code) + "\n";

        if (httpMethods.OPTIONS == httpRequest.getHttpMethod()) {
            response += "Allow: ";

            for (httpMethods method : httpMethods.values()) {
                response += method.toString();
                if (method != httpMethods.OPTIONS) response += ", ";
            }
            response+="\n";
        }
           response+=  "Date: " + new Date().toString() + "\n" +
                   "Accept-Ranges: none\n" +
                   "\n";
        return response;
    }


    private String getAnswer(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 404:
                return "Not Found";
            default:
                return "Internal Server Error";
        }
    }

}