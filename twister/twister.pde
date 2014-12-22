PImage textures[];
int sintab[];
int angletab[];
int vtab[][];

final int SINLEN = 512;
final int FACES = 4;
final int FACESIZE = SINLEN / FACES;
final int SIZE = 128;
final int LEN = SIZE * SIZE;

void setup(){
  size(320, 256);

  // Textures are 128 x 128.
  textures = new PImage[2];
  textures[0] = loadImage("texture-1.png");
  textures[1] = loadImage("texture-2.png");

  angletab = new int[FACES + 1];

  sintab = new int[SINLEN];
  for (int i = 0; i < SINLEN; i++)
    sintab[i] = (int)((Math.sin(float(i) / SINLEN * TWO_PI) + 1.0) * (SINLEN / 2 - 1));

  vtab = new int[SIZE][];
  for (int l = 1; l < SIZE; l++) {
    vtab[l] = new int[l];
    for (int i = 0; i < l; i++)
      vtab[l][i] = i * SIZE / l;
  }
}

void draw() {      
  background(0);
  loadPixels();

  for (int y = 0, u = 0, pos = 0;
       y < height;
       y++, u = (u + SIZE) % LEN, pos += width)
  {
    int angle = sintab[(frameCount + y / 2) % SINLEN];
    int w = 32 + sintab[y * 2 / SINLEN] / 8;
    
    for (int n = 0; n < FACES; n++, angle += FACESIZE)
      angletab[n] = sintab[angle % SINLEN] * w / 256 + (width / 2 - w);
    angletab[FACES] = angletab[0];  
    
    for (int n = 0; n < FACES;) {
      int xs = angletab[n++];
      int xe = angletab[n];
      int l = xe - xs;
      
      if (l > 0) {
        PImage texture = textures[n & 1];
        int vt[] = vtab[l];
        int p = pos + xs;
        
        for (int i = 0; i < l;)
          pixels[p++] = texture.pixels[u + vt[i++]];
      }
    }
  }
  
  updatePixels();
}
