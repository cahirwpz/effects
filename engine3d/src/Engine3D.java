import processing.core.*;

@SuppressWarnings("serial")
public class Engine3D extends PApplet {  
  Object3D obj1, obj2;
  Scene3D scene;
  Rasterizer rasterizer;
  
  public void setup() {
    size(640, 480);
    noSmooth();
    stroke(255);

    Mesh3D mesh = Mesh3D.parse(loadJSONObject("cube.json"));
    
    obj1 = new Object3D(mesh);
    obj2 = new Object3D(mesh);
    
    scene = new Scene3D();
    scene.add(obj1);
    scene.add(obj2);
    
    rasterizer = new Rasterizer(this);
  }

  float ax = 0.0f;

  public void draw() {
    background(0);
    loadPixels();

    obj1.reset();
    obj1.scale(1, 1, 1);
    obj1.rotate(ax, ax, ax);
    obj1.translate(2, 0, -8);

    obj2.reset();
    obj2.rotate(ax, ax, ax);
    obj2.translate(-2, 0, -8);

    scene.draw(rasterizer);

    updatePixels();

    ax += 0.5f;
  }
};