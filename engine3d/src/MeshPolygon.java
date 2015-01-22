public class MeshPolygon {
  int[] vertexIndex;
  int[] vertexNormalIndex;
  int[] uvIndex;
  int materialIndex;
  int normalIndex;
  
  MeshPolygon() {
    this.materialIndex = -1;
    this.normalIndex = -1;
  }
  
  Polygon toPolygon(Vector3D[] vertex, MeshMaterial[] surface) {
    Vector3D[] point = new Vector3D[vertexIndex.length + 1];
    int i;
  
    for (i = 0; i < vertexIndex.length; i++)
      point[i] = vertex[vertexIndex[i]];
    point[i] = point[0];
    
    return new Polygon(point, surface[materialIndex].color.toInteger());
  }
};
