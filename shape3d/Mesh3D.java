import processing.core.*;
import processing.data.*;
import java.util.*;

public class Mesh3D {
  Vector3D[] vertex;
  Face[] face;
    
  Mesh3D(int vertices, int faces) {
    this.vertex = new Vector3D[vertices];
    this.face = new Face[faces];
  }

  Mesh3D(Vector3D[] vertex, Face[] face) {
    this.vertex = vertex;
    this.face = face;
  }

  static Mesh3D parse(JSONObject json) {
    JSONArray vertices = json.getJSONArray("vertices");
    JSONArray faces = json.getJSONArray("faces");

    Mesh3D mesh = new Mesh3D(vertices.size(), faces.size());

    for (int i = 0; i < vertices.size(); i++) {
      JSONArray vertex = vertices.getJSONArray(i);
      
      mesh.vertex[i] = new Vector3D(vertex.getInt(0), vertex.getInt(1), vertex.getInt(2));
    }
  
    for (int i = 0; i < faces.size(); i++) {
      JSONArray face = faces.getJSONArray(i);
      
      mesh.face[i] = new Face(face.size());
      for (int j = 0; j < face.size(); j++)
        mesh.face[i].vertex[j] = face.getInt(j);
    }
    
    return mesh;
  }
  
  Mesh3D triangulate() {
    ArrayList<Face> ts = new ArrayList<Face>();
    
    for (Face f : face) {
      for (int i = 2; i < f.vertex.length; i++) {
        Face t = new Face(3);
        t.vertex[0] = f.vertex[0];
        t.vertex[1] = f.vertex[i - 1];
        t.vertex[2] = f.vertex[i];
        ts.add(t);
      }
    }
    
    Face[] triangles = new Face[ts.size()];
    triangles = ts.toArray(triangles);

    return new Mesh3D(vertex, triangles);
  }
};
