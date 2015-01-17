import processing.core.*;
import java.util.*;

public class Scene3D {
  ArrayList<Mesh3D> meshes;

  Scene3D() {
    reset();
  }

  void reset() {
    meshes = new ArrayList<Mesh3D>();
  }

  void add(Mesh3D mesh, Matrix3D toWorld) {
    Mesh3D m = mesh.copy();
    m.vertex = toWorld.transform(mesh.vertex);
    meshes.add(m);
  }

  private int[] clipFlags(Vector3D vertex[]) {
    int[] flags = new int[vertex.length];

    for (int i = 0; i < vertex.length; i++) {
      float x = vertex[i].x, y = vertex[i].y, z = vertex[i].z;

      if (x < -1.0f)
        flags[i] |= Polygon.PF_LEFT;
      if (x >= 1.0f)
        flags[i] |= Polygon.PF_RIGHT;
      if (y < -1.0f)
        flags[i] |= Polygon.PF_TOP;
      if (y >= 1.0f)
        flags[i] |= Polygon.PF_BOTTOM;
      if (z < -1.0f)
        flags[i] |= Polygon.PF_NEAR;
      if (z >= 1.0f)
        flags[i] |= Polygon.PF_FAR;
    }

    return flags;
  }

  private ArrayList<Polygon> preparePolygons(Mesh3D mesh) {
    int[] flags = clipFlags(mesh.vertex);
    ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    for (MeshPolygon f : mesh.face) {
      Vector3D v0 = mesh.vertex[f.vertex[0]];
      Vector3D v1 = mesh.vertex[f.vertex[1]];
      Vector3D v2 = mesh.vertex[f.vertex[2]];
      
      Vector3D normal = Vector3D.cross(Vector3D.sub(v1, v0), Vector3D.sub(v1, v2));
      
      if (normal.z < 0)
        continue;
      
      int clip = 0, outside = -1;

      for (int k : f.vertex) {
        clip |= flags[k]; 
        outside &= flags[k];
      }

      if (outside == 0) {
        Polygon p = new Polygon(f.vertex.length + 1);
        int i;

        for (i = 0; i < f.vertex.length; i++)
          p.point[i] = mesh.vertex[f.vertex[i]];
        p.point[i] = p.point[0];
        
        if (p.clip(clip)) {
          p.normal = Vector3D.normalize(normal, 1.0f);
          p.color = mesh.surface[f.surfaceIndex].color;

          polygons.add(p);
        }
      }
    }

    return polygons;
  }

  void draw(Rasterizer r) {
    for (Mesh3D mesh : meshes) {
      ArrayList<Polygon> polygons = preparePolygons(mesh);

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

