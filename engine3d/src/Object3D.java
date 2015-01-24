import processing.core.PImage;

public class Object3D {
  Mesh3D mesh;
  Matrix3D world;
  Polygon[] polygon;
  Vector3D[] vertex;
  Vector3D[] normal;
  int[] clipFlags;
  
  Object3D(ResourceManager man, int meshId) {
    mesh = man.getMesh(meshId);
    world = new Matrix3D();
    polygon = new Polygon[mesh.polygon.length];
    vertex = new Vector3D[mesh.vertex.length];
    clipFlags = new int[mesh.vertex.length];
    
    for (int i = 0; i < mesh.vertex.length; i++)
      vertex[i] = new Vector3D();
    
    if (mesh.normal != null) {
      normal = new Vector3D[mesh.normal.length];
      for (int i = 0; i < mesh.normal.length; i++)
        normal[i] = mesh.normal[i];
    }
 
    for (int i = 0; i < mesh.polygon.length; i++) {
      MeshPolygon mp = mesh.polygon[i];
      MeshMaterial mm = mesh.material[mp.materialIndex];
      PImage texture = (mm.texturemapId < 0) ? null : man.getImage(mm.texturemapId);
      Vertex[] pv = new Vertex[mp.vertex.length + 1];
      int j;
    
      for (j = 0; j < mp.vertex.length; j++) {
        MeshVertex mv = mp.vertex[j];
        Vector3D p = vertex[mv.index];
        Vector3D n = (mv.normalIndex < 0) ? null : normal[mv.normalIndex];
        UVCoord uv = (mv.uvIndex < 0) ? null : mesh.uv[mv.uvIndex];
        if (texture != null) {
          uv.u *= texture.width;
          uv.v *= texture.height;
        }
        pv[j] = new Vertex(mv.index, p, n, uv);
      }
      pv[j] = pv[0];
      
      polygon[i] = new Polygon(pv, mm.color.toInteger());
      polygon[i].texture = texture;
    }
  }
  
  void reset() {
    world.reset();
  }
  
  void scale(float scaleX, float scaleY, float scaleZ) {
    world = Matrix3D.mult(world, Matrix3D.scaling(scaleX, scaleY, scaleZ));
  }
  
  void rotate(float angleX, float angleY, float angleZ) {
    world = Matrix3D.mult(world, Matrix3D.rotation(angleX, angleY, angleZ));
  }

  void translate(float x, float y, float z) {
    world = Matrix3D.mult(world, Matrix3D.translation(x, y, z));
  }
  
  void transform(Matrix3D view, Matrix3D projection) {
    Matrix3D t = Matrix3D.mult(Matrix3D.mult(world, view), projection);

    for (int i = 0; i < vertex.length; i++) {
      t.transform(vertex[i], mesh.vertex[i]);
      vertex[i].scale(1.0f / vertex[i].w);
      clipFlags[i] = Frustum.clipFlags(vertex[i]);
    }
  }
}