import processing.core.PApplet;

public class Rasterizer {
  PApplet parent;
  int[] pixels;
  int width;
  int height;
  int color;
  
  Rasterizer(PApplet parent) {
    this.parent = parent;
    
    parent.loadPixels();
    this.width = parent.width;
    this.height = parent.height;
    this.pixels = parent.pixels;
  }
  
  int lerpColor(int c1, int c2, float amt) {
    return parent.lerpColor(c1, c2, amt);
  }

  void line(Vector3D v1, Vector3D v2) {
    if (v1.y > v2.y) {
      Vector3D vt = v1; v1 = v2; v2 = vt;
    }

    int x1 = (int) v1.x, y1 = (int) v1.y;
    int x2 = (int) v2.x, y2 = (int) v2.y;
    int x1i = (int) (v1.x * 16), y1i = (int) (v1.y * 16);
    int x2i = (int) (v2.x * 16), y2i = (int) (v2.y * 16);
    int dx = Math.abs(x2i - x1i), dy = Math.abs(y2i - y1i);

    int ix = (x1i < x2i) ? 1 : -1;
    int fr = (15 - y1i) & 15;

    if (dy < dx) {
      int k = x1 + width * y1;
      int n = Math.abs(x2 - x1);
      int err = 2 * dy - 2 * dx * fr / 16;
      int d1 = 2 * (dy - dx);
      int d2 = 2 * dy;

      do {
        pixels[k] = color;
        k += ix;
        if (err > 0) {
          k += width;
          err += d1;
        } else {
          err += d2;
        }
      } while (--n >= 0);
    } else {
      int k = x1 + width * y1;
      int n = Math.abs(y2 - y1);
      int err = 2 * dx - 2 * dy * fr / 16;
      int d1 = 2 * (dx - dy);
      int d2 = 2 * dx;

      do {
        pixels[k] = color;
        k += width;
        if (err > 0) {
          k += ix;
          err += d1;
        } else {
          err += d2;
        }
      } while (--n >= 0);
    }
  }

  public class EdgeScan {
    float dx, dy;
    int y, h;

    int xi;
    float x, dxdy;

    EdgeScan(Vertex s, Vertex e) {
      x = s.pos.x;
      dx = e.pos.x - s.pos.x;
      dy = e.pos.y - s.pos.y;
      
      y = Math.round(s.pos.y);
      h = Math.round(e.pos.y) - Math.round(s.pos.y);      

      if (h > 0) {
        float prestep = (float)Math.ceil(s.pos.y + 0.5f) - (s.pos.y + 0.5f);
        
        dxdy = dx / dy;
        x += dxdy * prestep;
      }

      xi = Math.round(x);
    }

    void step() {
      x += dxdy;
      xi = Math.round(x);
    }
  };

  void span(EdgeScan left, EdgeScan right, int y, int h) {
    int line = y * width;
    
    while (h-- > 0) {
      int xs = left.xi;
      int xe = right.xi;

      do {
        pixels[line + xs] = color;
      } while (++xs <= xe);

      left.step();
      right.step();
      line += width;
    }
  }

  void triangle(Vertex v0, Vertex v1, Vertex v2) {
    if (v0.pos.y > v1.pos.y) {
      Vertex vt = v0; v0 = v1; v1 = vt;
    }
    if (v0.pos.y > v2.pos.y) {
      Vertex vt = v0; v0 = v2; v2 = vt;
    }
    if (v1.pos.y > v2.pos.y) {
      Vertex vt = v1; v1 = v2; v2 = vt;
    }

    EdgeScan e01 = new EdgeScan(v0, v1);
    EdgeScan e02 = new EdgeScan(v0, v2);
    EdgeScan e12 = new EdgeScan(v1, v2);

    boolean longOnRight;

    if (e01.h == 0)
      longOnRight = e01.dx < 0;
    else if (e12.h == 0)
      longOnRight = e12.dx > 0;
    else
      longOnRight = e01.dxdy < e02.dxdy;

    if (longOnRight) {
      span(e01, e02, e01.y, e01.h);
      span(e12, e02, e12.y, e12.h);
    } else {
      span(e02, e01, e01.y, e01.h);
      span(e02, e12, e12.y, e12.h);
    }
  }
};
