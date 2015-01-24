import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PApplet;

public class Rasterizer {
  PApplet       parent;
  List<Polygon> polygons;

  Rasterizer(PApplet parent) {
    this.polygons = new ArrayList<>();
    this.parent = parent;
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
      int k = x1 + parent.width * y1;
      int n = Math.abs(x2 - x1);
      int err = 2 * dy - 2 * dx * fr / 16;
      int d1 = 2 * (dy - dx);
      int d2 = 2 * dy;

      do {
        parent.pixels[k] = col;
        k += ix;
        if (err > 0) {
          k += parent.width;
          err += d1;
        } else {
          err += d2;
        }
      } while (--n >= 0);
    } else {
      int k = x1 + parent.width * y1;
      int n = Math.abs(y2 - y1);
      int err = 2 * dx - 2 * dy * fr / 16;
      int d1 = 2 * (dx - dy);
      int d2 = 2 * dx;

      do {
        parent.pixels[k] = col;
        k += parent.width;
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
    int xi, y, h;
    float x, dx;

    EdgeScan(Vector3D s, Vector3D e) {
      x = s.x;
      y = Math.round(s.y);
      h = Math.round(e.y) - Math.round(s.y);

      if (h > 0) {
        dx = (e.x - s.x) / (e.y - s.y);
        x += dx * (Math.ceil(s.y + 0.5f) - (s.y + 0.5f));
      }

      xi = Math.round(x);
    }

    void next() {
      x += dx;
      xi = Math.round(x);
      y++;
    }
  };

  private void span(int xs, int xe, int y, int col) {
    int k = y * parent.width;

    do {
      parent.pixels[xs + k] = col;
    } while (++xs <= xe);
  }

  private void triangle(Vector3D p0, Vector3D p1, Vector3D p2, int col) {
    if (p0.y > p1.y) {
      Vector3D pt = p0; p0 = p1; p1 = pt;
    }
    if (p0.y > p2.y) {
      Vector3D pt = p0; p0 = p2; p2 = pt;
    }
    if (p1.y > p2.y) {
      Vector3D pt = p1; p1 = p2; p2 = pt;
    }

    EdgeScan e01 = new EdgeScan(p0, p1);
    EdgeScan e02 = new EdgeScan(p0, p2);
    EdgeScan e12 = new EdgeScan(p1, p2);

    boolean longOnRight;

    if (e01.h == 0)
      longOnRight = p1.x < p0.x;
    else if (e12.h == 0)
      longOnRight = p2.x > p1.x;
    else
      longOnRight = e01.dx < e02.dx;

    EdgeScan left = longOnRight ? e01 : e02;
    EdgeScan right = longOnRight ? e02 : e01;

    for (int i = 0; i < e01.h; i++, left.next(), right.next())
      span(left.xi, right.xi, left.y, col);

    if (longOnRight)
      left = e12;
    else
      right = e12;

    for (int i = 0; i < e12.h; i++, left.next(), right.next())
      span(left.xi, right.xi, left.y, col);
  }
  
  void reset() {
    polygons.clear();
  }

  void add(Polygon p) {
    if (p.vertex.length > 2) {
      p.refreshDepth();
      polygons.add(p);
    }
  }

  void draw() {
    Collections.sort(polygons);
    
    for (Polygon p : polygons) {
      Vector3D[] pt = new Vector3D[p.vertex.length];

      for (int i = 0; i < p.vertex.length; i++) {
        float x = 0.5f * (parent.width - 1) * (p.vertex[i].pos.x + 1.0f);
        float y = 0.5f * (parent.height - 1) * (p.vertex[i].pos.y + 1.0f);
        pt[i] = new Vector3D(x, y, p.vertex[i].pos.z);
      }

      int color = parent.lerpColor(0, p.color, p.normal.z);

      for (int i = 2; i < pt.length; i++)
        triangle(pt[0], pt[i - 1], pt[i], color);
    }
  }
};
