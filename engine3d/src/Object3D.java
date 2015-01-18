public class Object3D {
  Mesh3D mesh;
  Matrix3D world;
  Vector3D[] vertex;
  int[] clipFlags;
  
  Object3D(Mesh3D mesh) {
    this.mesh = mesh;
    this.world = new Matrix3D();
    this.vertex = new Vector3D[mesh.vertex.length];
    this.clipFlags = new int[mesh.vertex.length];
  }

  void refreshClipFlags() {
    for (int i = 0; i < vertex.length; i++) {
      float x = vertex[i].x, y = vertex[i].y, z = vertex[i].z;
      
      clipFlags[i] = 0;
      
      if (x < -1.0f)
        clipFlags[i] |= Polygon.PF_LEFT;
      if (x >= 1.0f)
        clipFlags[i] |= Polygon.PF_RIGHT;
      if (y < -1.0f)
        clipFlags[i] |= Polygon.PF_TOP;
      if (y >= 1.0f)
        clipFlags[i] |= Polygon.PF_BOTTOM;
      if (z < -1.0f)
        clipFlags[i] |= Polygon.PF_NEAR;
      if (z >= 1.0f)
        clipFlags[i] |= Polygon.PF_FAR;
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
      Vector3D v = t.transform(mesh.vertex[i]);
      v.scale(1.0f / v.w);
      vertex[i] = v;
    }
  }
}