import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    public static void main(String[] args) {
        Filter filter = new Filter(3, 10);
        ExecutorService executors = Executors.newFixedThreadPool(1);
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server started!");

            while (true) {
                // ожидаем подключения
                Socket socket = serverSocket.accept();
//                System.out.println("Client connected!");
                executors.execute(() -> {
                    boolean filtered = filter.filter("test");
                    System.out.println("Request " + (filtered ? "accepted" : "denied"));
                    // для подключившегося клиента открываем потоки
                    // чтения и записи
                    try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                         PrintWriter output = new PrintWriter(socket.getOutputStream())) {

                        // ждем первой строки запроса
//                    while (!input.ready()) ;

                        // считываем и печатаем все что было отправлено клиентом
//                    while (input.ready()) {
//                        System.out.println(input.readLine());
//                    }

                        // отправляем ответ
                        if (!filtered) {
                            output.println("HTTP/1.1 999 OK");
                            output.println("Content-Type: text/html; charset=utf-8");
                            output.println();
                            output.println("Запрос отклонен");
                        } else {
                            output.println("HTTP/1.1 200 OK");
                            output.println("Content-Type: text/html; charset=utf-8");
                            output.println();
                            output.println("<p>Привет всем!</p>");
                        }
                        output.flush();
//                        Thread.sleep(2000);

                        // по окончанию выполнения блока try-with-resources потоки,
                        // а вместе с ними и соединение будут закрыты
//                    System.out.println("Client disconnected!");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}