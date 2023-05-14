import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpServer2 implements HttpHandler {
    private static Filter filter;

    public static void main(String[] args) throws IOException {
        filter = new Filter(3, 10);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        HttpServer server = HttpServer.create(new InetSocketAddress("192.168.1.10", 8080), 0);
        server.createContext("/test", new HttpServer2());
        server.setExecutor(threadPoolExecutor);
        server.start();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestParamValue = null;
        String hostAddress = httpExchange.getRemoteAddress().getAddress().getHostAddress();
//        System.out.println(hostAddress);
        boolean filtered = filter.filter(hostAddress);
        System.out.println("Request " + (filtered ? "accepted" : "denied"));
        if (!filtered) {
            handleResponseError(httpExchange);
        }
        if ("GET".equals(httpExchange.getRequestMethod())) {
            requestParamValue = handleGetRequest(httpExchange);
        }

        handleResponseSuccess(httpExchange, requestParamValue);
    }

    private String handleGetRequest(HttpExchange httpExchange) {
        return httpExchange.
                getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("=")[1];
    }

    private void handleResponseSuccess(HttpExchange httpExchange, String requestParamValue) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.
                append("<h1>")
                .append("Hello ")
                .append(requestParamValue)
                .append("</h1>");
        // encode HTML content
        String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());

        // this line is a must
        httpExchange.sendResponseHeaders(200, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }
    private void handleResponseError(HttpExchange httpExchange) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("Request denied");
        // encode HTML content
        String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());

        // this line is a must
        httpExchange.sendResponseHeaders(999, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}