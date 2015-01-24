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

  void draw(Renderer r) {
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

    for (Polygon polygon : polygons) {
      Vertex[] vertex = polygon.vertex;

      for (int i = 0; i < vertex.length; i++) {
        Vector3D p = vertex[i].pos;
        // if w is zero the vector has been projected onto 2d plane
        if (p.w != 0.0f) {
          p.x = 0.5f * (r.width - 1) * (p.x + 1.0f);
          p.y = 0.5f * (r.height - 1) * (p.y + 1.0f);
          p.w = 0.0f;
        }
      }

      r.color = r.lerpColor(0, polygon.color, polygon.normal.z);

      for (int i = 2; i < vertex.length; i++)
        r.triangle(vertex[0], vertex[i - 1], vertex[i]);
    }
  }
};

