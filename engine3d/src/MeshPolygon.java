public class MeshPolygon {
  int[] vertexIndex;
  int surfaceIndex;
  
  MeshPolygon(int vertices) {
    this.vertexIndex = new int[vertices];
    this.surfaceIndex = 0;
  }
  
  int size() { return vertexIndex.length; }

  Polygon toPolygon(Vector3D[] vertex, MeshSurface[] surface) {
    Vector3D[] point = new Vector3D[size() + 1];
    int i;
  
    for (i = 0; i < size(); i++)
      point[i] = vertex[vertexIndex[i]];
    point[i] = point[0];
    
    return new Polygon(point, surface[surfaceIndex].color);
  }
};
