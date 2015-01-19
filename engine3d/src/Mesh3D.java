import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

  static Mesh3D readFromOBJ(String path) {
    List<Vector3D> vertex = new ArrayList<>();
    List<MeshPolygon> polygon = new ArrayList<>();
    List<MeshSurface> surface = new ArrayList<>();
    List<String> mtllib = new ArrayList<>();
    HashMap<String, Integer> mtlnum = new HashMap<>();
    Mesh3D mesh = null;
    int lastmtl = 0;
    int mtl = 0;
    
    try {
      List<String> lines = Files.readAllLines(
          Paths.get(path), Charset.defaultCharset());
      
      for (String l : lines) {
        String[] fs = l.trim().split("\\s+");
        
        if (fs.length == 0)
          continue;
        
        if (fs[0].equals("v")) {
          float x = Float.parseFloat(fs[1]);
          float y = Float.parseFloat(fs[2]);
          float z = Float.parseFloat(fs[3]);
          vertex.add(new Vector3D(x, y, z));
        } else if (fs[0].equals("f")) {
          MeshPolygon p = new MeshPolygon(fs.length - 1);
          for (int i = 0; i < fs.length - 1; i++) {
            String[] vs = fs[i+1].split("/");
            p.vertexIndex[i] = Integer.parseInt(vs[0]) - 1;
          }
          p.surfaceIndex = lastmtl;
          polygon.add(p);
        } else if (fs[0].equals("mtllib")) {
          if (!mtllib.contains(fs[1])) {
            List<String> mtllines = Files.readAllLines(
                Paths.get(fs[1]), Charset.defaultCharset());
            MeshSurface surf = null;
            for (String ml : mtllines) {
              String[] mfs = ml.trim().split("\\s+");
              
              if (mfs.length == 0)
                continue;
              
              if (mfs[0].equals("newmtl")) {
                if (surf != null)
                  surface.add(surf);
                surf = new MeshSurface(mfs[1], 0xffffff);
                mtlnum.put(mfs[1], mtl++);
              } else if (mfs[0].equals("Ka")) {
                int r = (int)(Float.parseFloat(mfs[1]) * 255.0f);
                int g = (int)(Float.parseFloat(mfs[2]) * 255.0f);
                int b = (int)(Float.parseFloat(mfs[3]) * 255.0f);
                surf.color = (r << 16) | (g << 8) | b;
              }
            }
            
            if (surf != null)
              surface.add(surf);
          }
        } else if (fs[0].equals("usemtl")) {
          lastmtl = mtlnum.get(fs[1]);
        }
      }
      
      mesh = new Mesh3D();
      mesh.vertex = vertex.toArray(new Vector3D[vertex.size()]);
      mesh.polygon = polygon.toArray(new MeshPolygon[polygon.size()]);
      mesh.surface = surface.toArray(new MeshSurface[surface.size()]);
    } catch (IOException e) {
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
