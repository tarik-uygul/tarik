package bin;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;

import EulerSolver;
import ODEFunction;
import ODESystemBuilder;
import RungeKutta4;

/**
 * SolverExperimentPlot
 *
 * Runs the accuracy + computation time experiment, then displays two
 * log-log plots side by side using JavaFX Canvas:
 *
 *   Left  - Error vs Step Size           (slope reveals solver order)
 *   Right - Computation Time vs Step Size (slope reveals time complexity)
 *
 * HOW TO COMPILE AND RUN (replace path with your JavaFX SDK location):
 *
 *   javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls \
 *         SolverExperimentPlot.java
 *
 *   java  --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls \
 *         SolverExperimentPlot
 *
 * All other project files (EulerSolver, RungeKutta4, ODEFunction,
 * ODESystemBuilder, ExpressionParser) must be on the classpath.
 */
public class SolverExperimentPlot extends Application {

    // ------------------------------------------------------------------
    // Experiment settings
    // ------------------------------------------------------------------
    private static final double   END_TIME   = 20.0;
    private static final double[] STEP_SIZES = {
        1.0, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005, 0.002, 0.001
    };

    // ------------------------------------------------------------------
    // Canvas / plot geometry
    // ------------------------------------------------------------------
    private static final int CANVAS_W = 560;
    private static final int CANVAS_H = 460;
    private static final int MARGIN_L = 80;   // space for y-axis labels
    private static final int MARGIN_R = 30;
    private static final int MARGIN_T = 50;   // space for title
    private static final int MARGIN_B = 65;   // space for x-axis label
    private static final int PLOT_W   = CANVAS_W - MARGIN_L - MARGIN_R;
    private static final int PLOT_H   = CANVAS_H - MARGIN_T - MARGIN_B;

    // ------------------------------------------------------------------
    // Colour palette
    // ------------------------------------------------------------------
    private static final Color C_EULER = Color.web("#e74c3c");  // red
    private static final Color C_RK4   = Color.web("#2980b9");  // blue
    private static final Color C_GRID  = Color.web("#dde3ea");
    private static final Color C_AXIS  = Color.web("#2c3e50");
    private static final Color C_BG    = Color.web("#f8f9fa");
    private static final Color C_SLOPE = Color.web("#95a5a6");  // grey guide

    // ------------------------------------------------------------------
    // JavaFX entry point
    // ------------------------------------------------------------------
    @Override
    public void start(Stage stage) {

        // 1. Run the experiment and collect results
        double[] errEuler  = new double[STEP_SIZES.length];
        double[] errRK4    = new double[STEP_SIZES.length];
        double[] timeEuler = new double[STEP_SIZES.length];
        double[] timeRK4   = new double[STEP_SIZES.length];
        runExperiment(errEuler, errRK4, timeEuler, timeRK4);

        // 2. Draw the two plots
        Canvas accuracyCanvas = new Canvas(CANVAS_W, CANVAS_H);
        Canvas timeCanvas     = new Canvas(CANVAS_W, CANVAS_H);

        drawPlot(accuracyCanvas.getGraphicsContext2D(),
                STEP_SIZES, errEuler, errRK4,
                "Accuracy vs Step Size",
                "Step size  h",
                "Error  ||numerical − exact||",
                true);

        drawPlot(timeCanvas.getGraphicsContext2D(),
                STEP_SIZES, timeEuler, timeRK4,
                "Computation Time vs Step Size",
                "Step size  h",
                "Time  (nanoseconds)",
                false);

        // 3. Layout
        Label heading = new Label(
            "ODE Solver Log-Log Analysis  —  Harmonic Oscillator  x\u2033 = \u2212x");
        heading.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        heading.setTextFill(C_AXIS);
        heading.setPadding(new Insets(16, 0, 4, 0));

        HBox plots = new HBox(16, accuracyCanvas, timeCanvas);
        plots.setAlignment(Pos.CENTER);
        plots.setPadding(new Insets(0, 20, 20, 20));

        VBox root = new VBox(6, heading, plots);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #ecf0f1;");

        stage.setScene(new Scene(root));
        stage.setTitle("Solver Comparison — Log-Log Plots");
        stage.setResizable(false);
        stage.show();
    }

