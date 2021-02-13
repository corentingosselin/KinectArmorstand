package fr.cocoraid.socket.kinect;

import edu.ufl.digitalworlds.j4k.DepthMap;
import edu.ufl.digitalworlds.j4k.Skeleton;
import edu.ufl.digitalworlds.j4k.VideoFrame;
import edu.ufl.digitalworlds.opengl.OpenGLPanel;

import javax.media.opengl.GL2;
import java.awt.*;
import java.awt.event.MouseEvent;

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
@SuppressWarnings("serial")
public class ViewerPanel3D extends OpenGLPanel
{
	private float view_rotx = 0.0f, view_roty = 0.0f, view_rotz = 0.0f;
	private int prevMouseX, prevMouseY;
	
	DepthMap map=null;
	boolean is_playing=false;
	boolean show_video=false;
	
	public void setShowVideo(boolean flag){show_video=flag;}
	
	VideoFrame videoTexture;
	
	Skeleton skeletons[];
	
	public void setup()
	{
			skeletons = new Skeleton[Kinect.NUI_SKELETON_COUNT];
			videoTexture=new VideoFrame();

		    background(0, 0, 0);
	}



	public void draw() {

		GL2 gl= getGL2();


		pushMatrix();

		translate(0,0,-2);
	    rotate(view_rotx, 1.0, 0.0, 0.0);
	    rotate(view_roty, 0.0, 1.0, 0.0);
	    rotate(view_rotz, 0.0, 0.0, 1.0);
	    translate(0,0,2);

	    gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);

	    gl.glDisable(GL2.GL_LIGHTING);
	    gl.glLineWidth(2);
	    gl.glColor3f(0F,1F,0F);

	    for(int i=0;i<Kinect.NUI_SKELETON_COUNT;i++)
	    	if(skeletons[i]!=null) 
	    	{
	    		if(skeletons[i].getTimesDrawn()<=10 && skeletons[i].isTracked())
	    		{
	    			skeletons[i].draw(gl);
	    			skeletons[i].increaseTimesDrawn();
	    		}
	    	}
		
	    popMatrix();
	}
	
	
	public void mouseDragged(int x, int y, MouseEvent e) {

	    Dimension size = e.getComponent().getSize();

	    if(isMouseButtonPressed(3)||isMouseButtonPressed(1))
	    {
	    	float thetaY = 360.0f * ( (float)(x-prevMouseX)/(float)size.width);
	    	float thetaX = 360.0f * ( (float)(prevMouseY-y)/(float)size.height);
	    	view_rotx -= thetaX;
	    	view_roty += thetaY;		
	    }
	    
	    prevMouseX = x;
	    prevMouseY = y;

	}

	public void mousePressed(int x, int y, MouseEvent e) {
		prevMouseX = x;
	    prevMouseY = y;
	}
	

}
