package fr.cocoraid.kinectarmorstand.socket;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer extends BukkitRunnable {
    private DatagramSocket udpSocket;

    private boolean started = true;

    public UDPServer() throws IOException {
        this.udpSocket = new DatagramSocket(7076);
        System.out.println("-- Running Server at " + InetAddress.getLocalHost() + "--");
    }

    @Override
    public void run() {

        try {
            //192.168.1.113

            String msg;
            while (started) {

               // long startTime = System.currentTimeMillis();

                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                // blocks until a packet is received
                udpSocket.receive(packet);
                msg = new String(packet.getData()).trim();

                SocketReceivedPacket event = new SocketReceivedPacket(msg);
                Bukkit.getPluginManager().callEvent(event);


               /* long duration = System.currentTimeMillis() - startTime;
                float delayMillis = 10;
                if (duration < delayMillis) {
                    try {
                        Thread.sleep((long) (delayMillis - duration));
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }*/
            }

            // System.out.println("Message from " + packet.getAddress().getHostAddress() + ": " + msg);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        started = false;
        udpSocket.close();

    }





}