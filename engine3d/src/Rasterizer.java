import processing.core.PApplet;

public class Rasterizer {
  PApplet parent;
  int[]   pixels;
  int     width;
  int     height;
  int     color;

  Rasterizer(PApplet parent) {
    this.parent = parent;
    
    parent.loadPixels();
    this.width = parent.width;
    this.height = parent.height;
    this.pixels = parent.pixels;
  }

  void line(float x1f, float y1f, float x2f, float y2f, int col) {
    if (y2f < y1f) {
      float xf = x1f; x1f = x2f; x2f = xf;
      float yf = y1f; y1f = y2f; y2f = yf;
    }

    int x1 = (int) x1f, y1 = (int) y1f;
    int x2 = (int) x2f, y2 = (int) y2f;
    int x1i = (int) (x1f * 16), y1i = (int) (y1f * 16);
    int x2i = (int) (x2f * 16), y2i = (int) (y2f * 16);
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
        pixels[k] = col;
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
        pixels[k] = col;
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
    int xi; 
    int y, h;
    float x, dx;

    EdgeScan(Vertex s, Vertex e) {
      x = s.pos.x;
      y = Math.round(s.pos.y);
      h = Math.round(e.pos.y) - Math.round(s.pos.y);

      if (h > 0) {
        float dy = 1.0f / (e.pos.y - s.pos.y);
        float prestep = (float)Math.ceil(s.pos.y + 0.5f) - (s.pos.y + 0.5f);
        
        dx = (e.pos.x - s.pos.x) * dy;
        x += dx * prestep;
      }

      xi = Math.round(x);
    }

    void next() {
      x += dx;
      xi = Math.round(x);
    }
  };

  private void span(EdgeScan left, EdgeScan right, int y, int h) {
    int line = y * width;
    
    while (h-- > 0) {
      int xs = left.xi;
      int xe = right.xi;

      do {
        pixels[line + xs] = color;
      } while (++xs <= xe);

      left.next();
      right.next();
      line += width;
    }
  }

  void draw(Vertex v0, Vertex v1, Vertex v2) {
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
      longOnRight = v1.pos.x < v0.pos.x;
    else if (e12.h == 0)
      longOnRight = v2.pos.x > v1.pos.x;
    else
      longOnRight = e01.dx < e02.dx;

    if (longOnRight) {
      span(e01, e02, e01.y, e01.h);
      span(e12, e02, e12.y, e12.h);
    } else {
      span(e02, e01, e01.y, e01.h);
      span(e02, e12, e12.y, e12.h);
    }
  }

  void draw(Polygon polygon) {
    Vertex[] vertex = polygon.vertex;

    for (int i = 0; i < vertex.length; i++) {
      Vector3D p = vertex[i].pos;
      // if w is zero the vector has been projected onto 2d plane
      if (p.w != 0.0f) {
        p.x = 0.5f * (width - 1) * (p.x + 1.0f);
        p.y = 0.5f * (height - 1) * (p.y + 1.0f);
        p.w = 0.0f;
      }
    }

    color = parent.lerpColor(0, polygon.color, polygon.normal.z);

    for (int i = 2; i < vertex.length; i++)
      draw(vertex[0], vertex[i - 1], vertex[i]);
  }
};
