package com.example.cs501hw2

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.example.cs501hw2.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Objects
import java.util.Stack
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var resultView : EditText
    private lateinit var one : Button
    private lateinit var equationString : String //Calculater input
    private lateinit var binding: ActivityMainBinding
    private var t = 4.3
    private var decimalCount = 0; //Count continuous decimals
    private var isInSqrt = false //Judge whether this input is in sqrt()
    private var endOfEquation = true //Have finished one round of calculation
    private var firstTimeInput = true // Whether it is the first time inputting
    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        //Create binding on the layout
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        equationString = ""
        resultView = findViewById(R.id.resultView)

        //Use InputFilter to modify changes to the EditText
        val filter = InputFilter { source, start, end, dest, dstart, dend ->
            //Delete one character
            if (end == 0){
                if (dest.isEmpty()) ""
                else {
                    dest.subSequence(dstart, dend - 1)//return the length-1 string
                }
            }
            //If a calculation has finished, clear the EditText before the next input
            else if(endOfEquation){
                val currentChar = source[0]
                if(firstTimeInput || Character.isDigit(currentChar)){
                    clearText()
                    equationString = ""
                    endOfEquation = false
                    firstTimeInput = false
                    binding.resultView.append(source)
                    ""
                }
                else if("+-*/".contains(currentChar)){
                    equationString = equationString + " " + source + " "
                    endOfEquation = false
                    source
                }
                else{
                    showBarTop("Invalid Input")
                    ""
                }
            }
            //Input to EditText
            else{
                //Get the first character of input
                val currentChar = source[0]
                //Input operators
                if(!Character.isDigit(currentChar)){
                    if(currentChar == '.'){
                        //Judge duplicated '.'
                        if(decimalCount == 0) {
                            decimalCount = 1
                            equationString += "."
                            source
                        }
                        else{
                            showBarTop("Can't have multiple decimal points")
                            ""
                        }
                    }
                    //Handle '+', '-', '*', '/'
                    else if("+-*/".contains(currentChar)){
                        if(!logicalErrors(dest.toString(), currentChar)) {
                            //Enclose the sqrt
                            if(isInSqrt){
                                isInSqrt = false
                                equationString  = equationString + ")" + source
                                ")" + source
                            }
                            //Normal case, add to the EditText and refresh decimalCount
                            else{
                                decimalCount = 0
                                equationString = equationString + " " + source + " "
                                source
                            }
                        }
                        //Errors, make no change
                        else ""
                    }
                    //Handle input of sqrt
                    else if(currentChar == 's'){
                        //Add "sqrt(" to the EditText
                        if(dend == 0 ||!sqrtLogicalErrors(dest.toString())) {
                            isInSqrt = true
                            equationString += "sqrt("
                            "sqrt("
                        }
                        //Errors, make no change
                        else ""
                    }
                    //Handle '=', clear the EditText and display result
                    else if(currentChar == '='){
                        if(!logicalErrors(dest.toString(), currentChar)){
                            var ret = 0.0
                            //Enclose the sqrt
                            if(isInSqrt){
                                isInSqrt = false
                                //Get result
                                ret = calculate(equationString + ")")
                            }
                            else
                                ret = calculate(equationString)
                            clearText()
                            equationString = ""
                            binding.resultView.append(ret.toString())
                            endOfEquation = true
                            decimalCount = 0
                            ""
                        }
                        //Errors, make no change
                        else ""
                    }
                    //Errors, make no change
                    else ""
                }
                //Input numbers
                //Cannot add '0' to '/'
                else if(currentChar == '0' && (!dest.isEmpty() && dest[dend-1] == '/')){
                    showBarTop("Cannot divide by 0")
                    ""
                }
                //Add number to the EditText
                else{
                    equationString += source
                    source
                }
            }
        }
        //Set InputFilter to the EditText
        binding.resultView.setFilters(arrayOf<InputFilter>(filter))

        //Set listener to buttons, use append to trigger the InputFilter
        binding.one.setOnClickListener{ //instances, revision needed
            binding.resultView.append("1")
        }
        binding.two.setOnClickListener{
            binding.resultView.append("2")
        }
        binding.three.setOnClickListener{
            binding.resultView.append("3")
        }
        binding.four.setOnClickListener{
            binding.resultView.append("4")
        }
        binding.five.setOnClickListener{
            binding.resultView.append("5")
        }
        binding.six.setOnClickListener{
            binding.resultView.append("6")
        }
        binding.seven.setOnClickListener{
            binding.resultView.append("7")
        }
        binding.eight.setOnClickListener{
            binding.resultView.append("8")
        }
        binding.nine.setOnClickListener{
            binding.resultView.append("9")
        }
        binding.zero.setOnClickListener{
            binding.resultView.append("0")
        }
        binding.dot.setOnClickListener{
            binding.resultView.append(".")
        }
        binding.addition.setOnClickListener{
            binding.resultView.append("+")
        }
        binding.subtraction.setOnClickListener{
            binding.resultView.append("-")
        }
        binding.multiplication.setOnClickListener{
            binding.resultView.append("*")
        }
        binding.division.setOnClickListener{
            binding.resultView.append("/")
        }
        //'s' stands for sqrt input
        binding.sqrt.setOnClickListener{
            binding.resultView.append("s")
        }
        binding.equation.setOnClickListener{// computation
            binding.resultView.append("=")
        }
    }

    //Judge whether the sqrt input leads to errors, show errors if exists
    private fun sqrtLogicalErrors(equation: String): Boolean{
        if(isInSqrt){
            showBarTop("Can't have consecutive square roots")
            return true
        }
        if(equation[equation.length-1].isDigit()){
            showBarTop("square root can't follow a digit")
            return true
        }
        return false
    }

    //Judge whether the operator input leads to errors, show errors if exists
    private fun logicalErrors(equation: String, curr: Char): Boolean{
        if(equation.isEmpty()) {
            if(curr != '-'){
                showBarTop("Can't start with an operator")
                return true
            }
        }
        else if(!equation[equation.length-1].isDigit()){
            showBarTop("Can't have consecutive operators")
            return true
        }
        return false
    }

    //The calculation part
    private fun calculate(equation : String): Double{ //implementation needed
        var compute = equation.split(" ").toMutableList()
        var stack =  Stack<Double>()

        for(i in compute.indices){
            if(compute[i][0] == 's'){
                compute[i] = sqrt(compute[i].substring(5,compute[i].length-1).toDouble()).toString()
            }
        }
        stack.push(compute[0].toDouble())

        for(i in compute.indices){
            if(i % 2 == 1){
                if(compute[i] == "+")
                    stack.push(compute[i+1].toDouble())
                if(compute[i] == "-")
                    stack.push(-compute[i+1].toDouble())
                if(compute[i] == "*")
                    stack.push(stack.pop() * compute[i+1].toDouble())
                if(compute[i] == "/")
                    if(compute[i+1].toDouble() == 0.0){
                        Toast.makeText(this, "Can't divide by zero", Toast.LENGTH_SHORT).show()
                        return 0.0
                    }else{
                        stack.push(stack.pop() / compute[i+1].toDouble())
                    }
            }
        }

        var res = 0.0
        while(!stack.empty()){
            res += stack.pop()
        }
        if(res == Double.NEGATIVE_INFINITY || res == Double.POSITIVE_INFINITY){
            Toast.makeText(this, "Overflow error", Toast.LENGTH_SHORT).show()
            return 0.0
        }
        return res
    }

    //To avoid conflicts with the soft keyboard, show snackbar at the top
    private fun showBarTop(sentence: String){
        val bar = Snackbar.make(binding.root, sentence, 1000)
        val view: View = bar.getView()
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        bar.show()
    }

    //Use while loop to clear the EditText
    private fun clearText(){
        while(!binding.resultView.text.isEmpty())
            binding.resultView.text.clear()
    }
}