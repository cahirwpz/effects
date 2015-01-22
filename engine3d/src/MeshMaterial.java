public class MeshMaterial {
  String name;
  Color color;
  float transparency;
  
  String texturemap;
  int texturemapId;
  
  MeshMaterial(String name) {
    this.name = name;
    this.color = new Color(1.0f, 1.0f, 1.0f);
    this.transparency = 0.0f;
    this.texturemap = null;
    this.texturemapId = -1;
  }

  void load(ResourceManager man) {
    if (texturemap != null)
      texturemapId = man.addImage(texturemap);
  }
};
