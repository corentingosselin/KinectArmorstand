package fr.cocoraid.socket.kinect;

import edu.ufl.digitalworlds.j4k.DepthMap;
import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.j4k.Skeleton;
import fr.cocoraid.socket.UDPClient;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;


/*
 * Copyright 2011, Digital Worlds Institute, University of
 * Florida, Angelos Barmpoutis.
 * All rights reserved.
 *
 * When this program is used for academic or research purposes,
 * please cite the following article that introduced this Java library:
 *
 * A. Barmpoutis. "Tensor Body: Real-time Reconstruction of the Human Body
 * and Avatar Synthesis from RGB-D', IEEE Transactions on Cybernetics,
 * October 2013, Vol. 43(5), Pages: 1347-1356.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain this copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce this
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Kinect extends J4KSDK{

    ViewerPanel3D viewer=null;
    JLabel label=null;
    boolean mask_players=true;

    public void maskPlayers(boolean flag){mask_players=flag;}

    private UDPClient client;
    public Kinect(UDPClient client) {
        super();
        this.client = client;
    }

    public Kinect(int index)
    {
        super(index);
    }

    public void setViewer(ViewerPanel3D viewer){this.viewer=viewer;}

    public void setLabel(JLabel l){this.label=l;}


    @Override
    public void onDepthFrameEvent(short[] depth, int[] U, int V[]) {
        if(viewer==null || label==null)return;
        float a[]=getAccelerometerReading();
        label.setText(((int)(a[0]*100)/100f)+","+((int)(a[1]*100)/100f)+","+((int)(a[2]*100)/100f));
        DepthMap map=new DepthMap(depthWidth(),depthHeight(),depth);
        if(U!=null && V!=null) map.setUV(U,V,videoWidth(),videoHeight());
        if(mask_players)map.maskPlayers();
        viewer.map=map;
    }

    private boolean wait = false;
    @Override
    public void onSkeletonFrameEvent(float[] data, boolean[] flags) {
        if(viewer==null || viewer.skeletons==null)return;
        for(int i = 0; i< Kinect.NUI_SKELETON_COUNT; i++) {
            viewer.skeletons[i] = Skeleton.getSkeleton(i, data, flags);
            try {
                if(!wait) {
                    Thread.sleep(5000);
                    wait = true;
                }
                moveArmorstand(viewer.skeletons[i]);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

    }






    int yawGlitchHead = 90;
    int pitchGlitchHead = -180;
    public String getHeadAngle(Skeleton skeleton, boolean normalized) {
        if (!skeleton.isTracked()) {
            return "null:null";
        } else {
            double[] neck = skeleton.get3DJoint(3);
            double[] head = skeleton.get3DJoint(2);

            double[] vector = new double[3];
            int index = 0;
            for (double h : head) {
                vector[index] = neck[index] - h;
                index++;
            }
            double dZ = vector[2];
            double dY = vector[1];
            double dX = vector[0];


            //warning remove Math.toDegree, because euler angle need radian
            // I used degree for testing
            double yaw = Math.atan2(dZ, dX);
            double pitch = Math.atan2(Math.sqrt((dZ * dZ) + (dX * dX)), dY) + Math.PI;

            //normalized should send yaw and pitch but we only need pitch
            //   return String.valueOf(normalize(Math.round(Math.toDegrees(yaw)) + yawGlitchHead))
            //                        + ":" + String.valueOf(normalize(Math.round(Math.toDegrees(pitch)) + pitchGlitchHead));
            if(normalized)
                return String.valueOf(normalize(Math.round(Math.toDegrees(pitch)) + pitchGlitchHead));
            else
                return String.valueOf(Math.round(Math.toDegrees(pitch) + pitchGlitchHead));
        }
    }





    //Im not sure, maybe using knee instead of ankle is better , (lol knee was better)
    private enum BodyPart {
        RIGHT_ARM(Skeleton.WRIST_RIGHT, Skeleton.ELBOW_RIGHT),LEFT_ARM(Skeleton.WRIST_LEFT, Skeleton.ELBOW_LEFT),RIGHT_LEG(Skeleton.KNEE_RIGHT,Skeleton.HIP_RIGHT),LEFT_LEG(Skeleton.KNEE_LEFT,Skeleton.HIP_LEFT);
        private int start;
        private int end;

        BodyPart(int end, int start) {
            this.end = end;
            this.start = start;
        }
    }

    //Orientation + distance of the kinect is important and change the value added for yaw and pitch
    public String getEulerAngle(Skeleton skeleton, BodyPart part) {
        if (!skeleton.isTracked()) {
            return "null:null";
        } else {
            double[] startPart = skeleton.get3DJoint(part.start);
            double[] endPart = skeleton.get3DJoint(part.end);

            double[] vector = new double[3];
            int index = 0;
            for (double start : startPart) {
                vector[index] = start - endPart[index];
                index++;
            }
            double dZ = vector[2];
            double dY = vector[1];
            double dX = vector[0];

            //Here we need to reset yaw and pitch (default = player is not moving arm and leg)
            int yawDebug = 0;
            int pitchDebug = 0;
            if(part == BodyPart.RIGHT_ARM || part == BodyPart.LEFT_ARM) {
                yawDebug = -100;
                pitchDebug = -190;
            } else {
                yawDebug = -240;
                pitchDebug = -185;
            }

            double yaw = Math.atan2(dZ, dX);
            double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;

            return String.valueOf(normalize(Math.round(Math.toDegrees(yaw) + yawDebug))) + ":" + String.valueOf(normalize(Math.round(Math.toDegrees(pitch) + pitchDebug)));

        }
    }

    private int normalize(double angle) {
        double newangle = angle % 360;
        if(newangle < 0)
            newangle+=360;
        return (int) newangle;
    }

    //Packet: (head)0,0,(left_arm)1,1,(right_arm)2,2,(left_leg)3,3,(right_leg)4,4,(pos)5,5
    // null:null:null:null:null:null

    //Maybe try to send combined packet with all euler
    //Maybe for optimization check if all packet sent are different or not. if not do not send it

    private double[] previousMovement = new double[] {0d,0d,0d};
    public void moveArmorstand(Skeleton skeleton) {

        if(!skeleton.isTracked()) {
            return;
        }


        //send arm and leg rotation
        String head = getHeadAngle(skeleton, true);
        String parts = "";
        for (BodyPart part : BodyPart.values()) {

            String p = getEulerAngle(skeleton,part);
            parts += p + ":";
        }

        String packet = head + ":" + parts.substring(0,parts.length() - 1);

        //client.sendMessage("BODY:" + packet);

        double[] pos = skeleton.get3DJoint(Skeleton.HIP_CENTER);

        double[] vector = new double[3];
        int index = 0;
        for (double start : previousMovement) {
            vector[index] = arrondir((start - pos[index]), 6);
            index++;
        }

        double dZ = vector[2];
        double dY = vector[1];
        double dX = vector[0];
        String pitch = getHeadAngle(skeleton, false);
        this.previousMovement = pos;

        String finalPacket = packet +  ":" + dX + ":" + dY + ":" + dZ + ":" + Math.round(skeleton.getBodyOrientation()) + ":" + pitch;
        client.sendMessage(finalPacket);
        //client.sendMessage("MOV:" + Math.round(skeleton.getBodyOrientation()) + ":" + dX + ":" + dY + ":" + dZ + ":" + pitch);
    }



    public double arrondir(double nombre,double nbApVirg) {
        return(double)((int)(nombre * Math.pow(10,nbApVirg) + .5)) / Math.pow(10,nbApVirg);
    }

    @Override
    public void onVideoFrameEvent(byte[] data) {
        if(viewer==null || viewer.videoTexture==null) return;
        viewer.videoTexture.update(videoWidth(), videoHeight(), data);
    }


}
