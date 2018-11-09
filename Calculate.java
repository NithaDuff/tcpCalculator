
package queuedserver;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 *
 * @author chalil
 */
public class Calculate {    
    String Calculate(String exp)
    {        
        String[] val;
        String input;
        double[] p;
        int cnt=0;
        char[] postfixStack;
        if(exp.isEmpty())
            return "";
        exp = "0+"+exp;
        exp = exp.replaceAll("\\(\\-\\)", "-");
        exp = exp.replaceAll("\\+\\-", "-");
        exp = exp.replaceAll("\\(\\+\\)", "+");
        exp = exp.replaceAll("\\s", "");
        byte[] data = exp.getBytes();
        
        for(byte b:data)
        {
            if(isAlpha(b))
                return "ERROR:Invalid Character";
        }
        input = encrypt(data);
        if(!bracketMatch(input))
            return "ERROR:Missing bracket";
        val = input.split(":");
        p = new double[val.length];
        for (int i=0;i < val.length ;i++ ) {
            try {
                p[cnt++] = Float.parseFloat(val[i]);
                val[i]="$";
            }catch(NumberFormatException e){cnt--;}
        }
        //System.out.println(input);
        String postfix = toPostfix(val);
        postfixStack = postfix.toCharArray();
        return evaluate(p,postfixStack);
    }
    
    private static boolean isNum(byte b) {
        return b >= 48 && b <= 57;
    }
    
     private boolean isAlpha(byte b) {
        String symbols = "+-*/().^";
        int i = symbols.indexOf((char)b);
         return !isNum(b) && i == -1;
    }
    
    private static String encrypt(byte[] data) {
        String eq="";
        for(int b = 0; b < data.length; b++)
        {
            if(isNum(data[b]) || (char)data[b] == '.' || validExp((char)data[b],(char)data[b-1]))
                eq += (char)data[b];
            else
                eq += ":" + (char)data[b] + ":"; 
        }
        return eq;
    }

    private static int getPrece(String n) {
        switch (n) {
            case "+":
            case "-":
                return 0;
            case "/":
            case "*":
                return 1;
            case "^":
                return 2;
            default:
                return -1;
        }
    }

    private static String toPostfix(String[] val) {
        String fin = "";
        String op = "+-*/^";
        Stack<String> stack = new Stack<>();
        for(String n:val)
        {
           //System.out.println(fin +"_{"+ stack+"}  "+n );
            if(!n.equals("$"))
            {
                if(n.equals("("))
                    stack.push(n);
                else if(n.equals(")")){
                    while(!stack.peek().equals("("))      
                        fin += stack.pop();
                    stack.pop();
                }
                else if(n.equals("+") || n.equals("-") || n.equals("*") || n.equals("/") || n.equals("^"))
                    fin = anOp(fin,stack,n);
            }
            else
                fin += n;
        }
        while(!stack.isEmpty()){
            fin += stack.pop();
        }
        return fin;
    }
    
    private static String evaluate(double[] p, char[] postfixStack) {
        double op1,op2,ans=0;
        Stack<Double> stack = new Stack<>();
        int count = 0;
        char op;
        try{
            for(char c:postfixStack)
            {
                if(c=='$')
                {
                    stack.push( p[count++]);                
                }
                else{
                    op = c;
                    op1 = stack.pop();
                    op2 = stack.pop();
                    switch(op)
                    {
                        case '+':ans= op1+op2;break;
                        case '-':ans= op2-op1;break;
                        case '*':ans= op1*op2;break;
                        case '/':if(op1 == 0)
                            return "ERROR:Divide by zero";
                        else
                            ans= op2/(float)op1;
                        break;
                        case '^':ans= (float)Math.pow((int)op2,(int)op1);
                    } 
                    stack.push(ans);
                }
            }
            //ans= DecimalUtils.round(ans,5);
            //String q = Float.toString(ans);
            return String.format("%.5f",ans);
        }catch(EmptyStackException ese){ return "ERROR:Invalid Expression";}
    }

    private boolean bracketMatch(String input) {
        Stack<Character> stack = new Stack<>();
        stack.clear();
        try
        {
            for(char c:input.toCharArray())
            {
                if(c == '(')
                    stack.push(c);
                else if(c == ')')
                    stack.pop();
            }
        }catch(EmptyStackException e){return false;}
        return stack.isEmpty();
        
    }

    private static boolean validExp( char c, char p) {
        return (c == '-' || c == '+') && (p == '-' || p == '+' || p == '*' || p == '/' || p =='(');
    }
    
    private static String anOp(String fin, Stack<String> stack, String n) {
        if(stack.isEmpty())
            stack.push(n);
        else if(stack.peek().equals("(") )
            stack.push(n);
        else if(getPrece(n) > getPrece(stack.peek()))
            stack.push(n);
        else if(getPrece(n) == getPrece(stack.peek()))
        {
            if(!n.equals("^"))
            {
                fin += stack.pop();
                stack.push(n);
            }
            else
                stack.push(n);
        }
        else
        {
            fin += stack.pop();
            fin = anOp(fin,stack,n);
        }
        return fin;
    }
}
