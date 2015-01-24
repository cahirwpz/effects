import java.util.ArrayList;
import java.util.List;

public class Frustum {
  abstract class Plane {
    static final int LEFT   = 1;
    static final int RIGHT  = 2;
    static final int TOP    = 4;
    static final int BOTTOM = 8;
    static final int NEAR   = 16;
    static final int FAR    = 32;
    
    abstract int number();
    abstract boolean isInside(Vector3D pos);
    abstract Vertex clipEdge(Vertex s, Vertex e);

    Vertex[] clipPolygon(Vertex[] vertex) {
      List<Vertex> out = new ArrayList<>();
      Vertex S = vertex[0];
      boolean S_inside = isInside(S.pos);
      boolean needClose = true;

      if (S_inside) {
        needClose = false;
        out.add(S);
      }

      for (int i = 1; i < vertex.length; i++) {
        Vertex E = vertex[i];
        boolean E_inside = isInside(E.pos);

        if (S_inside && E_inside) {
          out.add(E);
        } else if (S_inside && !E_inside) {
          out.add(clipEdge(S, E));
        } else if (!S_inside && E_inside) {
          out.add(clipEdge(E, S));
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
    
    UVCoord clipUV(UVCoord s, UVCoord e, float t) {
      if (s != null && e != null)
        return new UVCoord(e.u + (s.u - e.u) * t, e.v + (s.v - e.v) * t);      
      return null;
    }
  };

  class Left extends Plane {
    int number() {
      return LEFT;
    }
    
    boolean isInside(Vector3D pos) {
      return pos.x >= -1.0f;
    }

    Vertex clipEdge(Vertex s, Vertex e) {
      Vector3D d = Vector3D.sub(s.pos, e.pos);
      Vertex ns = s.copy();

      float t = (-1.0f - e.pos.x) / d.x;
      ns.pos = new Vector3D(-1.0f, e.pos.y + d.y * t, e.pos.z + d.z * t);
      ns.uv = clipUV(s.uv, e.uv, t);
      return ns;
    }
  };

  class Right extends Plane {
    int number() {
      return RIGHT;
    }

    boolean isInside(Vector3D pos) {
      return pos.x <= 1.0f;
    }

    Vertex clipEdge(Vertex s, Vertex e) {
      Vector3D d = Vector3D.sub(s.pos, e.pos);
      Vertex ns = s.copy();
      
      float t = (1.0f - e.pos.x) / d.x;
      ns.pos = new Vector3D(1.0f, e.pos.y + d.y * t, e.pos.z + d.z * t);
      ns.uv = clipUV(s.uv, e.uv, t);
      return ns;
    }
  };

  class Top extends Plane {
    int number() {
      return TOP;
    }

    boolean isInside(Vector3D pos) {
      return pos.y >= -1.0f;
    }

    Vertex clipEdge(Vertex s, Vertex e) {
      Vector3D d = Vector3D.sub(s.pos, e.pos);
      Vertex ns = s.copy();
      
      float t = (-1.0f - e.pos.y) / d.y;
      ns.pos = new Vector3D(e.pos.x + d.x * t, -1.0f, e.pos.z + d.z * t);
      ns.uv = clipUV(s.uv, e.uv, t);
      return ns;
    }
  };

  class Bottom extends Plane {
    int number() {
      return BOTTOM;
    }

    boolean isInside(Vector3D pos) {
      return pos.y <= 1.0f;
    }

    Vertex clipEdge(Vertex s, Vertex e) {
      Vector3D d = Vector3D.sub(s.pos, e.pos);
      Vertex ns = s.copy();
      float t = (1.0f - e.pos.y) / d.y;
      ns.pos = new Vector3D(e.pos.x + d.x * t, 1.0f, e.pos.z + d.z * t);
      ns.uv = clipUV(s.uv, e.uv, t);
      return ns;
    }
  };

  class Near extends Plane {
    int number() {
      return NEAR;
    }

    boolean isInside(Vector3D pos) {
      return pos.z >= -1.0f;
    }

    Vertex clipEdge(Vertex s, Vertex e) {
      Vector3D d = Vector3D.sub(s.pos, e.pos);
      Vertex ns = s.copy();
      float t = (-1.0f - e.pos.z) / d.z;
      ns.pos = new Vector3D(e.pos.x + d.x * t, e.pos.y + d.y * t, -1.0f);
      ns.uv = clipUV(s.uv, e.uv, t);
      return ns;
    }
  };

  class Far extends Plane {
    int number() {
      return FAR;
    }

    boolean isInside(Vector3D pos) {
      return pos.z <= 1.0f;
    }

    Vertex clipEdge(Vertex s, Vertex e) {
      Vector3D d = Vector3D.sub(s.pos, e.pos);
      Vertex ns = s.copy();
      
      float t = (1.0f - e.pos.z) / d.z;
      ns.pos = new Vector3D(e.pos.x + d.x * t, e.pos.y + d.y * t, 1.0f);
      ns.uv = clipUV(s.uv, e.uv, t);
      return ns;
    }
  }

  Plane[] planes;

  Frustum() {
    planes = new Plane[6];
    planes[0] = new Left();
    planes[1] = new Right();
    planes[2] = new Top();
    planes[3] = new Bottom();
    planes[4] = new Near();
    planes[5] = new Far();
  }
  
  int clipFlags(Vector3D pos) {
    int flags = 0;

    for (Plane p : planes)
      if (!p.isInside(pos))
        flags |= p.number();

    return flags;
  }

  Polygon clipPolygon(Polygon in, int clipFlags) {
    Vertex[] vertex = in.vertex;

    for (Plane p : planes) {
      if ((clipFlags & p.number()) == 0)
        continue;
      
      vertex = p.clipPolygon(vertex);
      if (vertex == null)
        return null;
    }

    Polygon out = in.copy();
    out.vertex = vertex;
    return out;
  }
}