    // ------------------------------------------------------------------
    // Experiment logic
    // ------------------------------------------------------------------
    private void runExperiment(double[] errE, double[] errR,
                               double[] timeE, double[] timeR) {
        // Harmonic oscillator:  x' = v,  v' = -x
        // Exact solution at t:  x(t) = cos(t),  v(t) = -sin(t)
        ODESystemBuilder ode = new ODESystemBuilder(
            new String[]{"v", "-1*x"},
            new String[]{"x", "v"},
            new HashMap<>()
        );
        double[] init = {1.0, 0.0};

        for (int i = 0; i < STEP_SIZES.length; i++) {
            double h = STEP_SIZES[i];

            long t0 = System.nanoTime();
            double[] resE = integrate("euler", init.clone(), h, ode);
            timeE[i] = System.nanoTime() - t0;
            errE[i]  = error(resE, exact(END_TIME));

            t0 = System.nanoTime();
            double[] resR = integrate("rk4", init.clone(), h, ode);
            timeR[i] = System.nanoTime() - t0;
            errR[i]  = error(resR, exact(END_TIME));
        }
    }

    private double[] integrate(String solver, double[] state,
                               double h, ODEFunction ode) {
        double t = 0.0;
        while (t + h <= END_TIME + 1e-12) {
            state = solver.equals("euler")
                    ? EulerSolver.step(state, h, ode)
                    : RungeKutta4.step(state, h, ode);
            t += h;
        }
        return state;
    }

    private double[] exact(double t) {
        return new double[]{Math.cos(t), -Math.sin(t)};
    }

    private double error(double[] num, double[] ex) {
        double s = 0;
        for (int i = 0; i < num.length; i++) { double d = num[i] - ex[i]; s += d * d; }
        return Math.sqrt(s);
    }

    // ------------------------------------------------------------------
    // Plot drawing
    // ------------------------------------------------------------------

    /**
     * Draws a complete log-log plot onto the given GraphicsContext.
     *
     * @param gc             JavaFX canvas graphics context
     * @param xVals          x data (step sizes) - same for both series
     * @param yEuler         y data for Euler
     * @param yRK4           y data for RK4
     * @param title          plot title
     * @param xLabel         x axis label
     * @param yLabel         y axis label
     * @param slopeGuides    if true, draw reference slope lines
     */
    private void drawPlot(GraphicsContext gc,
                          double[] xVals, double[] yEuler, double[] yRK4,
                          String title, String xLabel, String yLabel,
                          boolean slopeGuides) {

        // Determine axis ranges in log10 space
        double logXMin = Math.floor(Math.log10(min(xVals)));
        double logXMax = Math.ceil( Math.log10(max(xVals)));

        double[] allY = combined(yEuler, yRK4);
        double logYMin = Math.floor(Math.log10(min(allY))) - 0.3;
        double logYMax = Math.ceil( Math.log10(max(allY))) + 0.3;

        // Canvas background
        gc.setFill(C_BG);
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        // Plot area background
        gc.setFill(Color.WHITE);
        gc.fillRect(MARGIN_L, MARGIN_T, PLOT_W, PLOT_H);

        // Grid + tick labels
        drawGrid(gc, logXMin, logXMax, logYMin, logYMax);

        // Optional slope guide lines
        if (slopeGuides) {
            // Euler: slope = 1 (first-order)
            drawSlopeGuide(gc, logXMin, logXMax, logYMin, logYMax,
                           1.0, "slope 1  (Euler, O(h\u00B9))", 0.30, 0.70);
            // RK4:   slope = 4 (fourth-order)
            drawSlopeGuide(gc, logXMin, logXMax, logYMin, logYMax,
                           4.0, "slope 4  (RK4, O(h\u2074))",   0.68, 0.35);
        }

        // Clip subsequent drawing to the plot area so lines don't overflow
        gc.save();
        gc.beginPath();
        gc.rect(MARGIN_L, MARGIN_T, PLOT_W, PLOT_H);
        gc.clip();

        drawSeries(gc, xVals, yEuler, logXMin, logXMax, logYMin, logYMax, C_EULER);
        drawSeries(gc, xVals, yRK4,   logXMin, logXMax, logYMin, logYMax, C_RK4);

        gc.restore();  // end clip

        // Plot border
        gc.setStroke(C_AXIS);
        gc.setLineWidth(1.5);
        gc.strokeRect(MARGIN_L, MARGIN_T, PLOT_W, PLOT_H);

        // Title
        gc.setFill(C_AXIS);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        gc.fillText(title, MARGIN_L + 8, MARGIN_T - 14);

        // X axis label
        gc.setFont(Font.font("Georgia", FontWeight.NORMAL, 12));
        double xLabelX = MARGIN_L + PLOT_W / 2.0 - xLabel.length() * 3.0;
        gc.fillText(xLabel, xLabelX, CANVAS_H - 10);

        // Y axis label (rotated 90°)
        gc.save();
        gc.translate(12, MARGIN_T + PLOT_H / 2.0 + yLabel.length() * 3.0);
        gc.rotate(-90);
        gc.fillText(yLabel, 0, 0);
        gc.restore();

        // Legend
        drawLegend(gc);
    }

