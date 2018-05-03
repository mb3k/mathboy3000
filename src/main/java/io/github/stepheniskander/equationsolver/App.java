package io.github.stepheniskander.equationsolver;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

/**
 * @author Nick
 */
public class App extends Application {
    private ArrayList<String> inOutList;
    private TextField inField;
    private TextArea outField;
    private HashMap<String,Matrix> matrixMap;

    private void handleInput() {
        try {
            outField.clear();
            String s = inField.getText();
            String[] argus = s.split(" ");
            BigDecimal result;
            if (s.trim().length() != 0) {
                if (argus[0].equalsIgnoreCase("matrix")) {
                    if (argus[1].charAt(0) == '[') { //Checks to see if you just input a matrix without a variable
                        String inputString = s.substring(7, s.length()).trim();
                        Matrix inputMatrix = Parser.parseMatrix(inputString);                                       //I made a HashMap that stores matrices based on their Variable Name
                        inField.setText("");                                                                         //Matrices can be stored as an Upper case letter
                        inField.end();                                                                               //One assigns matrices with "matrix X = [[]]"
                        inOutList.add("-------------\n" + inputMatrix.toString() + "\n-------------\n");            //Then you can recall that matrix from the hashmap by
                    } else if (argus[1].matches("[A-Z]")) {                                                       //typing "matrix X"
                        if (argus.length > 2) { //if there are more than two arguments, then it will either be assigning or doing matrix multiplication
                            if (argus[2].equals("=")) {
                                String inputString = s.substring(10, s.length()).trim(); //The number 10 comes from the fact that assignment is always in the form:
                                Matrix inputMatrix = Parser.parseMatrix(inputString);   //matrix x = .... the 10th position in that string is right after the equals
                                matrixMap.put(argus[1], inputMatrix);
                                inField.setText("");
                                inField.end();
                                inOutList.add("Matrix " + argus[1] + ":\n" + "-------------\n" + inputMatrix.toString() + "\n-------------\n");
                            } else if (argus[2].equalsIgnoreCase("times")) {
                                try {
                                    Matrix a = matrixMap.get(argus[1]);
                                    Matrix b = matrixMap.get(argus[3]);
                                    Matrix c = Matrix.matrixMultiply(a, b);
                                    inField.setText("");
                                    inField.end();
                                    inOutList.add("-------------\n" + c.toString() + "\n-------------\n");
                                } catch (Exception e) {
                                    inField.setText("Error loading matrices");
                                }
                            }

                        } else { //If there are 2 arguments, then it will be just recalling the matrix from the hash map
                            Matrix curr = matrixMap.get(argus[1]);
                            inOutList.add("Matrix: " + argus[1] + ":\n" + "-------------\n" + curr.toString() + "\n-------------\n");
                            inField.setText("");
                            inField.end();
                        }
                    }
                } else if (argus[0].startsWith("integrate")) {
                    Pattern integratePattern = Pattern.compile("integrate\\((.+), *(.+), *(.+) *\\)");
                    Matcher integrateMatcher = integratePattern.matcher(s);
                    integrateMatcher.matches();
                    String expression = integrateMatcher.group(1);
                    int start = Integer.parseInt(integrateMatcher.group(2));
                    int end = Integer.parseInt(integrateMatcher.group(3));
                    BigDecimal answer = Calculus.integrate(expression, start, end);
                    inField.setText("");
                    inField.end();
                    inOutList.add("Integral of " + expression + " from " + start + " to " + end + ":\n      " + answer);
                } else if (argus[0].startsWith("derive")) {
                    Pattern derivePattern = Pattern.compile("derive\\((.+), *(.+) *\\)");
                    Matcher deriveMatcher = derivePattern.matcher(s);
                    deriveMatcher.matches();
                    String expression = deriveMatcher.group(1);
                    int point = Integer.parseInt(deriveMatcher.group(2));
                    BigDecimal answer = Calculus.derive(expression, point);
                    inField.setText("");
                    inField.end();
                    inOutList.add("Derivative of " + expression + " at " + point + ":\n      " + answer);
                } else {
                    Expression ex = Parser.parseExpression(s);
                    result = ex.evaluateRpn();
                    inField.setText(String.valueOf(result));
                    inField.end();
                    inOutList.add(s + ":"); //All inputs and outputs will be added to the list in the order they were entered and shown to the user in the output field
                    inOutList.add("             " + String.valueOf(result));
                }
            } else {
                inField.setText("");
            }
            for (String item : inOutList) {
                outField.appendText(item + "\n");
            }
        }
        catch(ArithmeticException ae){
            inField.setText("Arithmetic exception: i.e. divide by 0");
        }

    }
    
    @Override
    public void start(Stage primaryStage) {
        inOutList = new ArrayList<>(); //Contains inputs and outputs as strings to be shown to the user
        inField = new TextField();
        outField = new TextArea();
        outField.setEditable(false);
        inField.setPromptText("Please enter your expression");
        Button btn = new Button();
        btn.setText("Enter");

        matrixMap = new HashMap<>();
        btn.setOnAction(event -> handleInput());

        inField.setOnKeyPressed((KeyEvent ke) -> {
            if (ke.getCode() == KeyCode.ENTER) {
                handleInput();
            }
        });

        //These are the alignments to this pane that I have been experimenting with

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        VBox.setVgrow(outField, Priority.ALWAYS);
        HBox inputBox = new HBox();
        inputBox.setSpacing(5);
        HBox.setHgrow(inField, Priority.ALWAYS);
        inputBox.getChildren().addAll(inField, btn);
        root.getChildren().addAll(outField, inputBox);
        Scene scene = new Scene(root, 800, 500);

        inField.requestFocus();
        primaryStage.setTitle("MathBoy3000");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);

    }

}
