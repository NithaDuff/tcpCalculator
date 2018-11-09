/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queuedserver;

import java.io.*;
public class fcuk {
public static void main (String arg[]) throws IOException
{
 int a[]={1,0,0,0,1,0,1,0,2,1};
 int s=0;int x=0;int n=1234;
 while(n>0)
 {x=n%10;
 for(int i=0;i<10;i++)
 {
     if(x==i)
         s+=a[i];
 }
 n=n/10;
 
}
 System.out.println("no of zeros"+s);
}
}
