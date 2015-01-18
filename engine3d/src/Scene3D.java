import java.util.*;

public class Scene3D {
  ArrayList<Object3D> objects;
  Matrix3D projection;
  
  Scene3D() {
    objects = new ArrayList<Object3D>();
    
    perspective(45.0f, 4.0f / 3.0f, 1.0f, 100.0f);
  }

  void add(Object3D obj) {
    objects.add(obj);
  }

  void perspective(float fovy, float aspect, float near, float far) {
    projection = Matrix3D.loadPerspective(fovy, aspect, near, far);
  }

  private ArrayList<Polygon> preparePolygons(Object3D obj) {
    ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    for (MeshPolygon f : obj.mesh.face) {
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
        polygons.add(p);
    }

    return polygons;
  }

  void draw(Rasterizer r) {
    for (Object3D obj : objects) {
      obj.transform(projection);
      
      ArrayList<Polygon> polygons = preparePolygons(obj);

      for (Polygon polygon : polygons) {
        Vector3D[] p = r.toScreen(polygon.point);
        
        if (p.length < 3)
          continue;
          
        int color = r.lerpColor(0, polygon.color, polygon.normal.z);

        for (int i = 2; i < p.length; i++)
          r.triangle(p[0], p[i-1], p[i], color);
      }
    }
  }
};

