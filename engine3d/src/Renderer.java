import processing.core.PApplet;
import processing.core.PImage;

public class Renderer {
  PApplet parent;
  int[] pixels;
  int width;
  int height;
  
  PImage texture;
  int color;
  DisplayMode mode;
  
  Renderer(PApplet parent) {
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

  public class Edge {
    float dy;
    int y, h;

    float x, dx, dxdy;
    
    Edge(Vector3D l, Vector3D r) {
      x = l.x;
      dx = r.x - l.x;
      dy = r.y - l.y;
      
      y = Math.round(l.y);
      h = Math.round(r.y) - Math.round(l.y);      

      if (h > 0) {
        float prestep = (float)Math.round(l.y) + 0.5f - l.y;
        dxdy = dx / dy;
        x += dxdy * prestep;
      }
    }
    
    void step() {
      x += dxdy;
    }
  }

  void span(Edge left, Edge right, int y, int h) {
    int line = y * width;
    
    while (h-- > 0) {
      int xs = Math.round(left.x);
      int xe = Math.round(right.x);

      while (xs < xe) {
        pixels[line + xs] += color;
        xs++;
      }
      
      left.step();
      right.step();
      line += width;
    }
  }

  void triangle(Vector3D p0, Vector3D p1, Vector3D p2) {
    if (p0.y > p1.y) {
      Vector3D p = p0; p0 = p1; p1 = p;
    }
    if (p0.y > p2.y) {
      Vector3D p = p0; p0 = p2; p2 = p;
    }
    if (p1.y > p2.y) {
      Vector3D p = p1; p1 = p2; p2 = p;
    }

    Edge e01 = new Edge(p0, p1);
    Edge e02 = new Edge(p0, p2);
    Edge e12 = new Edge(p1, p2);

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

  public class TexturedEdge {
    float dy;
    int y, h;

    float x, dx, dxdy;
    float u, du, dudy;
    float v, dv, dvdy;
    
    TexturedEdge(Vector3D lp, UVCoord lt, Vector3D rp, UVCoord rt) {
      x = lp.x;
      u = lt.u;
      v = lt.v;

      dx = rp.x - lp.x;
      dy = rp.y - lp.y;
      du = rt.u - lt.u;
      dv = rt.v - lt.v;
      
      y = Math.round(lp.y);
      h = Math.round(rp.y) - Math.round(lp.y);      

      if (h > 0) {
        float prestep = (float)Math.round(lp.y) + 0.5f - lp.y;
        float inv_dy = 1.0f / dy;
        
        dxdy = dx * inv_dy;
        dudy = du * inv_dy;
        dvdy = dv * inv_dy;

        x += dxdy * prestep;
        u += dudy * prestep;
        v += dvdy * prestep;
      }
    }
    
    void step() {
      x += dxdy;
      u += dudy;
      v += dvdy;
    }
  }
  
  void texturedSpan(TexturedEdge left, TexturedEdge right, int y, int h) {
    int line = y * width;
    
    while (h-- > 0) {
      int xs = Math.round(left.x);
      int xe = Math.round(right.x);

      float dx = right.x - left.x;
      float du = right.u - left.u;
      float dv = right.v - left.v;
      
      float prestep = (float)Math.round(left.x) + 0.5f - left.x;
      float inv_dx = 1.0f / dx;

      float dudx = du * inv_dx;
      float dvdx = dv * inv_dx;

      float u = left.u + prestep * dudx;
      float v = left.v + prestep * dvdx;
      
      while (xs < xe) {
        int ui = Math.round(u) & (texture.width - 1);
        int vi = Math.round(v) & (texture.height - 1);

        pixels[line + xs] = texture.pixels[vi * texture.width + ui];
        
        u += dudx;
        v += dvdx;
        xs++;
      }
      
      left.step();
      right.step();
      line += width;
    }
  }

  void texturedTriangle(Vector3D p0, UVCoord t0,
                        Vector3D p1, UVCoord t1,
                        Vector3D p2, UVCoord t2)
  {
    if (p0.y > p1.y) {
      Vector3D p = p0; p0 = p1; p1 = p;
      UVCoord t = t0; t0 = t1; t1 = t;
    }
    if (p0.y > p2.y) {
      Vector3D p = p0; p0 = p2; p2 = p;
      UVCoord t = t0; t0 = t2; t2 = t;
    }
    if (p1.y > p2.y) {
      Vector3D p = p1; p1 = p2; p2 = p;
      UVCoord t = t1; t1 = t2; t2 = t;
    }

    TexturedEdge e01 = new TexturedEdge(p0, t0, p1, t1);
    TexturedEdge e02 = new TexturedEdge(p0, t0, p2, t2);
    TexturedEdge e12 = new TexturedEdge(p1, t1, p2, t2);

    boolean longOnRight;

    if (e01.h == 0)
      longOnRight = e01.dx < 0;
    else if (e12.h == 0)
      longOnRight = e12.dx > 0;
    else
      longOnRight = e01.dxdy < e02.dxdy;

    if (longOnRight) {
      texturedSpan(e01, e02, e01.y, e01.h);
      texturedSpan(e12, e02, e12.y, e12.h);
    } else {
      texturedSpan(e02, e01, e01.y, e01.h);
      texturedSpan(e02, e12, e12.y, e12.h);
    }
  }   
    
  void polygon(Polygon poly) {
    mode = poly.mode;
    texture = null;
    color = 0xffffff;
    
    if (mode == DisplayMode.WIREFRAME) {
      for (int i = 0; i < poly.vertex.length - 1; i++)
        line(poly.vertex[i].pos, poly.vertex[i + 1].pos);    
    } else if (mode == DisplayMode.FLAT_SHADED) {
      color = lerpColor(0, poly.color, poly.normal.z);
      for (int i = 2; i < poly.vertex.length; i++)
        triangle(poly.vertex[0].pos, poly.vertex[i - 1].pos, poly.vertex[i].pos);
    } else if (mode == DisplayMode.TEXTURED) {
      texture = poly.texture;
      texture.loadPixels();
      for (int i = 2; i < poly.vertex.length; i++)
        texturedTriangle(poly.vertex[0].pos, poly.vertex[0].uv,
                         poly.vertex[i - 1].pos, poly.vertex[i - 1].uv,
                         poly.vertex[i].pos, poly.vertex[i].uv);
    }
  }
};
