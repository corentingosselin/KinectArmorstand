package fr.cocoraid.kinectarmorstand.spigot;

import fr.cocoraid.kinectarmorstand.ArmorStandMovement;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.LinkedList;

public class ConvertedArmorStand {

    public Location loc;
    private EntityArmorStand as;
    private LinkedList<ArmorStandMovement> movements = new LinkedList<>();

    private void sendPacket(Packet packet) {
        Bukkit.getOnlinePlayers().forEach(cur -> ((CraftPlayer)cur).getHandle().playerConnection.sendPacket(packet));
    }

    public ConvertedArmorStand(Location location, LinkedList<String> list) {
        this.loc = location;



        as = new EntityArmorStand(((CraftWorld)loc.getWorld()).getHandle());
        as.setLocation(loc.getX(),loc.getY(),loc.getZ(),loc.getYaw(),loc.getPitch());
        as.setArms(true);
        as.setBasePlate(true);




        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getPlayer("cocoraid"));
        item.setItemMeta(meta);


        PacketPlayOutEntityEquipment equi = new PacketPlayOutEntityEquipment(as.getId(), EnumItemSlot.HEAD,  CraftItemStack.asNMSCopy(item));
        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(as);
        sendPacket(spawn);
        sendPacket(equi);

        list.forEach(s -> {
            String[] decoded = s.split(":");
            ArmorStandMovement movement = new ArmorStandMovement();
            movement.convert(decoded);
            movements.add(movement);
        });


    }


    public void nextMovement(short tick) {
        ArmorStandMovement move = movements.get(tick);


        as.setHeadPose(new Vector3f(move.headPitch,0,0));
        as.setRightArmPose(new Vector3f(-move.right_armX,move.right_armY,0));
        as.setLeftArmPose(new Vector3f(-move.left_armX,move.left_armY,0));

        as.setRightLegPose( new Vector3f(move.right_legX,move.right_legY,0));
        as.setLeftLegPose(new Vector3f(move.left_legX,move.left_legY,0));

        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(as.getId(),as.getDataWatcher(),true);

        as.setLocation(as.locX - move.vecZ, as.locY - move.vecY , as.locZ - move.vecX,(float) (loc.getYaw() - (move.yaw * 0.8)),  move.pitch + 10);
        PacketPlayOutEntityTeleport tp = new PacketPlayOutEntityTeleport(as);

        sendPacket(meta);
        sendPacket(tp);

    }

    private byte a(byte b0, final int i, final boolean flag) {
        if (flag) {
            b0 |= (byte)i;
        }
        else {
            b0 &= (byte)~i;
        }
        return b0;
    }
}