    // ------------------------------------------------------------------
    // Draw one series: line connecting points + filled circles at each point
    // ------------------------------------------------------------------
    private void drawSeries(GraphicsContext gc,
                            double[] xVals, double[] yVals,
                            double logXMin, double logXMax,
                            double logYMin, double logYMax,
                            Color color) {
        int n = xVals.length;
        double[] px = new double[n];
        double[] py = new double[n];
        for (int i = 0; i < n; i++) {
            px[i] = pixX(Math.log10(xVals[i]), logXMin, logXMax);
            py[i] = pixY(Math.log10(yVals[i]), logYMin, logYMax);
        }

        gc.setStroke(color);
        gc.setLineWidth(2.2);
        gc.beginPath();
        gc.moveTo(px[0], py[0]);
        for (int i = 1; i < n; i++) gc.lineTo(px[i], py[i]);
        gc.stroke();

        gc.setFill(color);
        double r = 5.0;
        for (int i = 0; i < n; i++) gc.fillOval(px[i] - r, py[i] - r, r * 2, r * 2);
    }

    // ------------------------------------------------------------------
    // Grid lines and tick labels at every integer power of 10
    // ------------------------------------------------------------------
    private void drawGrid(GraphicsContext gc,
                          double logXMin, double logXMax,
                          double logYMin, double logYMax) {

        gc.setFont(Font.font("Courier New", 11));

        // Vertical lines (x-axis ticks)
        for (int lx = (int) Math.ceil(logXMin); lx <= (int) Math.floor(logXMax); lx++) {
            double px = pixX(lx, logXMin, logXMax);
            gc.setStroke(C_GRID);
            gc.setLineWidth(1.0);
            gc.strokeLine(px, MARGIN_T, px, MARGIN_T + PLOT_H);

            gc.setFill(C_AXIS);
            String lbl = (lx == 0) ? "1" : ("10" + sup(lx));
            gc.fillText(lbl, px - 10, MARGIN_T + PLOT_H + 18);
        }

        // Horizontal lines (y-axis ticks)
        for (int ly = (int) Math.ceil(logYMin); ly <= (int) Math.floor(logYMax); ly++) {
            double py = pixY(ly, logYMin, logYMax);
            gc.setStroke(C_GRID);
            gc.setLineWidth(1.0);
            gc.strokeLine(MARGIN_L, py, MARGIN_L + PLOT_W, py);

            gc.setFill(C_AXIS);
            String lbl = "10" + sup(ly);
            gc.fillText(lbl, MARGIN_L - 40, py + 4);
        }
    }

