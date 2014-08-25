PImage image;

int mapU[];
int mapV[];
float light[];

final int FRAMERATE = 25;

void setup() {
  frameRate(FRAMERATE);
  
  light = new float[256 * 256];

  for (int y = 0, i = 0; y < 256; y++) {
    for (int x = 0; x < 256; x++, i++) {
      float u = lerp(-2.0, 2.0, float(y) / 256);
      float v = lerp(-2.0, 2.0, float(x) / 256);
      
      light[i] = constrain(1.0 - sqrt(sq(u) + sq(v)), -1.0, 1.0);
    }
  }

  image = loadImage("texture.png");
  image.loadPixels(); 

  size(image.width, image.height);

  mapU = new int[height * width];
  mapV = new int[height * width];

  for (int y = 0, i = 0; y < height; y++) {
    for (int x = 0; x < width; x++, i++) {
      int p = image.pixels[y * width + x];
      int down, right;
      
      if (y + 1 < height)
        down = image.pixels[(y + 1) * width + x];
      else
        down = image.pixels[x];
      
      if (x + 1 < width)
        right = image.pixels[y * width + (x + 1)];
      else
        right = image.pixels[y * width];

      float du = brightness(p) - brightness(down);
      float dv = brightness(p) - brightness(right);

      mapU[i] = constrain(int(1.5 * du), -255, 255);
      mapV[i] = constrain(int(1.5 * dv), -255, 255);
    }
  }   
  
  loadPixels();
}

void draw() {
  int lx = (width - 256) / 2 + int(100 * sin(float(frameCount) / FRAMERATE));
  int ly = (height - 256) / 2 + int(100 * cos(float(frameCount) / FRAMERATE));
  int ix = int(50 * cos(float(frameCount) / FRAMERATE));
  int iy = int(50 * sin(float(frameCount) / FRAMERATE));
  
  for (int y = 0, i = 0; y < height; y++) {
    for (int x = 0; x < width; x++, i++) {
      int s = (iy * height + ix + i) & 65535;
      color c = image.pixels[s];
      int bx = constrain(x - lx + int(mapV[s]), 0, 255);
      int by = constrain(y - ly + int(mapU[s]), 0, 255);
      float b = light[(bx + by * 256) & 0xffff];
      
      if (b >= 0.0)
        pixels[i] = lerpColor(c, color(255,255,255), b);
      else
        pixels[i] = lerpColor(c, color(0,0,0), abs(b));
    }
  }
 
  updatePixels();
}
