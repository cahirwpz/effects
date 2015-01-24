import java.util.ArrayList;
import java.util.List;

public class Frustum {
  static final int LEFT   = 1;
  static final int RIGHT  = 2;
  static final int TOP    = 4;
  static final int BOTTOM = 8;
  static final int NEAR   = 16;
  static final int FAR    = 32;
  
  static boolean left(int plane) {
    return (plane & LEFT) != 0;
  }
  
  static boolean right(int plane) {
    return (plane & RIGHT) != 0;
  }
  
  static boolean top(int plane) {
    return (plane & TOP) != 0;
  }
  
  static boolean bottom(int plane) {
    return (plane & BOTTOM) != 0;
  }
  
  static boolean near(int plane) {
    return (plane & NEAR) != 0;
  }
  
  static boolean far(int plane) {
    return (plane & FAR) != 0;
  }
  
  static int clipFlags(Vector3D pos) {
    int flags = 0;
    
    if (pos.x < -1.0f)
      flags |= LEFT;
    if (pos.x >= 1.0f)
      flags |= RIGHT;
    if (pos.y < -1.0f)
      flags |= TOP;
    if (pos.y >= 1.0f)
      flags |= BOTTOM;
    if (pos.z < -1.0f)
      flags |= NEAR;
    if (pos.z >= 1.0f)
      flags |= FAR;
  
    return flags;
  }

  static boolean checkInside(Vector3D pos, int plane) {
    if ((plane & LEFT) != 0)
      return (pos.x >= -1.0f);
    if ((plane & RIGHT) != 0)
      return (pos.x <= 1.0f);
    if ((plane & TOP) != 0)
      return (pos.y >= -1.0f);
    if ((plane & BOTTOM) != 0)
      return (pos.y <= 1.0f);
    if ((plane & NEAR) != 0)
      return (pos.z >= -1.0f);
    if ((plane & FAR) != 0)
      return (pos.z <= 1.0f);
    return false;
  }
  
  static Vertex clipEdge(Vertex s, Vertex e, int plane) {
    float dx = s.pos.x - e.pos.x;
    float dy = s.pos.y - e.pos.y;
    float dz = s.pos.z - e.pos.z;
    Vertex n = s.copy();
    
    if (left(plane)) {
      float t = (-1.0f - e.pos.x) / dx;
      n.pos = new Vector3D(-1.0f, e.pos.y + dy * t, e.pos.z + dz * t);
    } else if (right(plane)) {
      float t = (1.0f - e.pos.x) / dx;
      n.pos = new Vector3D(1.0f, e.pos.y + dy * t, e.pos.z + dz * t);
    } else if (top(plane)) {
      float t = (-1.0f - e.pos.y) / dy;
      n.pos = new Vector3D(e.pos.x + dx * t, -1.0f, e.pos.z + dz * t);
    } else if (bottom(plane)) {
      float t = (1.0f - e.pos.y) / dy;
      n.pos = new Vector3D(e.pos.x + dx * t, 1.0f, e.pos.z + dz * t);
    } else if (near(plane)) {
      float t = (-1.0f - e.pos.z) / dz;
      n.pos = new Vector3D(e.pos.x + dx * t, e.pos.y + dy * t, -1.0f);
    } else if (far(plane)) {
      float t = (1.0f - e.pos.z) / dz;
      n.pos = new Vector3D(e.pos.x + dx * t, e.pos.y + dy * t, 1.0f);
    } else {
      return s;
    }
    
    return n;
  }
  
  
  static Vertex[] clipToPlane(Vertex[] vertex, int clipFlags, int plane) {
    if ((clipFlags & plane) == 0)
      return vertex;
    
    List<Vertex> out = new ArrayList<>();  
    Vertex S = vertex[0];
    boolean S_inside = Frustum.checkInside(S.pos, plane);
    boolean needClose = true;
  
    if (S_inside) {
      needClose = false;
      out.add(S);
    }
 
    for (int i = 1; i < vertex.length; i++) {
      Vertex E = vertex[i];
      boolean E_inside = Frustum.checkInside(E.pos, plane);
  
      if (S_inside && E_inside) {
        out.add(E);
      } else if (S_inside && !E_inside) {
        out.add(Frustum.clipEdge(S, E, plane));
      } else if (!S_inside && E_inside) {
        out.add(Frustum.clipEdge(E, S, plane));
        out.add(E);
      }
  
      S_inside = E_inside;
      S = E;
    }
  
    if (out.size() == 0)
      return null;

    if (needClose)
      out.add(out.get(0));    
    
    return out.toArray(new Vertex[out.size()]);
  }
  
  static Polygon clip(Polygon in, int clipFlags) {
    Vertex[] clipped = in.vertex;
    
    clipped = Frustum.clipToPlane(clipped, clipFlags, Frustum.LEFT);
    if (clipped == null)
      return null;

    clipped = Frustum.clipToPlane(clipped, clipFlags, Frustum.TOP);
    if (clipped == null)
      return null;

    clipped = Frustum.clipToPlane(clipped, clipFlags, Frustum.NEAR);
    if (clipped == null)
      return null;

    clipped = Frustum.clipToPlane(clipped, clipFlags, Frustum.RIGHT);
    if (clipped == null)
      return null;
      
    clipped = Frustum.clipToPlane(clipped, clipFlags, Frustum.BOTTOM);
    if (clipped == null)
      return null;
      
    clipped = Frustum.clipToPlane(clipped, clipFlags, Frustum.FAR);
    if (clipped == null)
      return null;

    Polygon out = new Polygon(clipped, in.color);
    out.normal = in.normal;
    out.depth = in.depth;
    return out;
  }
}