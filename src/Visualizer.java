
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

// this is the main class that actually runs and shows the window
// it does three things:
// 1. draws the input fields so you can set initial conditions (starting position, velocity etc)
// 2. defines the physics as a lambda (the ODEFunction implementation)
// 3. runs the simulation loop and plots the results
//
// this class doesnt actually know anything about HOW euler or rk4 work
// it just calls .step() on them and gets back a new state
// euler and rk4 dont know anything about gravity or golf balls
// all the physics knowledge is in the "system" lambda below

public class Visualizer extends Application {

    @Override
    public void start(Stage primaryStage) {

        // initial position inputs: 
        // these are where the ball starts, in metres
        // x and y are horizontal, z is height (up is positive)
        TextField startX = new TextField("0");
        TextField startY = new TextField("0");
        TextField startZ = new TextField("0");

        // initial velocity inputs:
        // these are how fast the ball is moving at the start, in m/s
        // vz of 10 means its going upward at 10 metres per second initially
        TextField startVX = new TextField("10");
        TextField startVY = new TextField("5");
        TextField startVZ = new TextField("10");

        // how big each time jump is
        // 0.01 means were simulating in 0.01 second intervals
        // making this smaller makes the simulation more accurate but takes longer to run
        // making it bigger is faster but you lose accuracy, especially with euler
        TextField timeStep = new TextField("0.01");

        // how many seconds of flight to simulate total
        TextField simDuration = new TextField("5");

        // dropdown to pick which solver to use
        // both euler and rk4 do the same job but rk4 is more accurate
        // you can compare them by running with the same inputs and switching this
        ComboBox<String> whichSolver = new ComboBox<>();
        whichSolver.getItems().addAll("Euler", "RK4");
        whichSolver.setValue("RK4");

        // these let you pick what to plot on each axis
        // default is time on x axis and height (z) on y axis
        ComboBox<String> plotXAxis = new ComboBox<>();
        ComboBox<String> plotYAxis = new ComboBox<>();

        plotXAxis.getItems().addAll("t", "x", "y", "z", "vx", "vy", "vz");
        plotYAxis.getItems().addAll("x", "y", "z", "vx", "vy", "vz");

        plotXAxis.setValue("t");
        plotYAxis.setValue("z");

        // set up the chart - just a standard line chart with two number axes
        NumberAxis xAxisLabel = new NumberAxis();
        NumberAxis yAxisLabel = new NumberAxis();
        LineChart<Number, Number> theChart = new LineChart<>(xAxisLabel, yAxisLabel);

        // the motion of ball:
        // this lambda is the actual implementation of ODEFunction
        //
        // the state array coming in looks like this:
        //   state[0] = x position
        //   state[1] = y position
        //   state[2] = z position (height)
        //   state[3] = vx (velocity in x direction)
        //   state[4] = vy (velocity in y direction)
        //   state[5] = vz (velocity in z direction, positive = going up)
        //
        // what we return is the derivative of each of those
        // aka how fast each one is changing right now
        //
        // for position: the derivative of position is just velocity, thats literally the definition
        // so derivative of x-position = vx, which is state[3]
        //
        // for velocity: the derivative of velocity is acceleration
        // and acceleration = force / mass 
        // right now the only force is gravity pulling down: -9.81 m/s^2
        // later we could add drag and other forces
        // and we wouldnt need to change the solvers
        ODEFunction golfBallPhysics = (state) -> {
            double[] derivatives = new double[state.length];

            
            derivatives[0] = state[3];   // dx/dt = vx
            derivatives[1] = state[4];   // dy/dt = vy
            derivatives[2] = state[5];   // dz/dt = vz

           
            derivatives[3] = 0;          // dvx/dt = 0 
            derivatives[4] = 0;          // dvy/dt = 0 
            derivatives[5] = -9.81;      // dvz/dt = -g (gravity always pulls down)

            return derivatives;
        };

        Button goButton = new Button("Run Simulation");

        goButton.setOnAction(e -> {

            theChart.getData().clear();

            // pack all the starting values into the state array
            // order matters here - has to match what the lambda expects above
            double[] currentState = {
                Double.parseDouble(startX.getText()),
                Double.parseDouble(startY.getText()),
                Double.parseDouble(startZ.getText()),
                Double.parseDouble(startVX.getText()),
                Double.parseDouble(startVY.getText()),
                Double.parseDouble(startVZ.getText())
            };

            double dt = Double.parseDouble(timeStep.getText());
            double totalTime = Double.parseDouble(simDuration.getText());

            // store every state point so we can plot them all afterwards
            // we need the full history
            List<double[]> allStates = new ArrayList<>();
            List<Double> allTimes = new ArrayList<>();

            double currentTime = 0;

            //main loop: 
            // each iteration of this loop represents one tiny slice of time (dt seconds)
            // we record where things are, then advance the simulation forward by dt
            // repeat until weve simulated the full duration
            //
            // note: we record before stepping, so we capture the initial state too
            // note: we clone() the state when storing it because arrays are passed by reference
            // if we didnt clone, every entry in allStates would point to the same array
            // and theyd all show the final state which would make for a boring graph
            while (currentTime <= totalTime) {

                allStates.add(currentState.clone());  
                allTimes.add(currentTime);

                // ask whichever solver is selected to advance the state by one time step
                // both solvers take exactly the same arguments and return the same thing
                if (whichSolver.getValue().equals("Euler")) {
                    currentState = EulerSolver.step(currentState, dt, golfBallPhysics);
                } else {
                    currentState = RungeKutta4.step(currentState, dt, golfBallPhysics);
                }

                currentTime += dt;
            }

            //plotting: 
            // go through all the snapshots we recorded and pull out the two variables
            // the user wants to see on the chart axes
            // getValue() handles translating "z" into state[2] 
            XYChart.Series<Number, Number> plotLine = new XYChart.Series<>();

            for (int i = 0; i < allStates.size(); i++) {
                double[] snap = allStates.get(i);

                double xVal = extractVariable(plotXAxis.getValue(), snap, allTimes.get(i));
                double yVal = extractVariable(plotYAxis.getValue(), snap, allTimes.get(i));

                plotLine.getData().add(new XYChart.Data<>(xVal, yVal));
            }

            xAxisLabel.setLabel(plotXAxis.getValue());
            yAxisLabel.setLabel(plotYAxis.getValue());
            theChart.getData().add(plotLine);
        });

        // standard javafx layout, just arranging the inputs into a grid above the chart
        GridPane inputGrid = new GridPane();
        inputGrid.addRow(0, new Label("x0"), startX, new Label("y0"), startY, new Label("z0"), startZ);
        inputGrid.addRow(1, new Label("vx0"), startVX, new Label("vy0"), startVY, new Label("vz0"), startVZ);
        inputGrid.addRow(2, new Label("Step size"), timeStep, new Label("Total time"), simDuration);
        inputGrid.addRow(3, new Label("Solver"), whichSolver);
        inputGrid.addRow(4, new Label("X axis"), plotXAxis, new Label("Y axis"), plotYAxis);
        inputGrid.add(goButton, 0, 5);

        VBox mainLayout = new VBox(inputGrid, theChart);
        Scene scene = new Scene(mainLayout, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Golf Ball Simulator");
        primaryStage.show();
    }

    // maps a variable name (like "z" or "vx") to its actual value in the state array
    private double extractVariable(String varName, double[] state, double t) {
        switch (varName) {
            case "t":  return t;
            case "x":  return state[0];
            case "y":  return state[1];
            case "z":  return state[2];
            case "vx": return state[3];
            case "vy": return state[4];
            case "vz": return state[5];
        }
        return 0; //
    }

    public static void main(String[] args) {
        launch(args);
    }
}