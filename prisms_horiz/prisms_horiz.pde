class Edge {
  int xs, xe, y;
  int size() { return xe - xs; }
};

class Span {
  int xs, xe;
  int size() { return xe - xs; }
  void clear() { xs = 0; xe = 0; }
};

int sintab[];
Edge edge[];
Span span[];

final int SINLEN = 512;

void setup(){
  size(320, 256);

  sintab = new int[SINLEN];
  for (int i = 0; i < SINLEN; i++)
    sintab[i] = (int)(Math.sin(float(i) / SINLEN * TWO_PI) * SINLEN);
    
  edge = new Edge[100];
  span = new Span[height];
  for (int i = 0; i < height; i++)
    span[i] = new Span();
}

void renderPrism(int faces, int xc, int yc, int radius, int angle) {

  for (int i = 0; i < faces; i++, angle += SINLEN / faces) {
    int x = (sintab[angle & (SINLEN - 1)] * radius) / (2 * SINLEN) + radius;
    int y = (sintab[(angle + SINLEN / 4) & (SINLEN - 1)] * radius) / SINLEN;

    edge[i] = new Edge();
    edge[i].xs = xc - x;
    edge[i].xe = xc + x;
    edge[i].y  = yc + y;
  }
  edge[faces] = edge[0];
  
  for (int i = 0; i < faces; i++) {
    Edge e1 = edge[i];
    Edge e2 = edge[i+1];
   
    if (e1.y > e2.y) {
      Edge t = e1; e1 = e2; e2 = t;
    }

    for (int y = e1.y; y < e2.y; y++) {
      float step = (float)(y - e1.y) / (e2.y - e1.y);
      int xs = (int)lerp(e1.xs, e2.xs, step);
      int xe = (int)lerp(e1.xe, e2.xe, step);
      
      if (xe - xs > span[y].size()) {
        span[y].xs = xs;
        span[y].xe = xe;
      }
    }
  }
}

void draw() {      
  noStroke();
  noSmooth();
  background(0);

  for (Span s : span)
    s.clear();

  float xc = width / 2;
  float yc = height / 2;
  float o = sin(frameCount / 40.0) * 40;
  float r1 = 48 + sin(frameCount / 16.0) * 10;
  float r2 = 48 - sin(frameCount / 16.0) * 10;
  
  renderPrism(4, (int)xc, (int)(yc - o), (int)r1, (frameCount * 13) / 11);
  renderPrism(4, (int)xc, (int)(yc + o), (int)r2, -(frameCount * 5) / 7);

  for (int i = 0; i < height; i++) {
    Span s = span[i];
    
    if (s.size() <= 0)
      continue;

    stroke(map(s.size(), 64, 172, 0, 255));
    line(s.xs, i, s.xe, i);
  } 
}
