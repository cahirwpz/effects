import processing.core.PImage;

public class Polygon implements Comparable<Polygon> {
  Vertex[] vertex;
  float depth;      /* depth sorting */

  DisplayMode mode;
  Vector3D normal;  /* flat shading */
  int color;
  PImage texture;
  boolean doubleSided;
  
  Polygon(Vertex[] vertex, int color) {
    this.mode = DisplayMode.FLAT_SHADED;
    this.vertex = vertex;
    this.color = color;
  }

  Polygon(Vertex[] vertex, PImage texture) {
    this.mode = DisplayMode.TEXTURED;
    this.vertex = vertex;
    this.texture = texture;
  }

  Polygon copy() {
    Polygon p = null;
    
    switch (mode) {
      case FLAT_SHADED:
        p = new Polygon(vertex, color);
        break;
      case TEXTURED:
        p = new Polygon(vertex, texture);
        break;
    }
    
    p.depth = depth;
    p.normal = normal;
    p.doubleSided = doubleSided;
    return p;
  }
  
  public String toString() {
    String s = "";
    for (Vertex v : vertex)
      s += v.toString();
    return String.format("(%s)", s);
  }

  void updateNormal() {
    Vector3D u = Vector3D.sub(vertex[1].pos, vertex[0].pos);
    Vector3D v = Vector3D.sub(vertex[1].pos, vertex[2].pos);
    normal = Vector3D.normalize(Vector3D.cross(u, v), 1.0f);
  }
  
  void updateDepth() {
    depth = 0.0f;
    for (Vertex v : vertex)
      depth += v.pos.z;
    depth /= vertex.length;
  }

  @Override
  public int compareTo(Polygon p) {
    if (depth > p.depth)
      return -1;
    if (depth < p.depth)
      return 1;
    return 0;
  }
}
