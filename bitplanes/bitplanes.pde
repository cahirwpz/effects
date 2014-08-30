final int BPLS = 5;

void setup() {
  size(320, 256);
  
  initOCS(BPLS);

  for (int i = 0; i < 8; i++)
    palette[i] = color(i * 16 + 128, i * 16 + 128, i * 16 + 128);
  for (int i = 0; i < 8; i++)
    palette[i + 8] = color(i * 16 + 128, 0, i * 16 + 128);
  for (int i = 0; i < 8; i++)
    palette[i + 16] = color(i * 16 + 128, i * 16 + 128, 0);
  for (int i = 0; i < 8; i++)
    palette[i + 24] = color(0, i * 16 + 128, i * 16 + 128);
  
  for (int i = 0; i < height; i++) {
    float a = 8 * TWO_PI * float(i) / height;
    float c = constrain(sin(a) * 128 + 128, 0, 255);
    copper(0, i, 0, color(0, 0, c));
  }
  
  for (int i = 0; i < BPLS; i++) {
    bpl[i].lineE(i * 32 + 20, 40, i * 32 + 100, 180);
    bpl[i].lineE(100 + i * 32, 180, i * 32 + 180, 40);
    bpl[i].circleE(100 + i * 32, 180, 64);
    bpl[i].fill();
  }
}

void draw() {
  updateOCS();
}