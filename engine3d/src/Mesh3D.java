import processing.data.*;
import java.util.*;

public class Mesh3D {
  Vector3D[] vertex;
  MeshPolygon[] face;
  MeshSurface[] surface;
  
  Mesh3D() {}  
  
  Mesh3D(int vertices, int polygons, int surfaces) {
    this.vertex = new Vector3D[vertices];
    this.face = new MeshPolygon[polygons];
    this.surface = new MeshSurface[surfaces];
  }

  Mesh3D copy() {
    Mesh3D mesh = new Mesh3D();
    mesh.vertex = vertex;
    mesh.face = face;
    mesh.surface = surface;
    return mesh;
  }

  static Mesh3D parse(JSONObject json) {
    JSONArray pnts = json.getJSONArray("pnts");
    JSONArray pols = json.getJSONArray("pols");
    JSONArray surf = json.getJSONArray("surf");
    JSONArray ptag = json.getJSONArray("ptag");

    Mesh3D mesh = new Mesh3D(pnts.size(), pols.size(), surf.size());

    for (int i = 0; i < pnts.size(); i++) {
      JSONArray vertex = pnts.getJSONArray(i);
      
      mesh.vertex[i] = new Vector3D(vertex.getInt(0), vertex.getInt(1), vertex.getInt(2));
    }
  
    for (int i = 0; i < pols.size(); i++) {
      JSONArray p = pols.getJSONArray(i);
      
      mesh.face[i] = new MeshPolygon(p.size());
      for (int j = 0; j < p.size(); j++)
        mesh.face[i].vertexIndex[j] = p.getInt(j);
    }
    
    for (int i = 0; i < ptag.size(); i++) {
      JSONArray t = ptag.getJSONArray(i);
      int polygonIndex = t.getInt(0);
      int surfaceIndex = t.getInt(1);
      mesh.face[polygonIndex].surfaceIndex = surfaceIndex;
    }
    
    for (int i = 0; i < surf.size(); i++) {
      JSONObject s = surf.getJSONObject(i);
      String name = s.getString("name");
      JSONArray rgb = s.getJSONArray("color");
      
      int r = (int)(rgb.getFloat(0) * 255);
      int g = (int)(rgb.getFloat(1) * 255);
      int b = (int)(rgb.getFloat(2) * 255);
      
      mesh.surface[i] = new MeshSurface(name, (r << 16) | (g << 8) | b);
    } 
    
    return mesh;
  }
  
  Mesh3D triangulate() {
    ArrayList<MeshPolygon> ts = new ArrayList<MeshPolygon>();
    
    for (MeshPolygon p : face) {
      for (int i = 2; i < p.vertexIndex.length; i++) {
        MeshPolygon t = new MeshPolygon(3);
        t.vertexIndex[0] = p.vertexIndex[0];
        t.vertexIndex[1] = p.vertexIndex[i - 1];
        t.vertexIndex[2] = p.vertexIndex[i];
        ts.add(t);
      }
    }
    
    MeshPolygon[] triangles = new MeshPolygon[ts.size()];
    triangles = ts.toArray(triangles);
    
    Mesh3D m = copy();
    m.face = triangles;
    return m;
  }
};
