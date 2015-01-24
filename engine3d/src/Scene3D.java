import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scene3D {
  ArrayList<Object3D> objects;
  Matrix3D view, projection;
  Frustum frustum;
  
  Scene3D() {
    objects = new ArrayList<Object3D>();
    
    perspective(45.0f, 4.0f / 3.0f, 1.0f, 100.0f);
    view = new Matrix3D();
    
    frustum = new Frustum();
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
    List<Polygon> polygons = new ArrayList<>();
    
    for (Object3D obj : objects) {
      obj.transform(view, projection);
      obj.updateClipFlags(frustum);
 
      for (Polygon p : obj.polygon) {
        p.updateNormal();
                
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
          p = frustum.clipPolygon(p, clipFlags);

        if (p != null && p.vertex.length > 2) {
          p.updateDepth();
          polygons.add(p);
        }
      }
    }
    
    Collections.sort(polygons);
    
    for (Polygon polygon : polygons)
      r.draw(polygon);
  }
};

