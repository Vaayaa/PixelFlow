package VerletPhysics_ClothStudies;




import java.util.ArrayList;

import com.thomasdiewald.pixelflow.java.PixelFlow;
import com.thomasdiewald.pixelflow.java.verletphysics.SpringConstraint;
import com.thomasdiewald.pixelflow.java.verletphysics.VerletParticle2D;
import com.thomasdiewald.pixelflow.java.verletphysics.VerletPhysics2D;
import com.thomasdiewald.pixelflow.java.verletphysics.softbodies2D.SoftBody2D;
import com.thomasdiewald.pixelflow.java.verletphysics.softbodies2D.SoftGrid;

import controlP5.Accordion;
import controlP5.ControlP5;
import controlP5.Group;
import controlP5.RadioButton;
import controlP5.Toggle;
import processing.core.*;

public class VerletPhysics_ClothStudies extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;
  
  int gui_w = 200;
  int gui_x = viewport_w - gui_w;
  int gui_y = 0;
  
  // physics simulation
  VerletPhysics2D physics;
  
  // cloth parameters
  VerletParticle2D.Param param_cloth1 = new VerletParticle2D.Param();
  VerletParticle2D.Param param_cloth2 = new VerletParticle2D.Param();
  
  // cloth objects
  SoftGrid cloth1 = new SoftGrid();
  SoftGrid cloth2 = new SoftGrid();
  
  // list, that wills store the cloths
  ArrayList<SoftBody2D> softbodies;

  // 0 ... default: particles, spring
  // 1 ... tension
  int DISPLAY_MODE = 0;
  
  // entities to display
  boolean DISPLAY_PARTICLES      = true;
  boolean DISPLAY_SPRINGS_STRUCT = true;
  boolean DISPLAY_SPRINGS_SHEAR  = true;
  boolean DISPLAY_SPRINGS_BEND   = true;
  
  // first thing to do, inside draw()
  boolean NEED_REBUILD = true;
  
  // just for the window title-info
  int NUM_SPRINGS;
  int NUM_PARTICLES;
  

  
  public void settings(){
    size(viewport_w, viewport_h, P2D); 
    smooth(4);
  }
  


  public void setup() {
    surface.setLocation(viewport_x, viewport_y);
    
    // main library context
    PixelFlow context = new PixelFlow(this);
    context.print();
//    context.printGL();
    
    physics = new VerletPhysics2D();

    physics.param.GRAVITY = new float[]{ 0, 0.1f };
    physics.param.bounds  = new float[]{ 0, 0, viewport_w-gui_w, height };
    physics.param.iterations_collisions = 4;
    physics.param.iterations_springs    = 4;
    
    // Cloth1 Parameters
    param_cloth1.DAMP_BOUNDS          = 0.90f;
    param_cloth1.DAMP_COLLISION       = 0.90f;
    param_cloth1.DAMP_VELOCITY        = 0.991f; 
    param_cloth1.DAMP_SPRING_decrease = 0.999999f;    
    param_cloth1.DAMP_SPRING_increase = 0.0005999999f;
    
    // Cloth1 Parameters
    param_cloth2.DAMP_BOUNDS          = 0.90f;
    param_cloth2.DAMP_COLLISION       = 0.90f;
    param_cloth2.DAMP_VELOCITY        = 0.991f; 
    param_cloth2.DAMP_SPRING_decrease = 0.999999f;    
    param_cloth2.DAMP_SPRING_increase = 0.0005999999f;
    
    // initial cloth building parameters, both cloth start the same
    cloth1.CREATE_STRUCT_SPRINGS = true;
    cloth1.CREATE_SHEAR_SPRINGS  = true;
    cloth1.CREATE_BEND_SPRINGS   = true;
    cloth1.bend_spring_mode      = 0;
    cloth1.bend_spring_dist      = 3;
    
    cloth2.CREATE_STRUCT_SPRINGS = true;
    cloth2.CREATE_SHEAR_SPRINGS  = true;
    cloth2.CREATE_BEND_SPRINGS   = true;
    cloth2.bend_spring_mode      = 0;
    cloth2.bend_spring_dist      = 3;

    createGUI();
    
    frameRate(600);
  }
  
  
  
  public void initBodies(){
    
    physics.reset();
    
    softbodies = new ArrayList<SoftBody2D>();
    
    // create some particle-bodies: Cloth / SoftBody
    int nodex_x, nodes_y, nodes_r, color;
    float nodes_start_x, nodes_start_y;
    
    // both cloth are of the same size
    nodex_x = 30;
    nodes_y = 30;
    nodes_r = 8;
    nodes_start_y = 80;
    
    int   num_cloth = 2;
    float cloth_width = 2 * nodes_r * (nodex_x-1);
    float spacing = ((viewport_w-gui_w) - num_cloth * cloth_width) / (float)(num_cloth+1);  
    
    
    // cloth 1
    {

      nodes_start_x = spacing;
      color = color(255,180,0,128);
   
      cloth1.create(physics, param_cloth1, nodex_x, nodes_y, nodes_r, nodes_start_x, nodes_start_y);
      cloth1.getNode(               0, 0).enable(false, false, false); // fix node to current location
      cloth1.getNode(cloth1.nodes_x-1, 0).enable(false, false, false); // fix node to current location
      cloth1.createShape(this, color);
      softbodies.add(cloth1);
    }
    
    // cloth 2
    {
      nodes_start_x += cloth_width + spacing;
      color = color(0,180,255,128);
//      color = color(0,64);
      cloth2.create(physics, param_cloth2, nodex_x, nodes_y, nodes_r, nodes_start_x, nodes_start_y);
      cloth2.getNode(               0, 0).enable(false, false, false); // fix node to current location
      cloth2.getNode(cloth2.nodes_x-1, 0).enable(false, false, false); // fix node to current location
      cloth2.createShape(this, color);
      softbodies.add(cloth2);
    }
    
    NUM_SPRINGS   = SpringConstraint.getSpringCount(physics.getParticles(), true);
    NUM_PARTICLES = physics.getParticlesCount();
  }


  
  public void draw() {

    if(NEED_REBUILD){
      initBodies();
      NEED_REBUILD = false;
    }
    
    boolean mouseInteraction = !cp5.isMouseOver();

    // Mouse Interaction: particles position
    if(mouseInteraction && !DELETE_SPRINGS && particle_mouse != null){
      VerletParticle2D particle = particle_mouse;
      float dx = mouseX - particle.cx;
      float dy = mouseY - particle.cy;
      
      float damping_pos = 0.2f;
      particle.px = particle.cx;
      particle.py = particle.cy;
      particle.cx  += dx * damping_pos;
      particle.cy  += dy * damping_pos;
    }
    
    // Mouse Interaction: deleting springs/constraints between particles
    if(mouseInteraction && DELETE_SPRINGS && mousePressed){
      ArrayList<VerletParticle2D> list = findParticlesWithinRadius(mouseX, mouseY, DELETE_RADIUS);
      for(VerletParticle2D tmp : list){
        SpringConstraint.deleteSprings(tmp);
        tmp.collision_group = physics.getNewCollisionGroupId();
        tmp.rad_collision = tmp.rad;
      }
    }

    
    
    // update physics simulation
    physics.update(1);
    
    
    
    // render
    background(DISPLAY_MODE == 0 ?  255 : 92);
    
    // 1) particles
    if(DISPLAY_PARTICLES){
      for(SoftBody2D body : softbodies){
        body.use_particles_color = (DISPLAY_MODE == 0);
        body.drawParticles(this.g);
      }
    }
    
    // 2) springs
    for(SoftBody2D body : softbodies){
      if(DISPLAY_SPRINGS_BEND  ) body.drawSprings(this.g, SpringConstraint.TYPE.BEND  , DISPLAY_MODE);
      if(DISPLAY_SPRINGS_SHEAR ) body.drawSprings(this.g, SpringConstraint.TYPE.SHEAR , DISPLAY_MODE);
      if(DISPLAY_SPRINGS_STRUCT) body.drawSprings(this.g, SpringConstraint.TYPE.STRUCT, DISPLAY_MODE);
    }

    // interaction stuff
    if(DELETE_SPRINGS){
      fill(255,64);
      stroke(0);
      strokeWeight(1);
      ellipse(mouseX, mouseY, DELETE_RADIUS*2, DELETE_RADIUS*2);
    }

    
    // some info, windows title
    String txt_fps = String.format(getClass().getName()+ "   [particles %d]   [springs %d]   [frame %d]   [fps %6.2f]", NUM_PARTICLES, NUM_SPRINGS, frameCount, frameRate);
    surface.setTitle(txt_fps);
  }
  

  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // User Interaction
  //////////////////////////////////////////////////////////////////////////////
 
  VerletParticle2D particle_mouse = null;
  
  public VerletParticle2D findNearestParticle(float mx, float my){
    return findNearestParticle(mx, my, Float.MAX_VALUE);
  }
  
  public VerletParticle2D findNearestParticle(float mx, float my, float search_radius){
    float dd_min_sq = search_radius * search_radius;
    VerletParticle2D[] particles = physics.getParticles();
    VerletParticle2D particle = null;
    for(int i = 0; i < particles.length; i++){
      float dd_sq = getDistSq(mx, my, particles[i]);
      if( dd_sq < dd_min_sq){
        dd_min_sq = dd_sq;
        particle = particles[i];
      }
    }
    return particle;
  }
  
  public ArrayList<VerletParticle2D> findParticlesWithinRadius(float mx, float my, float search_radius){
    float dd_min_sq = search_radius * search_radius;
    VerletParticle2D[] particles = physics.getParticles();
    ArrayList<VerletParticle2D> list = new ArrayList<VerletParticle2D>();
    for(int i = 0; i < particles.length; i++){
      float dd_sq = getDistSq(mx, my, particles[i]);
      if(dd_sq < dd_min_sq){
        list.add(particles[i]);
      }
    }
    return list;
  }
  
  public float getDistSq(float mx, float my, VerletParticle2D particle){
    float dx = mx - particle.cx;
    float dy = my - particle.cy;
    return dx*dx + dy*dy;
  }
    

  boolean DELETE_SPRINGS = false;
  float   DELETE_RADIUS  = 20;
  
  boolean state_enable_collisions;
  boolean state_enable_springs;
  boolean state_enable_forces;

  public void mousePressed(){
    boolean mouseInteraction = !cp5.isMouseOver();
    if(mouseInteraction && !DELETE_SPRINGS){
      particle_mouse = findNearestParticle(mouseX, mouseY, 80);
      if(particle_mouse != null){
        // push states
        state_enable_collisions = particle_mouse.enable_collisions;
        state_enable_springs    = particle_mouse.enable_springs   ;
        state_enable_forces     = particle_mouse.enable_forces    ;  
        if(mouseButton == LEFT  ) particle_mouse.enable(false, false, false);
        if(mouseButton == CENTER) particle_mouse.enable(false, false, false);
        if(mouseButton == RIGHT ) particle_mouse.enable(false, false, false);
      }
    }
  }
  
  public void mouseReleased(){
    if(particle_mouse != null && !DELETE_SPRINGS){
      if(mouseButton == LEFT  ) particle_mouse.enable(state_enable_collisions, state_enable_springs, state_enable_forces);
      if(mouseButton == CENTER) particle_mouse.enable(true, true, true);
      if(mouseButton == RIGHT ) particle_mouse.enable(true, false, false);
      particle_mouse = null;
    }
  }
  

  public void keyPressed(){
    if(key ==' ') DELETE_SPRINGS = true;
  }
  public void keyReleased(){
    if(key ==' ') DELETE_SPRINGS = false;
    
    if(key =='s') SpringConstraint.makeAllSpringsUnidirectional(physics.getParticles());
    if(key =='r') initBodies();
    if(key =='1') DISPLAY_MODE = 0;
    if(key =='2') DISPLAY_MODE = 1;

    if(key =='p') DISPLAY_PARTICLES = !DISPLAY_PARTICLES;
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // GUI STUFF
  //////////////////////////////////////////////////////////////////////////////
  
  
  public void cloth1_CREATE_SPRING_TYPE  (float[] val){
    cloth1.CREATE_STRUCT_SPRINGS = (val[0] > 0);
    cloth1.CREATE_SHEAR_SPRINGS  = (val[1] > 0);
    cloth1.CREATE_BEND_SPRINGS   = (val[2] > 0);
    NEED_REBUILD = true;
  }
  public void cloth1_BEND_SPRING_MODE(int val){
    cloth1.bend_spring_mode = val;
    NEED_REBUILD = true;
  }
  public void cloth1_BEND_SPRING_LEN(int val){
    cloth1.bend_spring_dist = val;
    NEED_REBUILD = true;
  }
  
  
  
  public void cloth2_CREATE_SPRING_TYPE  (float[] val){
    cloth2.CREATE_STRUCT_SPRINGS = (val[0] > 0);
    cloth2.CREATE_SHEAR_SPRINGS  = (val[1] > 0);
    cloth2.CREATE_BEND_SPRINGS   = (val[2] > 0);
    NEED_REBUILD = true;
  }
  public void cloth2_BEND_SPRING_MODE(int val){
    cloth2.bend_spring_mode = val;
    NEED_REBUILD = true;
  }

  public void cloth2_BEND_SPRING_LEN(int val){
    cloth2.bend_spring_dist = val;
    NEED_REBUILD = true;
  }
  
  
  public void setDisplayMode(int val){
    DISPLAY_MODE = val;
  }
  
  public void setDisplayTypes(float[] val){
    DISPLAY_PARTICLES      = (val[0] > 0);
    DISPLAY_SPRINGS_STRUCT = (val[1] > 0);
    DISPLAY_SPRINGS_SHEAR  = (val[2] > 0);
    DISPLAY_SPRINGS_BEND   = (val[3] > 0);
  }
  
  public void setGravity(float val){
    physics.param.GRAVITY[1] = val;
  }
  
  
  
  
  
  ControlP5 cp5;
  
  public void createGUI(){
    cp5 = new ControlP5(this);
    
    int sx, sy, px, py, oy;
    
    sx = 100; sy = 14; oy = (int)(sy*1.4f);
    
    
    ////////////////////////////////////////////////////////////////////////////
    // GUI - CLOTH1
    ////////////////////////////////////////////////////////////////////////////
    Group group_physics = cp5.addGroup("global");
    {
      group_physics.setHeight(20).setSize(gui_w, height)
      .setBackgroundColor(color(16, 180)).setColorBackground(color(16, 180));
      group_physics.getCaptionLabel().align(CENTER, CENTER);
      
      px = 10; py = 15;
      
      cp5.addButton("rebuild").setGroup(group_physics).plugTo(this, "initBodies").setSize(80, 18).setPosition(px, py);
       
      cp5.addSlider("gravity").setGroup(group_physics).setSize(sx, sy).setPosition(px, py+=(int)(oy*1.5f))
          .setRange(0, 7).setValue(physics.param.GRAVITY[1]).plugTo(this, "setGravity");
      
      cp5.addSlider("iter: springs").setGroup(group_physics).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 30).setValue(physics.param.iterations_springs).plugTo( physics.param, "iterations_springs");
      
      cp5.addSlider("iter: collisions").setGroup(group_physics).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 10).setValue(physics.param.iterations_collisions).plugTo( physics.param, "iterations_collisions");
      
      cp5.addRadio("setDisplayMode").setGroup(group_physics).setSize(sy,sy).setPosition(px, py+=(int)(oy*1.4f))
          .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(1)
          .addItem("colored",0)
          .addItem("tension",1)
          .activate(DISPLAY_MODE);
      
      cp5.addCheckBox("setDisplayTypes").setGroup(group_physics).setSize(sy,sy).setPosition(px, py+=(int)(oy*2.4f))
          .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(1)
          .addItem("PARTICLES", 0).activate(DISPLAY_PARTICLES      ? 0 : 5)
          .addItem("STRUCT "  , 1).activate(DISPLAY_SPRINGS_STRUCT ? 1 : 5)
          .addItem("SHEAR"    , 2).activate(DISPLAY_SPRINGS_SHEAR  ? 2 : 5)
          .addItem("BEND"     , 3).activate(DISPLAY_SPRINGS_BEND   ? 3 : 5);
    }
    
    

    ////////////////////////////////////////////////////////////////////////////
    // GUI - CLOTH1
    ////////////////////////////////////////////////////////////////////////////
    Group group_cloth1 = cp5.addGroup("cloth 1");
    {
      Group group_cloth = group_cloth1;
      
      group_cloth.setHeight(20).setSize(gui_w, 210)
      .setBackgroundColor(color(16, 180)).setColorBackground(color(16, 180));
      group_cloth.getCaptionLabel().align(CENTER, CENTER);
      
      px = 10; py = 15;
       
      cp5.addSlider("C1.velocity").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py)
          .setRange(0, 1).setValue(param_cloth1.DAMP_VELOCITY).plugTo(param_cloth1, "DAMP_VELOCITY");
      
      cp5.addSlider("C1.contraction").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 1).setValue(param_cloth1.DAMP_SPRING_decrease).plugTo(param_cloth1, "DAMP_SPRING_decrease");
      
      cp5.addSlider("C1.expansion").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 1).setValue(param_cloth1.DAMP_SPRING_increase).plugTo(param_cloth1, "DAMP_SPRING_increase");

      cp5.addCheckBox("cloth1_CREATE_SPRING_TYPE").setGroup(group_cloth).setSize(sy,sy).setPosition(px, py+=oy)
          .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(1)
          .addItem("C1.struct springs", 0).activate(cloth1.CREATE_STRUCT_SPRINGS ? 0 : 2)
          .addItem("C1.shear springs" , 1).activate(cloth1.CREATE_SHEAR_SPRINGS  ? 1 : 2)
          .addItem("C1.bend springs"  , 2).activate(cloth1.CREATE_BEND_SPRINGS   ? 2 : 2)
          ;
      
      cp5.addRadio("cloth1_BEND_SPRING_MODE").setGroup(group_cloth).setSize(sy,sy).setPosition(px, py+=oy*3)
          .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(1)
          .addItem("C1.bend springs: diagonal",0)
          .addItem("C1.bend springs: ortho"   ,1)
          .addItem("C1.bend springs: random"  ,2)
          .activate(cloth1.bend_spring_mode);
      
      cp5.addSlider("C1.bend spring len").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py+=oy*3)
          .setRange(0, 50).setValue(cloth1.bend_spring_dist).plugTo(this, "cloth1_BEND_SPRING_LEN");

    }
    
    ////////////////////////////////////////////////////////////////////////////
    // GUI - CLOTH 2
    ////////////////////////////////////////////////////////////////////////////
    Group group_cloth2 = cp5.addGroup("cloth 2");
    {
      Group group_cloth = group_cloth2;
      
      group_cloth.setHeight(20).setSize(gui_w, 210)
      .setBackgroundColor(color(16, 180)).setColorBackground(color(16, 180));
      group_cloth.getCaptionLabel().align(CENTER, CENTER);
      
      px = 10; py = 15;
       
      cp5.addSlider("C2.velocity").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py)
          .setRange(0, 1).setValue(param_cloth2.DAMP_VELOCITY).plugTo(param_cloth2, "DAMP_VELOCITY");
      
      cp5.addSlider("C2.contraction").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 1).setValue(param_cloth2.DAMP_SPRING_decrease).plugTo(param_cloth2, "DAMP_SPRING_decrease");
      
      cp5.addSlider("C2.expansion").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py+=oy)
          .setRange(0, 1).setValue(param_cloth2.DAMP_SPRING_increase).plugTo(param_cloth2, "DAMP_SPRING_increase");

      cp5.addCheckBox("cloth2_CREATE_SPRING_TYPE").setGroup(group_cloth).setSize(sy,sy).setPosition(px, py+=oy)
          .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(1)
          .addItem("C2.struct springs", 0).activate(cloth1.CREATE_STRUCT_SPRINGS ? 0 : 2)
          .addItem("C2.shear springs" , 1).activate(cloth1.CREATE_SHEAR_SPRINGS  ? 1 : 2)
          .addItem("C2.bend springs"  , 2).activate(cloth1.CREATE_BEND_SPRINGS   ? 2 : 2)
          ;
      
      cp5.addRadio("cloth2_BEND_SPRING_MODE").setGroup(group_cloth).setSize(sy,sy).setPosition(px, py+=oy*3)
          .setSpacingColumn(2).setSpacingRow(2).setItemsPerRow(1)
          .addItem("C2.bend springs: diagonal",0)
          .addItem("C2.bend springs: ortho"   ,1)
          .addItem("C2.bend springs: random"  ,2)
          .activate(cloth2.bend_spring_mode);
      
      cp5.addSlider("C2.bend spring len").setGroup(group_cloth).setSize(sx, sy).setPosition(px, py+=oy*3)
      .setRange(0, 50).setValue(cloth2.bend_spring_dist).plugTo(this, "cloth2_BEND_SPRING_LEN");
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // GUI - ACCORDION
    ////////////////////////////////////////////////////////////////////////////
    cp5.addAccordion("acc").setPosition(gui_x, gui_y).setWidth(gui_w).setSize(gui_w, height)
      .setCollapseMode(Accordion.MULTI)
      .addItem(group_cloth1)
      .addItem(group_cloth2)
      .addItem(group_physics)
      .open(0, 1, 2);
   
  }
  

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  
  public static void main(String args[]) {
    PApplet.main(new String[] { VerletPhysics_ClothStudies.class.getName() });
  }
}