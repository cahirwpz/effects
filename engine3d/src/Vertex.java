public class Vertex {
  Vector3D pos;
  Vector3D normal;
  UVCoord uv;
  int index;
  
  Vertex(int index, Vector3D pos, Vector3D normal, UVCoord uv) {
    this.index = index;
    this.pos = pos;
    this.normal = normal;
    this.uv = uv;
  }

  Vertex copy() {
    return new Vertex(index, pos, normal, uv);
  }
}