package io.github.stepheniskander.equationsolver;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.geometry.Pos;

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
        // inField.setPromptText("Please enter your expression");
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
        VBox uiBox = new VBox();
        uiBox.setSpacing(10);
        uiBox.setPadding(new Insets(10, 10, 10, 10));
        VBox.setVgrow(uiBox, Priority.ALWAYS);
        VBox.setVgrow(outField, Priority.ALWAYS);
        HBox inputBox = new HBox();
        inputBox.setSpacing(5);
        HBox.setHgrow(inField, Priority.ALWAYS);
        inputBox.getChildren().addAll(inField /*, btn*/);
        uiBox.getChildren().addAll(outField, inputBox);

        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("File");
        Menu view = new Menu("View");
        view.getItems().add(new CheckMenuItem("Toggle Calculator Buttons"));
        Menu help = new Menu("Help");
        menuBar.getMenus().addAll(file, view, help);
        root.getChildren().addAll(menuBar, uiBox);

        GridPane buttonPane = generateButtonPane();
        uiBox.getChildren().add(buttonPane);
        Scene scene = new Scene(root, 400, 600);

        inField.requestFocus();
        primaryStage.setTitle("MathBoy3000");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane generateButtonPane() {
        GridPane buttonPane = new GridPane();

        CalculatorButton btn7 = new CalculatorButton("7");
        CalculatorButton btn8 = new CalculatorButton("8");
        CalculatorButton btn9 = new CalculatorButton("9");
        CalculatorButton btnDiv = new CalculatorButton("÷");
        CalculatorButton btnDel = new CalculatorButton("↵");
        CalculatorButton btnClr = new CalculatorButton("⌫");
        CalculatorButton btn4 = new CalculatorButton("4");
        CalculatorButton btn5 = new CalculatorButton("5");
        CalculatorButton btn6 = new CalculatorButton("6");
        CalculatorButton btnMul = new CalculatorButton("×");
        CalculatorButton btnLPar = new CalculatorButton("(");
        CalculatorButton btnRPar = new CalculatorButton(")");
        CalculatorButton btn1 = new CalculatorButton("1");
        CalculatorButton btn2 = new CalculatorButton("2");
        CalculatorButton btn3 = new CalculatorButton("3");
        CalculatorButton btnMin = new CalculatorButton("-");
        CalculatorButton btnExp = new CalculatorButton("xⁿ");
        CalculatorButton btn0 = new CalculatorButton("0");
        CalculatorButton btnDot = new CalculatorButton(".");
        CalculatorButton btnPlus = new CalculatorButton("+");
        CalculatorButton btnEq = new CalculatorButton("=");
        btnEq.setStyle(btnEq.getStyle() + "-fx-background-color: #2196F3;");

        EventHandler<ActionEvent> addLabelToInput = new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                inField.appendText(((Button) event.getSource()).getText());
            }
        };

        btn7.setOnAction(addLabelToInput);
        btn8.setOnAction(addLabelToInput);
        btn9.setOnAction(addLabelToInput);
        btn4.setOnAction(addLabelToInput);
        btn5.setOnAction(addLabelToInput);
        btn6.setOnAction(addLabelToInput);
        btnLPar.setOnAction(addLabelToInput);
        btnRPar.setOnAction(addLabelToInput);
        btn1.setOnAction(addLabelToInput);
        btn2.setOnAction(addLabelToInput);
        btn3.setOnAction(addLabelToInput);
        btnMin.setOnAction(addLabelToInput);
        btn0.setOnAction(addLabelToInput);
        btnDot.setOnAction(addLabelToInput);
        btnPlus.setOnAction(addLabelToInput);

        btnDiv.setOnAction(e -> inField.appendText("/"));
        btnMul.setOnAction(e -> inField.appendText("*"));
        btnExp.setOnAction(e -> inField.appendText("^"));

        btnDel.setOnAction(e -> {
            inField.positionCaret(inField.getLength());
            inField.deletePreviousChar();
        });

        btnClr.setOnAction(e -> {
            if(inField.getText().isEmpty()) {
                outField.clear();
            } else {
                inField.clear();
            }
        });

        btnEq.setOnAction(e -> handleInput());

        buttonPane.add(btn7, 0, 0);
        buttonPane.add(btn8, 1, 0);
        buttonPane.add(btn9, 2, 0);
        buttonPane.add(btnDiv, 3, 0);
        buttonPane.add(btnDel, 4, 0);
        buttonPane.add(btnClr, 5, 0);

        buttonPane.add(btn4, 0, 1);
        buttonPane.add(btn5, 1, 1);
        buttonPane.add(btn6, 2, 1);
        buttonPane.add(btnMul, 3, 1);
        buttonPane.add(btnLPar, 4, 1);
        buttonPane.add(btnRPar, 5, 1);

        buttonPane.add(btn1, 0, 2);
        buttonPane.add(btn2, 1, 2);
        buttonPane.add(btn3, 2, 2);
        buttonPane.add(btnMin, 3, 2);
        buttonPane.add(btnExp, 4, 2);

        buttonPane.add(btn0, 0, 3);
        buttonPane.add(btnDot, 1, 3);
        buttonPane.add(btnPlus, 3, 3);
        buttonPane.add(btnEq, 4, 3, 2, 1);
        buttonPane.setHgap(5);
        buttonPane.setVgap(5);
        buttonPane.setMaxWidth(Double.POSITIVE_INFINITY);

        return buttonPane;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);

    }

}
