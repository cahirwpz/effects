public class MeshPolygon {
  MeshVertex[] vertex;
  int materialIndex;
  int normalIndex;
  
  MeshPolygon(int vertices) {
    this.vertex = new MeshVertex[vertices];
    this.materialIndex = -1;
    this.normalIndex = -1;
  }
};