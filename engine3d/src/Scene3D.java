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
      obj.refreshClipFlags();
 
      for (MeshPolygon f : obj.mesh.polygon) {
        int clipFlags = 0, outside = -1;

        for (int k : f.vertexIndex) {
          clipFlags |= obj.clipFlags[k];
          outside &= obj.clipFlags[k];
        }

        if (outside > 0)
          continue;

        Polygon p = f.toPolygon(obj.vertex, obj.mesh.surface);

        // back-face culling
        if (p.normal.z < 0)
          continue;

        if (p.clip(clipFlags))
          r.add(p);
      }
    }
    
    r.draw();
  }
};

