PImage textures[];
int sintab[];

final int FACES = 4;
final int SIZE = 512;
final int MASK = 511;

void setup(){
  size(320, 256);

  // Textures are 128 x 128.
  textures = new PImage[2];
  textures[0] = loadImage("texture-1.png");
  textures[1] = loadImage("texture-2.png");

  sintab = new int[SIZE];
  for (int i = 0; i < SIZE; i++)
    sintab[i] = (int)((Math.sin(float(i) / SIZE * TWO_PI) + 1.0) * (SIZE / 2 - 1));
}

void draw() {      
  background(0);
  loadPixels();
  
  int faceSize = SIZE / FACES;
  
  for(int y = 0; y < height; y++) {
    int a = sintab[frameCount + (y / 2) & MASK];

    for(int n = 0; n < FACES; n++) {
      int bs = faceSize * n;
      int be = faceSize * (n + 1);
      int x1 = sintab[(a + bs) & MASK] / 4;
      int x2 = sintab[(a + be) & MASK] / 4;
      int l = x2 - x1;
      
      if (l > 0) {
        PImage texture = textures[n & 1];
    
        for(int i = 0; i < l; i++) {
          int u = y & 127;
          int v = i * 128 / l;
          int x = x1 + i + (width / 2 - 64);
          pixels[y * width + x] = texture.pixels[u * 128 + v];
        }
      }
    }
  }
  
  updatePixels();
}
