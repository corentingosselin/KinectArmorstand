package fr.cocoraid.kinectarmorstand.socket;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SocketReceivedPacket extends Event {



    private String packet;
    public SocketReceivedPacket(String packet) {
        this.packet = packet;

    }


    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getPacket() {
        return packet;
    }
}
