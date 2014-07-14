float frpart(float x) {
  return x - floor(x);
}

class UVCoord {
  float u, v;
}

interface UVGenerator {
  void calculate(UVCoord p, float x, float y);
}

class UVMap {
  UVCoord[][] map;
  PImage texture;

  UVMap() {
    map = new UVCoord[height][width];
  }
  
  void attachTexture(String path) {
    texture = loadImage(path);
    texture.loadPixels(); 
  }

  void generate(UVGenerator g) {
    for (int j = 0; j < height; j++) {
      for (int i = 0; i < width; i++) {      
        float x = lerp(-1.0, 1.0, float(i) / width);
        float y = lerp(-1.0, 1.0, float(j) / height);

        map[j][i] = new UVCoord();

        g.calculate(map[j][i], x, y);
      }
    }
  }
  
  void render(PApplet applet, float t) {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        float u = map[y][x].u + t / 4;
        float v = map[y][x].v + t / 4;
        int i = int(frpart(u) * texture.width);
        int j = int(frpart(v) * texture.height);
      
        applet.set(x, y, texture.get(i, j));
      }
    }
  }
}
