final int N = 64;
MyShape[] MyShapes;

void setup() {
  size(512, 512, P3D);

  MyShapes = new MyShape[N];

  for (int i = 0; i < N; i++) {
    MyShapes[i] = new MyShape();
    MyShapes[i].make();
  }
}

void draw() {
  background(0);
  
  for (int i = 0; i < N; i++) {
    MyShape a = MyShapes[i];
    a.display();
    a.move();
  }
}

