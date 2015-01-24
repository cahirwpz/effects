public class MeshVertex {
  int index;
  int normalIndex;
  int uvIndex;
  
  MeshVertex(int index) {
    this.index = index;
    this.normalIndex = -1;
    this.uvIndex = -1;
  }
}