/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queuedserver;

import java.io.*;
public class fcuk1 {
public static void main (String arg[]) throws IOException
{String s="";int c1=0;
    String a="nitha chalili ";
    for(int i=0;i<a.length();i++)
    {
        char c=a.charAt(i);
        if(c=='a' || c=='e'|| c=='i' || c=='o'|| c=='u')
        {
            a=a.replace(c,'$');
            
        }
     
  
    }
     for(int i=0;i<a.length();i++)
    {
        char c=a.charAt(i);
        if(c=='$')
            c1++;
        
    }

 System.out.println("no of zeros"+a);

 System.out.println("no of fckk uu"+c1); 
}
}
