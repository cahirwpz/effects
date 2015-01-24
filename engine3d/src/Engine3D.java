import processing.core.*;

@SuppressWarnings("serial")
public class Engine3D extends PApplet {  
  Object3D obj1, obj2;
  Scene3D scene;
  ResourceManager manager;
  Rasterizer rasterizer;
  
  public void setup() {
    size(640, 480);
    frameRate(60);
    
    manager = new ResourceManager();
    
    int rocketId = manager.addMesh("rocket.obj");
    int ballId = manager.addMesh("obj2.lwo");

    obj1 = new Object3D(manager, rocketId);
    obj2 = new Object3D(manager, ballId);
    
    scene = new Scene3D();
    scene.add(obj1);
    scene.add(obj2);
    
    rasterizer = new Rasterizer(this);
  }

  public void draw() {
    float alpha = (float)frameCount * 0.5f;
    float x = (float)Math.sin(Math.toRadians(frameCount)) * 2.0f;
    
    background(0);
    loadPixels();

    scene.cameraLookAt(new Vector3D(0, 0, 0),
                       new Vector3D(x, 0, -8.0f),
                       new Vector3D(0, 1, 0));

    obj1.reset();
    obj1.scale(0.01f, 0.01f, 0.01f);
    obj1.rotate(alpha, alpha, alpha);
    obj1.translate(2, 0, -8);

    obj2.reset();
    obj2.scale(2, 2, 2);
    obj2.rotate(alpha, alpha, alpha);
    obj2.translate(-2, 0, -8);

    scene.draw(rasterizer);

    updatePixels();
  }
};