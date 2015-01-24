import java.util.*;

public class Scene3D {
  ArrayList<Object3D> objects;
  Matrix3D view, projection;
  
  Scene3D() {
    objects = new ArrayList<Object3D>();
    
    perspective(45.0f, 4.0f / 3.0f, 1.0f, 100.0f);
    view = new Matrix3D();
  }

  void add(Object3D obj) {
    objects.add(obj);
  }

  void perspective(float fovy, float aspect, float near, float far) {
    projection = Matrix3D.perspective(fovy, aspect, near, far);
  }
  
  void cameraLookAt(Vector3D eye, Vector3D target, Vector3D up) {
    view = Matrix3D.cameraLookAt(eye, target, up);
  }

  void draw(Rasterizer r) {
    r.reset();
    
    for (Object3D obj : objects) {
      obj.transform(view, projection);
 
      for (Polygon p : obj.polygon) {
        p.refreshNormal();
                
        int outsideFlags = -1;
        int clipFlags = 0;

        for (Vertex v : p.vertex) {
          clipFlags |= obj.clipFlags[v.index];
          outsideFlags &= obj.clipFlags[v.index];
        }
          
        if (outsideFlags != 0)
          continue;

        // back-face culling
        if (p.normal.z < 0)
          continue;

        if (clipFlags != 0)
          p = Frustum.clip(p, clipFlags);

        if (p != null)
          r.add(p);
      }
    }
    
    r.draw();
  }
};

