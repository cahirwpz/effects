UVMap uvmap;

UVGenerator[] generators = {
  new FancyEye(),
  new InnerCycle(),
  new BentPlane(),
  new WeirdTunnel(),
  new Twist(),
  new Swirl(),
  new Some6(),
  new Some7(),
  new Some8(),
  new Some9()
};

int i = 0;

void setup() {
  size(640, 480);
  frameRate(25);
  
  println("Press LEFT or RIGHT key to change UVMap!");
  
  uvmap = new UVMap();
  uvmap.attachTexture("texture.png");
  uvmap.generate(generators[i]);
}

void keyPressed() {
  if (key == CODED) {
    if (keyCode == RIGHT) {
      i = (i + 1) % generators.length;
      uvmap.generate(generators[i]);
    }
    if (keyCode == LEFT) {
      i = (i + generators.length - 1) % generators.length;
      uvmap.generate(generators[i]);
    }
  }
}

void draw() {
  uvmap.render(this, frameCount / frameRate); 
}
