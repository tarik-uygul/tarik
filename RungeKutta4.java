public class RungeKutta4 {

    public static double[] step(double[] x, double h, ODEFunction f) {
        double[] k1 = f.compute(x);
        double[] k2 = f.compute(add(x, k1, h / 2));
        double[] k3 = f.compute(add(x, k2, h / 2));
        double[] k4 = f.compute(add(x, k3, h));

        double[] next = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            next[i] = x[i] + (h / 6) * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]);
        }

        return next;
    }

    private static double[] add(double[] a, double[] b, double factor) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + factor * b[i];
        }
        return result;
    }
}