    // ------------------------------------------------------------------
    // Dashed reference line showing a given slope
    // xFrac, yFrac: where to anchor the line (0–1 fraction of the plot area)
    // ------------------------------------------------------------------
    private void drawSlopeGuide(GraphicsContext gc,
                                double logXMin, double logXMax,
                                double logYMin, double logYMax,
                                double slope, String label,
                                double xFrac, double yFrac) {

        double lx0 = logXMin + xFrac * (logXMax - logXMin);
        double ly0 = logYMin + yFrac * (logYMax - logYMin);

        double half = 0.55;  // half-length in log-x units
        double lx1 = lx0 - half,  ly1 = ly0 + slope * half;
        double lx2 = lx0 + half,  ly2 = ly0 - slope * half;

        gc.save();
        gc.beginPath();
        gc.rect(MARGIN_L, MARGIN_T, PLOT_W, PLOT_H);
        gc.clip();

        gc.setStroke(C_SLOPE);
        gc.setLineWidth(1.4);
        gc.setLineDashes(6, 4);
        gc.strokeLine(pixX(lx1, logXMin, logXMax), pixY(ly1, logYMin, logYMax),
                      pixX(lx2, logXMin, logXMax), pixY(ly2, logYMin, logYMax));
        gc.setLineDashes();
        gc.restore();

        // Label just to the right of the line midpoint
        gc.setFill(C_SLOPE);
        gc.setFont(Font.font("Georgia", 10));
        gc.fillText(label,
                    pixX(lx0, logXMin, logXMax) + 6,
                    pixY(ly0, logYMin, logYMax) - 4);
    }

    // ------------------------------------------------------------------
    // Legend box (top-right corner of the plot area)
    // ------------------------------------------------------------------
    private void drawLegend(GraphicsContext gc) {
        double lx = MARGIN_L + PLOT_W - 102;
        double ly = MARGIN_T + 16;

        gc.setFill(Color.rgb(255, 255, 255, 0.88));
        gc.fillRoundRect(lx - 8, ly - 14, 96, 52, 6, 6);
        gc.setStroke(C_GRID);
        gc.setLineWidth(1);
        gc.strokeRoundRect(lx - 8, ly - 14, 96, 52, 6, 6);

        // Euler row
        gc.setStroke(C_EULER); gc.setLineWidth(2.2);
        gc.strokeLine(lx, ly, lx + 20, ly);
        gc.setFill(C_EULER);
        gc.fillOval(lx + 7, ly - 4, 8, 8);
        gc.setFill(C_AXIS);
        gc.setFont(Font.font("Georgia", 12));
        gc.fillText("Euler", lx + 26, ly + 4);

        // RK4 row
        ly += 22;
        gc.setStroke(C_RK4); gc.setLineWidth(2.2);
        gc.strokeLine(lx, ly, lx + 20, ly);
        gc.setFill(C_RK4);
        gc.fillOval(lx + 7, ly - 4, 8, 8);
        gc.setFill(C_AXIS);
        gc.fillText("RK4", lx + 26, ly + 4);
    }

    // ------------------------------------------------------------------
    // Coordinate helpers:  log-space value  →  canvas pixel
    // ------------------------------------------------------------------
    private double pixX(double logX, double logXMin, double logXMax) {
        return MARGIN_L + (logX - logXMin) / (logXMax - logXMin) * PLOT_W;
    }

    private double pixY(double logY, double logYMin, double logYMax) {
        // screen y increases downward, so we flip
        return MARGIN_T + PLOT_H - (logY - logYMin) / (logYMax - logYMin) * PLOT_H;
    }

    // ------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------
    private double min(double[] a) {
        double m = Double.MAX_VALUE;
        for (double v : a) if (v > 0 && v < m) m = v;
        return m;
    }
    private double max(double[] a) {
        double m = -Double.MAX_VALUE;
        for (double v : a) if (v > m) m = v;
        return m;
    }
    private double[] combined(double[] a, double[] b) {
        double[] c = new double[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /** Integer → Unicode superscript, e.g.  -3 → "⁻³" */
    private String sup(int n) {
        String[] s = {"⁰","¹","²","³","⁴","⁵","⁶","⁷","⁸","⁹"};
        StringBuilder sb = new StringBuilder();
        if (n < 0) { sb.append("⁻"); n = -n; }
        for (char c : Integer.toString(n).toCharArray()) sb.append(s[c - '0']);
        return sb.toString();
    }

    public static void main(String[] args) { launch(args); }
}