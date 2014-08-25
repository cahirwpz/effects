UVMap uvmap;

UVGenerator[] generators = {
  new Flush(),
  new Ball(),
  new HotMagma(),
  new HorizontalPlanes(),
  new VerticalPlanes(),
  new WavyStarBurst(),
  new MagneticFlare(),
  new HypnoticRainbowSpiral(),
  new FancyEye(),
  new Anamorphosis(),
  new BentPlane(),
  new RotatingTunnelOfWonder(),
  new Twist(),
  new Swirl(),

  new Some8(),
  new Some9()
};

int i = 0;

void setup() {
  size(640, 480);
  frameRate(25);
  
  println("Press LEFT or RIGHT key to change UVMap!");
  
  uvmap = new UVMap(width, height);
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
  loadPixels();
  uvmap.render(this, frameCount / frameRate);
  updatePixels();
}
