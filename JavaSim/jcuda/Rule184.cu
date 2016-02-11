
#define NOW(time, i) (2 * (i) + time)
#define FUTURE(time, i) (2 * (i) + (1 - time))

extern "C"
__global__ void move(int n, int time, int *a) {
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    if ((i > 2) && (2 * i + 1 < n)) {
        char p = a[NOW(time, i - 1)];
        char q = a[NOW(time, i)];
        char r = a[NOW(time, i + 1)];
        a[FUTURE(time, i)] = ((!p && q && r) || (p && !q && !r) || (p && !q && r) || (p && q && r));
    }
}