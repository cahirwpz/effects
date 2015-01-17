import processing.core.*;

@SuppressWarnings("serial")
public class Engine3D extends PApplet {  
  Matrix3D m1, m2;
  Scene3D scene;
  Rasterizer rasterizer;
  Mesh3D mesh;
  
  public void setup() {
    size(640, 480);
    noSmooth();
    stroke(255);

    rasterizer = new Rasterizer(this);
    scene = new Scene3D();
    mesh = Mesh3D.parse(loadJSONObject("cube.json"));
    m1 = new Matrix3D();
    m2 = new Matrix3D();
  }

  float ax = 0.0f;

  public void draw() {
    background(0);
    loadPixels();

    m1.reset();
    // m1.scale(0.25, 0.25, 1);
    m1.rotate(ax, ax, ax);
    m1.translate(0, 0, -8);
    m1.perspective(45.0f, 4.0f / 3.0f, 2.0f, 100.0f);

    m2.reset();
    m2.rotate(-ax, -ax, -ax);
    m2.translate(-2, 0, -10);
    m2.perspective(45.0f, 4.0f / 3.0f, 1.0f, 10.0f);

    scene.reset();
    scene.add(mesh, m1);
    // scene.add(mesh, m2);
    scene.draw(rasterizer);

    updatePixels();

    ax += 0.5f;
  }
};