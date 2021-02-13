package fr.cocoraid.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {


    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int port;
    private Scanner scanner;

    public UDPClient() throws IOException {
        this.serverAddress = InetAddress.getByName("192.168.1.113");
        this.port = 7076;
        udpSocket = new DatagramSocket(this.port);
        scanner = new Scanner(System.in);
    }

    public void sendMessage(String message) {

        DatagramPacket p = new DatagramPacket(message.getBytes(), message.getBytes().length, serverAddress, port);
        try {
            this.udpSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int start() throws IOException {
        String in;
        while (true) {
            in = scanner.nextLine();

            DatagramPacket p = new DatagramPacket(in.getBytes(), in.getBytes().length, serverAddress, port);

            this.udpSocket.send(p);
        }
    }


}