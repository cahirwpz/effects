import java.io.BufferedReader;
import java.util.ArrayList;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class Mesh3D {
  Vector3D[] vertex;
  MeshPolygon[] polygon;
  MeshSurface[] surface;
  
  Mesh3D() {}  
  
  Mesh3D(int vertices, int polygons, int surfaces) {
    this.vertex = new Vector3D[vertices];
    this.polygon = new MeshPolygon[polygons];
    this.surface = new MeshSurface[surfaces];
  }

  Mesh3D copy() {
    Mesh3D mesh = new Mesh3D();
    mesh.vertex = vertex;
    mesh.polygon = polygon;
    mesh.surface = surface;
    return mesh;
  }

  static Mesh3D readFromJSON(BufferedReader in) {
    JSONObject json = new JSONObject(in);
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
      
      mesh.polygon[i] = new MeshPolygon(p.size());
      for (int j = 0; j < p.size(); j++)
        mesh.polygon[i].vertexIndex[j] = p.getInt(j);
    }
    
    for (int i = 0; i < ptag.size(); i++) {
      JSONArray t = ptag.getJSONArray(i);
      int polygonIndex = t.getInt(0);
      int surfaceIndex = t.getInt(1);
      mesh.polygon[polygonIndex].surfaceIndex = surfaceIndex;
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
  
  static Mesh3D readFromLWO(String path) {
    IffFile iff = IffFile.read(path);
    
    if (!iff.isType("LWO2"))
      return null;
    
    Mesh3D mesh = new Mesh3D();
    
    ArrayList<MeshSurface> srfs = new ArrayList<>();
    ArrayList<String> tags = new ArrayList<>();
    
    for (IffFile.Chunk chunk : iff.chunks) {
      if (chunk.isType("PNTS")) {
        int n = chunk.size() / 12;
        mesh.vertex = new Vector3D[n];
        for (int i = 0; i < n; i++) {
          mesh.vertex[i] = chunk.getVector3D();
        }
      } else if (chunk.isType("POLS")) {
        if (chunk.getId().equals("FACE")) {
          ArrayList<MeshPolygon> pols = new ArrayList<>();
          while (chunk.hasRemaining()) {
            MeshPolygon poly = new MeshPolygon(chunk.getShort());
            for (int i = 0; i < poly.size(); i++)
              poly.vertexIndex[i] = chunk.getShort();
            pols.add(poly);
          }
          mesh.polygon = pols.toArray(new MeshPolygon[pols.size()]);
        }
      } else if (chunk.isType("PTAG")) {
        if (chunk.getId().equals("SURF")) {
          while (chunk.hasRemaining()) {
            int poly = chunk.getShort();
            int surf = chunk.getShort() - 1;
            mesh.polygon[poly].surfaceIndex = surf;
          }
        }
      } else if (chunk.isType("SURF")) {
        String name = chunk.getString();
        chunk.getString(); /* skip source */
        MeshSurface surf = new MeshSurface(name, 0xffffff);
        for (IffFile.Chunk minick : chunk.parseMiniChunks()) {
          if (minick.isType("COLR")) {
            Vector3D c = minick.getVector3D();
            c.scale(255.0f);
            surf.color = ((int)c.x << 16) | ((int)c.y << 8) | (int)c.z; 
          } else if (minick.isType("SPEC")) {
            surf.specular = minick.getFloat();
          } else if (minick.isType("DIFF")) {
            surf.diffuse = minick.getFloat();
          } else if (minick.isType("VERS") || minick.isType("NODS")) {
            /* skip */
          } else {
            System.out.println("Mini chunk not handled: " + minick.id);
          }
        }
        srfs.add(surf);
      } else if (chunk.isType("TAGS")) {
        while (chunk.hasRemaining())
          tags.add(chunk.getString());
      } else if (chunk.isType("BBOX") || chunk.isType("LAYR")) {
        /* skip */
      } else {
        System.out.println("Chunk not handled: " + chunk.id);
      }
    }
    
    mesh.surface = srfs.toArray(new MeshSurface[srfs.size()]);
    
    return mesh;
  }
  
  Mesh3D triangulate() {
    ArrayList<MeshPolygon> ts = new ArrayList<MeshPolygon>();
    
    for (MeshPolygon p : polygon) {
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
    m.polygon = triangles;
    return m;
  }
};
