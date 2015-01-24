import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Mesh3D {
  Vector3D[] vertex;
  Vector3D[] normal;
  UVCoord[] uv;
  
  MeshPolygon[] polygon;
  MeshMaterial[] material;
  
  Mesh3D() {}  
  
  Mesh3D(int vertices, int polygons, int surfaces) {
    this.vertex = new Vector3D[vertices];
    this.normal = new Vector3D[vertices];
    this.polygon = new MeshPolygon[polygons];
    this.material = new MeshMaterial[surfaces];
  }

  Mesh3D copy() {
    Mesh3D mesh = new Mesh3D();
    mesh.vertex = vertex;
    mesh.polygon = polygon;
    mesh.material = material;
    mesh.normal = normal;
    return mesh;
  }

  static Mesh3D readFromOBJ(String path) {
    List<Vector3D> vertex = new ArrayList<>();
    List<Vector3D> normal = new ArrayList<>();
    List<UVCoord> uv = new ArrayList<>();
    List<MeshPolygon> polygon = new ArrayList<>();
    List<MeshMaterial> material = new ArrayList<>();
    List<String> mtllib = new ArrayList<>();
    HashMap<String, Integer> mtlnum = new HashMap<>();
    Mesh3D mesh = null;
    int lastmtl = 0;
    
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
        } else if (fs[0].equals("vn")) {
          float x = Float.parseFloat(fs[1]);
          float y = Float.parseFloat(fs[2]);
          float z = Float.parseFloat(fs[3]);
          normal.add(new Vector3D(x, y, z));
        } else if (fs[0].equals("vt")) {
          float u = Float.parseFloat(fs[1]); // horizontal
          float v = 1.0f - Float.parseFloat(fs[2]); // vertical
          uv.add(new UVCoord(u, v));
        } else if (fs[0].equals("f")) {
          int n = fs.length - 1;
          MeshPolygon p = new MeshPolygon(n);
          int normalIndex = -1;
          boolean sameNormal = true;
          
          for (int i = 0; i < n; i++) {
            String[] vs = fs[i+1].split("/");
            MeshVertex mv = new MeshVertex(Integer.parseInt(vs[0]) - 1);
            if (vs.length > 1 && !vs[1].isEmpty())
              mv.uvIndex = Integer.parseInt(vs[1]) - 1;
            if (vs.length > 2) {
              mv.normalIndex = Integer.parseInt(vs[2]) - 1;
              if (normalIndex < 0) {
                normalIndex = mv.normalIndex;
              } if (sameNormal && normalIndex != mv.normalIndex) {
                sameNormal = false;
              }
            }
            p.vertex[i] = mv;
          }
          /* if all normal index are the same, then polygon normal is encoded */
          if (sameNormal)
            for (MeshVertex mv : p.vertex)
              mv.normalIndex = -1;
          p.normalIndex = normalIndex;
          p.materialIndex = lastmtl;
          polygon.add(p);
        } else if (fs[0].equals("mtllib")) {
          if (!mtllib.contains(fs[1])) {
            List<String> mtllines = Files.readAllLines(
                Paths.get(fs[1]), Charset.defaultCharset());
            MeshMaterial mtl = null;
            for (String ml : mtllines) {
              String[] mfs = ml.trim().split("\\s+");
              
              if (mfs.length == 0)
                continue;
              
              if (mfs[0].equals("newmtl")) {
                if (mtl != null)
                  material.add(mtl);
                mtl = new MeshMaterial(mfs[1]);
                mtlnum.put(mfs[1], material.size());
              } else if (mfs[0].equals("map_Kd")) {
                mtl.texturemap = mfs[1];
              } else if (mfs[0].equals("Kd")) {
                mtl.color = new Color(Float.parseFloat(mfs[1]),
                                       Float.parseFloat(mfs[2]),
                                       Float.parseFloat(mfs[3]));
              } else if (mfs[0].equals("d") || mfs[0].equals("Tr")) {
                mtl.transparency = Float.parseFloat(mfs[1]);
              }
            }
            
            if (mtl != null)
              material.add(mtl);
          }
        } else if (fs[0].equals("usemtl")) {
          lastmtl = mtlnum.get(fs[1]);
        }
      }
      
      mesh = new Mesh3D();
      mesh.vertex = vertex.toArray(new Vector3D[vertex.size()]);
      mesh.normal = normal.toArray(new Vector3D[normal.size()]);
      mesh.uv = uv.toArray(new UVCoord[uv.size()]);    
      mesh.polygon = polygon.toArray(new MeshPolygon[polygon.size()]);
      mesh.material = material.toArray(new MeshMaterial[material.size()]);
    } catch (IOException e) {
    }

    return mesh;
  }
  
  static Mesh3D readFromLWO(String path) {
    IffFile iff = IffFile.read(path);
    
    if (!iff.isType("LWO2"))
      return null;
    
    Mesh3D mesh = new Mesh3D();
    
    ArrayList<MeshMaterial> srfs = new ArrayList<>();
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
            int n = chunk.getShort();
            MeshPolygon mp = new MeshPolygon(n);
            for (int i = 0; i < n; i++)
              mp.vertex[i] = new MeshVertex(chunk.getShort());
            pols.add(mp);
          }
          mesh.polygon = pols.toArray(new MeshPolygon[pols.size()]);
        }
      } else if (chunk.isType("PTAG")) {
        if (chunk.getId().equals("SURF")) {
          while (chunk.hasRemaining()) {
            int poly = chunk.getShort();
            int surf = chunk.getShort() - 1;
            mesh.polygon[poly].materialIndex = surf;
          }
        }
      } else if (chunk.isType("SURF")) {
        String name = chunk.getString();
        chunk.getString(); /* skip source */
        MeshMaterial surf = new MeshMaterial(name);
        for (IffFile.Chunk minick : chunk.parseMiniChunks()) {
          if (minick.isType("COLR")) {
            Vector3D c = minick.getVector3D();
            surf.color = new Color(c.x, c.y, c.z);
          } else if (minick.isType("TRAN")) {
            surf.transparency = minick.getFloat();
          } else if (minick.isType("SPEC") ||
                     minick.isType("DIFF") ||
                     minick.isType("VERS") ||
                     minick.isType("NODS"))
          {
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
    
    mesh.material = srfs.toArray(new MeshMaterial[srfs.size()]);
    
    return mesh;
  }
  
  void load(ResourceManager man) {
    for (MeshMaterial m : material)
      m.load(man);
  }
};
