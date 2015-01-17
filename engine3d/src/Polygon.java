import java.util.*;

public class Polygon {
  Vector3D[] point;
  Vector3D normal;
  int color;

  static final int PF_LEFT   = 1;
  static final int PF_RIGHT  = 2;
  static final int PF_TOP    = 4;
  static final int PF_BOTTOM = 8;
  static final int PF_NEAR   = 16;
  static final int PF_FAR    = 32;
  
  Polygon(int points) {
    this.point = new Vector3D[points];
    this.color = 0xffffff;
  }
  
  int size() { return point.length; }

  public String toString() {
    String s = "";
    for (Vector3D v : point)
      s += v.toString();
    return String.format("(%s)", s);
  }

  private boolean checkInside(Vector3D p, int plane) {
    if ((plane & PF_LEFT) != 0)
      return (p.x >= -1.0f);
    if ((plane & PF_RIGHT) != 0)
      return (p.x <= 1.0f);
    if ((plane & PF_TOP) != 0)
      return (p.y >= -1.0f);
    if ((plane & PF_BOTTOM) != 0)
      return (p.y <= 1.0f);
    if ((plane & PF_NEAR) != 0)
      return (p.z >= -1.0f);
    if ((plane & PF_FAR) != 0)
      return (p.z <= 1.0f);
    return false;
  }

  private Vector3D clipEdge(Vector3D s, Vector3D e, int plane) {
    float dx = s.x - e.x, dy = s.y - e.y, dz = s.z - e.z;
  
    if ((plane & PF_LEFT) != 0) {
      float t = (-1.0f - e.x) / dx;
      s = new Vector3D(-1.0f, e.y + dy * t, e.z + dz * t);
    } else if ((plane & PF_RIGHT) != 0) {
      float t = (1.0f - e.x) / dx;
      s = new Vector3D(1.0f, e.y + dy * t, e.z + dz * t);
    } else if ((plane & PF_TOP) != 0) {
      float t = (-1.0f - e.y) / dy;
      s = new Vector3D(e.x + dx * t, -1.0f, e.z + dz * t);
    } else if ((plane & PF_BOTTOM) != 0) {
      float t = (1.0f - e.y) / dy;
      s = new Vector3D(e.x + dx * t, 1.0f, e.z + dz * t);
    } else if ((plane & PF_NEAR) != 0) {
      float t = (-1.0f - e.z) / dz;
      s = new Vector3D(e.x + dx * t, e.y + dy * t, -1.0f);
    } else if ((plane & PF_FAR) != 0) {
      float t = (1.0f - e.z) / dz;
      s = new Vector3D(e.x + dx * t, e.y + dy * t, 1.0f);
    }
    
    return s;
  }
  
  private boolean clipToPlane(int plane) {
    ArrayList<Vector3D> out = new ArrayList<Vector3D>();

    Vector3D S = point[0];
    boolean S_inside = checkInside(S, plane);
    boolean needClose = true;
  
    if (S_inside) {
      needClose = false;
      out.add(S);
    }

    for (int i = 1; i < point.length; i++) {
      Vector3D E = point[i];
      boolean E_inside = checkInside(E, plane);
  
      if (S_inside && E_inside) {
        out.add(E);
      } else if (S_inside && !E_inside) {
        out.add(clipEdge(S, E, plane));
      } else if (!S_inside && E_inside) {
        out.add(clipEdge(E, S, plane));
        out.add(E);
      }
  
      S_inside = E_inside;
      S = E;
    }
  
    if (out.size() == 0)
      return false;

    if (needClose)
      out.add(out.get(0));    
    
    this.point = new Vector3D[out.size()];
    this.point = out.toArray(this.point);
    return true;
  }

  boolean clip(int flags) {
    if ((flags & PF_LEFT) != 0)
      if (!clipToPlane(PF_LEFT))
        return false;

    if ((flags & PF_TOP) != 0)
      if (!clipToPlane(PF_TOP))
        return false;

    if ((flags & PF_NEAR) != 0)
      if (!clipToPlane(PF_NEAR))
        return false;

    if ((flags & PF_RIGHT) != 0)
      if (!clipToPlane(PF_RIGHT))
        return false;

    if ((flags & PF_BOTTOM) != 0)
      if (!clipToPlane(PF_BOTTOM))
        return false;

    if ((flags & PF_FAR) != 0)
      if (!clipToPlane(PF_FAR))
        return false;

    return true;
  }
};
