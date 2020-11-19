package client;

import modele.Message;
import modele.Protocol;
import modele.Rename;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ChatClientUDPReceiveThread extends Thread {
    private final DatagramSocket socket;

    private boolean shouldStop = false;

    ChatClientUDPReceiveThread(DatagramSocket socket) throws IOException {
        this.socket = socket;
    }

    public void endThread() {
        shouldStop = true;
    }

    public void run() {
        String line = null;
        String[] arguments;

        byte[] buffer = new byte[1024];
        while (!shouldStop) {
            DatagramPacket incomingDatagramPacket = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(incomingDatagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            line = new String(
                    incomingDatagramPacket.getData(),
                    incomingDatagramPacket.getOffset(),
                    incomingDatagramPacket.getLength()
            );

            // Parse the input command
            arguments = line.split("\u0000", 2);

            String commande = arguments.length > 0 ? arguments[0] : "";
            String parametres = arguments.length > 1 ? arguments[1] : "";

            switch (commande) {
                case "msg":
                    Message message = Protocol.readMessage(parametres);
                    System.out.println(message.toString());
                    break;
                case "rename":
                    Rename rename = Protocol.readRename(parametres);
                    System.out.println(rename.toString());
                    break;
                case "protocol error":
                    System.err.println("Erreur de protocole");
                    return;
                case "disconnect":
                    System.err.println("Vous avez été déconnecté.e");
                    return;
                default:
                    System.err.println("Commande serveur inconnue: " + line);
                    return; // stop the thread
            }
        }
    }
}
