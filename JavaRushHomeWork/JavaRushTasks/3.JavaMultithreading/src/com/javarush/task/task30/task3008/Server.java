package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт: ");
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Не создался ServerSocket.");
        }
        ConsoleHelper.writeMessage("Сервер запушен!");
        assert serverSocket != null;
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Не создался Socket.");
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (socket != null && socket.getRemoteSocketAddress() != null) {
                ConsoleHelper.writeMessage("установлено новое соединение с удаленным адресом" + socket.getRemoteSocketAddress());
            }
            String name = null;
            try (Connection connection = new Connection(socket)) {
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                sendListOfUsers(connection, name);
                serverMainLoop(connection, name);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");
            } finally {
                if (name != null) {
                    connectionMap.remove(name);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                }
                ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто: " + socket.getRemoteSocketAddress());
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message answer = connection.receive();
                if (answer.getType() == MessageType.USER_NAME
                        && !answer.getData().isEmpty()
                        && !connectionMap.containsKey(answer.getData())) {
                    connectionMap.put(answer.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return answer.getData();
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> cM : connectionMap.entrySet()) {
                if (!cM.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, cM.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message answer = connection.receive();
                if (answer != null && answer.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + answer.getData()));
                } else {
                    ConsoleHelper.writeMessage("Сообщение не является текстом.");
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        try {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                pair.getValue().send(message);
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Сообщение не отправено!");
        }

    }
}
