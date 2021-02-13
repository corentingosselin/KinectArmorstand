package fr.cocoraid.kinectarmorstand.spigot;

import fr.cocoraid.kinectarmorstand.ArmorStandMovement;
import fr.cocoraid.kinectarmorstand.KinectArmorStand;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

public class MovingArmorStand {



    private Location loc;
    private EntityArmorStand armorStand;
    private ArmorStand as;

    private double x,y,z,pitch,rotation;


    private ArmorStand look;
    private Player player;
    public MovingArmorStand(Location l) {
        //Place armorstand far away
        Location location = l.clone();
        location.setPitch(0);

        this.loc = location.clone().add(location.getDirection().multiply(4));
        //Inverse the rotation of the armorstand
        loc.setDirection(location.getDirection().multiply(-1));


    }

    private void sendPacket(Packet packet) {
        Bukkit.getOnlinePlayers().forEach(cur -> ((CraftPlayer)cur).getHandle().playerConnection.sendPacket(packet));
    }

    public void spawn(Player target) {
        this.player = target;

        /*as = loc.getWorld().spawn(loc,ArmorStand.class);
        as.setArms(true);
        as.setBasePlate(false);
        as.setGravity(false);


        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(target);
        item.setItemMeta(meta);
        as.setHelmet(new ItemStack(item));*/




        armorStand = new EntityArmorStand(((CraftWorld)loc.getWorld()).getHandle());
        armorStand.setLocation(loc.getX(),loc.getY(),loc.getZ(),loc.getYaw(),loc.getPitch());
        armorStand.setArms(true);
        armorStand.setBasePlate(false);

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(target);
        item.setItemMeta(meta);


        PacketPlayOutEntityEquipment equi = new PacketPlayOutEntityEquipment(armorStand.getId(),EnumItemSlot.HEAD,  CraftItemStack.asNMSCopy(item));
        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(armorStand);
        sendPacket(spawn);
        sendPacket(equi);
    }


    //Maybe a possibility to rotate pitch with packet ??
    public void update(String packet) {

        String[] decoded = packet.split(":");
        //System.out.println(packet);

        ArmorStandMovement move = new ArmorStandMovement(decoded);

        if(KinectArmorStand.getInstance().isStartSaving()) {
            KinectArmorStand.getInstance().getCache().add(move);
        }


        armorStand.setHeadPose(new Vector3f(move.headPitch,0,0));

        armorStand.setRightArmPose(new Vector3f(-move.right_armX,move.right_armY,0));
        armorStand.setLeftArmPose(new Vector3f(-move.left_armX,move.left_armY,0));

        armorStand.setRightLegPose( new Vector3f(move.right_legX,move.right_legY,0));
        armorStand.setLeftLegPose(new Vector3f(move.left_legX,move.left_legY,0));
        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(armorStand.getId(),armorStand.getDataWatcher(),true);

        armorStand.setLocation(armorStand.locX - move.vecZ, armorStand.locY - move.vecY , armorStand.locZ - move.vecX,(float) (loc.getYaw() - (move.yaw * 0.8)),  move.pitch + 10);
        PacketPlayOutEntityTeleport tp = new PacketPlayOutEntityTeleport(armorStand);

        sendPacket(meta);
        sendPacket(tp);



       /* as.setHeadPose(new EulerAngle(Math.toRadians(move.headPitch),0,0));

        as.setRightArmPose(new EulerAngle(Math.toRadians(-move.right_armX),Math.toRadians(move.right_armY),0));
        as.setLeftArmPose(new EulerAngle(Math.toRadians(-move.left_armX),Math.toRadians(move.left_armY),0));

        as.setRightLegPose( new EulerAngle(Math.toRadians(move.right_legX),Math.toRadians(move.right_legY),0));
        as.setLeftLegPose(new EulerAngle(Math.toRadians(move.left_legX),Math.toRadians(move.left_legY),0));


        ((CraftEntity)as).getHandle().setLocation(as.getLocation().getX() - move.vecZ, as.getLocation().getY() - move.vecY , as.getLocation().getZ() - move.vecX,(float) (loc.getYaw() - (move.yaw * 0.8)), (float) move.pitch + 10);
        PacketPlayOutEntityTeleport tp = new PacketPlayOutEntityTeleport(((CraftEntity)as).getHandle());
        sendPacket(tp);*/


    }


}
