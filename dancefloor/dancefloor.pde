int N = 1024;
int M = 1024;
int ROWS = 16;
int COLS = 16;
int near_z = 100;
int far_z = 300;
int far_y;

class Line {
  int xs, ys;
  int xe, ye;
  
  Line(int _xs, int _ys, int _xe, int _ye) {
    xs = _xs; ys = _ys; xe = _xe; ye = _ye;
  }
};

int horiz[];
Line vert[];

void setup() {
  frameRate(25);
  size(640, 480);  

  far_y = height * near_z / far_z;
  int far_w = width * far_z / 256; 

  vert = new Line[N];
  horiz = new int[M];

  for (int i = 0; i < N; i++) {
    int x = int(far_w * lerp(-0.5, 0.5, float(i) / N));
    int far_x = width / 2 + x * 256 / far_z;
    int near_x = width / 2 + x * 256 / near_z;
    int near_y = height;
    
    if (near_x < 0) {
      near_y = far_y + (height - far_y) * (0 - far_x) / (near_x - far_x);
      near_x = 0;
    }
    if (near_x >= width) {
      near_y = far_y + (height - far_y) * (width - far_x - 1) / (near_x - far_x);
      near_x = width - 1;
    }
    
    vert[i] = new Line(far_x, far_y, near_x, near_y);
  }

  for (int i = 0; i < M; i++) {
    int z = int(lerp(far_z, near_z, float(i) / M));
    
    horiz[i] = height * near_z / z;
  }
  
  initOCS(1);

  palette[0] = #000000;
  palette[1] = #ffffff;
}

void draw() {
  float t = frameCount / 25.0;
  
  for (int i = 0; i < far_y; i++)
    copper(0, i, 0, lerpColor(#0080ff, #80ffff, float(i) / far_y));
  copper(0, far_y, 0, 0);

  bpl[0].clear();

  /* Render columns */
  int xo = int((1.0 + sin(t)) * N / 4);
  Line l0 = new Line(0, 0, width - 1, far_y);

  for (int k = COLS - 1; k >= 0; k--) {
    int xi = xo % (N / COLS) + k * (N / COLS);
    int col = xo * COLS / N + k;
    Line l1 = vert[xi & (N - 1)];
    
    bpl[0].lineE(l1.xs, l1.ys, l1.xe, l1.ye);

    if ((col % 2 == 1) && (l0.xe == width - 1))
      bpl[0].lineE(width - 1, l0.ye, width - 1, l1.ye);
    
    l0 = l1;
  }

  /* Render rows */
  int yo = int((1.0 + cos(t)) * M / 4);
  int y0 = far_y;
  
  for (int k = 0; k <= 16; k++) {
    int row = yo * 16 / M + k;
    int yi = yo % (M / ROWS) + k * (M / ROWS); 
    int y1 = horiz[(yi < M) ? yi : M - 1];    
    
    if (row % 2 == 1)
      bpl[0].lineE(width - 1, y0, width - 1, y1);

    y0 = y1;
  }  
 
  bpl[0].fill();

  updateOCS();
}
