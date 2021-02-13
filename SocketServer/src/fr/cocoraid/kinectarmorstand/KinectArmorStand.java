package fr.cocoraid.kinectarmorstand;

import fr.cocoraid.kinectarmorstand.socket.SocketReceivedPacket;
import fr.cocoraid.kinectarmorstand.socket.UDPServer;
import fr.cocoraid.kinectarmorstand.spigot.ConvertedArmorStand;
import fr.cocoraid.kinectarmorstand.spigot.MovingArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class KinectArmorStand extends JavaPlugin implements Listener {

    private static KinectArmorStand instance;
    private MovingArmorStand armorstand;
    private UDPServer server;
    private BukkitTask task;
    private boolean startSaving = false;

    private LinkedList<ArmorStandMovement> cache = new LinkedList<>();

    private ConvertedArmorStand convertedArmorStand;

    @Override
    public void onEnable() {
        instance = this;


        try {
            server = new UDPServer();
            server.runTaskAsynchronously(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(this, this);

    }



    @EventHandler
    public void onSocket(SocketReceivedPacket e) {
        if (armorstand != null) {
            armorstand.update(e.getPacket());

        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (command.getName().equalsIgnoreCase("kinect")) {
                if (args.length == 0) {
                    armorstand = new MovingArmorStand(p.getLocation());
                    armorstand.spawn(p);
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("save")) {
                        startSaving = true;
                    } else if (args[0].equalsIgnoreCase("stop")) {
                        startSaving = false;
                        convertedArmorStand = null;
                    } else if (args[0].equalsIgnoreCase("file")) {
                        startSaving = false;
                        LinkedList<String> list = new LinkedList<>();
                        cache.forEach(c -> {
                            String line = c.headPitch + ":" +
                                    c.right_armX + ":" + c.right_armY  + ":" +
                                    c.left_armX + ":" + c.left_armY + ":" +
                                    c.right_legX + ":" + c.right_legY + ":" +
                                    c.left_legX + ":" + c.left_legY + ":" +
                                    c.vecX + ":" + c.vecY + ":" + c.vecZ + ":" + c.yaw + ":" + c.pitch;
                            list.add(line);
                        });
                        try {
                            createFile("armorstand", list);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (args[0].equalsIgnoreCase("test")) {
                        try {
                            Scanner s = new Scanner(new File("armorstand.txt"));
                            LinkedList<String> list = new LinkedList<>();
                            while (s.hasNext()) {
                                list.add(s.next());
                            }
                            s.close();
                            convertedArmorStand = new ConvertedArmorStand(p.getLocation(), list);


                            task = Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
                                short tick = 0;
                                @Override
                                public void run() {
                                    while (convertedArmorStand != null) {
                                        long startTime = System.currentTimeMillis();



                                        tick++;
                                        if (tick > list.size() - 1) {
                                            convertedArmorStand = null;
                                            return;
                                        }

                                        convertedArmorStand.nextMovement(tick);


                                        long duration = System.currentTimeMillis() - startTime;
                                        float delayMillis = 30;
                                        if (duration < delayMillis) {
                                            try {
                                                Thread.sleep((long) (delayMillis - duration));
                                            } catch (InterruptedException e) {
                                                // do nothing
                                            }
                                        }
                                    }
                                }
                            });
                        } catch(FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        return false;
    }


    private void createFile(String file, LinkedList<String> arrData)
            throws IOException {
        FileWriter writer = new FileWriter(file + ".txt");
        int size = arrData.size();
        for (int i = 0; i < size; i++) {
            String str = arrData.get(i).toString();
            writer.write(str);
            if (i < size - 1)
                writer.write("\n");
        }
        writer.close();
    }

    @Override
    public void onDisable() {
        convertedArmorStand = null;
        if(task != null)
            task.cancel();
        server.stop();
    }


    public static KinectArmorStand getInstance() {
        return instance;
    }


    public LinkedList<ArmorStandMovement> getCache() {
        return cache;
    }


    public boolean isStartSaving() {
        return startSaving;
    }
}
