final String datapath = "/Users/cahir/Amiga/src/demoscene/artwork/";

PImage texture;
float[][] umap;
float[][] vmap;

float frpart(float x) {
  return x - floor(x);
}

void setup() {
  size(640, 480);
  frameRate(25);
  
  umap = new float[height][width];
  vmap = new float[height][width];
  
  for (int i = 0; i < height; i++) {
    for (int j = 0; j < width; j++) {
      float x = float(j - width / 2) * (2.0 / width);
      float y = float(i - height / 2) * (2.0 / height);
      float a = atan2(x, y);
      float r = sqrt(x * x + y * y);
      
      umap[i][j] = x * cos(2.0 * r) - y * sin(2.0 * r);
      vmap[i][j] = y * cos(2.0 * r) + x * sin(2.0 * r);
    }
  }
    
  texture = loadImage(datapath + "textures/rork-1.png");
  texture.loadPixels();
}

void draw() {
  float t = frameCount / frameRate;
 
  for (int y = 0; y < height; y++) {
    for (int x = 0; x < width; x++) {
      float u = umap[y][x] + t / 4;
      float v = vmap[y][x] + t / 4;
      
      set(x, y, texture.get(int(frpart(u) * texture.width),
                            int(frpart(v) * texture.height)));
    }
  }
}
