final int FRAMERATE = 25;
PImage flare;

void setup() {
  frameRate(FRAMERATE);
  size(320, 256);
  
  flare = createImage(32, 32, RGB);

  for (int y = 0; y < flare.height; y++) {
    for (int x = 0; x < flare.width; x++) {
      float u = lerp(-1.0, 1.0, float(y) / flare.width);
      float v = lerp(-1.0, 1.0, float(x) / flare.height);
      float d = sq(1.25 - sqrt(sq(u) + sq(v)));
      int p = int(constrain(d, 0.0, 1.0) * 255);
      
      flare.set(x, y, color(p, p, p));
    }
  }
}

final int N = 96;

void draw() {
  background(0);
  blendMode(ADD);
 
  float t = float(frameCount) / FRAMERATE;
  float xo = (width - flare.width) / 2;
  float yo = (height - flare.height) / 2;
  
  for (int i = 0; i < N; i++) {
    float a = lerp(0, TWO_PI, float(i) / N) + t / 1.5;
    float x = (sin(-4 * a + t) * cos(-2 * a + t)) * 96;
    float y = (sin(3 * a - t) * cos(2 * a - t)) * 96;
    image(flare, x + xo, y + yo);
  }
}
