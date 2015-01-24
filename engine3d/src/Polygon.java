import processing.core.PImage;

public class Polygon implements Comparable<Polygon> {
  Vertex[] vertex;
  PImage texture;
  Vector3D normal;  /* flat shading */
  int color;
  float depth;      /* depth sorting */

  Polygon(Vertex[] vertex, int color) {
    this.vertex = vertex;
    this.color = color;
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
};
